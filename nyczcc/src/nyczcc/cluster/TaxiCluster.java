package nyczcc.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import nyczcc.Cluster;
import nyczcc.Point;
import nyczcc.Trajectory;
import nyczcc.database.ReadCSV;
import nyczcc.database.SQLiteDBC;
import nyczcc.database.WriteCSV;
import nyczcc.optimization.SetCovering;
import nyczcc.optimization.SetCovering.SetPartition;
import nyczcc.validation.DistanceMetricValidator;
import nyczcc.visual.DisplayPicture;

public class TaxiCluster {
	
	private static boolean performClustering = false;

	private static List<Trajectory> trajectories;
	private static double thetaW = 1;
	private static double paraW = 1;
	private static double perpW = 1;
	private static boolean printDist = false;

	static SQLiteDBC db = new SQLiteDBC();
	static List<Trajectory> visitedList = new LinkedList<>();
	static Map<Integer, Boolean> vMap = new HashMap<>();

	public static void main(String[] args) {
			
		db.connect();
		// printDist = true;
		SQLiteDBC db = new SQLiteDBC();
		db.connect();
		trajectories = db.retrieveRows(0, Integer.MAX_VALUE);

		if (performClustering) {
			// reset the trajectories
			trajectories.forEach(t -> {
				t.setCluster(0);
				t.setVisited(false);
			});

			db.updateTrajectoryBatch(trajectories);

			System.out.println("Total size: " + trajectories.size());
			double eps = 3.5;
			int minPts = 100;

			int clusterNum = 1;

			for (Trajectory t : trajectories) {

				if (vMap.containsKey(t.getRowID())) {
					continue;
				}
				if (!t.isVisited()) {
					// System.out.println(t);
					t.setVisited(true);
					visitedList.add(t);
					vMap.put(t.getRowID(), true);

					Queue<Trajectory> neighbors = getNeighbors(t, eps, minPts);
					if (neighbors.size() < minPts) {
						t.setCluster(-1);
					} else {
						int clusterId = clusterNum++;
						t.setCluster(clusterId);
						expandCluster(neighbors, clusterId, eps, minPts);
					}

				}

			}
			System.out.println("Finished Clustering!");
			System.out.println("Writing To DB");
			db.updateTrajectoryBatch(visitedList);
			System.out.println("Finished Writing To DB");
			trajectories = db.retrieveRows(0, Integer.MAX_VALUE);
		}

		Map<Integer, Long> results = trajectories.stream()
				.collect(Collectors.groupingBy(p -> p.getCluster(), Collectors.counting()));

		results.forEach((id, count) -> System.out.println("id: " + id + " count: " + count));

		new WriteCSV("clustert.csv").writeCSV(trajectories);

		DisplayPicture pic = new DisplayPicture();

		// pic.displayPicture("Trajectory Plot", trajectories);

		List<Trajectory> ref = new LinkedList<>();
		for (Integer x : trajectories.stream().map(z -> z.getCluster()).distinct()
				.collect(Collectors.toCollection(LinkedList::new))) {
			List<Trajectory> nt = trajectories.stream()
					.filter(z -> z.getCluster() == x && z.getPickUpLatitude() != 0 && z.getPickUpLongitude() != 0
							&& z.getDropOffLatitude() != 0 && z.getDropOffLongitude() != 0)
					.collect(Collectors.toCollection(LinkedList::new));
			if (nt.size() == 0) {
				continue;
			}
			Trajectory r = new ReferenceTrajectory(nt).ref;
			r.setRowID(x);
			ref.add(r);
		}

		pic.displayPicture("Reference Trajectory Plot", ref);

		// db.updateTrajectory(trajectories);

		new WriteCSV("reftrajectories.csv").writeCSV(ref);
		
		
		Set<Cluster> clist = new HashSet<Cluster>();
		for (Trajectory r : ref){
			clist.add(new Cluster(trajectories.stream().filter(x -> x.getCluster() == r.getCluster()).collect(Collectors.toCollection(ArrayList::new)), r));
		}
		
		SetCovering s = new SetCovering(clist, 40.64, 40.83, -74.02, -73.78, 0.00001);
		
		List<SetPartition> opt = s.getOptimal(33, .001);
		
		new WriteCSV("setpartition.csv").writeSetCSV(opt);
		
		pic.displayOptimization("Optimization", ref, opt);
		
		System.out.println("---------------------------- Self Validation ------------------------------");
		DistanceMetricValidator dmv = new DistanceMetricValidator(trajectories, opt.stream().map(x -> new Point((x.minLat + x.maxLat)/2, (x.minLon + x.maxLon)/2)).collect(Collectors.toCollection(ArrayList::new)));
		System.out.println("Num Valid Users: " + dmv.sortValidation(.005).size());
		System.out.println("Num Trajectories Begin: " + dmv.startPoints.size());
		System.out.println("Num Trajectories End: " + dmv.endPoints.size());
		
		List<Point> point = new ReadCSV("locations.csv").readEnterpriseLocation("Enterprise");
		List<SetPartition> setp = point.stream().map(x -> new SetPartition(x.y , x.x)).collect(Collectors.toCollection(ArrayList::new));
		
		System.out.println("---------------------------- Enterprise Validation --------------------------");
		System.out.println("Enterprise Size: " + setp.size());
		pic.displayOptimization("Optimization", ref, setp);
		dmv = new DistanceMetricValidator(trajectories, setp.stream().map(x -> new Point((x.minLat + x.maxLat)/2, (x.minLon + x.maxLon)/2)).collect(Collectors.toCollection(ArrayList::new)));
		System.out.println("Num Valid Users: " + dmv.sortValidation(.005).size());
		System.out.println("Num Trajectories Begin: " + dmv.startPoints.size());
		System.out.println("Num Trajectories End: " + dmv.endPoints.size());
		
	}

