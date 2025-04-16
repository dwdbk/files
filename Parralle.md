Perfect — let's go full power now:

You want:

1. Parallel Excel file generation — utilize CPU cores for speed.


2. User-controlled sheet selection — only generate requested sheets.


3. Still stream results as a ZIP with progress tracking.



We’ll use:

ExecutorService for parallel generation.

Future/Callable for thread-safe results.

Custom request DTO to filter sheets.

Progress tracking across threads.



---

1. Request DTO: User-selected sheets

import java.util.Set;

public class ExportRequest {
    private Set<String> sheetsToInclude;

    public Set<String> getSheetsToInclude() {
        return sheetsToInclude;
    }

    public void setSheetsToInclude(Set<String> sheetsToInclude) {
        this.sheetsToInclude = sheetsToInclude;
    }
}


---

2. Controller: Accept filters, stream ZIP, return session ID

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ExportController {

    private final ExcelExportService excelExportService;
    private final ExportProgressService progressService;

    public ExportController(ExcelExportService excelExportService, ExportProgressService progressService) {
        this.excelExportService = excelExportService;
        this.progressService = progressService;
    }

    @PostMapping("/export-zip")
    public void exportZip(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
        String sessionId = UUID.randomUUID().toString();

        Map<String, List<?>> dataMap = new LinkedHashMap<>();
        dataMap.put("Users", generateUsers());
        dataMap.put("Products", generateProducts());
        dataMap.put("Logs", generateLogs());

        // Filter sheets
        if (request.getSheetsToInclude() != null && !request.getSheetsToInclude().isEmpty()) {
            dataMap.keySet().retainAll(request.getSheetsToInclude());
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=data_export.zip");
        response.setHeader("X-Export-Session", sessionId);

        excelExportService.exportToZipParallel(dataMap, response.getOutputStream(), sessionId);
    }

    @GetMapping("/export-status/{sessionId}")
    public int getProgress(@PathVariable String sessionId) {
        return progressService.getProgress(sessionId);
    }

    // Sample data generators
    private List<User> generateUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            users.add(new User((long) i, "User " + i, "user" + i + "@mail.com"));
        }
        return users;
    }

    private List<Product> generateProducts() {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            products.add(new Product((long) i, "Product " + i, i * 1.25));
        }
        return products;
    }

    private List<Log> generateLogs() {
        List<Log> logs = new ArrayList<>();
        for (int i = 0; i < 50_000; i++) {
            logs.add(new Log(i, "INFO", "Log message " + i));
        }
        return logs;
    }
}


---

3. Service: Parallel Excel + Progress

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExcelExportService {

    private final ExportProgressService progressService;

    public ExcelExportService(ExportProgressService progressService) {
        this.progressService = progressService;
    }

    public void exportToZipParallel(Map<String, List<?>> dataMap, OutputStream out, String sessionId) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(out);

        int total = dataMap.size();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, total));
        List<Future<ExcelFileResult>> futures = new ArrayList<>();

        for (Map.Entry<String, List<?>> entry : dataMap.entrySet()) {
            futures.add(executor.submit(() -> generateExcelFile(entry.getKey(), entry.getValue())));
        }

        int completed = 0;
        for (Future<ExcelFileResult> future : futures) {
            ExcelFileResult result = future.get(); // wait and get
            zos.putNextEntry(new ZipEntry(result.fileName()));
            zos.write(result.content());
            zos.closeEntry();
            completed++;
            progressService.setProgress(sessionId, (int) ((completed / (double) total) * 100));
        }

        executor.shutdown();
        zos.finish();
        progressService.clearProgress(sessionId);
    }

    private ExcelFileResult generateExcelFile(String sheetName, List<?> data) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            if (data == null || data.isEmpty()) return new ExcelFileResult(sheetName + ".xlsx", baos.toByteArray());

            Sheet sheet = workbook.createSheet(sheetName);
            PropertyDescriptor[] props = getBeanProperties(data.get(0));
            writeHeader(sheet, props);
            writeData(sheet, data, props);

            workbook.write(baos);
            workbook.dispose();
            return new ExcelFileResult(sheetName + ".xlsx", baos.toByteArray());
        }
    }

    private void writeHeader(Sheet sheet, PropertyDescriptor[] props) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < props.length; i++) {
            row.createCell(i).setCellValue(props[i].getName());
        }
    }

    private void writeData(Sheet sheet, List<?> data, PropertyDescriptor[] props) throws Exception {
        int rowIndex = 1;
        for (Object obj : data) {
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < props.length; i++) {
                Method getter = props[i].getReadMethod();
                Object value = getter.invoke(obj);
                Cell cell = row.createCell(i);
                if (value instanceof Number num) {
                    cell.setCellValue(num.doubleValue());
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                }
            }
            if (rowIndex % 1000 == 0) {
                ((SXSSFSheet) sheet).flushRows(100);
            }
        }
    }

    private PropertyDescriptor[] getBeanProperties(Object obj) throws Exception {
        return Introspector.getBeanInfo(obj.getClass(), Object.class).getPropertyDescriptors();
    }

    private record ExcelFileResult(String fileName, byte[] content) {}
}


---

Client-side Suggestions

1. Send request with:



POST /export-zip
{
  "sheetsToInclude": ["Users", "Products"]
}

2. Track progress via:



GET /export-status/{sessionId}


---

You now have:

Multi-threaded Excel creation = fast.

Memory-safe streaming = scalable.

User-filtered sheets = flexible.

ZIP packaging with progress tracking = polished UX.



---

Want to add:

Email delivery when done?

Background jobs for huge exports?

Excel cell styling or localization?


Just say the word.

