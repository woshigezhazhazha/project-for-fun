package com.tizz.signin.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.MajorAdapter;
import com.tizz.signin.R;
import com.tizz.signin.utils.DBUtils;

import java.util.ArrayList;

public class StuSigninInfo extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private Spinner classChoose;
    private Button check;
    private ListView info;
    private ArrayList<String> signinList=new ArrayList<>();
    private ArrayList<String> signinClass=new ArrayList<>();
    private String className=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_signin_info);
        initView();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("签到记录");
        classChoose=(Spinner)findViewById(R.id.sp_class);
        check=(Button)findViewById(R.id.btn_check);
        check.setOnClickListener(this);
        info=(ListView)findViewById(R.id.lv_checkList);
        initSpinner();
    }

    private void initSpinner(){
        DBUtils dbUtils=new DBUtils(StuSigninInfo.this,"userInfo.db",null,2);
        SQLiteDatabase db=dbUtils.getWritableDatabase();
        Cursor cursor=db.query("StudentClass",
                null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String name=cursor.getString(cursor.getColumnIndex("className"));
                signinClass.add(name);
            }while (cursor.moveToNext());
        }
        cursor.close();

        signinClass.add("选择课堂");

        MajorAdapter adapter=new MajorAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,signinClass);
        classChoose.setAdapter(adapter);
        classChoose.setSelection(signinClass.size()-1,true);
        classChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                className=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initListView(){
        if(className==null){
            Toast.makeText(StuSigninInfo.this,"还没有选择课堂！",
                    Toast.LENGTH_LONG).show();
            return;
        }
        DBUtils dbUtils=new DBUtils(StuSigninInfo.this,"userInfo.db",null,2);
        SQLiteDatabase db=dbUtils.getWritableDatabase();
        Cursor cursor=db.rawQuery("select * from StudentSignin where className=?",
                new String[]{className});
        if(cursor.moveToFirst()){
            do{
                String name=cursor.getString(cursor.getColumnIndex("className"));
                String time=cursor.getString(cursor.getColumnIndex("signinTime"));
                String showInfo=name+"\n"+time;
                signinList.add(showInfo);
            }while (cursor.moveToNext());
        }
        cursor.close();

        if(signinList.size()==0){
            Toast.makeText(StuSigninInfo.this,"还没有课堂签到数据！",
                    Toast.LENGTH_LONG).show();
        }
        else{
            ArrayAdapter<String> adapter=new ArrayAdapter<>(
                    StuSigninInfo.this,android.R.layout.simple_list_item_1,signinList);
            info.setAdapter(adapter);
        }
    }

    private void clearListView(){
        if(signinList.size()!=0){
            signinList.clear();
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(
                StuSigninInfo.this,android.R.layout.simple_list_item_1,signinList);
        info.setAdapter(adapter);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                StuSigninInfo.this.finish();
                break;
            case R.id.btn_check:
                //update the listview info
                clearListView();
                initListView();
                break;
        }
    }
}
