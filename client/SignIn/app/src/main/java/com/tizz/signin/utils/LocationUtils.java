package com.tizz.signin.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.List;

public class LocationUtils {

    private static LocationManager locationManager;
    private static  String provider;
    private static Location myLocation;
    public static double latitude;
    public static double longitude;

    public static double getLatitude(){
        return latitude;
    }

    public static double getLongitude(){
        return longitude;
    }

    public static void initLocationManager(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions((Activity)context ,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }

        locationManager =(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList=locationManager.getProviders(true);

        if(providerList.contains(LocationManager.NETWORK_PROVIDER)){
            //provider=LocationManager.NETWORK_PROVIDER;
            //Toast.makeText(MainActivity.this,"NETWORK to use",Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,locationListener);
        }
        if(providerList.contains(LocationManager.GPS_PROVIDER)){
            //provider=LocationManager.GPS_PROVIDER;
            //Toast.makeText(MainActivity.this,"GPS to use",Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
        else{
            Toast.makeText(context,"没有可供使用的位置提供器！",
                    Toast.LENGTH_SHORT).show();
        }

    }



    private static LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(isBetterLocation(location,myLocation)){
                latitude=location.getLatitude();
                longitude=location.getLongitude();
                myLocation=location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private static final int TWO_MINUTES=1000*60*2;

    private static boolean isBetterLocation(Location location, Location currentLocation){
        if(currentLocation==null){
            return true;
        }

        //check time
        long time=location.getTime()-currentLocation.getTime();
        boolean isSignificantlyNewer=time>TWO_MINUTES;
        boolean isSignificantlyOler=time<-TWO_MINUTES;
        boolean isNewer=time>0;

        if(isSignificantlyNewer){
            //the newer one is better
            return true;
        }
        else if(isSignificantlyOler){
            //the older one is worse
            return false;
        }

        //check accuracy
        int accuracy=(int)(location.getAccuracy()-currentLocation.getAccuracy());
        boolean isMoreAccurate=accuracy<0;
        boolean isLessAccurate=accuracy>0;
        boolean isSignificantlyLessAccurate=accuracy>200;

        boolean isFromSameProvider=isSameProvider(location.getProvider(),currentLocation.getProvider());

        if(isMoreAccurate){
            return true;
        }
        else if(isNewer && isLessAccurate){
            return true;
        }
        else if(isNewer && isFromSameProvider && isSignificantlyLessAccurate){
            return true;
        }

        return false;
    }

    private static boolean isSameProvider(String provider1,String provider2){
        if(provider1==null){
            return provider2==null;
        }
        return provider1.equals(provider2);
    }

}
