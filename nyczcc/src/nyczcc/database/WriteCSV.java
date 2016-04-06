package nyczcc.database;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import nyczcc.Trajectory;

public class WriteCSV {
	
	private String csvloc;
	
	public WriteCSV(String c)
	{
		csvloc = c;
	}
	
	public void writeCSV(List<Trajectory> t)
	{
        try {
			FileWriter f = new FileWriter(csvloc);
			
			f.append(Trajectory.getCSVHeader());
			f.append("\n");
			
			for (Trajectory tr : t)
			{
				f.append(tr.getCSVRow());
			}
			
			f.flush();
			f.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        System.out.println("Finished Writing Trajectories To: " + csvloc);

	}

}
