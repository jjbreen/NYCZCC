package nyczcc;

import java.time.LocalDateTime;

public class Trajectory {
	private LocalDateTime pickupt;
	private LocalDateTime dropofft;
	private double plat;
	private double plong;
	private double dlat;
	private double dlong;
	
	public Trajectory(String pickupt, String dropofft, double plat, double plong, double dlat, double dlong)
	{
		
	}
	
	public LocalDateTime getPickUpTime(){
		return pickupt;
	}
	
	public LocalDateTime getDropOffTime(){
		return dropofft;
	}
	
	public double getPickUpLongitude(){
		return plong;
	}
	
	public double getPickUpLatitude(){
		return plat;
	}
	
	public double getDropOffLatitude(){
		return dlat;
	}
	
	public double getDropOffLongitude(){
		return dlong;
	}
	

}
