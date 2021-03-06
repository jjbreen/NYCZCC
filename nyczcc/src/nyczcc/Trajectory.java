package nyczcc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Trajectory {
	private LocalDateTime pickupt;
	private LocalDateTime dropofft;
	private double plat;
	private double plong;
	private double dlat;
	private double dlong;
	private int id;
	private int clusterid;
	private boolean visited;
	
	private static int cid = 0;

	//LocalDateTime Formatter. Use this to parse from and to the database
	DateTimeFormatter formatter;
	
	public Trajectory(String pickupt, String dropofft, double plat, double plong, double dlat, double dlong, int clusterid, int visited)
	{
		id = cid;
		cid++;
		
		pickupt = pickupt.replace("T", " ");
		dropofft = dropofft.replace("T", " ");

		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		this.pickupt = LocalDateTime.parse(pickupt, formatter);
		this.dropofft = LocalDateTime.parse(dropofft, formatter);

		this.plat = plat;
		this.plong = plong;
		this.dlat = dlat;
		this.dlong = dlong;
		this.clusterid = clusterid;
		this.visited = visited == 0 ? false : true;
	}

	public Trajectory(int rowid, String pickupt, String dropofft, double plat, double plong, double dlat, double dlong, int clusterid, int visited)
	{
		id = rowid;
		
		pickupt = pickupt.replace("T", " ");
		dropofft = dropofft.replace("T", " ");
		
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		this.pickupt = LocalDateTime.parse(pickupt, formatter);
		this.dropofft = LocalDateTime.parse(dropofft, formatter);

		this.plat = plat;
		this.plong = plong;
		this.dlat = dlat;
		this.dlong = dlong;
		this.clusterid = clusterid;
		this.visited = visited == 0 ? false : true;
	}
	
	public Trajectory(double plat, double plong, double dlat, double dlong, int cluster)
	{
		pickupt = LocalDateTime.now();
		dropofft = LocalDateTime.now();
		
		formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
		
		this.plat = plat;
		this.plong = plong;
		this.dlat = dlat;
		this.dlong = dlong;
		this.clusterid = cluster;
		
		visited = false;
	}

	public LocalDateTime getPickUpTime() {
		return pickupt;
	}

	public LocalDateTime getDropOffTime() {
		return dropofft;
	}
	
	public String getPickUpTimeString(){
		return pickupt.format(formatter);
	}
	
	public String getDropOffTimeString(){
		return dropofft.format(formatter);
	}
	
	public double getPickUpLongitude(){
		return plong;
	}

	public double getPickUpLatitude() {
		return plat;
	}

	public double getDropOffLatitude() {
		return dlat;
	}

	public double getDropOffLongitude() {
		return dlong;
	}

	public int getRowID() {
		return id;
	}

	public String getDatabaseTypes() {
		return "(pickupt,dropofft,plat,plong,dlat,dlong,clusterid,visited)";
	}
	
	public static String getCSVHeader()
	{
		return "pickupt,dropofft,plat,plong,dlat,dlong,clusterid,visited";
	}
	
	public String getCSVRow()
	{
		StringBuilder bob = new StringBuilder();
		bob.append(this.getPickUpTimeString());
		bob.append(",");
		bob.append(this.getDropOffTimeString());
		bob.append(",");
		bob.append(getPickUpLatitude());
		bob.append(",");
		bob.append(getPickUpLongitude());
		bob.append(",");
		bob.append(getDropOffLatitude());
		bob.append(",");
		bob.append(getDropOffLongitude());
		bob.append(",");
		bob.append(getCluster());
		bob.append(",");
		bob.append(isVisited());
		bob.append("\n");
		
		return bob.toString();
	}

	public String getDatabaseValues() {
		return "(" + pickupt.toString() + "," + dropofft.toString() + "," + plat + "," + plong + "," + dlat + "," + dlong + "," + clusterid + "," + visited + ")";
	}

	public ArrayList<String> getDatabaseValueList() {
		ArrayList<String> dblist = new ArrayList<>();
		dblist.add(this.getPickUpTimeString());
		dblist.add(this.getDropOffTimeString());
		dblist.add(String.valueOf(plat));
		dblist.add(String.valueOf(plong));
		dblist.add(String.valueOf(dlat));
		dblist.add(String.valueOf(dlong));
		dblist.add(String.valueOf(clusterid));
		dblist.add(String.valueOf(visited));

		return dblist;
	}
	
	public static String DBNameValuePairBatch()
	{
		return "pickupt=?,dropofft=?,plat=?,plong=?,dlat=?,dlong=?,clusterid=?,visited=?";
	}
	
	public String DBNameValuePair()
	{
		return "pickupt='" + this.getPickUpTimeString() +"',dropofft='" + this.getDropOffTimeString() + "',plat='" + plat +"',plong='" + plong +
				"',dlat='"+dlat+"',dlong='" + dlong + "',clusterid='" + clusterid + "', visited=" + this.intVisited();
	}

	public String toString() {
		return "Trajectory: " + id + " Cluster: " + clusterid + " Visited: " + visited + " - Pick Up Time: " + pickupt.toString() + " Drop Off Time: " + dropofft.toString()
				+ "\n\tPick Up Latitude: " + plat +
				" Pick Up Longitude: " + plong + "\n\tDrop Off Latitude: " + dlat + " Drop Off Longitude: " + dlong;
	}

	public boolean isVisited() {
		return visited;
	}
	
	public int intVisited(){
		return visited ? 1 : 0;
	}

	public void setVisited(boolean b) {
		visited = b;
	}

	public void setCluster(int cluster) {
		clusterid = cluster;

	}

	public int getCluster() {
		return clusterid;
	}

	public double getLength() {
//		return Math.sqrt(
//				Math.pow(plat - dlat, 2) +
//						Math.pow(plong - dlong, 2)) * 1000;
		return Math.acos(Math.sin(plat) * Math.sin(dlat) + Math.cos(plat) * Math.cos(dlat) * Math.cos(dlong - plong));
	}
	
	public void setRowID(int r)
	{
		id = r;
	}

}
