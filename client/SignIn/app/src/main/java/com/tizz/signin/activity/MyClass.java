package com.tizz.signin.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.R;
import com.tizz.signin.utils.DBUtils;

import java.util.ArrayList;

public class MyClass extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private ListView classList;
    private ArrayList<String> lists=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_class);
        initView();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("我的课堂");
        classList=(ListView) findViewById(R.id.lv_classLists);
        getDBData();
        setListView();
    }

    public void getDBData(){
        DBUtils dbUtils=new DBUtils(MyClass.this,"userInfo.db",null,2);
        SQLiteDatabase db=dbUtils.getWritableDatabase();
        Cursor cursor=db.query("TeacherClass",
                null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String name=cursor.getString(cursor.getColumnIndex("className"));
                lists.add(name);
            }while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void setListView(){
        if(lists.size()==0){
            Toast.makeText(MyClass.this,"还没有课堂数据！",Toast.LENGTH_LONG).show();
        }
        else{
            ArrayAdapter<String> adapter=new ArrayAdapter<>(
                    MyClass.this,android.R.layout.simple_list_item_1,lists);
            classList.setAdapter(adapter);
            classList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String classname=lists.get(position);
                    Intent intent=new Intent(MyClass.this,ClassDetails.class);
                    intent.putExtra("className",classname);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                MyClass.this.finish();
                break;
        }
    }
}
