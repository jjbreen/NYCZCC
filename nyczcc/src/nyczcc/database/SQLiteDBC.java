package nyczcc.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import nyczcc.Trajectory;

public class SQLiteDBC {
	private Connection c;
	private Statement stmt;

	public void connect(){
		c = null;
		stmt = null;
	    try {
	      Class.forName("org.sqlite.JDBC");
	      c = DriverManager.getConnection("jdbc:sqlite:test.db");
	      //System.out.println("Opened database successfully");

	      stmt = c.createStatement();
	    } catch ( Exception e ) {
	      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
	      System.exit(0);
	    }
	}
	
	public void close(){
		try {
			stmt.close();
			c.close();
			//System.out.println("closed database successfully");
		} catch (Exception e ){
			System.out.println("Failed to close DB Connection!");
		}
	}
	
	public void createTable(){
		if (!tableExists())
		{
			this.connect();
			try{
				String sql = "CREATE TABLE " + TrajectorySchema.getTableName() +
						" (" + TrajectorySchema.getSchema() + ")";
				stmt.executeUpdate(sql);
				
				System.out.println("created table successfully");
			
			} catch (Exception e ){
				System.out.println("Failed to Create Table!");
			}

			this.close();
		}
		else{
			System.out.println("table already exists!");
		}
	}
	
	public boolean tableExists()
	{
		boolean exists = false;
		this.connect();
		try{
			String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + TrajectorySchema.getTableName() + "'";
			ResultSet r = stmt.executeQuery(sql);
			while (r.next()){
				exists = true;
				break;
			}
		} catch (Exception e ){
			System.out.println("Failed to check if table exists");
		}
		this.close();
		return exists;
	}
	
	public void insertValues(Trajectory t)
	{
		
	}
	
	public void insertValues(List<String> a)
	{
		this.connect();
		try {
			StringBuilder bob = new StringBuilder();
			bob.append("INSERT INTO ");
			bob.append(TrajectorySchema.getTableName());
			bob.append(" ");
			bob.append(TrajectorySchema.getInsertDBNames());
			bob.append(" VALUES('");
			for (int x = 0; x < a.size(); x++)
			{
				bob.append(a.get(x));
				if (x != a.size() -1)
				{
					bob.append("','");
				}
			}
			bob.append("')");
			
			stmt.executeUpdate(bob.toString());
		}catch(Exception e)
		{
			System.out.println("Failed to insert into table");
		}
		this.close();
	}
	
	public void updateTrajectoryBatch(List<Trajectory> tlist){
		this.connect();
		StringBuilder bob = new StringBuilder();
		bob.append("UPDATE ");
		bob.append(TrajectorySchema.getTableName());
		bob.append(" SET ");
		bob.append(Trajectory.DBNameValuePairBatch());
		bob.append(" WHERE ROWID=");
		bob.append("?");
		
		System.out.println(bob.toString());
		
		List<Trajectory> failedVal = new LinkedList<>();
		
		try{
			c.setAutoCommit(false);
			PreparedStatement pstmt = c.prepareStatement(bob.toString());
		
			
			for (int x = 0; x < tlist.size(); x++)
			{

					if (tlist.get(x).getRowID() == -1){
						failedVal.add(tlist.get(x));
						continue;
					}

					int y = 1;
					for (String s : tlist.get(x).getDatabaseValueList()){
						pstmt.setString(y, s);
						y++;
					}
					pstmt.setString(y, String.valueOf(tlist.get(x).getRowID()));

					
					pstmt.addBatch();
			}
			
			pstmt.executeBatch();
			c.commit();
			
			if (failedVal.size() != 0){
				insertBatchValues(failedVal.stream().map(x -> x.getDatabaseValueList()).collect(Collectors.toCollection(ArrayList::new)));
			}
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		
		this.close();
		System.out.println("Finished Updating Trajectories");
	}
	
	public void insertBatchValues(List<List<String>> dl){
		this.connect();
		try{
			StringBuilder bob = new StringBuilder();
			bob.append("INSERT INTO ");
			bob.append(TrajectorySchema.getTableName());
			bob.append(" ");
			bob.append(TrajectorySchema.getInsertDBNames());
			bob.append(" VALUES(?,?,?,?,?,?,?,?)");

			c.setAutoCommit(false);
			PreparedStatement pstmt = c.prepareStatement(bob.toString());
			
			for (int x = 0; x < dl.size(); x++)
			{
				List<String> a = dl.get(x);
				
				for (int y = 0; y < a.size(); y++)
				{
					pstmt.setString(y+1, a.get(y));
				}
				
				pstmt.addBatch();
				
			}
			pstmt.executeBatch();
			c.commit();
		}
		catch (Exception e)
		{
			System.out.println("Failed to insert into table");
		}
		
		
		this.close();
	}
	
	public void insertValues(HashMap<String, String> dmap)
	{
		this.connect();
		try{
			StringBuilder bob = new StringBuilder();
			bob.append("INSERT INTO ");
			bob.append(TrajectorySchema.getTableName());
			bob.append(" ");
			bob.append(TrajectorySchema.getInsertDBNames());
			bob.append(" VALUES('");
			
			int x = 0;
			for (String col : dmap.keySet())
			{
				bob.append(dmap.get(col));
				if (x != dmap.size() -1)
				{
					bob.append("','");
				}
				x++;
			}
			bob.append("')");
			

			System.out.println(bob.toString());
			
			stmt.executeUpdate(bob.toString());
			System.out.println("Insert Successful!");
		}catch (Exception e)
		{
			System.out.println("Failed to insert into table");
		}
		
		this.close();
	}
	
	public LinkedList<Trajectory> retrieveRows(int from, int to)
	{
		LinkedList<Trajectory> tlist = new LinkedList<>();
		this.connect();
		try{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT rowid, * FROM ");
			sql.append(TrajectorySchema.getTableName());
			sql.append(" WHERE ROWID >= ");
			sql.append(from);
			sql.append(" AND ROWID <= ");
			sql.append(to);
			
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next())
			{
				
				int rowid = rs.getInt("rowid");
				double plat = rs.getDouble("plat");
				double plong = rs.getDouble("plong");
				double dlat = rs.getDouble("dlat");
				double dlong = rs.getDouble("dlong");
				String pickupt = rs.getString("pickupt");
				String dropofft = rs.getString("dropofft");
				int clusterid = rs.getInt("clusterid");
				int visited = rs.getInt("visited");
				
				Trajectory t = new Trajectory(rowid, pickupt, dropofft, plat, plong, dlat, dlong, clusterid, visited); 
				//System.out.println(t);
				tlist.add(t);
			}
			
		}catch (Exception e){
			System.out.println("Failed to retrieve values from database");
			System.out.println(e);
		}
		System.out.println("Finished Retrieving!");
		this.close();
		return tlist;
	}
	
