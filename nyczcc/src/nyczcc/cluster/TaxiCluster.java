package nyczcc.cluster;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nyczcc.Trajectory;

public class TaxiCluster {

	public static void main(String[] args) {
		double eps = 1.0;
		int minPts = 5;
		int clusterNum = 0;
		// TODO for each point in the db
		Trajectory t = null;

		if (!t.isVisited()) {
			t.setVisited(true);
			// TODO set in db

			Queue<Trajectory> neighbors = getNeighbors(t, eps);
			if (neighbors.size() < minPts) {
				t.setCluster("NOISE");
				// TODO set in db
			} else {
				String clusterId = "" + (clusterNum++);
				t.setCluster(clusterId);
				expandCluster(neighbors, clusterId, eps, minPts);
			}
		}

	}

	private static void expandCluster(Queue<Trajectory> queue, String clusterId, double eps, int minPts) {

		while (queue.size() > 0) {
			Trajectory t = queue.poll();
			if (!t.isVisited()) {
				t.setVisited(true);
				// set in db

				Queue<Trajectory> neighbors = getNeighbors(t, eps);
				if (neighbors.size() > 0) {
					queue.addAll(neighbors);
				}
			}
			if (t.getCluster() == null) {
				t.setCluster(clusterId);
				// TODO set in db
			}
		}
	}

	private static Queue<Trajectory> getNeighbors(Trajectory t, Double eps) {
		Queue<Trajectory> neighbors = new LinkedList<Trajectory>();
		// TODO for each point in db
		Trajectory x = null;
		double dist = calcDTheta(t, x) + calcDPerp(t, x) + calcDPara(t, x);
		if(dist < eps){
			neighbors.add(x);
		}

		return neighbors;
	}

	private static int calcDPara(Trajectory t, Trajectory x) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int calcDPerp(Trajectory t, Trajectory x) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int calcDTheta(Trajectory t, Trajectory x) {
		// TODO Auto-generated method stub
		return 0;
	}

}
