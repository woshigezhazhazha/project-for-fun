import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import signin.DBUtils;

public class SigninInfos implements SigninInfo{
	
	private Connection connection=null;
	private ResultSet resultSet=null;
	
	

	@Override
	public ArrayList<String> getClassStudents(String className) {
		// TODO Auto-generated method stub
		ArrayList<String> list=new ArrayList<String>();
		String getClassStudentsInfo="select * from "+className+"课堂学生信息";
		resultSet=DBUtils.select(connection, getClassStudentsInfo);
		try{
			while(resultSet.next()){
				String name=resultSet.getString("stuName");
				list.add(name);
			}
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if(!list.isEmpty()){
			return list;
		}
		return null;
		
	}
	
	@Override
	public int getClassStudentNum(String className) {
		// TODO Auto-generated method stub
		String selectSql="select count(stuId) as counts from "+className+"课堂学生信息";
		resultSet=DBUtils.select(connection, selectSql);
		int result=0;
		try{
			while(resultSet.next()){
				result=resultSet.getInt("counts");
			}
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ArrayList<String> getAllClassInfo() {
		// TODO Auto-generated method stub
		ArrayList<String> list=new ArrayList<>();
		String getClassInfo="select * from classInfo";
		resultSet=DBUtils.select(connection, getClassInfo);
		try {
			while(resultSet.next()){
				String name=resultSet.getString("name");
				list.add(name);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!list.isEmpty()){
			return list;
		}
		else{
			return null;
		}
	}


	@Override
	public int getStudentSigninTimes(String className,String stuId) {
		// TODO Auto-generated method stub
		String getTimes="select count(stuNum) as counts from "+className+"课堂签到信息,studentReg where "
				+ "stuId=studentReg.stuid and studentReg.num="+className+"课堂签到信息.stuNum";
		int result=0;
		resultSet=DBUtils.select(connection, getTimes);
		try {
			if(resultSet.next()){
				result=resultSet.getInt("counts");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ResultSet getClassData(String className) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet getStudentData(String className, String stuId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getAllStudentsInfo() {
		// TODO Auto-generated method stub
		//the list to save students info
		ArrayList<String> list=new ArrayList<>();
		
		String selectSql="select * from studentReg";
		resultSet=DBUtils.select(connection, selectSql);
		
		try {
			while(resultSet.next()){
				String name=resultSet.getString("name");
				String stuId=resultSet.getString("idnum");
				String info=name+" "+stuId;
				list.add(info);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}

	@Override
	public ArrayList<String> getAllTeachersInfo() {
		// TODO Auto-generated method stub
		//the list to save teachers info
		ArrayList<String> list=new ArrayList<>();
		
		String selectSql="select * from teacherReg";
		resultSet=DBUtils.select(connection, selectSql);
		
		try {
			while(resultSet.next()){
				String name=resultSet.getString("name");
				list.add(name);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}


}