	public LinkedList<Trajectory> retrieveRows(double plati, double plate, double ploni, double plone)
	{
		LinkedList<Trajectory> tlist = new LinkedList<>();
		this.connect();
		try{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT rowid, * FROM ");
			sql.append(TrajectorySchema.getTableName());
			sql.append(" WHERE plat >= ");
			sql.append(plati);
			sql.append(" AND plat <= ");
			sql.append(plate);
			sql.append(" AND plong >= ");
			sql.append(ploni);
			sql.append(" AND plong <= ");
			sql.append(plone);
			
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next())
			{
				
				int rowid = rs.getInt("rowid");
				double plat = rs.getDouble("plat");
				double plong = rs.getDouble("plong");
				double dlat = rs.getDouble("dlat");
				double dlong = rs.getDouble("dlong");
				String pickupt = rs.getString("pickupt");
				String dropofft = rs.getString("dropofft");
				int clusterid = rs.getInt("clusterid");
				int visited = rs.getInt("visited");
				
				Trajectory t = new Trajectory(rowid, pickupt, dropofft, plat, plong, dlat, dlong, clusterid, visited); 
				//System.out.println(t);
				tlist.add(t);
			}
			
		}catch (Exception e){
			System.out.println("Failed to retrieve values from database");
			System.out.println(e);
		}
	//	System.out.println("Finished Retrieving!");
		this.close();
		return tlist;
	}
	
	public void updateTrajectory(List<Trajectory> tlist)
	{
		this.connect();
		for (int x = 0; x < tlist.size(); x++)
		{
			
			try{
				if (tlist.get(x).getRowID() == -1){
					insertValues(tlist.get(x).getDatabaseValueList());
					continue;
				}
				StringBuilder bob = new StringBuilder();
				bob.append("UPDATE ");
				bob.append(TrajectorySchema.getTableName());
				bob.append(" SET ");
				bob.append(tlist.get(x).DBNameValuePair());
				bob.append(" WHERE ROWID=");
				bob.append(tlist.get(x).getRowID());
				
				
				//System.out.println(bob.toString());
				
				stmt.executeUpdate(bob.toString());
				//System.out.println("Succesfully Updated Row: " + tlist.get(x).getRowID());
				
			}catch (Exception e)
			{
				System.out.println("Failed to Update Trajectory: " + tlist.get(x));
				System.out.println(e);
			}
			
			
		}
		this.close();
		System.out.println("Finished Updating Trajectories");
	}
	
	
	public static void main(String [] args)
	{
		SQLiteDBC db = new SQLiteDBC();
		db.createTable();
		ReadCSV reader = new ReadCSV("afternoon_2015-1-10.csv");
		try {
			reader.importCSVtoDB(db);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*ArrayList<Trajectory> t = db.retrieveRows(1,1);
		for (int x = 0; x < t.size(); x++)
		{
			t.get(x).setVisited(true);
		}
		System.out.println(t.get(0));
		
		db.updateTrajectory(t);
		db.retrieveRows(1,1);*/
	}
}
