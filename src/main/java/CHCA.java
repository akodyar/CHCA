import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;

import mdsj.MDSJ;
import smile.io.Read;
import smile.plot.swing.Canvas;
import smile.plot.swing.ScatterPlot;

/**
 * CHCA — Center-based Hierarchical Clustering Algorithm.
 * Isolated from {@code NewClass.hClusterTT} (DBHCA) which calls {@code NewHC.fitCDB}.
 */
public class CHCA {
    static double t = 0.5;
    static int n = 3;
    static String filename = "iris_csv.csv";
    static String writePath = "output/";

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, InvocationTargetException {
        File outDir = new File(writePath);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        File folder = new File("data");
        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("No data directory found: " + folder.getAbsolutePath());
            return;
        }

        for (File f : files) {
            filename = f.getName();
            if (!filename.contains(".csv")) {
                continue;
            }

            double[][] x = Read.csv(f.getAbsolutePath(), CSVFormat.DEFAULT.withDelimiter(',')).toArray();
            double[][] x1 = new double[x.length][x[0].length];
            for (int i = 0; i < x.length; i++) {
                for (int j = 0; j < x[i].length; j++) {
                    x1[i][j] = x[i][j];
                }
            }

            double[][] disSim;
            if (x.length == x[0].length) {
                disSim = new double[x.length][x[0].length];
                for (int i = 0; i < x.length; i++) {
                    for (int j = 0; j < x[i].length; j++) {
                        disSim[i][j] = x[i][j];
                    }
                }
                MDS(disSim.clone());
                disSim = x.clone();
            } else if (x.length != x[0].length && x[0].length > 2) {
                disSim = distSimilarity(x);
                MDS(disSim.clone());
            } else {
                disSim = distSimilarity(x);
            }

            List<String[]> d = ReadWrite.readFromCSV("data/distanceGen.csv");
            double[][] dim1 = new double[x.length][2];
            for (int i = 0; i < dim1.length; i++) {
                for (int j = 0; j < dim1[i].length; j++) {
                    if (x.length != x[0].length && x[0].length == 2) {
                        dim1 = x1.clone();
                        break;
                    } else if (d != null && i < d.size()) {
                        dim1[i][j] = Double.parseDouble(d.get(i)[j]);
                    }
                }
            }

            if (x.length == x[0].length || x[0].length > 2) {
                x = dim1.clone();
            }

            double[][] plotXX;
            if (x1.length != x1[0].length && x1[0].length >= 2) {
                plotXX = new double[x1.length][2];
                for (int i = 0; i < x1.length; i++) {
                    plotXX[i][0] = x1[i][0];
                    plotXX[i][1] = x1[i][1];
                }
            } else {
                plotXX = x.clone();
            }
            hClusterTT(disSim.clone(), plotXX, n, t);
        }
    }

    public static void hClusterTT(double[][] x, double[][] xx, int partition, double th) throws InterruptedException, InvocationTargetException {
        long t1 = System.currentTimeMillis();
        Map<Integer, List<HCCluster>> ls = NewHC.fitCDB(x);

        long t2 = System.currentTimeMillis();
        int indx = -1;
        int min = Integer.MAX_VALUE;
        int k = 0;
        List<HCCluster> l = null;
        Collection<List<HCCluster>> le = ls.values();
        {
            for (List<HCCluster> next : le) {
                if (next.size() == n) {
                    indx = k;
                    min = next.size();
                    l = next;
                    break;
                }
                k++;
            }
        }
        partition = indx;
        if (l == null) {
            System.err.println("No hierarchy level with n=" + n + " clusters for " + filename);
            return;
        }
        n = l.size();
        int[] y1 = new int[x.length];
        int[] yCenter = new int[x.length];
        List<String[]> clusters1 = new ArrayList<>();

        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.get(i).members.size(); j++) {
                int xv = l.get(i).members.get(j);
                y1[xv] = i;
                yCenter[xv] = l.get(i).clusterCenter;

                String[] axis1 = new String[2];
                axis1[0] = l.get(i).members.get(j) + "";
                axis1[1] = yCenter[xv] + "";
                clusters1.add(axis1);
            }
        }

        System.out.println("HC Time= " + (t2 - t1));

        int[] kmeans = y1;
        ReadWrite.writeCSV(clusters1, writePath + "HCA_CNN" + filename + l.size() + ".csv");

        double[][] xy = xx;
        if (xx.length > 0 && xx[0].length > 3) {
            xy = new double[xx.length][2];
            for (int i = 0; i < xx.length; i++) {
                xy[i][0] = xx[i][0];
                xy[i][1] = xx[i][1];
            }
        }
        Canvas canvas = ScatterPlot.of(xy, kmeans, '*').canvas();
        canvas.setTitle("HCA DB  " + l.size());
        canvas.setAxisLabels("sepallength", "sepalwidth");
        canvas.window();
    }

    public static void MDS(double[][] sim) throws IOException, URISyntaxException {
        double[][] xx = MDSJ.stressMinimization(sim.clone(), 2);
        double[][] dim = new double[xx[0].length][xx.length];

        for (int i = 0; i < xx[0].length; i++) {
            for (int j = 0; j < xx.length; j++) {
                dim[i][j] = xx[j][i];
            }
        }
        ReadWrite.writeCSVD(dim, "data/distanceGen.csv");
    }

    public static double[][] distSimilarity(double[][] dim) {
        double[][] dsim = new double[dim.length][dim.length];
        for (int i = 0; i < dsim.length; i++) {
            for (int j = 0; j < dsim.length; j++) {
                double sum = 0;
                for (int k = 0; k < dim[i].length; k++) {
                    sum += Math.pow(dim[i][k] - dim[j][k], 2);
                }
                dsim[i][j] = Math.sqrt(sum);
            }
        }

        ReadWrite.writeCSVD(dsim, "data/IntentionSimilarityGen.csv");
        return dsim;
    }
}
