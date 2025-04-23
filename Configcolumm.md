Perfect — let’s make the user config fully flexible so users can:

1. Set column order


2. Rename columns


3. Hide columns




---

✅ Step 1: Define a Column Config Model

public record ColumnConfig(String fieldName, String label, boolean hidden) {}


---

✅ Step 2: Replace the basic list with full config

// key = class or sheet name, value = ordered list of custom config
private final Map<String, List<ColumnConfig>> userColumnConfigs = new ConcurrentHashMap<>();


---

✅ Step 3: Set config at runtime

public void setUserColumnConfig(String classOrSheet, List<ColumnConfig> configs) {
    userColumnConfigs.put(classOrSheet, configs);
}

Example call:

setUserColumnConfig("User", List.of(
    new ColumnConfig("id", "User ID", false),
    new ColumnConfig("name", "Full Name", false),
    new ColumnConfig("email", "", true) // hidden
));


---

✅ Step 4: Update getExportFields() to use config

private List<ExportField> getExportFields(Class<?> clazz) {
    return exportFieldCache.computeIfAbsent(clazz, clz -> {
        Field[] declaredFields = clz.getDeclaredFields();
        Map<String, Field> fieldMap = Arrays.stream(declaredFields)
            .collect(Collectors.toMap(Field::getName, f -> f));

        String typeKey = clazz.getSimpleName();
        List<ColumnConfig> configList = userColumnConfigs.get(typeKey);

        List<ExportField> fields = new ArrayList<>();

        if (configList != null && !configList.isEmpty()) {
            for (int i = 0; i < configList.size(); i++) {
                ColumnConfig config = configList.get(i);
                if (config.hidden()) continue;

                Field field = fieldMap.get(config.fieldName());
                if (field == null) continue;

                String getter = "get" + Character.toUpperCase(config.fieldName().charAt(0)) + config.fieldName().substring(1);
                try {
                    clazz.getMethod(getter);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Missing getter '" + getter + "' for '" + config.fieldName() + "'");
                }

                String label = config.label().isEmpty() ? config.fieldName() : config.label();
                fields.add(new ExportField(config.fieldName(), label, i, getter));
            }

            return fields;
        }

        // Fallback to annotation/default logic
        List<ExportField> fallbackFields = new ArrayList<>();
        for (int i = 0; i < declaredFields.length; i++) {
            Field field = declaredFields[i];
            ExcelColumn ann = field.getAnnotation(ExcelColumn.class);
            if (ann != null && ann.hidden()) continue;

            String name = field.getName();
            String label = ann != null && !ann.label().isEmpty() ? ann.label() : name;
            String getter = ann != null && !ann.getter().isEmpty()
                ? ann.getter()
                : "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

            try {
                clazz.getMethod(getter);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Missing getter '" + getter + "' for '" + name + "'");
            }

            int order = ann != null ? ann.order() : i;
            fallbackFields.add(new ExportField(name, label, order, getter));
        }

        fallbackFields.sort(Comparator.comparingInt(ExportField::order));
        return fallbackFields;
    });
}


---

✅ Now you can:

Fully control column layout

Rename columns (even override annotations)

Hide fields without modifying the class


Want me to make this configurable via API call or YAML/JSON file too?


Great! Let’s make this fully configurable via:

1. REST API endpoint (for dynamic updates)


2. Optional support for YAML/JSON file config (at startup)




---

✅ 1. REST API to update column config

a) Create a DTO for user request:

public record ColumnConfigDTO(String fieldName, String label, boolean hidden) {}

b) Create a REST controller:

@RestController
@RequestMapping("/api/export-config")
public class ExportConfigController {

    private final ExportService exportService;

    public ExportConfigController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping("/{className}")
    public ResponseEntity<Void> updateColumnConfig(
        @PathVariable String className,
        @RequestBody List<ColumnConfigDTO> configs
    ) {
        List<ColumnConfig> mapped = configs.stream()
            .map(dto -> new ColumnConfig(dto.fieldName(), dto.label(), dto.hidden()))
            .toList();

        exportService.setUserColumnConfig(className, mapped);
        return ResponseEntity.ok().build();
    }
}


---

✅ 2. YAML or JSON-based config on startup (optional)

a) Sample export-config.yaml:

User:
  - fieldName: "id"
    label: "User ID"
    hidden: false
  - fieldName: "name"
    label: "Full Name"
    hidden: false
  - fieldName: "email"
    label: ""
    hidden: true

Product:
  - fieldName: "name"
    label: "Product"
    hidden: false

b) Java config loader (YAML via SnakeYAML or Spring config):

@Component
public class ExportConfigLoader {

    @Autowired
    private ExportService exportService;

    @PostConstruct
    public void loadConfigFromYaml() throws IOException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("export-config.yaml");

        if (in != null) {
            Yaml yaml = new Yaml();
            Map<String, List<Map<String, Object>>> yamlMap = yaml.load(in);

            yamlMap.forEach((className, list) -> {
                List<ColumnConfig> configs = list.stream()
                    .map(map -> new ColumnConfig(
                        (String) map.get("fieldName"),
                        (String) map.getOrDefault("label", ""),
                        (Boolean) map.getOrDefault("hidden", false)))
                    .toList();

                exportService.setUserColumnConfig(className, configs);
            });
        }
    }
}

> You can also support .json with Jackson if you prefer that over YAML.




---

✅ Now you’ve got:

Live API config control

Optional static startup config

Field-level control per class


Need help with Swagger UI support for this API or a UI dashboard to manage it visually?

