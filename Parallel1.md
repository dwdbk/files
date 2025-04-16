public class User {
    private Long id;
    private String name;
    private String email;

    public User() {}
    public User(Long id, String name, String email) {
        this.id = id; this.name = name; this.email = email;
    }

    // Getters
}

public class Product {
    private Long id;
    private String name;
    private double price;

    public Product() {}
    public Product(Long id, String name, double price) {
        this.id = id; this.name = name; this.price = price;
    }

    // Getters
}

Nice — combining progress tracking with multi-file ZIP streaming adds polish to your export process, especially for large datasets.

Here’s how we’ll do it:


---

Goal:

Stream multiple Excel files as a single ZIP download.

Generate each Excel file efficiently.

Provide real-time progress tracking via an optional endpoint (e.g., UUID-based export session).



---

Updated Architecture Overview

1. Client requests export → server starts background generation (async).


2. Each Excel file is streamed to a ZipOutputStream.


3. Export progress is tracked in a concurrent map (per session UUID).


4. Client can call /export-status/{id} to check progress.




---

1. ExportProgressService (Tracks % Complete)

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExportProgressService {
    private final Map<String, Integer> progressMap = new ConcurrentHashMap<>();

    public void setProgress(String sessionId, int percent) {
        progressMap.put(sessionId, percent);
    }

    public int getProgress(String sessionId) {
        return progressMap.getOrDefault(sessionId, 0);
    }

    public void clearProgress(String sessionId) {
        progressMap.remove(sessionId);
    }
}


---

2. ExcelExportService – Write to ZIP Entry

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ExcelExportService {

    private final ExportProgressService progressService;

    public ExcelExportService(ExportProgressService progressService) {
        this.progressService = progressService;
    }

    public void exportToZip(Map<String, List<?>> dataMap, OutputStream zipOut, String sessionId) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(zipOut);
        int totalSheets = dataMap.size();
        int processedSheets = 0;

        for (Map.Entry<String, List<?>> entry : dataMap.entrySet()) {
            String fileName = entry.getKey() + ".xlsx";
            List<?> data = entry.getValue();

            zos.putNextEntry(new ZipEntry(fileName));
            writeSingleExcel(data, zos, entry.getKey());
            zos.closeEntry();

            processedSheets++;
            int progress = (int) ((processedSheets / (double) totalSheets) * 100);
            progressService.setProgress(sessionId, progress);
        }

        zos.finish();
        progressService.clearProgress(sessionId);
    }

    private void writeSingleExcel(List<?> data, OutputStream out, String sheetName) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            if (data == null || data.isEmpty()) return;

            Sheet sheet = workbook.createSheet(sheetName);
            PropertyDescriptor[] props = getBeanProperties(data.get(0));

            writeHeader(sheet, props);
            writeData(sheet, data, props);

            workbook.write(out);
            workbook.dispose();
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
                if (value != null) {
                    if (value instanceof Number num) {
                        cell.setCellValue(num.doubleValue());
                    } else {
                        cell.setCellValue(value.toString());
                    }
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
}


---

3. Controller – Start Export & Track Progress

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

    @GetMapping("/export-zip")
    public void exportZip(HttpServletResponse response) throws Exception {
        String sessionId = UUID.randomUUID().toString();

        Map<String, List<?>> dataMap = new LinkedHashMap<>();
        dataMap.put("Users", generateUsers());
        dataMap.put("Products", generateProducts());

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=data_export.zip");
        response.setHeader("X-Export-Session", sessionId); // Optional: pass sessionId to client

        excelExportService.exportToZip(dataMap, response.getOutputStream(), sessionId);
    }

    @GetMapping("/export-status/{sessionId}")
    public int getProgress(@PathVariable String sessionId) {
        return progressService.getProgress(sessionId);
    }

    private List<User> generateUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100_000; i++) {
            users.add(new User((long) i, "User " + i, "user" + i + "@example.com"));
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
}


---

4. Frontend Integration Notes

Call /export-zip, capture X-Export-Session header.

Poll /export-status/{sessionId} every few seconds.

Show progress bar based on returned percent.



---

Want to support:

Parallel Excel generation for even faster export?

Async + WebSocket updates for progress?

Filtered/selected sheets by user request?


Let me know, I’ll tailor it to your case!

