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

	public Trajectory(String pickupt, String dropofft, double plat, double plong, double dlat, double dlong, int clusterid, boolean visited) {
		id = -1;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		this.pickupt = LocalDateTime.parse(pickupt, formatter);
		this.dropofft = LocalDateTime.parse(dropofft, formatter);

		this.plat = plat;
		this.plong = plong;
		this.dlat = dlat;
		this.dlong = dlong;
		this.clusterid = clusterid;
		this.visited = visited;
	}

	public Trajectory(int rowid, String pickupt, String dropofft, double plat, double plong, double dlat, double dlong, int clusterid, boolean visited) {
		id = rowid;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		this.pickupt = LocalDateTime.parse(pickupt, formatter);
		this.dropofft = LocalDateTime.parse(dropofft, formatter);

		this.plat = plat;
		this.plong = plong;
		this.dlat = dlat;
		this.dlong = dlong;
		this.clusterid = clusterid;
		this.visited = visited;
	}

	public LocalDateTime getPickUpTime() {
		return pickupt;
	}

	public LocalDateTime getDropOffTime() {
		return dropofft;
	}

	public double getPickUpLongitude() {
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

	public String getDatabaseValues() {
		return "(" + pickupt.toString() + "," + dropofft.toString() + "," + plat + "," + plong + "," + dlat + "," + dlong + "," + clusterid + "," + visited + ")";
	}

	public ArrayList<String> getDatabaseValueList() {
		ArrayList<String> dblist = new ArrayList<>();
		dblist.add(pickupt.toString());
		dblist.add(dropofft.toString());
		dblist.add(plat + "");
		dblist.add(plong + "");
		dblist.add(dlat + "");
		dblist.add(dlong + "");
		dblist.add(clusterid + "");
		dblist.add(visited + "");

		return dblist;
	}

	public String DBNameValuePair() {
		return "pickupt=" + pickupt.toString() + ",dropofft=" + dropofft.toString() + ",plat=" + plat + ",plong=" + plong +
				",dlat=" + dlat + ",dlong=" + dlong + ",clusterid=" + clusterid + ",visited=" + visited;
	}

	public String toString() {
		return "Trajectory: " + id + " Cluster: " + clusterid + " Visited: " + visited + " - Pick Up Time: " + pickupt.toString() + " Drop Off Time: " + dropofft.toString()
				+ "\n\tPick Up Latitude: " + plat +
				" Pick Up Longitude: " + plong + "\n\tDrop Off Latitude: " + dlat + " Drop Off Longitude: " + dlong;
	}

	public boolean isVisited() {
		return visited;
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
		return Math.sqrt(
				Math.pow(plat - dlat, 2) +
						Math.pow(plong - dlong, 2));
	}

}
