package com.tizz.signin.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.R;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClassDetails extends AppCompatActivity implements View.OnClickListener {

    private ArrayList<String> lists=new ArrayList<>();
    private ListView stuInfo;
    private LinearLayout back;
    private TextView classDetail;
    private String className="课堂";
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private TextView title;
    private String classTimeLimit;
    private Button updateTimeLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);
        initView();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        classDetail=(TextView)findViewById(R.id.tv_classDetails);
        stuInfo=(ListView)findViewById(R.id.lv_classDetails);
        Intent intent=getIntent();
        className=intent.getStringExtra("className");
        title=(TextView)findViewById(R.id.tv_common);
        title.setText(className);
        updateTimeLimit=(Button)findViewById(R.id.btn_update);
        updateTimeLimit.setOnClickListener(this);

        //get timeLimit for this class
        classTimeLimit="100";
        DBUtils dbUtils=new DBUtils(ClassDetails.this,
                "userInfo.db",null,2);
        SQLiteDatabase db=dbUtils.getWritableDatabase();
        String getTimeLimit="select * from TeacherClass where className=?";
        Cursor cursor=db.rawQuery(getTimeLimit,new String[]{className});
        while(cursor.moveToNext()){
            classTimeLimit=cursor.getString(cursor.getColumnIndex("timeLimit"));
        }
        cursor.close();
        classDetail.setText("签到时限："+classTimeLimit+"分钟");

        //get the students' info
        new ClassDetailTask().execute();
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                ClassDetails.this.finish();
                break;
            case R.id.btn_update:
                Intent intent=new Intent(ClassDetails.this, UpdateTimeLimit.class);
                intent.putExtra("className",className);
                intent.putExtra("timeLimit",classTimeLimit);
                startActivity(intent);
                break;
        }
    }

    class ClassDetailTask extends AsyncTask<Void,Void,Integer> {

        @Override
        public void onPreExecute(){
        }

        @Override
        public Integer doInBackground(Void ... params){
            try{
                Socket socket=new Socket(SocketUtils.ip,SocketUtils.port);
                socket.setSoTimeout(5*5000);
                if(socket==null)
                    return -1;

                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());

                outputStream.writeUTF("classDetails");
                outputStream.writeUTF(className);
                String result=inputStream.readUTF();
                while(!result.equals("@@@the stu info is at an end!!!!")){
                    lists.add(result);
                    result=inputStream.readUTF();
                }
                socket.close();
                inputStream.close();
                outputStream.close();
                return 1;

            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void onPostExecute(Integer result){
            switch (result){
                case 1:
                    if(lists.size()==0){
                        Toast.makeText(ClassDetails.this,"还没有学生加入该课堂！",
                                Toast.LENGTH_SHORT).show();
                    }
                    else{
                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(
                                ClassDetails.this,android.R.layout.simple_list_item_1,lists);
                        stuInfo.setAdapter(adapter);
                    }
                    break;
                case -1:
                    Toast.makeText(ClassDetails.this,"无法连接网络！",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

}
