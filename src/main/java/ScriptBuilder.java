import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class ScriptBuilder {

    private static final String BEGIN_QUERY = "BEGIN;\n" +
            "\n" +
            "create temporary table if not exists oldvals_newvals\n" +
            "(\n" +
            "    old_position             varchar(1000),\n" +
            "    new_position             varchar(1000)\n" +
            ");\n" +
            "\n" +
            "insert into oldvals_newvals (old_position, new_position)\n" +
            "values ";

    private static final String END_QUERY = ";\n" +
            "\n" +
            "update covidinfo_employee\n" +
            "set position = onp.new_position\n" +
            "from oldvals_newvals onp\n" +
            "where covidinfo_employee.position = onp.old_position;\n" +
            "\n" +
            "drop table oldvals_newvals;\n" +
            "END;";

    private static final Integer FIRST_PAGE_EXCEL_BOOK = 0;
    private static final Integer FIRST_COLUMN = 0;
    private static final Integer SECOND_COLUMN = 1;
    private static final String PATH_TO_SCRIPT_FILE = "/home/naglezh/IdeaProjects/script_builder/script.txt";
    private static final String PATH_TO_MAP_POSITION_FILE = "/home/naglezh/IdeaProjects/script_builder/union.xlsx";
    private static final String PATH_TO_SORTED_POSITION_FILE = "/home/naglezh/IdeaProjects/script_builder/sorted_union.xlsx";
    private static final String PATH_TO_POSITIONS = "/home/naglezh/IdeaProjects/script_builder/positions.txt";


    //прочитать из файла старые и новые значения. Создать скрип для подмены
    public static void main(String[] args) throws IOException {
        Map<String, String> oldNewPositions = readTwoColumns(new File(PATH_TO_MAP_POSITION_FILE));
        writeScript(oldNewPositions, PATH_TO_SCRIPT_FILE);
    }

    //прочитать из файла старые/новые значения. Отсортировать по должностям второй колонки записать в файл Excel
//    public static void main(String[] args) throws IOException {
//        Map<String, String> oldNewPositions = readTwoColumns(new File(PATH_TO_MAP_POSITION_FILE));
//        writeToExcelTwoColumns(sortByPositionRange(oldNewPositions));
//    }

    // прочитать из файла отсортированные должности взять должности руководителей - записать через запятую должности руководителей
//    public static void main(String[] args) throws IOException {
//        Map<String, String> sortedPosition = readTwoColumns(new File(PATH_TO_SORTED_POSITION_FILE));
//        Map<String, String> extractPositions = extractPositions(sortedPosition);
//        writePositions(sortByPositionRange(extractPositions), PATH_TO_POSITIONS);
//    }

    private static void writeScript(Map<String, String> oldNewPositions, String pathToScriptFile) throws IOException {
        String bodyInsert = buildInsertBody(oldNewPositions);
        String str = BEGIN_QUERY + bodyInsert + END_QUERY;
        Path file = Paths.get(pathToScriptFile);
        Files.writeString(file, str, StandardCharsets.UTF_8);
    }

    private static String buildInsertBody(Map<String, String> oldNewPositions) {
        String insertBodyDirty = oldNewPositions.entrySet().stream().map(entry ->
                "('" + entry.getKey() + "', '" + entry.getValue() + "'),\n"
        ).reduce((acc, x) -> acc + x).get();
        String insertBody = insertBodyDirty.substring(0, insertBodyDirty.length() - 2);
        return insertBody;
    }

    private static Map<String, String> readTwoColumns(File file) throws IOException {
        Workbook wb = WorkbookFactory.create(file);
        Sheet sheet = wb.getSheetAt(FIRST_PAGE_EXCEL_BOOK);
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<String, String> oldNewPositionMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell1 = row.getCell(FIRST_COLUMN);
            Cell cell2 = row.getCell(SECOND_COLUMN);
            if (nonNull(cell1) && nonNull(cell2)) {
                if (!cell2.getStringCellValue().isEmpty() && !cell1.getStringCellValue().isEmpty()) {
                    oldNewPositionMap.put(cell1.getStringCellValue(), cell2.getStringCellValue());
                }
            }
        }
        return oldNewPositionMap;
    }

    private static void writeToExcel(Set<String> positions) throws IOException {
        int rowNum = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Union position name sheet");
        XSSFCellStyle style = createStyle(workbook);
        for (String position : positions) {
            if (!position.equals("")) {
                Row row = sheet.createRow(rowNum++);
                Cell cell = row.createCell(FIRST_COLUMN, CellType.STRING);
                cell.setCellValue(position);
                cell.setCellStyle(style);
            }
        }
        try (FileOutputStream fos = new FileOutputStream("/home/naglezh/IdeaProjects/script_builder/union.xlsx")) {
            workbook.write(fos);
        }
    }

    private static void writeToExcelTwoColumns(Map<String, String> positions) throws IOException {
        int rowNum = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("sorted position");
        XSSFCellStyle style = createStyle(workbook);
        for (var entry : positions.entrySet()) {
            Row row = sheet.createRow(rowNum++);
            Cell firstCell = row.createCell(FIRST_COLUMN, CellType.STRING);
            Cell secondCell = row.createCell(SECOND_COLUMN, CellType.STRING);
            firstCell.setCellValue(entry.getKey());
            secondCell.setCellValue(entry.getValue());
            firstCell.setCellStyle(style);
            secondCell.setCellStyle(style);
        }
        try (FileOutputStream fos = new FileOutputStream(PATH_TO_SORTED_POSITION_FILE)) {
            workbook.write(fos);
        }
    }

    private static LinkedHashMap<String, String> sortByPositionRange(Map<String, String> positions) {
        return positions.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    private static XSSFCellStyle createStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11.00);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

    private static void writePositions(Map<String, String> oldPositions, String pathToWriteFile) throws IOException {
        String positions = buildIterationPositions(oldPositions);
        Path file = Paths.get(pathToWriteFile);
        Files.writeString(file, positions, StandardCharsets.UTF_8);
    }

    private static LinkedHashMap<String, String> extractPositions(Map<String, String> positions) {
        return positions.entrySet().stream()
                .filter(entry -> Arrays.asList("Руководитель"
                        ).contains(entry.getValue())
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
    }

    private static String buildIterationPositions(Map<String, String> oldNewPositions) {
        return oldNewPositions.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\", ")
                .reduce((acc, x) -> acc + x).get();
    }

}
