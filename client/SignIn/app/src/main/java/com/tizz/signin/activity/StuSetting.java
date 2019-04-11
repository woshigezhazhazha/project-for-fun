package com.tizz.signin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tizz.signin.R;

public class StuSetting extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private LinearLayout addClass;
    private LinearLayout reglog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_setting);
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
        }
    }

}
