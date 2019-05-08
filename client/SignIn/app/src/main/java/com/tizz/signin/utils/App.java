package com.tizz.signin.utils;

import android.app.Activity;

import java.util.ArrayList;

public class App {

    private static ArrayList<Activity> aList=new ArrayList<>();


    public static void addActivity(Activity activity){
        aList.add(activity);
    }

    public static void exit(){
        try{
            for(Activity activity:aList){
                if(activity!=null){
                    activity.finish();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            System.exit(0);
        }
    }
}
