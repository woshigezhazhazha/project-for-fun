package com.tizz.signin.activity;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.tizz.signin.FingerprintDialogFragment;
import com.tizz.signin.R;
import com.tizz.signin.SpAdapter;
import com.tizz.signin.utils.App;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;
import com.tizz.signin.utils.TimeUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView settings;
    private RelativeLayout rlSettings;
    private Button signIn;
    private TextView currentTime;
    private TextView signinTime;
    private TextView signinNum;
    private boolean isLogined=false;
    private boolean isStudent=true;
    private boolean autoSignin=false;
    private boolean signined=false;
    private Location location;
    private double latitude;
    private double longitude;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private int stuNum;
    private String stuId;

    private static final int UPDATE_TIME=1;
    private static final int FINGERPRINT_READY=2;
    private static final int AUTOFINGERPRINT_READY=3;

    private Spinner spinner;
    private ArrayList<String> classList=new ArrayList<>();
    private String firstClass;
    private String signintime=null;

    private static final String DEFAULT_KEY_NAME = "default_key";
    private KeyStore keyStore;
    private boolean fingerprintSucceded=false;
    private int autoSigninTimes=0;
    private LocationClient locationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.addActivity(this);
        initViews();
        setLogined();
        initLocationOption();
        initSpinner();
        autoSignin();
    }

    private boolean supportFingerprint(){
        if(Build.VERSION.SDK_INT<23){
            return false;
        }else{
            KeyguardManager keyguardManager=getSystemService(KeyguardManager.class);
            FingerprintManager fingerprintManager=getSystemService(FingerprintManager.class);
            if(!fingerprintManager.isHardwareDetected()){
                return false;
            }
        }
        return true;
    }

    private boolean isFingerprintReady(){
        KeyguardManager keyguardManager=getSystemService(KeyguardManager.class);
        FingerprintManager fingerprintManager=getSystemService(FingerprintManager.class);
        if(!keyguardManager.isKeyguardSecure()){
            Toast.makeText(MainActivity.this,"请到设置里添加指纹识别！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!fingerprintManager.hasEnrolledFingerprints()){
            Toast.makeText(MainActivity.this,"请到设置里添加指纹识别！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC) .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyGenerator.init(builder.build()); keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initCipher() {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            showFingerPrintDialog(cipher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showFingerPrintDialog(Cipher cipher) {
        FingerprintDialogFragment fragment = new FingerprintDialogFragment();
        fragment.setCipher(cipher);
        fragment.show(getFragmentManager(), "fingerprint");
    }

    private void setLogined(){
        SharedPreferences sharedPreferences=getSharedPreferences("userInfo",MODE_PRIVATE);
        isLogined=sharedPreferences.getBoolean("isLogined",false);
        isStudent=sharedPreferences.getBoolean("isStudent",true);
        stuNum=sharedPreferences.getInt("userid",0);
        stuId=sharedPreferences.getString("stuid","");
        autoSignin=sharedPreferences.getBoolean("autoSignin",false);
        if(!isLogined){
            final AlertDialog alertDialog=new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("请先登录")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent=new Intent(MainActivity.this,Login.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            alertDialog.show();
        }
    }

    private void initSpinner(){
        if(isLogined){
            SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
            firstClass=sp.getString("firstClass","");
            if(!firstClass.equals("")){
                classList.add(firstClass);
                DBUtils dbUtils=new DBUtils(MainActivity.this,"userInfo.db",null,2);
                SQLiteDatabase db=dbUtils.getWritableDatabase();
                if(isStudent){
                    Cursor cursor=db.query("StudentClass",
                            null,null,null,null,null,null);
                    if(cursor.moveToFirst()){
                        do{
                            String name=cursor.getString(cursor.getColumnIndex("className"));
                            if(!name.equals(firstClass)){
                                classList.add(name);
                            }
                        }while (cursor.moveToNext());
                        classList.add(firstClass);
                    }
                    cursor.close();
                    if(classList.size()!=0){
                        SpAdapter adapter=new SpAdapter(this,
                                R.layout.support_simple_spinner_dropdown_item,classList);
                        spinner.setAdapter(adapter);
                        spinner.setVisibility(View.VISIBLE);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                TextView tv = (TextView) view;
                                tv.setTextColor(Color.WHITE);
                                tv.setGravity(Gravity.CENTER);
                                String newFirst=parent.getItemAtPosition(position).toString();
                                if(!newFirst.equals(firstClass)){
                                    //update the class for signin
                                    signined=false;
                                    firstClass=newFirst;
                                    SharedPreferences.Editor editor=getSharedPreferences("userInfo",
                                            MODE_PRIVATE).edit();
                                    editor.putString("firstClass",newFirst);
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
                }
                else{
                    Cursor cursor=db.query("TeacherClass",
                            null,null,null,null,null,null);
                    if(cursor.moveToFirst()){
                        do{
                            String name=cursor.getString(cursor.getColumnIndex("className"));
                            if(!name.equals(firstClass)){
                                classList.add(name);
                            }
                        }while (cursor.moveToNext());
                        classList.add(firstClass);
                    }
                    cursor.close();
                    if(classList.size()!=0){
                        SpAdapter adapter=new SpAdapter(this,
                                R.layout.support_simple_spinner_dropdown_item,classList);
                        spinner.setAdapter(adapter);
                        spinner.setVisibility(View.VISIBLE);
                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                TextView tv = (TextView) view;
                                tv.setTextColor(Color.WHITE);
                                tv.setGravity(Gravity.CENTER);
                                String newFirst=parent.getItemAtPosition(position).toString();
                                if(!newFirst.equals(firstClass)){
                                    firstClass=newFirst;
                                    SharedPreferences.Editor editor=getSharedPreferences("userInfo",
                                            MODE_PRIVATE).edit();
                                    editor.putString("firstClass",newFirst);
                                    editor.commit();
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }
            }
            }
        }
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case UPDATE_TIME:
                    currentTime.setText("当前时间:"+ TimeUtils.getSysTime());
                    break;
                case FINGERPRINT_READY:
                    new SigninTask().execute();
                    break;
                case AUTOFINGERPRINT_READY:
                    new AutoSigninTask().execute();
                    break;
                default:
                    break;
            }
        }
    };

    public void initViews(){
        rlSettings=(RelativeLayout) findViewById(R.id.rl_settings);
        rlSettings.setOnClickListener(this);
        signIn=(Button)findViewById(R.id.btn_signIn);
        signIn.setOnClickListener(this);
        currentTime=(TextView)findViewById(R.id.tv_currentTime);
        signinTime=(TextView)findViewById(R.id.tv_signinTime);
        spinner=(Spinner)findViewById(R.id.sp_classList);


        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try{
                        Thread.sleep(1000);
                        Message message=new Message();
                        message.what=UPDATE_TIME;
                        handler.sendMessage(message);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    private void autoSignin(){
        if(isLogined && isStudent && autoSignin){
            if(firstClass.equals("")){
                Toast.makeText(MainActivity.this,"请先添加课堂！",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if(!signined){
                signinTime.setVisibility(View.VISIBLE);
                fingerprintSucceded=FingerprintDialogFragment.succed;
                if(!fingerprintSucceded){
                    if(supportFingerprint()){
                        if(isFingerprintReady()){
                            initKey();
                            initCipher();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    fingerprintSucceded=FingerprintDialogFragment.succed;
                                    while(!fingerprintSucceded){
                                        try{
                                            Thread.sleep(200);
                                            fingerprintSucceded=FingerprintDialogFragment.succed;
                                        }catch (InterruptedException e){
                                            e.printStackTrace();
                                        }
                                    }
                                    Message message=new Message();
                                    message.what=AUTOFINGERPRINT_READY;
                                    handler.sendMessage(message);
                                }
                            }).start();
                        }
                        else{
                            return;
                        }
                    }
                    else{
                        new AutoSigninTask().execute();
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_signIn:
                if(!isLogined){
                    Toast.makeText(MainActivity.this,"请先登录！",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                if(firstClass.equals("")){
                    Toast.makeText(MainActivity.this,"尚未添加任何课堂！",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                if(!signined){
                    if(!isStudent){
                        new SigninTask().execute();
                        break;
                    }
                    signinTime.setVisibility(View.INVISIBLE);
                    fingerprintSucceded=FingerprintDialogFragment.succed;
                    if(!fingerprintSucceded){
                        if(supportFingerprint()){
                            if(isFingerprintReady()){
                                initKey();
                                initCipher();
                                //start a thread to get fingerprint result
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fingerprintSucceded=FingerprintDialogFragment.succed;
                                        while(!fingerprintSucceded){
                                            try{
                                                Thread.sleep(200);
                                                fingerprintSucceded=FingerprintDialogFragment.succed;
                                            }catch (InterruptedException e){
                                                e.printStackTrace();
                                            }
                                        }
                                        Message message=new Message();
                                        message.what=FINGERPRINT_READY;
                                        handler.sendMessage(message);
                                    }
                                }).start();
                                break;
                            }
                            else{
                                break;
                            }
                        }
                    }
                    new SigninTask().execute();
                    break;
                }
                else{
                    Toast.makeText(MainActivity.this,"你已签到，请勿重复操作！",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
            case R.id.rl_settings:
                if(!isLogined){
                    final AlertDialog alertDialog=new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("请先登录")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent=new Intent(MainActivity.this,Login.class);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    alertDialog.show();
                    break;
                }
                else{
                    if(isStudent){
                        Intent intent=new Intent(MainActivity.this, StuSetting.class);
                        startActivity(intent);
                        break;
                    }
                    else{
                        Intent intent=new Intent(MainActivity.this, TeaSetting.class);
                        startActivity(intent);
                        break;
                    }
                }
        }
    }

    public class SigninTask extends AsyncTask<Void,Void,Integer> {

        ProgressDialogUtils pd=new ProgressDialogUtils();

        @Override
        public void onPreExecute(){
            if(isStudent){
                pd.showProgressDialog(MainActivity.this,"签到","签到中");
            }
            else{
                pd.showProgressDialog(MainActivity.this,"签到","开启签到中");
            }
        }

        @Override
        public Integer doInBackground(Void ... params){
            try{
                if(firstClass==null)
                    return -20;

                //latitude=LocationUtils.getLatitude();
                //longitude=LocationUtils.getLongitude();
                //if(latitude==0 && longitude==0){
                //    return -5;
                //}
                Socket socket=new Socket(SocketUtils.ip,6000);
                socket.setSoTimeout(5*1000);
                if(socket==null)
                    return -1;
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                int result;
                if(isStudent){
                    outputStream.writeUTF("signin");
                    outputStream.writeUTF(firstClass);
                    outputStream.writeDouble(latitude);
                    outputStream.writeDouble(longitude);
                    outputStream.writeInt(stuNum);
                    outputStream.writeUTF(stuId);
                    result=inputStream.readInt();
                    if(result==1){
                        signintime=inputStream.readUTF();
                        String time=inputStream.readUTF();
                        DBUtils dbUtils=new DBUtils(MainActivity.this,
                                "userInfo.db",null,2);
                        SQLiteDatabase db=dbUtils.getWritableDatabase();
                        db.execSQL("insert into StudentSignin(className,signinTime) values(?,?)",
                                new String[]{firstClass,time});
                    }
                }
                else{
                    outputStream.writeUTF("startSignin");
                    outputStream.writeUTF(firstClass);
                    outputStream.writeDouble(latitude);
                    outputStream.writeDouble(longitude);
                    result=inputStream.readInt();
                }
                socket.close();
                inputStream.close();
                outputStream.close();
                return result;
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return -2;
        }

        @Override
        public void onPostExecute(Integer result){
            pd.finishProgressDialog();
            switch(result){
                case 1:
                    if(isStudent){
                        signined=true;
                        signinTime.setText("签到时间:"+signintime);
                        signinTime.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this,"签到成功！",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        String time=TimeUtils.getSysTime();
                        signinTime.setText("打开签到时间:"+time);
                        signinTime.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this,"打开签到成功！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -1:
                    Toast.makeText(MainActivity.this,"无法连接服务器！",Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    if(isStudent){
                        Toast.makeText(MainActivity.this,"该课堂尚未开启签到！",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"打开签到失败！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -3:
                    if(isStudent){
                        Toast.makeText(MainActivity.this,"不在签到范围之内！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -4:
                    if(isStudent){
                        Toast.makeText(MainActivity.this,"签到失败！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -5:
                    Toast.makeText(MainActivity.this,"无法获取位置，请稍后重试！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -20:
                    Toast.makeText(MainActivity.this,"你还未添加任何课堂！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -50:
                    Toast.makeText(MainActivity.this,"你已签到，勿重复操作！",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }


    class AutoSigninTask extends AsyncTask<Void,Void,Integer>{

        ProgressDialogUtils pd=new ProgressDialogUtils();

        @Override
        public void onPreExecute(){
            pd.showProgressDialog(MainActivity.this,"签到","签到中");
        }

        @Override
        public Integer doInBackground(Void ... params){
            try{
                if(firstClass==null)
                    return -20;
                int count=0;
                while(latitude==0 || longitude==0){
                    //latitude=LocationUtils.getLatitude();
                    //longitude=LocationUtils.getLongitude();
                    Thread.sleep(1000);
                    count++;
                    if(count==10)
                        return -10;
                }


                Socket socket=new Socket(SocketUtils.ip,6000);
                count=0;
                while (socket==null){
                    socket=new Socket(SocketUtils.ip,6000);
                    Thread.sleep(1000);
                    count++;
                    if(count==10)
                        return -11;
                }
                socket.setSoTimeout(5*1000);
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                int result;
                outputStream.writeUTF("signin");
                outputStream.writeUTF(firstClass);
                outputStream.writeDouble(latitude);
                outputStream.writeDouble(longitude);
                outputStream.writeInt(stuNum);
                outputStream.writeUTF(stuId);
                result=inputStream.readInt();
                count=0;
                while(result!=1){
                    Thread.sleep(1000);
                    outputStream.writeUTF("signin");
                    outputStream.writeUTF(firstClass);
                    outputStream.writeDouble(latitude);
                    outputStream.writeDouble(longitude);
                    outputStream.writeInt(stuNum);
                    outputStream.writeUTF(stuId);
                    result=inputStream.readInt();
                    count++;
                    if(count==10)
                        return -12;
                }
                signintime=inputStream.readUTF();
                String time=inputStream.readUTF();
                DBUtils dbUtils=new DBUtils(MainActivity.this,
                        "userInfo.db",null,2);
                SQLiteDatabase db=dbUtils.getWritableDatabase();
                db.execSQL("insert into StudentSignin(className,signinTime) values(?,?)",
                        new String[]{firstClass,time});
                socket.close();
                inputStream.close();
                outputStream.close();
                return result;
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (InterruptedException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void onPostExecute(Integer result){
            pd.finishProgressDialog();
            switch (result){
                case 1:
                    signined=true;
                    signinTime.setText("签到时间:"+signintime);
                    signinTime.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"签到成功！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    if(isStudent){
                        Toast.makeText(MainActivity.this,"不在签到范围之内！",Toast.LENGTH_SHORT).show();
                        autoSigninTimes++;
                        if(autoSigninTimes<10){
                            autoSignin();
                        }
                    }
                    break;
                case -10:
                    Toast.makeText(MainActivity.this,"无法获取位置，请稍后手动签到！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -11:
                    Toast.makeText(MainActivity.this,"无法连接服务器，请稍后手动签到！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -12:
                    Toast.makeText(MainActivity.this,"签到失败，请稍后手动签到！",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -20:
                    break;
                case -50:
                    Toast.makeText(MainActivity.this,"你已签到，勿重复操作！",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            App.exit();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,keyEvent);
    }


    private void initLocationOption() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){//未开启定位权限
            //开启定位权限,200是标识码
            ActivityCompat.requestPermissions(this ,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }


//定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        LocationClient locationClient = new LocationClient(getApplicationContext());
//声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
//注册监听函数
        locationClient.registerLocationListener(myLocationListener);
//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("gcj02");
//可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
//可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
//可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
//可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
//可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
//可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
//可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
//设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);
//开始定位
        locationClient.start();
    }
    /**
     * 实现定位回调
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            if (location.getFloor() != null) {
                // 当前支持高精度室内定位
                Log.d("indoor","true");
                String buildingID = location.getBuildingID();// 百度内部建筑物ID
                String buildingName = location.getBuildingName();// 百度内部建筑物缩写
                String floor = location.getFloor();// 室内定位的楼层信息，如 f1,f2,b1,b2
                locationClient.startIndoorMode();// 开启室内定位模式（重复调用也没问题），开启后，定位SDK会融合各种定位信息（GPS,WI-FI，蓝牙，传感器等）连续平滑的输出定位结果；
            }

            Log.d("location value::::",location.toString());
            //获取纬度信息
            latitude = location.getLatitude();
            //获取经度信息
            longitude = location.getLongitude();
            //获取定位精度，默认值为0.0f
            float radius = location.getRadius();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
            String coorType = location.getCoorType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            int errorCode = location.getLocType();

        }
    }


}
