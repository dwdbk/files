Got it — instead of creating one Excel file per entry in the ZIP, you now want:

> One Excel file with multiple sheets inside — each sheet representing a data entry like "Users", "Products", "Logs", etc.



This simplifies the ZIP (now just one Excel file), and inside it you’ll have:

Sheet: "Users"

Sheet: "Products"

Sheet: "Logs"

(Filtered by user's selection)


And still keeping:

Parallel data processing

Progress tracking

Stream as HTTP response

Heap-safe with SXSSFWorkbook



---

Updated Plan

1. Client sends which sheets to include

2. Server generates one Excel file

3. Each sheet filled in parallel (then merged)

4. The Excel file is streamed directly


---

1. Update Controller

@PostMapping("/export-excel")
public void exportSingleExcel(@RequestBody ExportRequest request, HttpServletResponse response) throws Exception {
    String sessionId = UUID.randomUUID().toString();

    Map<String, List<?>> dataMap = new LinkedHashMap<>();
    dataMap.put("Users", generateUsers());
    dataMap.put("Products", generateProducts());
    dataMap.put("Logs", generateLogs());

    if (request.getSheetsToInclude() != null) {
        dataMap.keySet().retainAll(request.getSheetsToInclude());
    }

    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=data_export.xlsx");
    response.setHeader("X-Export-Session", sessionId);

    excelExportService.exportToSingleExcel(dataMap, response.getOutputStream(), sessionId);
}


---

2. ExcelExportService: One Excel file with parallel sheet writing

public void exportToSingleExcel(Map<String, List<?>> dataMap, OutputStream out, String sessionId) throws Exception {
    try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(4, dataMap.size()));
        List<Future<Void>> futures = new ArrayList<>();

        int total = dataMap.size();
        AtomicInteger done = new AtomicInteger(0);

        for (Map.Entry<String, List<?>> entry : dataMap.entrySet()) {
            futures.add(executor.submit(() -> {
                createSheet(workbook, entry.getKey(), entry.getValue());
                int progress = (int) ((done.incrementAndGet() / (double) total) * 100);
                progressService.setProgress(sessionId, progress);
                return null;
            }));
        }

        for (Future<Void> future : futures) {
            future.get(); // wait for all
        }

        workbook.write(out);
        workbook.dispose();
        executor.shutdown();
        progressService.clearProgress(sessionId);
    }
}

private void createSheet(SXSSFWorkbook workbook, String sheetName, List<?> data) throws Exception {
    if (data == null || data.isEmpty()) return;

    Sheet sheet = workbook.createSheet(sheetName);
    PropertyDescriptor[] props = Introspector.getBeanInfo(data.get(0).getClass(), Object.class).getPropertyDescriptors();

    Row header = sheet.createRow(0);
    for (int i = 0; i < props.length; i++) {
        header.createCell(i).setCellValue(props[i].getName());
    }

    int rowIndex = 1;
    for (Object obj : data) {
        Row row = sheet.createRow(rowIndex++);
        for (int i = 0; i < props.length; i++) {
            Object value = props[i].getReadMethod().invoke(obj);
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


---

You now have:

One Excel file with one sheet per dataset

Each sheet processed in parallel for speed

Heap-safe, uses SXSSFWorkbook

Progress tracked via /export-status/{id}

User can filter sheets by name



---

Bonus Ideas

Want to:

Add cell formatting or date handling?

Add column filters in each sheet?

Zip this Excel if it gets huge?


I’ve got you — just let me know what to plug in next.



Nice! Since you said yes, I’ll walk you through 3 powerful upgrades you can bolt on:


---

1. Add Column Filters (Excel AutoFilter)

Let users filter columns right in Excel.

Update createSheet():

private void createSheet(SXSSFWorkbook workbook, String sheetName, List<?> data) throws Exception {
    if (data == null || data.isEmpty()) return;

    Sheet sheet = workbook.createSheet(sheetName);
    PropertyDescriptor[] props = Introspector.getBeanInfo(data.get(0).getClass(), Object.class).getPropertyDescriptors();

    Row header = sheet.createRow(0);
    for (int i = 0; i < props.length; i++) {
        header.createCell(i).setCellValue(props[i].getName());
    }

    int rowIndex = 1;
    for (Object obj : data) {
        Row row = sheet.createRow(rowIndex++);
        for (int i = 0; i < props.length; i++) {
            Object value = props[i].getReadMethod().invoke(obj);
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

    // Add filter to header row
    sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, props.length - 1));
}


---

2. Style Header Row

Make headers bold with background color:

In createSheet() (just after creating header):

CellStyle headerStyle = workbook.createCellStyle();
Font font = workbook.createFont();
font.setBold(true);
headerStyle.setFont(font);
headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

for (int i = 0; i < props.length; i++) {
    Cell cell = header.createCell(i);
    cell.setCellValue(props[i].getName());
    cell.setCellStyle(headerStyle);
}


---

3. Optionally: Compress the Excel File into ZIP

Only if you want to download a .zip with a single .xlsx inside (to reduce size):

In controller:

response.setContentType("application/zip");
response.setHeader("Content-Disposition", "attachment; filename=export.zip");

ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
zos.putNextEntry(new ZipEntry("data_export.xlsx"));

excelExportService.exportToSingleExcel(dataMap, zos, sessionId);

zos.closeEntry();
zos.finish();


---

Want to go even further?

You could add:

Date formatting per field type

Column auto-sizing (for small exports)

WebSocket-based live progress

Password-protected Excel / Zip


Let me know what fits your use case — I’ll tailor it!

