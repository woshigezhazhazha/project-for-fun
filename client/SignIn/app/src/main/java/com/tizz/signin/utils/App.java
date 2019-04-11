package com.tizz.signin.utils;

import android.app.Activity;

import java.util.ArrayList;

public class App {
    private static App instance;
    private ArrayList<Activity> aList=new ArrayList<>();

    public synchronized static App getInstance(){
        if(instance==null){
            instance=new App();
        }
        return instance;
    }

    public void addActivity(Activity activity){
        aList.add(activity);
    }

    public void exit(){
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
