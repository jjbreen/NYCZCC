package nyczcc.compute;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nyczcc.Point;
import nyczcc.Trajectory;
import nyczcc.database.ReadCSV;
import nyczcc.database.SQLiteDBC;

public class ComputeDistanceMatrix {
	
	private static List<Trajectory> trajectories;
	private static ConcurrentLinkedQueue<Trajectory> tq = new ConcurrentLinkedQueue<>();
	private static List<List<Double>> matrix;
	private static Object matrixLock = new Object();

	public static void main(String [] args)
	{
		SQLiteDBC db = new SQLiteDBC();
		db.createTable();
		ReadCSV reader = new ReadCSV("afternoon_2015-1-10.csv");
		try {
			reader.importCSVtoDB(db);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		trajectories = db.retrieveRows(0, Integer.MAX_VALUE);
		
		matrix = new LinkedList<>();
		for (int x = 0; x < trajectories.size(); x++){
			matrix.add(new LinkedList<Double>());
		}
		
		tq.addAll(trajectories);
		
		ExecutorService executor = Executors.newFixedThreadPool(25);
		for (int i = 0; i < 25; i++){
			Runnable worker = new ComputeThread();
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()){
			
		}
		
		try {
			FileWriter f = new FileWriter("DataMatrix.csv");
			
			for (List<Double> row : matrix)
			{
				for (Double d : row){
					f.append(String.valueOf(d));
					f.append(",");
				}
				f.append("\n");
			}
			
			f.flush();
			f.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("Finished Writing Trajectories To: " + "DataMatrix.csv");
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
	
	private static class ComputeThread implements Runnable{

		@Override
		public void run() {
			while (true){
				Trajectory t = tq.poll();
				if (t == null){
					return;
				}
				List<Double> mrow = new LinkedList<>();
				for (int x = 0; x < trajectories.size(); x++){
					mrow.add(0.0);
				}
				for (Trajectory tra : trajectories){
					double dTheta = calcDTheta(t, tra);
					double dPerp = calcDPerp(t, tra);
					double dPara = calcDPara(t, tra);
					double dist = dTheta + dPerp + dPara;
					
					mrow.add(tra.getRowID()-1, dist);
				}
				synchronized(matrixLock){
					matrix.add(t.getRowID()-1, mrow);
				}
			}
		}
		
	}
}
