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
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class ScriptBuilder {


    private static final Integer FIRST_PAGE_EXCEL_BOOK = 0;
    private static final Integer FIRST_COLUMN = 0;
    private static final Integer SECOND_COLUMN = 1;
    private static final String PATH_TO_SCRIPT_FILE = "/home/naglezh/IdeaProjects/script_builder/script.txt";
    private static final String PATH_TO_MAP_POSITION_FILE = "/home/naglezh/IdeaProjects/script_builder/union.xlsx";


    //прочитать из файла старые и новые значения. Создать скрип для подмены
    public static void main(String[] args) throws IOException {
        Map<String, String> oldNewPositions = readTwoColumns(new File(PATH_TO_MAP_POSITION_FILE));
        setCapitalLetter(oldNewPositions);
        writeScript(oldNewPositions);
    }

    private static void setCapitalLetter(Map<String, String> oldNewPositions) {
        oldNewPositions.entrySet()
                .forEach(e -> {
                    String newPositionName = e.getValue();
                    String s = newPositionName.substring(0, 1).toUpperCase() + newPositionName.substring(1).toLowerCase();
                    e.setValue(s);
                });
    }

    private static void writeScript(Map<String, String> oldNewPositions) throws IOException {
        String bodyInsert = buildInsertBody(oldNewPositions);
        String str = QueryConstant.BEGIN_QUERY + bodyInsert + QueryConstant.END_QUERY;
        Path file = Paths.get(PATH_TO_SCRIPT_FILE);
        Files.writeString(file, str, StandardCharsets.UTF_8);
    }

    private static String buildInsertBody(Map<String, String> oldNewPositions) {
        String insertBodyDirty = oldNewPositions.entrySet().stream().map(entry ->
                "PERFORM renamePosition('" + entry.getKey() + "', '" + entry.getValue() + "');\n"
        ).reduce((acc, x) -> acc + x).get();
        return insertBodyDirty.substring(0, insertBodyDirty.length() - 2);
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
}
