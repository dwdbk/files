Nice — let’s level this up fully.

We’ll add:

hidden = true support

Optional custom value extractor method (like getDisplayName() instead of getName())

Cleaner, reusable extraction logic



---

✅ 1. Update the @ExcelColumn annotation

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {
    String label() default "";
    int order();
    boolean hidden() default false;
    String getter() default ""; // Optional custom method
}


---

✅ 2. Update ExportField record to support getterName

public record ExportField(String fieldName, String label, int order, String getterName) {}


---

✅ 3. Update getExportFields() to filter hidden fields

private List<ExportField> getExportFields(Class<?> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(ExcelColumn.class))
        .map(f -> {
            ExcelColumn ann = f.getAnnotation(ExcelColumn.class);
            if (ann.hidden()) return null;
            String label = ann.label().isEmpty() ? f.getName() : ann.label();
            String getter = ann.getter().isEmpty()
                ? "get" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1)
                : ann.getter();
            return new ExportField(f.getName(), label, ann.order(), getter);
        })
        .filter(Objects::nonNull)
        .sorted(Comparator.comparingInt(ExportField::order))
        .toList();
}


---

✅ 4. Update writeData() to use getterName

private void writeData(Sheet sheet, List<?> data, CellStyle dateStyle) throws Exception {
    if (data == null || data.isEmpty()) return;

    Class<?> clazz = data.get(0).getClass();
    List<ExportField> fields = getExportFields(clazz);

    Map<String, Method> getterMap = new HashMap<>();
    for (ExportField ef : fields) {
        try {
            Method getter = clazz.getMethod(ef.getterName());
            getterMap.put(ef.fieldName(), getter);
        } catch (NoSuchMethodException e) {
            // Optionally log or throw for strict mode
        }
    }

    int rowIdx = 1;
    for (Object obj : data) {
        Row row = sheet.createRow(rowIdx++);
        for (int col = 0; col < fields.size(); col++) {
            ExportField ef = fields.get(col);
            Method getter = getterMap.get(ef.fieldName());
            if (getter == null) continue;

            Object value = getter.invoke(obj);
            Cell cell = row.createCell(col);

            if (value instanceof Date dateVal) {
                cell.setCellValue(dateVal);
                cell.setCellStyle(dateStyle);
            } else if (value instanceof Number numberVal) {
                cell.setCellValue(numberVal.doubleValue());
            } else if (value != null) {
                cell.setCellValue(value.toString());
            }
        }
    }
}


---

✅ 5. Example usage in a class

public class User {
    @ExcelColumn(label = "ID", order = 1)
    private Long id;

    @ExcelColumn(label = "Name", order = 2, getter = "getDisplayName")
    private String name;

    @ExcelColumn(label = "Hidden Email", order = 3, hidden = true)
    private String email;

    @ExcelColumn(label = "Joined", order = 4)
    private Date createdAt;

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getDisplayName() { return "User: " + name; }

    public String getEmail() { return email; }

    public Date getCreatedAt() { return createdAt; }
}


---

Final Result:

Use @ExcelColumn(order = X) to define order

Use label = "..." for headers

Use getter = "customGetter" if needed

Use hidden = true to skip fields


Let me know if you'd like:

Support for @ExcelColumn on getter methods

Excel formatting (e.g. column type or number/date formats via annotations)

Localization for labels


