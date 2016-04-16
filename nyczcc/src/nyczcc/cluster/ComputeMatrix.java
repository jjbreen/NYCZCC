package nyczcc.cluster;

import java.util.LinkedList;
import java.util.List;

import nyczcc.Trajectory;
import nyczcc.database.SQLiteDBC;

public class ComputeMatrix {

	public static void main(String[] args) {
		SQLiteDBC db = new SQLiteDBC();
		db.connect();
		List<Trajectory> trajectories = db.retrieveRows(0, Integer.MAX_VALUE);
		int size = trajectories.size();
		
		List<List<Double>> matrix = new LinkedList<List<Double>>();
		
		for(int i = 0; i < size; i++){
			List<Double> temp = new LinkedList<Double>();
			for(int j = 0; j < size; j++){
				temp.add(0.0);
			}
			matrix.add(temp);
			if(i%1000 == 0){
				System.out.println(i);
			}
		}
	}
}
