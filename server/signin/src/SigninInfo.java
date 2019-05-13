import java.sql.ResultSet;
import java.util.ArrayList;

public interface SigninInfo {

	//get all students' info for this class
	public ArrayList<String> getClassStudents(String className);
	
	//get all students' info
	public ArrayList<String> getAllStudentsInfo();
		
	//get all teachers' info
	public ArrayList<String> getAllTeachersInfo();
	
	//get all classes info
	public ArrayList<String> getAllClassInfo();
	
	//get students' signin times
	public int getStudentSigninTimes(String className,String stuId);
	
	//get class's students num
	public int getClassStudentNum(String className);
	
	//get the class data that the user want
	public ResultSet getClassData(String className);
	
	//get the student data 
	public ResultSet getStudentData(String className,String stuId);
	
}
