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

public class TeaSetting extends AppCompatActivity implements View.OnClickListener {

    private TextView title;
    private LinearLayout back;
    private LinearLayout reglog;
    private LinearLayout addClass;
    private Button logout;
    private LinearLayout myClass;
    private LinearLayout signinInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tea_setting);
        App.addActivity(this);
        initView();
    }

    private void initView(){
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("设置");
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        logout=(Button)findViewById(R.id.btn_logout);
        logout.setOnClickListener(this);
        reglog=(LinearLayout) findViewById(R.id.ll_reglog);
        reglog.setOnClickListener(this);
        addClass=(LinearLayout)findViewById(R.id.ll_addClass);
        addClass.setOnClickListener(this);
        myClass=(LinearLayout)findViewById(R.id.ll_myClass);
        myClass.setOnClickListener(this);
        signinInfo=(LinearLayout)findViewById(R.id.ll_signinInfo);
        signinInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                TeaSetting.this.finish();
                break;
            case R.id.ll_reglog:
                Intent intent=new Intent(TeaSetting.this,Login.class);
                startActivity(intent);
                break;
            case R.id.ll_myClass:
                Intent intent3=new Intent(TeaSetting.this,MyClass.class);
                startActivity(intent3);
                break;
            case R.id.ll_addClass:
                Intent intent2=new Intent(TeaSetting.this, SetClass.class);
                startActivity(intent2);
                break;
            case R.id.ll_signinInfo:
                Intent intent4=new Intent(TeaSetting.this,ShowSigninInfo.class);
                startActivity(intent4);
                break;
            case R.id.btn_logout:
                AlertDialog alertDialog=new AlertDialog.Builder(TeaSetting.this)
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
