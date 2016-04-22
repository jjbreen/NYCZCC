package nyczcc.cluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

public class ValidateTesting {
	
	public static DisplayPicture pic = new DisplayPicture();
	
	
	/* -------------------------- Change These Parameters -------------------------*/
	public static double AREA_PARTITION_SIZE = 0.00001;
	public static int NUMBER_HUBS = 33;
	public static double COVER_RADIUS = 0.001;

	// This is how far people are willing to walk to get to this Station (0.005 is around .5 miles)
	public static double WALK_DISTANCE = 0.005;
	
	public static void main(String [] args)
	{
		ReadCSV reader = new ReadCSV("afternoon_reftrajectories.csv");
		ReadCSV reader2 = new ReadCSV("morning_reftrajectories.csv");
		
		ReadCSV reader3 = new ReadCSV("afternoon_trajectories.csv");
		ReadCSV reader4 = new ReadCSV("morning_trajectories.csv");
	
		List<Trajectory> ref = reader.readTrajectoryCSVFile(0);
		
		ref.addAll(reader2.readTrajectoryCSVFile(1000));
		List<Trajectory> trajectories = reader3.readTrajectoryCSVFile(0);
		trajectories.addAll(reader4.readTrajectoryCSVFile(1000));
		
		
		pic.displayPicture("Reference Trajectory Plot", ref);
		
		
		Set<Cluster> clist = new HashSet<Cluster>();
		for (Trajectory r : ref){
			clist.add(new Cluster(trajectories.stream().filter(x -> x.getCluster() == r.getCluster()).collect(Collectors.toCollection(ArrayList::new)), r));
		}
		
		SetCovering s = new SetCovering(clist, 40.64, 40.83, -74.02, -73.78, AREA_PARTITION_SIZE);
		
		List<SetPartition> opt = s.getOptimal(NUMBER_HUBS, COVER_RADIUS);
		
		new WriteCSV("ours_setpartition.csv").writeSetCSV(opt);
		
		pic.displayOptimization("Our Optimization", ref, opt);
		
		List<Point> point = new ReadCSV("locations.csv").readEnterpriseLocation();
		List<SetPartition> setp = point.stream().map(x -> new SetPartition(x.y , x.x)).collect(Collectors.toCollection(ArrayList::new));
		
		pic.displayOptimization("Their Optimization", ref, setp);
		
		
		System.out.println("---------------------------- Self Validation ------------------------------");
		DistanceMetricValidator dmv = new DistanceMetricValidator(trajectories, opt.stream().map(x -> new Point((x.minLat + x.maxLat)/2, (x.minLon + x.maxLon)/2)).collect(Collectors.toCollection(ArrayList::new)));
		System.out.println("Num Valid Users: " + dmv.sortValidation(WALK_DISTANCE).size());
		System.out.println("Num Trajectories Begin: " + dmv.startPoints.size());
		System.out.println("Num Trajectories End: " + dmv.endPoints.size());
		
		
		System.out.println("---------------------------- Enterprise Validation --------------------------");
		System.out.println("Enterprise Size: " + setp.size());
		dmv = new DistanceMetricValidator(trajectories, setp.stream().map(x -> new Point((x.minLat + x.maxLat)/2, (x.minLon + x.maxLon)/2)).collect(Collectors.toCollection(ArrayList::new)));
		System.out.println("Num Valid Users: " + dmv.sortValidation(WALK_DISTANCE).size());
		System.out.println("Num Trajectories Begin: " + dmv.startPoints.size());
		System.out.println("Num Trajectories End: " + dmv.endPoints.size());
		
		new WriteCSV("theirs_setpartition.csv").writeSetCSV(setp);
	}
	
}
