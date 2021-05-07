package exporter.influx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//запись в xlsx
public class Report {

    private String path;
    private File file;
    private FileInputStream inputStream;
    private XSSFWorkbook workbook;

    //конструктор, который всё сразу делает
    public Report(String template, String path, Data d) {
        try {
            Open(template, path);
            WriteData(d);
            Close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //очистска страницы
    private void ClearSheet(XSSFSheet sheet) {
        try {
            for (int i = sheet.getLastRowNum(); i >= 0; --i) {
                XSSFRow row = sheet.getRow(i);
                if (row != null) {
                    sheet.removeRow(row);
                }
            }
        } catch (Exception ex) {
            System.out.println("/r/nProbably can't find this sheet: " + sheet);
        }
    }

    //создание реплики шаблона
    private void Open(String template, String path) {
        try {
            this.path = path;
            this.file = new File(template);
            this.inputStream = new FileInputStream(file);
            this.workbook = new XSSFWorkbook(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //сохранение результатов
    private void Close() {
        try {
            XSSFFormulaEvaluator.evaluateAllFormulaCells(this.workbook);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            this.inputStream.close();
            File e = new File(this.path);
            FileOutputStream out = new FileOutputStream(e);
            this.workbook.write(out);
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    //запись в шаблон
    private void WriteData(Data d) {
        //по количеству запросов (1 запрос = 1 лист)
        for (int i = 0; i < d.sheetName.size(); i++) {
            //чистим лист
            ClearSheet(workbook.getSheet(d.sheetName.get(i)));
            XSSFSheet xsheet = workbook.getSheet(d.sheetName.get(i));

            int columnOffset = 0;
            int rowsMaxCount = 0;
            //по количеству периодов
            ArrayList<String[][]> sheetData = d.sheetData.get(i);
            for (int j = 0; j < sheetData.size(); j++) {
                int currentRows = 0;
                //берём текущий период
                String[][] durationData = sheetData.get(j);

                //запоминаем на сколько сдвинуть таблицу для след периода
                int columnOff = durationData[0].length;
                //идем по строкам периода
                for (int r = 0; r < durationData.length; r++) {
                    //создаем или берем созданную строку
                    XSSFRow row = null;
                    currentRows++;
                    if (currentRows > rowsMaxCount) {
                        row = xsheet.createRow(r);
                        rowsMaxCount++;
                    } else {
                        row = xsheet.getRow(r);
                    }

                    //идем по столбацм
                    for (int c = 0; c < durationData[r].length; c++) {
                        //заполняем с учетом типа данных и смещения по периодам
                        if (r == 1 && (c == 0 || c == 1)) {
                            //времена переводим в LocalDateTime
                            XSSFCell cell = row.createCell(c + columnOffset, CellType.BLANK);
                            cell.setCellValue(durationData[r][c]);
                            cell.setCellValue(Utils.StringToLocalDateTime(durationData[r][c]));
                        } else {
                            //остальные поля в число, если не получилось - в строку
                            try {
                                double resd = Double.parseDouble(durationData[r][c]);
                                XSSFCell cell = row.createCell(c + columnOffset, CellType.NUMERIC);
                                cell.setCellValue(resd);
                                //System.out.println("DOUBLE. " + "durationData[" + r + "][" + c + "]=" + durationData[r][c]);
                            } catch (Exception ex) {
                                XSSFCell cell = row.createCell(c + columnOffset, CellType.STRING);
                                cell.setCellValue(durationData[r][c]);
                                //System.out.println("STRING. " + "durationData[" + r + "][" + c + "]=" + durationData[r][c]);
                            }
                        }
                    }
                    //System.out.println();
                }
                //сдвигаем таблицу влево для нового периода
                columnOffset += columnOff + 1;
            }
        }
    }
}
