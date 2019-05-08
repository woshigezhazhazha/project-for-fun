package signin;

import java.io.DataInputStream;

import java.io.DataOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;

import javafx.scene.chart.PieChart.Data;

public class SocketThread implements Runnable {
	
	private Socket socket;
	private Connection connection;
	private ResultSet resultSet;
	
	public SocketThread(Socket socket){
		this.socket=socket;
	}
	
    public void run() {
		try{
			connection=DBUtils.connect();
			DataInputStream inputStream=new DataInputStream(socket.getInputStream());
			DataOutputStream outputStream=new DataOutputStream(socket.getOutputStream());
			String cmdkind=inputStream.readUTF();
			
			if(cmdkind.equals("teacherRegister")){
				String name=inputStream.readUTF();
				String psw=inputStream.readUTF();
				String yesAnswer="succed";
				String noAnswer="fail";
				//get the maxest id num;
				int num=0;
				int result=0;
				String numsql="select max(num) from teacherReg";
				synchronized(connection)  {
					resultSet=DBUtils.select(connection, numsql);
					if(resultSet.next()){
						num=resultSet.getInt(1);
					}
					//the num increases by one
					num++;
					String sql="insert into teacherReg values("+num+",'"+name+"','"+psw+"')";
					result=DBUtils.insert(connection, sql);
				}
			
				if(result==-1){
					outputStream.writeUTF(noAnswer);
				}
				else{
					outputStream.writeUTF(yesAnswer);
					outputStream.writeInt(num);
				}
			}
			
			
			else if(cmdkind.equals("studentRegister")){
				String name=inputStream.readUTF();
				String psw=inputStream.readUTF();
				String stuID=inputStream.readUTF();
				String stuMajor=inputStream.readUTF();
				String yesAnswer="succed";
				String noAnswer="fail";
				
				int num=0;
				int result=0;
				
				synchronized (connection) {
					String numsql="select max(num) from studentReg";
					resultSet=DBUtils.select(connection, numsql);
					if(resultSet.next()){
						num=resultSet.getInt(1);
					}
					//the num increases by one
					num++;
					//create the insert sql
					String sql="insert into studentReg values("+num+",'"+name+"','"+psw+"','"+stuID+"','"+stuMajor+"')";
					result=DBUtils.insert(connection, sql);
				}
				
				if(result==-1){
					outputStream.writeUTF(noAnswer);
				}
				else{
					outputStream.writeUTF(yesAnswer);
					outputStream.writeInt(num);
				}
			}
			
			
			else if(cmdkind.equals("setClass")){
				String className=inputStream.readUTF();
				int teacherNum=inputStream.readInt();
				int timeLimit=inputStream.readInt();
				
				//search if the class name has been used
				String checksql="select * from classInfo where name='"+className+"'";
				int num=0;
				int result=0;
				
				synchronized (connection) {
					resultSet=	DBUtils.select(connection, checksql);
					if(resultSet.next()){
						//the class name already exists
						outputStream.writeInt(-2);
						outputStream.close();
						inputStream.close();
						socket.close();
						return;
					}
					
					String numsql="select max(num) from classInfo";
					resultSet=DBUtils.select(connection, numsql);
					if(resultSet.next()){
						num=resultSet.getInt(1);
					}
					num++;
					//create class sql
					String sql="insert into classInfo values("+num+",'"+className+"',"+timeLimit+","+teacherNum+",0,0,0)";
					result=DBUtils.insert(connection, sql);
				}
				
				
				if(result==-1){
					outputStream.writeInt(-1);
				}
				else{
					//create a signin info table for this class 
					String classTable="create table "+className+"课堂签到信息("
							+ "stuNum int,"
							+ "signinTime datetime2(0))";
					int createClassTable=DBUtils.createTable(connection,classTable);
					
					//create a talbe to show students who add this class
					String addClassStus="create table "+className+"课堂学生信息("
							+ "stuName nvarchar(10),"
							+ "stuId nvarchar(20),"
							+ "stuMajor nvarchar(40),"
							+ "lastSigninTime datetime2(0))";
					int createAddClassStus=DBUtils.createTable(connection, addClassStus);
					
					if(createClassTable==-1 ||createAddClassStus==-1){
						//delete this class info from the classInfo table
						String deleteSql="delete from classInfo where name='"+className+"'";
						int deleteResult=DBUtils.update(connection, deleteSql);
						outputStream.writeInt(-1);
					}
					else{
						//successfully created
						outputStream.writeInt(1);
						//return the class number
						outputStream.writeInt(num);
					}
				}
			}
			
			
			else if(cmdkind.equals("addClass")){
				String className=inputStream.readUTF();
				String checkClass="select * from classInfo where name='"+className+"'";
				resultSet=DBUtils.select(connection, checkClass);
				if(!resultSet.next()){
					outputStream.writeInt(-5);
					return;
				}
				int classNum=resultSet.getInt(1);
				String name=inputStream.readUTF();
				String stuid=inputStream.readUTF();
				String major=inputStream.readUTF();
				
				String insertSql="insert into "+className+"课堂学生信息 values('"+name+"','"+stuid+"','"+major+"',null)";
				int result=DBUtils.insert(connection, insertSql);
				if(result>0)
					outputStream.writeInt(1);
				else
					outputStream.writeInt(0);
				
			}
			
			
			else if(cmdkind.equals("startSignin")){
				String name=inputStream.readUTF();
				String opensql="update classInfo set isOpen=1 where name='"+name+"'";
				String closesql="update classInfo set isOpen=0 where name='"+name+"'";
				String getTimeLimit="select * from classInfo where name='"+name+"'";
				int timeLimit=0;
				resultSet=DBUtils.select(connection, getTimeLimit);
				if(resultSet.next()){
					timeLimit=resultSet.getInt(3);
				}
				
				//update the signin location
				double latitude=inputStream.readDouble();
				System.out.println(latitude);			
				
				double longitude=inputStream.readDouble();
				System.out.println(longitude);
				String updateLatitude="update classInfo set latitude="+latitude+" where name='"+name+"'";
				String updateLongitude="update classInfo set longitude="+longitude+" where name='"+name+"'";
				int updatela=DBUtils.update(connection, updateLatitude);
				int updatelo=DBUtils.update(connection, updateLongitude);
				if(updatela<0 || updatelo<0){
					System.out.println("update class location failed");
				}
				
				int openresult=DBUtils.update(connection, opensql);
				if(openresult>0){
					outputStream.writeInt(1);
					//set the class open for timeLimit
					Thread.sleep(timeLimit*60*1000);
					//close signin after timeLimit
				    DBUtils.update(connection, closesql);
				}
				else{
					outputStream.writeInt(-2);
				}
			}
			
			
			else if(cmdkind.equals("signin")){
				//get system time first
				Timestamp time=new Timestamp(System.currentTimeMillis());	
				int classOpen=0;
				double classLatitude=0;
				double classLongitude=0;
				int timeLimit=0;
				String className=inputStream.readUTF();
				String checkClass="select * from classInfo where name='"+className+"'";
				resultSet=DBUtils.select(connection, checkClass);
				if(resultSet.next()){
					classOpen=resultSet.getInt("isOpen");
					classLatitude=resultSet.getDouble("latitude");
					classLongitude=resultSet.getDouble("longitude");
					timeLimit=resultSet.getInt("timeLimit");
				}
				if(classOpen==1){
					//check the location
					double userLatitude=inputStream.readDouble();
					double userLongitude=inputStream.readDouble();
			
					/*
					if(!DistanceUtils.isBetweenDistance(classLongitude, classLatitude, userLongitude, userLatitude)){
						outputStream.writeInt(-3);
						return;
					}
					*/
					
					
					int stuNum=inputStream.readInt();
					String stuId=inputStream.readUTF();
					
					//check for signin times
					String signinTimes="select * from "+className+"课堂学生信息 where stuId='"+stuId+"'";
					resultSet=DBUtils.select(connection, signinTimes);
					if(resultSet.next()){
						Timestamp lasttime=resultSet.getTimestamp("lastSigninTime");
						if(lasttime!=null){
							boolean isInTimeLimit=TimeUtils.isInTimeLimit(time, lasttime, timeLimit);
							System.out.println(isInTimeLimit);
							if(!isInTimeLimit){
								outputStream.writeInt(-50);
								return;
							}
													
					    }
					
						
						
					String insertSignin="insert into "+className+"课堂签到信息 values("+stuNum+",'"+time+"')";
					String updateSigninTime="update "+className+"课堂学生信息 set lastSigninTime='"+time+"' where stuId='"+stuId+"'";
					int result=DBUtils.insert(connection, insertSignin);
					int result2=DBUtils.update(connection, updateSigninTime);
					if(result>0 && result2>0){
						outputStream.writeInt(1);
						SimpleDateFormat df=new SimpleDateFormat("HH:mm:ss");
						SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						//return signin time to app
						outputStream.writeUTF(df.format(time));
						outputStream.writeUTF(df2.format(time));
					}
					else{
						outputStream.writeInt(-4);
					}
				}
				}
				else{
					//the class is not open
					outputStream.writeInt(-2);
				}
			}
			
			
			else if(cmdkind.equals("classDetails")){
				String className=inputStream.readUTF();
				String getData="select * from "+className+"课堂学生信息 ";
				resultSet=DBUtils.select(connection, getData);
				while(resultSet.next()){
					String name=resultSet.getString("stuName");
					String stuid=resultSet.getString("stuId");
					String major=resultSet.getString("stuMajor");
					String stuInfo=name+"("+stuid+")\n("+major+")";
					outputStream.writeUTF(stuInfo);
				}
				outputStream.writeUTF("@@@the stu info is at an end!!!!");	
			}
			
			
			else if(cmdkind.equals("checkStudent")){
				String className=inputStream.readUTF();
				String getData="select studentReg.name,studentReg.idnum,studentReg.major,count("+className+"课堂签到信息.stuNum) as counts "
						+ "from studentReg,"+className+"课堂签到信息 "
								+ "where studentReg.num="+className+"课堂签到信息.stuNum "
										+ "group by studentReg.idnum,studentReg.name,studentReg.major";
				resultSet=DBUtils.select(connection, getData);
				while(resultSet.next()){
					String name=resultSet.getString("name");
					String stuid=resultSet.getString("idnum");
					String major=resultSet.getString("major");
					int count=resultSet.getInt("counts");
					String dataReturned=name+"("+stuid+")("+major+")\n签到"+count+"次";
					outputStream.writeUTF(dataReturned);
				}
				outputStream.writeUTF("###the sigin info for all students is over!!!");
			}
			
			
            else if(cmdkind.equals("checkAllInfo")){
            	String className=inputStream.readUTF();
            	String getData="select studentReg.name,studentReg.idnum,"+className+"课堂签到信息.signinTime "
            			+ "from "+className+"课堂签到信息,studentReg "
            					+ "where "+className+"课堂签到信息.stuNum=studentReg.num";
            	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            	resultSet=DBUtils.select(connection, getData);
            	while(resultSet.next()){
            			String name=resultSet.getString("name");
            			String stuid=resultSet.getString("idnum");
            			Timestamp date=resultSet.getTimestamp("signinTime");
            			String time=df.format(date);
            			String dataReturned=name+"("+stuid+")\n签到:"+time;
            			outputStream.writeUTF(dataReturned);
            	}
            	outputStream.writeUTF("###the sigin info for all info is over!!!");
			}
			
			
            else if(cmdkind.equals("checkMajor")){
            	String className=inputStream.readUTF();
            	String major=inputStream.readUTF();
            	String getData="select name,idnum,signinTime "
            			+ "from "+className+"课堂签到信息,studentReg "
            					+ "where "+className+"课堂签到信息.stuNum=studentReg.num "
            							+ "and studentReg.major='"+major+"'";
            	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            	resultSet=DBUtils.select(connection, getData);
            	while(resultSet.next()){
            			String name=resultSet.getString("name");
            			String stuid=resultSet.getString("idnum");
            			Timestamp date=resultSet.getTimestamp("signinTime");
            			String time=df.format(date);
            			String dataReturned=name+"("+stuid+")\n签到:"+time;
            			outputStream.writeUTF(dataReturned);
            	}
            	outputStream.writeUTF("###the sigin info for this major is over!!!");
			}
			
			
            else if(cmdkind.equals("checkTime")){
            	String className=inputStream.readUTF();
            	int year=inputStream.readInt();
            	int month=inputStream.readInt();
            	int day=inputStream.readInt();
            	System.out.println("客户端 "+year+month+day);
            	//String getDate="select * from "+className+"课堂签到信息";
            	String getData="select studentReg.name,studentReg.idnum,studentReg.major,"+className+"课堂签到信息.signinTime "
        				+ "from "+className+"课堂签到信息,studentReg "
        						+ "where "+className+"课堂签到信息.stuNum=studentReg.num";
            	boolean find=false;
            	Timestamp time = null;
            	Date date=null;
            	String time2=null;
            	resultSet=DBUtils.select(connection, getData);
            	while(resultSet.next()){
            		time=resultSet.getTimestamp("signinTime");
            		time2=TimeUtils.timestampToString(time);
            		date=TimeUtils.stringToDate(time2);
            		
            		
            		int y=TimeUtils.getYear(date);
            		int m=TimeUtils.getMonth(date);
            		int d=TimeUtils.getDay(date);
            		
            		System.out.println("服务器 "+date+" "+y+" "+m+" "+d);
            		if(y==year && m==month && d==day){
            			String name=resultSet.getString("name");
            			String stuid=resultSet.getString("idnum");
            			String stumajor=resultSet.getString("major");
            			
            			
            			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            			String stime=df.format(time);

            			String dataReturned=name+"("+stuid+")("+stumajor+")\n"+stime;
            			outputStream.writeUTF(dataReturned);
            			
            		}
            	}
            	
            	outputStream.writeUTF("###the sigin info for this time is over!!!");
			}
			
			
			inputStream.close();
			outputStream.close();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
