import java.util.*;

public class Kohonen extends ClusteringAlgorithm {
    // Size of clustersmap
    private int n;

    // Number of epochs
    private int epochs;

    // Dimensionality of the vectors
    private int dim;

    // Threshold above which the corresponding html is prefetched
    private double prefetchThreshold;

    private double initialLearningRate;

    // This class represents the clusters, it contains the prototype (the mean
    // of all it's members)
    // and a memberlist with the ID's (Integer objects) of the datapoints that
    // are member of that cluster.
    private Cluster[][] clusters;

    // Vector which contains the train/test data
    private Vector<float[]> trainData;
    private Vector<float[]> testData;

    // Results of test()
    private double hitrate;
    private double accuracy;

    static class Cluster {
        float[] prototype;

        Set<Integer> currentMembers;

        public Cluster() { currentMembers = new HashSet<Integer>(); }
    }

    public Kohonen(int n, int epochs, Vector<float[]> trainData,
                   Vector<float[]> testData, int dim) {
        this.n = n;
        this.epochs = epochs;
        prefetchThreshold = 0.5;
        initialLearningRate = 0.8;
        this.trainData = trainData;
        this.testData = testData;
        this.dim = dim;

        Random rnd = new Random();

        // Here n*n new cluster are initialized
        clusters = new Cluster[n][n];
        for (int i = 0; i < n; i++) {
            for (int i2 = 0; i2 < n; i2++) {
                clusters[i][i2] = new Cluster();
                clusters[i][i2].prototype = new float[dim];
                for (int j = 0; j < dim; j++) {
                    clusters[i][i2].prototype[j] = rnd.nextFloat();
                }
            }
        }
    }

    public boolean train() {
        // Step 1: initialize map with random vectors (A good place to do this,
        // is in the initialisation of the clusters)
        /// r and eta are created outside of the loop
        float r, eta;
        // Repeat 'epochs' times:
        for (int t = 0; t < epochs; t++) {
            printProgress((double)t/epochs);
            // Step 2: Calculate the squareSize and the learningRate, these decrease
            // lineary with the number of epochs.
            eta = (float)0.8   * (1 - (float)t/epochs);
            r   = ((float)n)/2 * (1 - (float)t/epochs);
            // Step 3: Every input vector is presented to the map (always in the
            // same order)
            // For each vector its Best Matching Unit is found, and :
            for (float[] in : trainData) {
                int mini = 0, minj = 0;
                double dist = 0, mindist = Double.POSITIVE_INFINITY;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        for (int k = 0; k < dim; k ++ ) {
                            dist += Math.pow(clusters[i][j].prototype[k] - in[k], 2);
                        }
                        if (dist < mindist) {
                            mini = i;
                            minj = j;
                            mindist = dist;
                        }
                    }
                }
                for (int i = mini - (int)r; i < mini + (int)r; i++) {
                    if (i < n && i >= 0) {
                        for (int j = minj - (int)r; j < minj - (int)r; j++) {
                            if (0 <= j && j < n) {
                                Cluster c = clusters[i][j];
                                float[] prot = new float[dim];
                                for (int k = 0; k < dim; k++ ) {
                                    prot[k] = (1 - eta) * c.prototype[k] + eta * in[k];
                                }
                                c.prototype = prot;
                            }
                        }
                    }
                }
            }
            // Step 4: All nodes within the neighbourhood of the BMU are changed,
            // you don't have to use distance relative learning.
            // Since training kohonen maps can take quite a while, presenting the
            // user with a progress bar would be nice
        }
        System.out.print('\n');
        return true;
    }

    public boolean test() {
        // iterate along all clients
        // for each client find the cluster of which it is a member
        // get the actual testData (the vector) of this client
        // iterate along all dimensions
        // and count prefetched htmls
        // count number of hits
        // count number of requests
        // set the global variables hitrate and accuracy to their appropriate
        // value
        return true;
    }

    public void showTest() {
        System.out.println("Initial learning Rate=" + initialLearningRate);
        System.out.println("Prefetch threshold=" + prefetchThreshold);
        System.out.println("Hitrate: " + hitrate);
        System.out.println("Accuracy: " + accuracy);
        System.out.println("Hitrate+Accuracy=" + (hitrate + accuracy));
    }

    public void showMembers() {
        for (int i = 0; i < n; i++)
            for (int i2 = 0; i2 < n; i2++)
                System.out.println("\nMembers cluster[" + i + "][" + i2 +
                                   "] :" + clusters[i][i2].currentMembers);
    }

    public void showPrototypes() {
        for (int i = 0; i < n; i++) {
            for (int i2 = 0; i2 < n; i2++) {
                System.out.print("\nPrototype cluster[" + i + "][" + i2 +
                                 "] :");

                for (int i3 = 0; i3 < dim; i3++)
                    System.out.print(" " + clusters[i][i2].prototype[i3]);

                System.out.println();
            }
        }
    }

    public void setPrefetchThreshold(double prefetchThreshold) {
        this.prefetchThreshold = prefetchThreshold;
    }


    public static void printProgress(double progress) {
        final int width = 60;
        System.out.print("\r0 [");
        int i = 0;
        for (; i <= (int)(progress*width);i++)
            System.out.print('#');
        for (; i < width; i++)
            System.out.print(' ');
        System.out.print("] 100");
    }
}
