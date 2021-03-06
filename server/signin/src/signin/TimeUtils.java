package signin;


import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.Calendar;
import java.util.Date;

import javafx.scene.chart.PieChart.Data;

public class TimeUtils {
	static int year1=0;
	static int year2=0;
	static int month1=0;
	static int month2=0;
	static int day1=0;
	static int day2=0;
	static int hour1=0;
	static int hour2=0;
	static int minute1=0;
	static int minute2=0;
	static int second1=0;
	static int second2=0;
	public static boolean isInTimeLimit(Timestamp date1,Timestamp date2,int limit){
		year1=date1.getYear();
		year2=date2.getYear();
		if(year1!=year2)
			return true;
		month1=date1.getMonth();
		month2=date2.getMonth();
		if(month1!=month2)
			return true;
		day1=date1.getDay();
		day2=date2.getDay();
		if(day1!=day2)
			return true;
		hour1=date1.getHours();
		hour2=date2.getHours();
		minute1=date1.getMinutes();
		minute2=date2.getMinutes();
		if((minute1+hour1*60)-(minute2+hour2*60)>limit)
			return true;
		
		return false;
	}
	
	public static String timestampToString(Timestamp timestamp){
		   SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	   	   String dateString = formatter.format(timestamp);
	   	   return dateString;
	}
	
	public static Date stringToDate(String string){
		Date date=new Date();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
        try {
			date = sdf.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return date;
	}
	
	public static int getYear(Date date){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		
		int y=calendar.get(Calendar.YEAR);
		
		return y;
	}
	
	public static int getMonth(Date date){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		
		int m=calendar.get(Calendar.MONTH);
		
		return m+1;
	}
	
	public static int getDay(Date date){
		Calendar calendar=Calendar.getInstance();
		calendar.setTime(date);
		
		int d=calendar.get(Calendar.DAY_OF_MONTH);
		
		return d;
	}

}
