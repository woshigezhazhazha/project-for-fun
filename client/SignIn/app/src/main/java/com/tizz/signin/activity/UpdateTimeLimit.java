package com.tizz.signin.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.R;
import com.tizz.signin.SpAdapter;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class UpdateTimeLimit extends AppCompatActivity implements View.OnClickListener {

    private Spinner showTimeLimit;
    private TextView oldTimeLimit;
    private Button update;
    private LinearLayout back;
    private TextView title;
    private ArrayList<String> timeLimits=new ArrayList();
    private String limit;
    private String  className;
    private String timeLimit;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_time_limit);
        initView();
    }

    private void initView(){
        Intent intent=getIntent();
        className=intent.getStringExtra("className");
        timeLimit=intent.getStringExtra("timeLimit");

        oldTimeLimit=(TextView)findViewById(R.id.tv_oldTimeLimit);
        oldTimeLimit.setText("当前签到时限为"+timeLimit+"分钟");

        update=(Button)findViewById(R.id.btn_updateTimeLimit);
        update.setOnClickListener(this);

        title=(TextView)findViewById(R.id.tv_common);
        title.setText("修改签到时限");

        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);

        showTimeLimit=(Spinner)findViewById(R.id.sp_newTimeLimit);
        initAdapter();

        showTimeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                limit=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.btn_updateTimeLimit:
                if(limit==null){
                    Toast.makeText(this,"先选择新的签到时限！",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(limit.equals(timeLimit)){
                    Toast.makeText(this,"你选择的新签到时限与旧值相同！",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                else{
                    new updateTime().execute();
                }
                break;
            case R.id.ll_back:
                UpdateTimeLimit.this.finish();
                break;
        }
    }

    private void initAdapter(){
        timeLimits.add("5分钟");
        timeLimits.add("10分钟");
        timeLimits.add("15分钟");
        timeLimits.add("20分钟");
        timeLimits.add("签到时长");

        SpAdapter classTimeAdapter=new SpAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,timeLimits);
        showTimeLimit.setAdapter(classTimeAdapter);
        showTimeLimit.setSelection(timeLimits.size()-1,true);
    }

    private int getTimeLimit(){
        if(limit.equals("5分钟"))
            return 5;
        else if(limit.equals("10分钟"))
            return 10;
        else if(limit.equals("20分钟"))
            return 20;
        else if(limit.equals("15分钟"))
            return 15;
        return 20;
    }

    private boolean classTimeChoosen(){
        if(limit==null)
            return false;
        return true;
    }

    class updateTime extends AsyncTask<Void,Void,Integer>{
        ProgressDialogUtils pd=new ProgressDialogUtils();

        @Override
        public void onPreExecute(){
            pd.showProgressDialog(UpdateTimeLimit.this,"修改时限","更新中");
        }

        @Override
        public Integer doInBackground(Void ... Params){
            try{
                socket=new Socket(SocketUtils.ip,SocketUtils.port);
                socket.setSoTimeout(5*1000);
                if(socket==null){
                    return -1;
                }

                int time=getTimeLimit();
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());

                outputStream.writeUTF("updateTimeLimit");
                outputStream.writeUTF(className);
                outputStream.writeInt(time);

                int result=inputStream.readInt();
                if(result==-1){
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                    return 0;
                }
                else{

                    DBUtils dbUtils=new DBUtils(UpdateTimeLimit.this,
                            "userInfo.db",null,2);
                    SQLiteDatabase db=dbUtils.getWritableDatabase();
                    ContentValues values=new ContentValues();
                    values.put("timeLimit",limit);
                    db.update("TeacherClass",values,"className=?",
                            new String[]{className});

                    socket.close();
                    inputStream.close();
                    outputStream.close();
                    return 1;
                }
            }catch (UnknownHostException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
            return 0;
        }

        @Override
        public void onPostExecute(Integer result){
            pd.finishProgressDialog();
            switch (result){
                case 1:
                    timeLimit=limit;
                    oldTimeLimit.setText("当前签到时限为"+timeLimit);
                    Toast.makeText(UpdateTimeLimit.this,"修改成功！",Toast.LENGTH_SHORT).show();
                    //update class listView
                    break;
                case -1:
                    Toast.makeText(UpdateTimeLimit.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(UpdateTimeLimit.this,"修改失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
