package nyczcc.optimization;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nyczcc.Cluster;
import nyczcc.Trajectory;

public class SetCovering {
	
	Set<Cluster> clusters;
	
	double minLat, maxLat, minLon, maxLon;
	
	double minArea;
	
	SetPartition root;
	
	public SetCovering(Set<Cluster> clusters, double minLat, double maxLat, double minLon, double maxLon, double minArea){
		
		this.clusters = clusters;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minArea = minArea;
		
		root = new SetPartition(this.minLat, this.maxLat, this.minLon, this.maxLon, this.minArea);
		assignWeights();
	}
	
	public List<SetPartition> getOptimal(int budget, double radius){
		return optimizeLocation(getSetMap(radius), budget);
	}
	
	public void assignWeights()
	{
		for (Cluster c : clusters){
			Trajectory ref = c.reference;
			SetPartition partPickup = root.findNode(ref.getPickUpLatitude(), ref.getPickUpLongitude());
			
			if (partPickup == null){
				continue;
			}
			
			System.out.println(c.tlist.size());
			
			partPickup.setWeight(partPickup.getWeight() + c.tlist.size());
			
			SetPartition partDropoff = root.findNode(ref.getDropOffLatitude(), ref.getDropOffLongitude());
			
			if (partDropoff == null){
				continue;
			}
			
			partDropoff.setWeight(partDropoff.getWeight() + c.tlist.size());
		}
	}
	
	public Map<SetPartition, Set<SetPartition>> getSetMap(double setRadius)
	{
		Map<SetPartition, Set<SetPartition>> smap = new HashMap<>();
		
		List<SetPartition> leaves = root.retrieveLeaves(this.minLat, this.maxLat, this.minLon, this.maxLon);
		
		for (SetPartition l : leaves){
			Set<SetPartition> hset = new HashSet<>();
	
			double midlat = (l.minLat + l.maxLat) / 2;
			double midlon = (l.minLon + l.maxLon) / 2;
			
			double nminLat = midlat - setRadius;
			double nmaxLat = midlat + setRadius;
			double nminLon = midlon - setRadius;
			double nmaxLon = midlon + setRadius;
			
			hset.add(l);
			hset.addAll(root.retrieveLeaves(nminLat, nmaxLat, nminLon, nmaxLon));
		
			smap.put(l , hset);
		}
		
		return smap;
	}
	
	public List<SetPartition> optimizeLocation(Map<SetPartition, Set<SetPartition>> hset, int budget){
		List<SetPartition> blist = new LinkedList<>();
		
		for (int y =0; y< budget; y++){
			Map<SetPartition, Double> valmap = new HashMap<>();
			List<SetPartition> pset = new LinkedList<SetPartition>();
			
			for (SetPartition s : hset.keySet()){
				double w = hset.get(s).stream().map(x -> x.getWeight()).reduce(0.0, (a, b) -> a + b);
				valmap.put(s, w);
				pset.add(s);
			}
			
			
			pset.sort(new Comparator<SetPartition>(){
				@Override
				public int compare(SetPartition o1, SetPartition o2) {
					if (valmap.get(o1) - valmap.get(o2) == 0)
					{
						return 0;
					}
					// TODO Auto-generated method stub
					return valmap.get(o1) - valmap.get(o2) > 0 ? -1 : 1;
				}
			});
		
			blist.add(pset.get(0));
			
			Set<SetPartition> values = hset.get(pset.get(0));
			hset.remove(pset.get(0));
			
			for (SetPartition s : hset.keySet()){
				
				Set<SetPartition> sset = hset.get(s);
				
				for (SetPartition m : values){
					if (sset.contains(m)){
						sset.remove(m);
					}
				}
			}
			
			
		}
		return blist;
	}
	
	
	public static class SetPartition{
		
		private double weight;
		private int setNum;
		private static int numSets = 0;
		
		private SetPartition left, right;
		
		public double minLat, maxLat, minLon, maxLon;
		
		private boolean alt;
		
		public SetPartition(double minLat, double maxLat, double minLon, double maxLon, double minArea)
		{
			alt = true;
			this.minLat = minLat;
			this.maxLat = maxLat;
			this.minLon = minLon;
			this.maxLon = maxLon;
			
			double length = maxLat - minLat;
			double width = maxLon - minLon;
			
			setNum = numSets;
			numSets++;
			
			weight = length * width;
			
			if (length * width > minArea){
				if (alt){
					left = new SetPartition(minLat, (minLat + maxLat) / 2, minLon, maxLon, minArea, false);
					right = new SetPartition((minLat + maxLat) / 2, maxLat, minLon, maxLon, minArea, false);
				}
				else{
					left = new SetPartition(minLat, maxLat, minLon, (minLon + maxLon) / 2, minArea);
					right = new SetPartition(minLat, maxLat, (minLon + maxLon) / 2, maxLon, minArea);
				}
			}
			else{
				left = null;
				right = null;
			}
			
		}
		
