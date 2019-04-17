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
    private static Location location;

    public static Location getLocation(Context context){
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions((Activity)context ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }

        locationManager =(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providerList=locationManager.getProviders(true);
        if(providerList.contains(LocationManager.NETWORK_PROVIDER)){
            provider=LocationManager.NETWORK_PROVIDER;
            //Toast.makeText(MainActivity.this,"NETWORK to use",Toast.LENGTH_SHORT).show();
        }
        else if(providerList.contains(LocationManager.GPS_PROVIDER)){
            provider=LocationManager.GPS_PROVIDER;
            //Toast.makeText(MainActivity.this,"GPS to use",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context,"没有可供使用的位置提供器！",Toast.LENGTH_SHORT).show();
            return null;
        }

        location=locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(provider,1000,1,locationListener);
        return location;
    }

    private static LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
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

}
