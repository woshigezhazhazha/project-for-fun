package signin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {
	
	private static final String URL="jdbc:sqlserver://localhost:1433;DatabaseName=salaryCharge";
	private static final String USER="sa";
	private static final String PASSWORD="123456";
	
	private static Connection connection=null;
	private static ResultSet resultset=null;
	private static PreparedStatement statement=null;
	
	@SuppressWarnings("finally")
	public static Connection connect(){
		try{
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection=(Connection)DriverManager.getConnection(URL, USER, PASSWORD);
		}catch (ClassNotFoundException e) {
			// TODO: handle exception
			e.printStackTrace();
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			return connection;
		}
	}
	
	@SuppressWarnings("finally")
	public static ResultSet select(Connection connection,String sql){
		try{
			statement=connection.prepareStatement(sql);
			resultset=statement.executeQuery();
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally {
			return resultset;
		}
	}
	
	public static int insert(Connection connection,String sql){
		try{
			statement=connection.prepareStatement(sql);
			int count=statement.executeUpdate();
			return count;
		}catch (SQLException exception) {
			// TODO: handle exception
			exception.printStackTrace();
		}
		return -1;
	}
	
	public static int update(Connection connection,String sql){
		try{
			statement=connection.prepareStatement(sql);
			int count=statement.executeUpdate();
			return count;
		}catch (SQLException exception) {
			// TODO: handle exception
			exception.printStackTrace();
		}
		return -1;
	}
	
	public static int createTable(Connection connection,String sql){
		try{
			statement=connection.prepareStatement(sql);
			int count=statement.executeUpdate();
			return count;
		}catch (SQLException exception) {
			// TODO: handle exception
			exception.printStackTrace();
		}
		return -1;
	}
	
	public static boolean tableExisted(Connection connection,String tableName){
		try {
			resultset=connection.getMetaData().getTables(null, null,tableName, null);
			if(resultset.next())
				return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
