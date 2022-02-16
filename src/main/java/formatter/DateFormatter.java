package formatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import static java.util.Objects.nonNull;

public class DateFormatter {
    private static final Integer FORTY_COLUMN = 3;
    private static final String PATH_FILE_IN = "/home/naglezh/IdeaProjects/script_builder/date_test.xlsx";
    private static final String PATH_FILE_OUT = "/home/naglezh/IdeaProjects/script_builder/date_test_gk.xlsx";
    private static final Integer FIRST_PAGE_EXCEL_BOOK = 0;
    private static final SimpleDateFormat formatterIn = new SimpleDateFormat("dd.MM.yyyy" );
    private static final SimpleDateFormat formatterOut = new SimpleDateFormat("yyyy-MM-dd" );

    public static void main(String[] args) throws IOException, ParseException {
        rewrite(new File(PATH_FILE_IN));
    }

    private static void rewrite(File file) throws IOException, ParseException {
        Workbook wb = WorkbookFactory.create(file);
        Sheet sheet = wb.getSheetAt(FIRST_PAGE_EXCEL_BOOK);
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell4 = row.getCell(FORTY_COLUMN);
            if (nonNull(cell4)) {
                if (!cell4.getStringCellValue().isEmpty()) {
                    String valueIn = cell4.getStringCellValue();
                    Date date = formatterIn.parse(valueIn);
                    cell4.setCellValue(formatterOut.format(date));
                }
            }
        }

        FileOutputStream out = new FileOutputStream(new File(PATH_FILE_OUT));
        wb.write(out);
        out.close();
    }
}
