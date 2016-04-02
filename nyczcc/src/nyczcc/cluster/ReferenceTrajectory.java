package nyczcc.cluster;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import nyczcc.Point;
import nyczcc.Trajectory;

public class ReferenceTrajectory {

	private List<Trajectory> cluster;
	
	public Trajectory ref;
	
	public ReferenceTrajectory(List<Trajectory> c)
	{
		cluster = c;
		
		Point dropoff = findCenterOfMinDist(c.stream().map(x -> x.getDropOffLatitude()).collect(Collectors.toCollection(ArrayList::new)),
											c.stream().map(x -> x.getDropOffLongitude()).collect(Collectors.toCollection(ArrayList::new)),
											new DistanceFun(){
									
												@Override
												public double getDistance(Double x1, Double y1, Double x2, Double y2) {
													return Math.acos(Math.sin(x1) * Math.sin(x2)) + Math.cos(x1) * Math.cos(x2) + Math.cos(y2 - y1);
												}
										
											});
		
		Point pickup = findCenterOfMinDist(c.stream().map(x -> x.getPickUpLatitude()).collect(Collectors.toCollection(ArrayList::new)),
											c.stream().map(x -> x.getPickUpLongitude()).collect(Collectors.toCollection(ArrayList::new)),
											new DistanceFun(){
									
												@Override
												public double getDistance(Double x1, Double y1, Double x2, Double y2) {
													return Math.acos(Math.sin(x1) * Math.sin(x2) + Math.cos(x1) * Math.cos(x2) * Math.cos(y2 - y1));
												}
										
											});
		
		ref = new Trajectory(pickup.x, pickup.y, dropoff.x, dropoff.y, c.get(0).getCluster());
	}
	
	public double nonLinearSigmoid(double x, boolean deriv)
	{
		if (deriv){
			return x * ( 1 - x);
		}
		else
		{
			return 1 / (1 + Math.exp(-x));
		}
	}
	
	public Point findGeographicalMidPoint(List<Double> x1, List<Double> y1)
	{
		List<Double> xp1 = x1.stream().map(x -> x * Math.PI/180).collect(Collectors.toCollection(ArrayList::new));
		List<Double> yp1 = y1.stream().map(x -> x * Math.PI/180).collect(Collectors.toCollection(ArrayList::new));
		
		List<Double> xcoord1 = new ArrayList<>();
		List<Double> ycoord1 = new ArrayList<>();
		List<Double> zcoord1 = new ArrayList<>();
		for (int x = 0; x < xp1.size(); x++)
		{
			xcoord1.add(Math.cos(xp1.get(x)) * Math.cos(yp1.get(x)));
			ycoord1.add(Math.cos(xp1.get(x)) * Math.sin(yp1.get(x)));
			zcoord1.add(Math.sin(xp1.get(x)));
		}
		
		//TODO: Add in Time Weight Calculation
		
		
		Double avgX = xcoord1.stream().reduce(0.0, Double::sum) / xcoord1.size();
		Double avgY = ycoord1.stream().reduce(0.0, Double::sum) / ycoord1.size();
		Double avgZ = zcoord1.stream().reduce(0.0, Double::sum) / zcoord1.size();
		
		Double lon = Math.atan2(avgY, avgX);
		Double hyp = Math.sqrt(avgX * avgX + avgY * avgY);
		Double lat = Math.atan2(avgZ, hyp);
		
		Double fixedLat = lat * 180 / Math.PI;
		Double fixedLon = lon * 180 / Math.PI;
		
		return new Point(fixedLat, fixedLon);
	}
	
	public Point findCenterOfMinDist(List<Double> x1, List<Double> y1, DistanceFun d)
	{
		Point bestPoint = findGeographicalMidPoint(x1, y1);
		
		if (true)
		{
			System.out.println(bestPoint);
			return bestPoint;
		}
		
		Double distSum = this.getDistFromPointToPlaces(bestPoint, x1, y1, d);
		
		for (int x = 0; x < x1.size(); x++)
		{
			Double nDist = this.getDistFromPointToPlaces(new Point(x1.get(x), y1.get(x)), x1, y1, d);
			if (nDist < distSum)
			{
				distSum = nDist;
				bestPoint = new Point(x1.get(x), y1.get(x));
			}
		}
		
		double td = Math.PI / 2;
		while(td > 0.0000002)
		{
			List<Point> check = getCompassCoordinates(td, bestPoint);
			for (int x = 0; x < check.size(); x++)
			{
				Double nDist = this.getDistFromPointToPlaces(check.get(x), x1, y1, d);
				if (nDist < distSum)
				{
					distSum = nDist;
					bestPoint = check.get(x);
					td = Math.PI;
				}
			}
			td /= 2;
		}
		
		return bestPoint;
	}
	
	private List<Point> getCompassCoordinates(Double td, Point bestPoint)
	{
		Double fdisplace = Math.cos(Math.PI/4) * td;
		
		Point north = new Point(bestPoint.x + td, bestPoint.y);
		Point northeast = new Point(bestPoint.x + fdisplace, bestPoint.y + fdisplace);
		Point east = new Point(bestPoint.x, bestPoint.y + td);
		Point southeast = new Point(bestPoint.x - fdisplace, bestPoint.y + fdisplace);
		Point south = new Point(bestPoint.x - td, bestPoint.y);
		Point southwest = new Point(bestPoint.x - fdisplace, bestPoint.y - fdisplace);
		Point west = new Point(bestPoint.x, bestPoint.y - td);
		Point northwest = new Point(bestPoint.x + fdisplace, bestPoint.y - fdisplace);
		
		List<Point> compass = new ArrayList<>();
		compass.add(north);
		compass.add(northeast);
		compass.add(east);
		compass.add(southeast);
		compass.add(south);
		compass.add(southwest);
		compass.add(west);
		compass.add(northwest);
		
		return compass;
	}
	
	private Double getDistFromPointToPlaces(Point p, List<Double> x1, List<Double> y1, DistanceFun d)
	{
		Double distSum = 0.0;
		for (int x = 0; x < x1.size(); x++)
		{
			distSum += d.getDistance(p.x, p.y, x1.get(x), y1.get(x));
		}
		
		return distSum;
	}
	
	private interface DistanceFun{
		public double getDistance(Double x1, Double y1, Double x2, Double y2);
	}
	
}
