import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ReadWrite {

    public static List<String[]> readFromCSV(String path) {
        List<String[]> li = null;
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(path));
            li = reader.readAll();
        } catch (IOException e) {
        }
        return li;
    }

    public static void writeCSV(List<String[]> li, String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileWriter fw = new FileWriter(file);
            CSVWriter writer = new CSVWriter(fw);
            writer.writeAll(li);
            writer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }

    public static void writeCSVD(double[][] li, String path) {
        try {
            List<String[]> stli = new ArrayList<>();
            for (int i = 0; i < li.length; i++) {
                String[] a = new String[li[i].length];
                for (int j = 0; j < li[i].length; j++) {
                    a[j] = li[i][j] + "";
                }
                stli.add(a);
            }
            File file = new File(path);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            FileWriter fw = new FileWriter(file);
            CSVWriter writer = new CSVWriter(fw);
            writer.writeAll(stli);
            writer.close();
        } catch (IOException e) {
            System.out.print(e.getMessage());
        }
    }
}
