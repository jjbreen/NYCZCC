package nyczcc.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nyczcc.Point;
import nyczcc.Trajectory;

public class ReadCSV {
	private String csvLocation;
	
	public ReadCSV(String loc)
	{
		csvLocation = loc;
	}
	
	public List<Point> readEnterpriseLocation(String rental){
		List<Point> pt = new LinkedList<>();
		try{
			
			
			BufferedReader br = new BufferedReader(new FileReader(csvLocation));
			String line;
			boolean first = true;
			
			while ( (line=br.readLine()) != null)
			{
				if (first){
					first = false;
					continue;
				}
			    String[] values = line.split(",");
			    
			    if (values[0].toLowerCase().contains(rental.toLowerCase())){
			    	pt.add(new Point(Double.valueOf(values[2]), Double.valueOf(values[1])));
			    }
			}
		
		}catch(IOException e){
			
		}
		
		return pt;
	}
	
	public List<Trajectory> readTrajectoryCSVFile(int addCluster){
		List<Trajectory> pt = new LinkedList<>();
		try{
			
			
			BufferedReader br = new BufferedReader(new FileReader(csvLocation));
			String line;
			boolean first = true;
			
			while ( (line=br.readLine()) != null)
			{
				if (first){
					first = false;
					continue;
				}
			    String[] values = line.split(",");
			    
			    pt.add(new Trajectory(values[0], values[1], Double.valueOf(values[2]), Double.valueOf(values[3]), Double.valueOf(values[4]), Double.valueOf(values[5]), Integer.valueOf(values[6]) + addCluster, Boolean.valueOf(values[7]) ? 1 : 0));
			}
		
		}catch(IOException e){
			
		}
		
		return pt;
	}
	
	public void importCSVtoDB(SQLiteDBC c) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(csvLocation));
		String line;
		boolean first = true;
		ArrayList<Integer> valmap = new ArrayList<>();
		List<List<String>> vmap = new ArrayList<>();
		
		while ( (line=br.readLine()) != null)
		{
		         String[] values = line.split(","); 

		         
		         if (first)
		         {
		        	String [] colnames = TrajectorySchema.getCSVNames();
		        	for (int x = 0; x < colnames.length; x++){
		        		for (int y = 0; y < values.length; y++)
		        		{
		        			if (values[y].compareTo(colnames[x]) == 0){
		        				valmap.add(y);
		        			}
		        		}
		        	}
		        	first = false;
		        	System.out.println("Have Map Cols: " + valmap);
		        
		         }
		         else{
		        	 ArrayList<String> datmap = new ArrayList<>();
		        	 for (int x = 0; x < valmap.size(); x++)
		        	 {
		        		 datmap.add(values[valmap.get(x)]);
		        	 }
		        	 datmap.add("0");
		        	 datmap.add("False");
		        	 //System.out.println("Inserting Values: " + datmap);
		        	 vmap.add(datmap);
		        	 //c.insertValues(datmap);
		         }
		}
		
		c.insertBatchValues(vmap);
		
		br.close();

	}
}
