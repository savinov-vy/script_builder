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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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


    public static void main(String[] args) throws IOException {
        Map<String, String> oldNewPositions = readTwoColumns(new File(PATH_TO_MAP_POSITION_FILE));
        writeScript(oldNewPositions, PATH_TO_SCRIPT_FILE);
    }

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
        int rownum = 0;
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Union position name sheet");
        XSSFCellStyle style = createStyle(workbook);
        for (String position : positions) {
            if (!position.equals("")) {
                Row row = sheet.createRow(rownum++);
                Cell cell = row.createCell(FIRST_COLUMN, CellType.STRING);
                cell.setCellValue(position);
                cell.setCellStyle(style);
            }
        }
        try (FileOutputStream fos = new FileOutputStream("/home/naglezh/IdeaProjects/script_builder/union.xlsx")) {
            workbook.write(fos);
        }
    }

    private static XSSFCellStyle createStyle(XSSFWorkbook workbook) {
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11.00);
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFont(font);
        return style;
    }

}