		public SetPartition(double minLat, double maxLat, double minLon, double maxLon, double minArea, boolean a)
		{
			alt = a;
			this.minLat = minLat;
			this.maxLat = maxLat;
			this.minLon = minLon;
			this.maxLon = maxLon;
			
			double length = maxLat - minLat;
			double width = maxLon - minLon;
			
			setNum = numSets;
			numSets++;
			
			weight = length * width;
			
			if (length * width > minArea){
				if (alt){
					left = new SetPartition(minLat, (minLat + maxLat) / 2, minLon, maxLon, minArea);
					right = new SetPartition((minLat + maxLat) / 2, maxLat, minLon, maxLon, minArea);
				}
				else{
					left = new SetPartition(minLat, maxLat, minLon, (minLon + maxLon) / 2, minArea);
					right = new SetPartition(minLat, maxLat, (minLon + maxLon) / 2, maxLon, minArea);
				}
			}
			else{
				left = null;
				right = null;
			}
			
		}
		
		public void initializeSetPartition(){
			
		}
		
		public String toString(){
			return "Weight: " + weight + " setNum: " + setNum + " - Coordinates: " + minLat + ", " + maxLat + ", " + minLon + ", " + maxLon;
		}
		
		public void setLeft(SetPartition left)
		{
			this.left = left;
		}
		
		public void setRight(SetPartition right)
		{
			this.right = right;
		}
		
		public SetPartition getLeft()
		{
			return left;
		}
		
		public SetPartition getRight()
		{
			return right;
		}
		
		public double calculateWeight()
		{
			if (left == null && right == null){
				return weight;
			}
			else{
				return left.calculateWeight() + right.calculateWeight();
			}
		}
		
		public void setWeight(double w){
			
			System.out.println("YAYYY");
			
			weight = w;
		}
		
		public double getWeight(){
			return weight;
		}
		
		public SetPartition findNode(double lat, double lon){
			
			if (left == null && right == null){
				if (minLat <= lat && maxLat >= lat && minLon <= lon && maxLon >= lon){
					
					//System.out.println("FOUND");
					
					return this;
				}else{
					
					//System.out.println("NOT FOUND");
					
					return null;
				}
			}
			
			double halfLat = (minLat + maxLat) / 2;
			double halfLon = (minLon + maxLon) / 2;
			
			//System.out.println("Half Lat: " + halfLat + " HALF LON: " + halfLon + " LAT: " + lat + " LON: " + lon);
			
			if ((lat <= halfLat && alt) || (lon <= halfLon && (!alt))){
				return left.findNode(lat, lon);
			}else{
				return right.findNode(lat, lon);
			}
		}
		
		public List<SetPartition> retrieveLeaves(double minLat, double maxLat, double minLon, double maxLon){
			List<SetPartition> leaves = new LinkedList<>();
			
			if (left == null && right == null){
				
				if (containsCoord(minLat, maxLat, minLon, maxLon, this.minLat, this.minLon) ||
					containsCoord(minLat, maxLat, minLon, maxLon, this.maxLat, this.minLon) ||
					containsCoord(minLat, maxLat, minLon, maxLon, this.minLat, this.maxLon) ||
					containsCoord(minLat, maxLat, minLon, maxLon, this.maxLat, this.maxLon)){
					
					
					leaves.add(this);
				}
				return leaves;
			}
			
			leaves.addAll(left.retrieveLeaves(minLat, maxLat, minLon, maxLon));
			leaves.addAll(right.retrieveLeaves(minLat, maxLat, minLon, maxLon));
			
			return leaves;
		}
		
		public static String getCSVHeader(){
			return "minLat,maxLat,minLon,maxLon,X,Y,ID";
		}
		
		public String writeRow(){
			return minLat +"," + maxLat + "," + minLon +"," + maxLon +"," + (minLat + maxLat)/2 + ","+ (minLon + maxLon)/2 +","+setNum +"\n";
		}
		
		private boolean containsCoord(double minLat, double maxLat, double minLon, double maxLon, double lat, double lon){
			return minLat <= lat && maxLat >= lat && minLon <= lon && maxLon >= lon;
		}
		
	}

}
