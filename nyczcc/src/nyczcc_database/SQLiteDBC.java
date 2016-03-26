package nyczcc_database;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

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
	      System.out.println("Opened database successfully");

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
			System.out.println("closed database successfully");
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
	
	public void insertValues(ArrayList<String> a)
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
			
			System.out.println(bob.toString());
			
			stmt.executeUpdate(bob.toString());
		}catch(Exception e)
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
	
	public ArrayList<Trajectory> retrieveRows(int from, int to)
	{
		ArrayList<Trajectory> tlist = new ArrayList<>();
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
				
				Trajectory t = new Trajectory(rowid, pickupt, dropofft, plat, plong, dlat, dlong, clusterid); 
				System.out.println(t);
				tlist.add(t);
			}
			
		}catch (Exception e){
			System.out.println("Failed to retrieve values from database");
			System.out.println(e);
		}
		this.close();
		return tlist;
	}
	
	public void updateTrajectory(ArrayList<Trajectory> tlist)
	{
		for (int x = 0; x < tlist.size(); x++)
		{
			this.connect();
			try{
				if (tlist.get(x).getRowID() == -1){
					insertValues(tlist.get(x).getDatabaseValueList());
					return;
				}
				StringBuilder bob = new StringBuilder();
				bob.append("UPDATE ");
				bob.append(TrajectorySchema.getTableName());
				bob.append(" SET ");
				bob.append(tlist.get(x).DBNameValuePair());
				bob.append(" WHERE ROWID=");
				bob.append(tlist.get(x).getRowID());
				
				stmt.executeUpdate(bob.toString());
				System.out.println("Succesfully Updated Row: " + tlist.get(x).getRowID());
			}catch (Exception e)
			{
				System.out.println("Failed to Update Trajectory: " + tlist.get(x));
			}
			
			this.close();
		}
	}
	
	public static void main(String [] args)
	{
		SQLiteDBC db = new SQLiteDBC();
		db.createTable();
//		ReadCSV reader = new ReadCSV("/home/jjbreen/Git/NYCZCC/data/yellow_tripdata_2015-01.csv");
//		try {
//			reader.importCSVtoDB(db);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		db.retrieveRows(10,20);
	}
}
