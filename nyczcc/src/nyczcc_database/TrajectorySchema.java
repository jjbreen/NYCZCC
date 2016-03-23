package nyczcc_database;

public class TrajectorySchema {
	
	public static String getTableName(){
		return "TrajectorySchema";
	}
	
	public static String getSchema(){
		StringBuilder bob = new StringBuilder();
		bob.append("pickupt DATETIME,");
		bob.append("dropofft DATETIME,");
		bob.append("plat REAL,");
		bob.append("plong REAL,");
		bob.append("dlat REAL,");
		bob.append("dlong REAL");
		return bob.toString();
	}
	
	public static String[] getCSVNames()
	{
		return new String[]{"tpep_pickup_datetime", "tpep_dropoff_datetime", "pickup_latitude", "pickup_longitude", "dropoff_latitude", "dropoff_longitude"};
	}
	
	
}
