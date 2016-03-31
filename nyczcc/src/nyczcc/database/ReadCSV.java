package nyczcc.database;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ReadCSV {
	private String csvLocation;
	
	public ReadCSV(String loc)
	{
		csvLocation = loc;
	}
	
	public void importCSVtoDB(SQLiteDBC c) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(csvLocation));
		String line;
		boolean first = true;
		ArrayList<Integer> valmap = new ArrayList<>();
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
		        	 System.out.println("Inserting Values: " + datmap);
		        	 c.insertValues(datmap);
		         }
		}
		br.close();

	}
}
