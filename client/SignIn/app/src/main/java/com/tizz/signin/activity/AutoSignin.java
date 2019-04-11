package com.tizz.signin.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tizz.signin.R;

public class AutoSignin extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private Button autoSignin;
    private TextView autoInfo;
    private boolean auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_signin);
        initView();
        initInfo();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("自动签到");
        autoInfo=(TextView)findViewById(R.id.tv_autoInfo);
        autoSignin=(Button)findViewById(R.id.btn_autoSignin);
        autoSignin.setOnClickListener(this);
    }

    private void initInfo(){
        SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
        auto=sp.getBoolean("autoSignin",false);
        if(auto){
            autoInfo.setText("自动签到已开启");
            autoSignin.setText("关闭");
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                AutoSignin.this.finish();
                break;
            case R.id.btn_autoSignin:
                SharedPreferences.Editor editor=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
                if(autoSignin.getText().equals("开启")){
                    editor.putBoolean("autoSignin",true);
                    editor.commit();
                    autoInfo.setText("自动签到已开启");
                    autoSignin.setText("关闭");
                }
                else{
                    editor.putBoolean("autoSignin",false);
                    editor.commit();
                    autoInfo.setText("自动签到未开启");
                    autoSignin.setText("开启");
                }
                break;
        }
    }
}
