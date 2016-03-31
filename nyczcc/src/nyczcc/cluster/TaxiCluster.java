package nyczcc.cluster;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import nyczcc.Point;
import nyczcc.Trajectory;
import nyczcc.database.SQLiteDBC;

public class TaxiCluster {

	private static List<Trajectory> trajectories;

	public static void main(String[] args) {

		SQLiteDBC db = new SQLiteDBC();
		db.connect();
		trajectories = db.retrieveRows(0, Integer.MAX_VALUE);

		double eps = 0.05;
		int minPts = 5;
		int clusterNum = 1;

		for (Trajectory t : trajectories) {

			if (!t.isVisited()) {
				t.setVisited(true);

				Queue<Trajectory> neighbors = getNeighbors(t, eps);
				if (neighbors.size() < minPts) {
					t.setCluster(-1);
				} else {
					int clusterId = clusterNum++;
					t.setCluster(clusterId);
					expandCluster(neighbors, clusterId, eps, minPts);
				}
			}
		}

		db.updateTrajectory(trajectories);

	}

	private static void expandCluster(Queue<Trajectory> queue, int clusterId, double eps, int minPts) {

		while (queue.size() > 0) {
			Trajectory t = queue.poll();
			if (!t.isVisited()) {
				t.setVisited(true);

				Queue<Trajectory> neighbors = getNeighbors(t, eps);
				if (neighbors.size() > minPts) {
					queue.addAll(neighbors);
				}
			}
			if (t.getCluster() == 0) {
				t.setCluster(clusterId);
			}
		}
	}

	private static Queue<Trajectory> getNeighbors(Trajectory t, Double eps) {
		Queue<Trajectory> neighbors = new LinkedList<Trajectory>();
		for (Trajectory x : trajectories) {
			double dist = calcDTheta(t, x) + calcDPerp(t, x) + calcDPara(t, x);
			// System.out.println(dist);
			if (dist < eps) {
				neighbors.add(x);
			}
		}

		return neighbors;
	}

	private static double calcDPara(Trajectory t, Trajectory x) {
		Trajectory l;
		Trajectory s;
		if (t.getLength() > x.getLength()) {
			l = t;
			s = x;
		} else {
			l = x;
			s = t;
		}

		Point lStart = new Point(l.getPickUpLatitude(), l.getPickUpLongitude());
		Point lEnd = new Point(l.getDropOffLatitude(), l.getDropOffLongitude());

		Point sStart = new Point(s.getPickUpLatitude(), s.getPickUpLongitude());
		Point sEnd = new Point(s.getDropOffLatitude(), s.getDropOffLongitude());

		// System.out.println("longer: " + lStart + ", " + lEnd);
		// System.out.println("shorter: " + sStart + ", " + sEnd);

		// get projection of start
		Point startProj = project(lStart, lEnd, sStart);

		// get Projection of end
		Point endProj = project(lStart, lEnd, sEnd);

		// System.out.println("Projections: " + startProj.toString() + ", " + endProj.toString());

		// calc distances
		double startDistance = startProj.distance(lStart);
		double endDistance = endProj.distance(lEnd);

		double result = Math.min(startDistance, endDistance);
		// System.out.println(result);
		return result;
	}

	private static double calcDPerp(Trajectory t, Trajectory x) {
		double tLat = (t.getPickUpLatitude() + t.getDropOffLatitude()) / 2;
		double tLon = (t.getPickUpLongitude() + t.getDropOffLongitude()) / 2;

		double xLat = (x.getPickUpLatitude() + x.getDropOffLatitude()) / 2;
		double xLon = (x.getPickUpLongitude() + x.getDropOffLongitude()) / 2;

		double result = new Point(tLat, tLon).distance(new Point(xLat, xLon));
		// System.out.println(result);
		return result;
	}

	private static double calcDTheta(Trajectory t, Trajectory x) {
		double result;

		Trajectory shorter;
		if (t.getLength() > x.getLength()) {
			shorter = x;
		} else {
			shorter = t;
		}

		double angle1 = Math.atan2(t.getPickUpLongitude() - t.getDropOffLongitude(),
				t.getPickUpLatitude() - t.getDropOffLatitude());
		double angle2 = Math.atan2(x.getPickUpLongitude() - x.getDropOffLongitude(),
				x.getPickUpLatitude() - x.getDropOffLatitude());
		double angle = Math.abs(Math.toDegrees(angle1 - angle2));
		if (angle > 180) {
			angle = angle - 180;
		}

		result = shorter.getLength();

		if (angle < 90) {
			result = result * Math.sin(Math.toRadians(angle));
		}

		// System.out.println(result);
		return result;
	}

	private static Point project(Point line1, Point line2, Point toProject) {
		double m = (double) (line2.y - line1.y) / (line2.x - line1.x);
		double b = (double) line1.y - (m * line1.x);

		double x = (m * toProject.y + toProject.x - m * b) / (m * m + 1);
		double y = (m * m * toProject.y + m * toProject.x + b) / (m * m + 1);

		Point result = new Point(x, y);
		return result;
	}

}
