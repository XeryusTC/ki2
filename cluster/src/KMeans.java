import java.util.*;

public class KMeans extends ClusteringAlgorithm {
    // Number of clusters
    private int k;

    // Dimensionality of the vectors
    private int dim;

    // Threshold above which the corresponding html is prefetched
    private double prefetchThreshold;

    // Array of k clusters, class cluster is used for easy bookkeeping
    private Cluster[] clusters;

    // This class represents the clusters, it contains the prototype (the mean
    // of all it's members)
    // and memberlists with the ID's (which are Integer objects) of the
    // datapoints that are member of that cluster.
    // You also want to remember the previous members so you can check if the
    // clusters are stable.
    static class Cluster {
        float[] prototype;

        Set<Integer> currentMembers;
        Set<Integer> previousMembers;

        public Cluster() {
            currentMembers = new HashSet<Integer>();
            previousMembers = new HashSet<Integer>();
        }
    }
    // These vectors contains the feature vectors you need; the feature vectors
    // are float arrays.
    // Remember that you have to cast them first, since vectors return objects.
    private Vector<float[]> trainData;
    private Vector<float[]> testData;

    // Results of test()
    private double hitrate;
    private double accuracy;

    public KMeans(int k, Vector<float[]> trainData, Vector<float[]> testData,
                  int dim) {
        this.k = k;
        this.trainData = trainData;
        this.testData = testData;
        this.dim = dim;
        prefetchThreshold = 0.5;

        // Here k new cluster are initialized
        clusters = new Cluster[k];
        for (int ic = 0; ic < k; ic++) {
            clusters[ic] = new Cluster();
			clusters[ic].prototype = new float[dim];
		}
    }

    public boolean train() {
        // implement k-means algorithm here:
        // Step 1: Select an initial random partioning with k clusters
		Random rand = new Random();
		for (int i=0; i<trainData.size(); ++i) {
			clusters[rand.nextInt(clusters.length)].currentMembers.add(i);
		}

		boolean converged = false;
		while (!converged) {
			/// Calculate prototype and previousMembers for each cluster
			for (Cluster c : clusters) {
				for (int i=0; i<dim; ++i) /// Reset cluster centres
					c.prototype[i] = 0f;
				Iterator it = c.currentMembers.iterator();
				while (it.hasNext()) {
					Integer id = (Integer)it.next();
					for (int j=0; j<dim; ++j)
						c.prototype[j] += trainData.get(id)[j];
				}
				for (int i=0; i<dim; ++i) {
					c.prototype[i] /= (float)(c.currentMembers.size());
				}
				c.previousMembers.clear();
				c.previousMembers.addAll(c.currentMembers);
				c.currentMembers.clear();
			}
			/// Calculate distance to each cluster center and add datapoint to closest cluster
			for (int i=0; i<trainData.size(); ++i) {
				double minerr = Double.POSITIVE_INFINITY;
				int mincluster=0;
				for (int k=0; k<clusters.length; ++k) {
					double err = 0f;
					for (int j=0; j<dim; ++j)
						err += Math.pow(trainData.get(i)[j] - clusters[k].prototype[j], 2);
					err = Math.sqrt(err);
					if (err < minerr) {
						mincluster = k;
						minerr = err;
					}
				}
				clusters[mincluster].currentMembers.add(i);
			}
			/// Check if we converged
			converged = true;
			for (Cluster c : clusters) {
				if (!c.currentMembers.equals(c.previousMembers))
					converged = false;
			}
		}
		return false;
    }

    public boolean test() {
        // iterate along all clients. Assumption: the same clients are in the
        // same order as in the testData
        // for each client find the cluster of which it is a member
        // get the actual testData (the vector) of this client
        // iterate along all dimensions
        // and count prefetched htmls
        // count number of hits
        // count number of requests
        // set the global variables hitrate and accuracy to their appropriate
        // value
		/// Iterate over all the clusters
		int hits=0, requests=0, prefetch=0;
		for (Cluster c : clusters) {
			/// Iterate over the members of the clusters and count their hitrate etc
			Iterator it = c.currentMembers.iterator();
			while (it.hasNext()) {
				Integer id = (Integer)it.next(); // Select the right client
				for (int i=0; i<dim; ++i) {
					if (c.prototype[i] > prefetchThreshold &&
							testData.get(id)[i] > prefetchThreshold)
						++hits;
					if (testData.get(id)[i] > prefetchThreshold)
						++requests;
					if (c.prototype[i] > prefetchThreshold)
						prefetch++;
				}
			}
		}
		hitrate  = hits/(float)prefetch;
		accuracy = hits/(float)requests;

        return true;
    }

    // The following members are called by RunClustering, in order to present
    // information to the user
    public void showTest() {
        System.out.println("Prefetch threshold = " + this.prefetchThreshold);
        System.out.println("Hitrate = " + this.hitrate);
        System.out.println("Accuracy = " + this.accuracy);
        System.out.println("Hitrate+Accuracy = " +
                           (this.hitrate + this.accuracy));
    }

    public void showMembers() {
        for (int i = 0; i < k; i++)
            System.out.println("\nMembers cluster[" + i + "] :" +
                               clusters[i].currentMembers);
    }

    public void showPrototypes() {
        for (int ic = 0; ic < k; ic++) {
            System.out.print("\nPrototype cluster[" + ic + "] :");

            for (int ip = 0; ip < dim; ip++)
                System.out.print(clusters[ic].prototype[ip] + " ");

            System.out.println();
        }
    }

    // With this function you can set the prefetch threshold.
    public void setPrefetchThreshold(double prefetchThreshold) {
        this.prefetchThreshold = prefetchThreshold;
    }
}
