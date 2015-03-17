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
        double r, eta;
        // Repeat 'epochs' times:
        for (int t = 0; t < epochs; t++) {
            /// Lets start with printing the progress
            printProgress((double)t/epochs);
            // Step 2: Calculate the squareSize and the learningRate, these decrease
            // lineary with the number of epochs.
            eta = initialLearningRate * (1 - (float)t/epochs);
            r   = ((float)n)/2        * (1 - (float)t/epochs);
            /// Clear all the current members of each cluster
            for (int i = 0; i < n; i++ )
                for (int j = 0; j < n; j++)
                    clusters[i][j].currentMembers.clear();
            // Step 3: Every input vector is presented to the map (always in the
            // same order)
            // For each vector its Best Matching Unit is found, and :
            for (int in =0; in < trainData.size(); in++) {
                /// We remember the indices of the best matching unit
                int mini = 0, minj = 0;
                double dist = 0, mindist = Double.POSITIVE_INFINITY;
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        for (int k = 0; k < dim; k ++ ) {
                            dist += Math.pow(clusters[i][j].prototype[k]
                                    - trainData.get(in)[k], 2);
                        }
                        dist = Math.sqrt(dist);
                        if (dist < mindist) {
                            mini = i;
                            minj = j;
                            mindist = dist;
                        }
                    }
                }
                // Step 4: All nodes within the neighbourhood of the BMU are changed,
                // you don't have to use distance relative learning.
                // Since training kohonen maps can take quite a while, presenting the
                // user with a progress bar would be nice
                /// Lets clamp the upper and lower bound so we don't train
                /// outside of the network
                int iUpper = Math.min(n-1, mini + (int)r);
                for (int i = Math.max(0, mini - (int)r); i <= iUpper; i++) {
                    int jUpper =  Math.min(n, minj - (int)r);
                    for (int j = Math.max(0, minj - (int)r); j <= jUpper; j++) {
                        Cluster c = clusters[i][j];
                        float[] prot = new float[dim];
                        /// Calculate the new prototype from the given formula
                        for (int k = 0; k < dim; k++ ) {
                            prot[k] = (float)((1 - eta) * c.prototype[k]
                                    + eta * trainData.get(in)[k]);
                        }
                        /// Update the prototype in place
                        c.prototype = prot;
                    }
                }
                clusters[mini][minj].currentMembers.add(in);
            }
        }
        System.out.print('\n');
        return true;
    }

    public boolean test() {
        // iterate along all clients
        int hits = 0, requests = 0, prefetch = 0;
        // for each client find the cluster of which it is a member
        for (int client = 0; client < trainData.size(); client++) {
            Cluster owner = null;
            for (int i=0; i < n; i++)
                for (int j=0; j < n; j++)
                    if (clusters[i][j].currentMembers.contains(client))
                        owner = clusters[i][j];
            // get the actual testData (the vector) of this client
            float[] test = testData.get(client);
            // iterate along all dimensions
            // and count prefetched htmls
            for (int i = 0; i < dim; i++) {
                if (owner.prototype[i] > prefetchThreshold && test[i] > prefetchThreshold)
                    hits++;
                if (test[i] > prefetchThreshold)
                    requests++;
                if (owner.prototype[i] > prefetchThreshold)
                    prefetch++;
            }
            // count number of hits
            // count number of requests
            // set the global variables hitrate and accuracy to their appropriate
            // value
        }
        hitrate  = (float)hits/prefetch;
        accuracy = (float)hits/requests;
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
