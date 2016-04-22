package nyczcc.validation;

import java.util.LinkedList;
import java.util.List;

import nyczcc.Point;
import nyczcc.Trajectory;

public class DistanceMetricValidator {
	
	public List<Trajectory> validPoints;
	public List<Trajectory> startPoints;
	public List<Trajectory> endPoints;
	
	private List<Trajectory> tlist;
	private List<Point> locations;
	
	public DistanceMetricValidator(List<Trajectory> t, List<Point> l){
		validPoints = new LinkedList<>();
		startPoints = new LinkedList<>();
		endPoints = new LinkedList<>();
		tlist = t;
		locations = l;
	}
	
	public List<Trajectory> sortValidation(double minDist){
		for (Trajectory t : tlist){
			boolean validp = false;
			boolean validd = false;
			
			Point pickup = new Point(t.getPickUpLatitude(), t.getPickUpLongitude());
			Point dropoff = new Point(t.getDropOffLatitude(), t.getDropOffLongitude());
			
			for (Point p : locations){
				if (getDistance(pickup, p) <= minDist){
					if (!validp){
						startPoints.add(t);
					}
					validp = true;
				}
				if (getDistance(dropoff, p) <= minDist){
					if (!validd){
						endPoints.add(t);
					}
					validd = true;
				}
				if (validd && validp){
					validPoints.add(t);
					break;
				}
			}
			
		}
		
		return validPoints;
	}
	
	private double getDistance(Point p1, Point p2){
		return Math.acos(Math.sin(p1.x) * Math.sin(p2.x) + Math.cos(p1.x) * Math.cos(p2.x) * Math.cos(p2.y - p1.y));
	}

}
