package position;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static java.util.Objects.nonNull;

public class ScriptBuilder {


    private static final Integer FIRST_PAGE_EXCEL_BOOK = 0;
    private static final Integer OLD_POSITION_COLUMN = 0;
    private static final Integer NEW_POSITION_COLUMN = 1;
    private static final Integer NEW_DEPARTMENT_COLUMN = 2;
    private static final String PATH_TO_SCRIPT_FILE_CHANGE_POSITION = "/home/naglezh/IdeaProjects/script_builder/position/change_position_script.txt";
    private static final String PATH_TO_SCRIPT_FILE_CHANGE_DEPARTMENT = "/home/naglezh/IdeaProjects/script_builder/department/change_department_script.txt";
    private static final String PATH_TO_MAP_FILE = "/home/naglezh/IdeaProjects/script_builder/source_excel/union.xlsx";


    /**
     * прочитать из файла старые и новые значения. Создать скрипты для подмены
     */
    public static void main(String[] args) throws IOException {

        Map<String, String> oldNewPositions = readOldNewPositions();
        setCapitalLetter(oldNewPositions);
        writeScriptChangePositions(oldNewPositions);

        Map<String, String> oldNewDepartments = readOldNewDepartments();
        writeScriptChangeDepartments(oldNewDepartments);
    }

    private static void setCapitalLetter(Map<String, String> oldNewPositions) {
        oldNewPositions.entrySet()
                .forEach(e -> {
                    String newPositionName = e.getValue();
                    String s = newPositionName.substring(0, 1).toUpperCase() + newPositionName.substring(1).toLowerCase();
                    e.setValue(s);
                });
    }

    private static void writeScriptChangePositions(Map<String, String> oldNewPositions) throws IOException {
        String bodyInsert = buildInsertBodyPositions(oldNewPositions);
        String str = QueryConstant.BEGIN_POSITION_QUERY + bodyInsert + QueryConstant.END_POSITION_QUERY;
        Path file = Paths.get(PATH_TO_SCRIPT_FILE_CHANGE_POSITION);
        Files.writeString(file, str, StandardCharsets.UTF_8);
    }

    private static void writeScriptChangeDepartments(Map<String, String> oldNewDepartments) throws IOException {
        String bodyInsert = buildInsertBodyDepartments(oldNewDepartments);
        String str = QueryConstant.BEGIN_DEPARTMENT_QUERY + bodyInsert + QueryConstant.END_DEPARTMENT_QUERY;
        Path file = Paths.get(PATH_TO_SCRIPT_FILE_CHANGE_DEPARTMENT);
        Files.writeString(file, str, StandardCharsets.UTF_8);
    }

    private static String buildInsertBodyPositions(Map<String, String> oldNewPositions) {
        String insertBodyDirty = oldNewPositions.entrySet().stream().map(entry ->
                "PERFORM renamePosition('" + entry.getKey() + "', '" + entry.getValue() + "');\n"
        ).reduce((acc, x) -> acc + x).get();
        return insertBodyDirty.substring(0, insertBodyDirty.length() - 2);
    }

    private static String buildInsertBodyDepartments(Map<String, String> oldNewPositions) {
        String insertBodyDirty = oldNewPositions.entrySet().stream()
                .filter(entry -> StringUtils.isNotBlank(entry.getKey()) && StringUtils.isNotBlank(entry.getValue()))
                .map(entry ->
                "PERFORM insertDepartmentToEmployee('" + entry.getKey() + "', '" + entry.getValue() + "');\n"
        ).reduce((acc, x) -> acc + x).get();
        return insertBodyDirty.substring(0, insertBodyDirty.length() - 2);
    }

    private static Map<String, String> readOldNewPositions() throws IOException {
        Workbook wb = WorkbookFactory.create(new File(PATH_TO_MAP_FILE));
        Sheet sheet = wb.getSheetAt(FIRST_PAGE_EXCEL_BOOK);
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<String, String> oldNewPositionMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell oldPosition = row.getCell(OLD_POSITION_COLUMN);
            Cell newPosition = row.getCell(NEW_POSITION_COLUMN);
            if (nonNull(oldPosition) && nonNull(newPosition)) {
                if (!newPosition.getStringCellValue().isEmpty() && !oldPosition.getStringCellValue().isEmpty()) {
                    oldNewPositionMap.put(oldPosition.getStringCellValue(), newPosition.getStringCellValue());
                }
            }
        }
        return oldNewPositionMap;
    }

    private static Map<String, String> readOldNewDepartments() throws IOException {
        Workbook wb = WorkbookFactory.create(new File(PATH_TO_MAP_FILE));
        Sheet sheet = wb.getSheetAt(FIRST_PAGE_EXCEL_BOOK);
        Iterator<Row> rowIterator = sheet.rowIterator();
        Map<String, String> oldNewDepartmentMap = new HashMap<>();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell oldPosition = row.getCell(OLD_POSITION_COLUMN);
            Cell newDepartment = row.getCell(NEW_DEPARTMENT_COLUMN);
            if (nonNull(oldPosition) && nonNull(newDepartment)) {
                if (!newDepartment.getStringCellValue().isEmpty() && !oldPosition.getStringCellValue().isEmpty()) {
                    oldNewDepartmentMap.put(oldPosition.getStringCellValue(), newDepartment.getStringCellValue());
                }
            }
        }
        return oldNewDepartmentMap;
    }
}
