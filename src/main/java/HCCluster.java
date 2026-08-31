import java.util.ArrayList;
import java.util.List;

public class HCCluster {

    public List<Integer> members;
    public int clusterCenter, clusterID;

    public HCCluster() {
        members = new ArrayList<>();
    }

    public void setClusterCenter(double[][] sim) {
        int minID = -1;
        double min = Double.MAX_VALUE;
        double sum;
        for (int i = 0; i < members.size(); i++) {
            sum = 0;
            for (int j = 0; j < members.size(); j++) {
                if (i != j) {
                    sum += sim[members.get(i)][members.get(j)];
                }
            }
            if (sum < min) {
                min = sum;
                minID = members.get(i);
            }
        }

        clusterCenter = minID;
    }
}
