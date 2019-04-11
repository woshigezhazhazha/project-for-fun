package com.tizz.signin.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.R;
import com.tizz.signin.utils.App;
import com.tizz.signin.utils.LocationUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;
import com.tizz.signin.utils.TimeUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView settings;
    private RelativeLayout rlSettings;
    private Button signIn;
    private TextView currentTime;
    private TextView signinTime;
    private TextView signinNum;
    private boolean isLogined=false;
    private boolean isStudent=true;
    private Location location;
    private double latitude;
    private double longitude;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private int stuNum;
    private String sTime="2019";

    private static final int UPDATE_TIME=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        App.getInstance().addActivity(this);
        initViews();
        setLogined();
    }



    private void setLogined(){
        SharedPreferences sharedPreferences=getSharedPreferences("userInfo",MODE_PRIVATE);
        isLogined=sharedPreferences.getBoolean("isLogined",false);
        isStudent=sharedPreferences.getBoolean("isStudent",true);
        stuNum=sharedPreferences.getInt("userid",0);
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

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message message){
            switch (message.what){
                case UPDATE_TIME:
                    currentTime.setText("当前时间:"+ TimeUtils.getSysTime());
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
        signinNum=(TextView)findViewById(R.id.tv_signinNum);

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

        location= LocationUtils.getLocation(MainActivity.this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_signIn:

                new SigninTask().execute();
                break;
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

    class SigninTask extends AsyncTask<Void,Void,Integer> {

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

                if(location==null){
                    location=LocationUtils.getLocation(MainActivity.this);
                }
                if(location!=null){
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                }
                else{
                    return -5;
                }
                Socket socket=new Socket(SocketUtils.ip,6000);
                socket.setSoTimeout(5*1000);
                if(socket==null)
                    return -1;
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                String className=null;
                SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
                className=sp.getString("firstClass","");
                int result;
                if(isStudent){
                    outputStream.writeUTF("signin");
                    outputStream.writeUTF(className);
                    outputStream.writeDouble(latitude);
                    outputStream.writeDouble(longitude);
                    outputStream.writeInt(stuNum);
                    result=inputStream.readInt();
                }
                else{
                    outputStream.writeUTF("startSignin");
                    outputStream.writeUTF(className);
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
                        Toast.makeText(MainActivity.this,"签到成功！",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"打开签到成功！",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -1:
                    Toast.makeText(MainActivity.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
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
            }
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            App.getInstance().exit();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,keyEvent);
    }

}
