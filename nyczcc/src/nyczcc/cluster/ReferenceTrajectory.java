package nyczcc.cluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import nyczcc.Trajectory;

public class ReferenceTrajectory {

	private ArrayList<Trajectory> cluster;
	
	private double refPickUpLat;
	private double refPickUpLong;
	private double refDropOffLat;
	private double refDropOffLong;
	
	public ReferenceTrajectory(ArrayList<Trajectory> c)
	{
		cluster = c;
		
		
	}
	
	public void findMinDistance(ArrayList<Double> x, ArrayList<Double> y, DistanceFun d)
	{
		double mx = x.stream().max(new Comparator<Double>(){
						@Override
						public int compare(Double o1, Double o2) {
							double d = o1 - o2;
							return d > 0 ? 1 : d < 0 ? -1 : 0;
						}
					}).get();
	
		double mavgx = x.stream().reduce(0.0, Double::sum) / x.size();
		
		List<Double> tX = new ArrayList(x);
		tX.stream().map(p -> (p - mavgx) / mavgx);
		
		
		double mavgy = x.stream().reduce(0.0, Double::sum) / x.size();
		
		List<Double> tY = new ArrayList(y);
		tY.stream().map(p -> (p - mavgy) / mavgy);
		
		
	}
	
	private interface DistanceFun{
		public double getDistance(Double x, Double y);
	}
}
