package nyczcc;

import java.util.List;

public class Cluster {
	
	public List<Trajectory> tlist;
	public Trajectory reference;
	
	public Cluster(List<Trajectory> t,Trajectory r){
		tlist = t;
		reference = r;
	}

}
