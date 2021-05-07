package exporter.influx;

import java.util.ArrayList;

//хранение результатов перед записью в xlsx
public class Data {

    public ArrayList<String> sheetName = new ArrayList();
    public ArrayList<ArrayList<String[][]>> sheetData = new ArrayList();

    public void add(String sheetn, ArrayList<String[][]> sheetd) {
        this.sheetName.add(sheetn);
        this.sheetData.add(sheetd);
    }
}
