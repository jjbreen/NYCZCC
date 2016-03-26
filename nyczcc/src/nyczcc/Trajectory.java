package nyczcc;

import java.time.LocalDateTime;

public class Trajectory {
	private LocalDateTime pickupt;
	private LocalDateTime dropofft;
	private double plat;
	private double plong;
	private double dlat;
	private double dlong;
	private int id;
	private static int gid;
	
	
	public Trajectory(String pickupt, String dropofft, double plat, double plong, double dlat, double dlong)
	{
		id = gid;
		gid++;
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
	
	public String getDatabaseTypes(){
		return "(ID,pickupt,dropofft,plat,plong,dlat,dlong)";
	}

	public boolean isVisited() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setVisited(boolean b) {
		// TODO Auto-generated method stub
		
	}

	public void setCluster(String string) {
		// TODO Auto-generated method stub
		
	}

	public String getCluster() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
