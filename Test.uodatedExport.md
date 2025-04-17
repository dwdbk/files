Awesome — let’s power up your export even more with:


---

✅ 1. Date Formatting per Field Type

We'll detect Date or LocalDate/Time fields and apply a format like yyyy-MM-dd HH:mm:ss.

Modify your createSheet() method like this:

private void createSheet(SXSSFWorkbook workbook, String sheetName, List<?> data) throws Exception {
    if (data == null || data.isEmpty()) return;

    Sheet sheet = workbook.createSheet(sheetName);
    PropertyDescriptor[] props = Introspector.getBeanInfo(data.get(0).getClass(), Object.class).getPropertyDescriptors();

    // Header styling
    CellStyle headerStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    headerStyle.setFont(font);
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

    // Date formatting style
    CellStyle dateStyle = workbook.createCellStyle();
    CreationHelper createHelper = workbook.getCreationHelper();
    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd HH:mm:ss"));

    // Write header
    Row header = sheet.createRow(0);
    for (int i = 0; i < props.length; i++) {
        Cell cell = header.createCell(i);
        cell.setCellValue(props[i].getName());
        cell.setCellStyle(headerStyle);
    }

    // Write data
    int rowIndex = 1;
    for (Object obj : data) {
        Row row = sheet.createRow(rowIndex++);
        for (int i = 0; i < props.length; i++) {
            Object value = props[i].getReadMethod().invoke(obj);
            Cell cell = row.createCell(i);
            if (value == null) continue;

            if (value instanceof Number num) {
                cell.setCellValue(num.doubleValue());
            } else if (value instanceof java.util.Date date) {
                cell.setCellValue(date);
                cell.setCellStyle(dateStyle);
            } else if (value instanceof java.time.LocalDateTime ldt) {
                cell.setCellValue(ldt.toString()); // OR use formatter
            } else if (value instanceof java.time.LocalDate ld) {
                cell.setCellValue(ld.toString());
            } else {
                cell.setCellValue(value.toString());
            }
        }

        if (rowIndex % 1000 == 0) {
            ((SXSSFSheet) sheet).flushRows(100);
        }
    }

    // Enable filter
    sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, props.length - 1));
}

You can later enhance LocalDateTime/LocalDate with a custom formatter if needed.


---

✅ 2. WebSocket Live Progress Tracking

Here’s the structure:

Step 1: Add WebSocket Config

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-progress").setAllowedOrigins("*").withSockJS();
    }
}


---

Step 2: WebSocket Controller (Push Progress)

@Controller
public class ProgressWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public ProgressWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendProgress(String sessionId, int percent) {
        messagingTemplate.convertAndSend("/topic/progress/" + sessionId, percent);
    }
}


---

Step 3: Update Export Service to Push via WebSocket

Inject the controller and push progress:

public class ExcelExportService {

    private final ExportProgressService progressService;
    private final ProgressWebSocketController webSocketController;

    public ExcelExportService(ExportProgressService progressService,
                              ProgressWebSocketController webSocketController) {
        this.progressService = progressService;
        this.webSocketController = webSocketController;
    }

    public void exportToSingleExcel(Map<String, List<?>> dataMap, OutputStream out, String sessionId) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, dataMap.size()));
            List<Future<Void>> futures = new ArrayList<>();
            AtomicInteger done = new AtomicInteger(0);
            int total = dataMap.size();

            for (Map.Entry<String, List<?>> entry : dataMap.entrySet()) {
                futures.add(executor.submit(() -> {
                    createSheet(workbook, entry.getKey(), entry.getValue());
                    int progress = (int) ((done.incrementAndGet() / (double) total) * 100);
                    progressService.setProgress(sessionId, progress);
                    webSocketController.sendProgress(sessionId, progress);
                    return null;
                }));
            }

            for (Future<Void> future : futures) {
                future.get();
            }

            workbook.write(out);
            workbook.dispose();
            executor.shutdown();
            progressService.clearProgress(sessionId);
        }
    }
}


---

Step 4: Frontend WebSocket Client (JS Example)

const socket = new SockJS('/ws-progress');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/progress/YOUR_SESSION_ID', function (message) {
        console.log("Progress: ", message.body + "%");
        // Update progress bar here
    });
});


---

That’s it!

You now have:

Dynamic date formatting

Live real-time progress via WebSocket

Still memory-safe, stream-based Excel export


Want to:

Push download link when export finishes?

Auto-redirect user on 100%?

Cache/export result on disk or cloud?


Say the word!

