import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hierarchical clustering used by {@code CHCA.hClusterTT} via {@link #fitCDB(double[][])}.
 */
public class NewHC {
    static List<HCCluster> lHcCluster = new ArrayList<>();
    static Map<Integer, List<HCCluster>> HC = new HashMap<Integer, List<HCCluster>>();

    public static Map<Integer, List<HCCluster>> fitCDB(double[][] data) {
        intializedClustersDB(data);
        HC.clear();
        List<Double> mergeDistances = new ArrayList<>();
        while (lHcCluster.size() >= 2) {
            int[] mg = new int[2];
            double min = findMax(data, mg, lHcCluster, Double.MAX_VALUE);
            try {
                merge(mg, data);
                HC.put(lHcCluster.size(), cloneB(lHcCluster));
                mergeDistances.add(min);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                break;
            }
        }
        return HC;
    }

    public static void merge(int[] meg, double[][] data) {
        if (meg[0] < 0 || meg[1] < 0) {
            throw new IllegalStateException("No mergeable pair under threshold");
        }
        HCCluster a = lHcCluster.get(meg[0]);
        HCCluster b = lHcCluster.get(meg[1]);
        a.members.addAll(b.members);
        a.setClusterCenter(data);
        lHcCluster.remove(meg[1]);
    }

    private static void intializedClustersDB(double[][] data) {
        lHcCluster = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            HCCluster c = new HCCluster();
            c.members.add(i);
            c.clusterCenter = i;
            c.clusterID = i;
            lHcCluster.add(c);
        }
    }

    private static double findMax(double[][] data, int[] mg, List<HCCluster> hcList, double th) {
        double min = Double.MAX_VALUE;
        double rmin = Double.MAX_VALUE;
        for (int i = 0; i < hcList.size(); i++) {
            for (int j = 0; j < hcList.size(); j++) {
                if (i == j) {
                    continue;
                }
                List<Integer> xList = hcList.get(i).members;
                List<Integer> yList = hcList.get(j).members;
                int x = hcList.get(i).clusterCenter;
                int y = hcList.get(j).clusterCenter;
                double dMin = data[x][y];
                if (!xList.isEmpty() && !yList.isEmpty()) {
                    double fMin = 0, sMin = 0;
                    for (int yy : yList) {
                        fMin += data[x][yy];
                    }
                    fMin /= yList.size();
                    for (int xx : xList) {
                        sMin += data[xx][y];
                    }
                    sMin /= xList.size();
                    dMin = (fMin + sMin) / 2.0;
                }

                if (dMin < min) {
                    min = dMin;
                    rmin = dMin;
                    mg[0] = Math.min(i, j);
                    mg[1] = Math.max(i, j);
                }
            }
        }

        if (min > th) {
            mg[0] = -1;
            mg[1] = -1;
        }

        return rmin;
    }

    public static List<HCCluster> cloneB(List<HCCluster> li) {
        List<HCCluster> stl = new ArrayList<>();
        for (HCCluster hCCluster : li) {
            HCCluster hhc = new HCCluster();
            hhc.clusterCenter = hCCluster.clusterCenter;
            hhc.clusterID = hCCluster.clusterID;
            List<Integer> inL = new ArrayList<>();
            for (int me : hCCluster.members) {
                inL.add(me);
            }
            hhc.members = inL;
            stl.add(hhc);
        }
        return stl;
    }
}