	private static void expandCluster(Queue<Trajectory> queue, int clusterId, double eps, int minPts) {
		int csize = 0;

		Map<Integer, Boolean> queueSet = new HashMap<>();

		while (queue.size() > 0) {
			Trajectory t = queue.poll();
			queueSet.put(t.getRowID(), true);

			if (!vMap.containsKey(t.getRowID())) {

				if (!t.isVisited()) {
					t.setVisited(true);
					visitedList.add(t);
					vMap.put(t.getRowID(), true);

					Queue<Trajectory> neighbors = getNeighbors(t, eps, minPts);
					if (neighbors.size() > minPts) {
						while (neighbors.size() > 0) {
							Trajectory nt = neighbors.poll();
							if (!queueSet.containsKey(nt.getRowID())) {
								queue.add(nt);
								queueSet.put(nt.getRowID(), true);
							}
						}
					}
				}
			}
			if (t.getCluster() == 0 || t.getCluster() == -1) {
				// System.out.println("Cluster Size: " + csize + " Queue Size: "
				// + queue.size());
				csize++;
				t.setCluster(clusterId);
			}
		}
		if (csize == 1) {
			System.out.println("SIZE 1 CLUSTER!!!");
		}
	}

	private static Queue<Trajectory> getNeighbors(Trajectory t, Double eps, int minPts) {

		LinkedList<Trajectory> nlist = db.retrieveRows(t.getPickUpLatitude() - 0.0025, t.getPickUpLatitude() + 0.0025,
				t.getPickUpLongitude() - 0.0025, t.getPickUpLongitude() + 0.0025);

		if (nlist.size() < minPts) {
			return new LinkedList<Trajectory>();
		} else {
			for (int i = nlist.size(); --i >= 0;) {
				Trajectory x = nlist.get(i);

				double dTheta = calcDTheta(t, x);
				double dPerp = calcDPerp(t, x);
				double dPara = calcDPara(t, x);
				double dist = dTheta * thetaW + dPerp * perpW + dPara * paraW;
				if (printDist) {
					// System.out.println("Dist: " + dist + " :: " + dTheta +
					// "," + dPerp + "," + dPara);
					System.out.println("Dist: " + dist);
				}
				if (dist >= eps) {
					nlist.remove(x);
				}
			}

			return nlist;
		}
	}

	private static double calcPairWiseDist(Trajectory t1, Trajectory t2, int numPoints) {

		double t1xdisplace = (t1.getDropOffLongitude() - t1.getPickUpLongitude());
		double t1ydisplace = (t1.getDropOffLatitude() - t1.getPickUpLatitude());
		double t2xdisplace = (t2.getDropOffLongitude() - t2.getPickUpLongitude());
		double t2ydisplace = (t2.getDropOffLatitude() - t2.getPickUpLatitude());

		List<Point> t1points = new LinkedList<>();
		List<Point> t2points = new LinkedList<>();
		for (int x = 0; x < numPoints; x++) {
			t1points.add(new Point((t1xdisplace / numPoints) * x + t1.getPickUpLatitude(),
					(t1ydisplace / numPoints) * x + t1.getPickUpLongitude()));
			t2points.add(new Point((t2xdisplace / numPoints) * x + t2.getPickUpLatitude(),
					(t2ydisplace / numPoints) * x + t2.getPickUpLongitude()));
		}

		double dist = 0;
		for (int x = 0; x < t1points.size(); x++) {
			dist += Math.acos(Math.sin(t1points.get(x).x) * Math.sin(t2points.get(x).x) + Math.cos(t1points.get(x).x)
					* Math.cos(t2points.get(x).x) * Math.cos(t2points.get(x).y - t1points.get(x).y));
		}

		return dist;
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

		// System.out.println("Projections: " + startProj.toString() + ", " +
		// endProj.toString());

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
