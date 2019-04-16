package com.tizz.signin.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    public static String getSysTime(){
        long sysTime=System.currentTimeMillis();
        Date date=new Date(sysTime);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm:ss");
        String sysTimeStr= simpleDateFormat.format(date);
        return sysTimeStr;
    }
}
