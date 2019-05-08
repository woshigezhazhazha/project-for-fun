package com.tizz.signin.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tizz.signin.R;
import com.tizz.signin.utils.App;

public class StuSetting extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private LinearLayout addClass;
    private LinearLayout reglog;
    private LinearLayout signinInfo;
    private LinearLayout autoSignin;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_setting);
        App.addActivity(this);
        initView();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("设置");
        addClass=(LinearLayout)findViewById(R.id.ll_addClass2);
        addClass.setOnClickListener(this);
        reglog=(LinearLayout)findViewById(R.id.ll_reglog2);
        reglog.setOnClickListener(this);
        signinInfo=(LinearLayout)findViewById(R.id.ll_signinInfo);
        signinInfo.setOnClickListener(this);
        autoSignin=(LinearLayout)findViewById(R.id.ll_autoSignin);
        autoSignin.setOnClickListener(this);
        logout=(Button)findViewById(R.id.btn_logout);
        logout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                StuSetting.this.finish();
                break;
            case R.id.ll_reglog2:
                Intent intent=new Intent(StuSetting.this,Login.class);
                startActivity(intent);
                break;
            case R.id.ll_addClass2:
                Intent intent2=new Intent(StuSetting.this, AddClass.class);
                startActivity(intent2);
                break;
            case R.id.ll_signinInfo:
                Intent intent3=new Intent(StuSetting.this, StuSigninInfo.class);
                startActivity(intent3);
                break;
            case R.id.ll_autoSignin:
                Intent intent4=new Intent(StuSetting.this, AutoSignin.class);
                startActivity(intent4);
                break;
            case R.id.btn_logout:
                AlertDialog alertDialog=new AlertDialog.Builder(StuSetting.this)
                        .setTitle("")
                        .setMessage("确定退出登录吗?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor=getSharedPreferences("userInfo",
                                        MODE_PRIVATE).edit();
                                editor.putBoolean("isLogined",false);
                                editor.commit();
                                App.exit();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                            }
                        })
                        .create();
                alertDialog.show();

                break;
        }
    }

}
