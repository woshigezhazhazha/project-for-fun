package signin;

import java.awt.print.Printable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class main {
	
	private static Connection connection=null;
	private static boolean studentRegExist=false;
	private static boolean teacherRegExist=false;
	private static boolean classInfoExist=false;
	
	//创建注册信息表
	private static final String createStuReg="create table studentReg(num int primary key,"
			+ "name nvarchar(10),"
			+ "psw nvarchar(20),"
			+ "idnum nvarchar(20),"
			+ "major nvarchar(40))";
	private static final String createTeaReg="create table teacherReg(num int primary key,"
			+ "name nvarchar(10),"
			+ "psw nvarchar(20))";
	//create class info table
	private static final String createClassInfo="create table classInfo(num int primary key,"
			+ "name nvarchar(40),"
			+ "timeLimit int,"
			+ "teacherNum int,"
			+ "isOpen int,"
			+ "latitude decimal(38,20),"
			+ "longitude decimal(38,20))";

	public static void main(String args[]){
		//连接数据库
		connection=DBUtils.connect();
		
		studentRegExist=DBUtils.tableExisted(connection, "studentReg");
		if(!studentRegExist){
			//create tables for register
			int createstu=DBUtils.createTable(connection, createStuReg);
			if(createstu!=-1){
				System.out.println("students' register table is created");
			}
		}
		
		teacherRegExist=DBUtils.tableExisted(connection, "teacherReg");
		if(!teacherRegExist){
			int createtea=DBUtils.createTable(connection, createTeaReg);
			if(createtea!=-1){
				System.out.println("teachers' register table is created");
			}
		}
		
		classInfoExist=DBUtils.tableExisted(connection, "classInfo");
		if(!classInfoExist){
			int createclass=DBUtils.createTable(connection, createClassInfo);
			if(createclass!=-1){
				System.out.println("class info table is created");
			}
		}
		
		//start the server
		Server server=new Server();
		server.startServer();
	}
}
