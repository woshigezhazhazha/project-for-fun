package com.tizz.signin.activity;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.SpAdapter;
import com.tizz.signin.R;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SetClass extends AppCompatActivity implements View.OnClickListener {

    private Button setClass;
    private EditText className;
    private TextView title;
    private LinearLayout back;
    private String classname;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Spinner classTime;
    private String timeLimit=null;
    private ArrayList<String> timeLimits=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_class);
        initView();
    }

    private void initView(){
        setClass=(Button)findViewById(R.id.btn_setClass);
        setClass.setOnClickListener(this);
        className=(EditText)findViewById(R.id.et_className);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("新建课堂");
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        classTime=(Spinner)findViewById(R.id.sp_classTime);
        initAdapter();
        classTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timeLimit=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initAdapter(){
        timeLimits.add("5分钟");
        timeLimits.add("10分钟");
        timeLimits.add("15分钟");
        timeLimits.add("20分钟");
        timeLimits.add("签到时长");

        SpAdapter classTimeAdapter=new SpAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,timeLimits);
        classTime.setAdapter(classTimeAdapter);
        classTime.setSelection(timeLimits.size()-1,true);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_setClass:
                classname=className.getText().toString();
                if(classname.equals("") || !classTimeChoosen()){
                    Toast.makeText(SetClass.this,"先完善课堂信息!",Toast.LENGTH_SHORT)
                            .show();
                    break;
                }
                else{
                     new SetClassTask().execute();
                }

                break;
            case R.id.ll_back:
                SetClass.this.finish();
                break;
        }
    }

    private boolean classTimeChoosen(){
        if(timeLimit==null)
            return false;
        return true;
    }

    private int getTimeLimit(){
        if(timeLimit.equals("5分钟"))
            return 5;
        else if(timeLimit.equals("10分钟"))
            return 10;
        else if(timeLimit.equals("20分钟"))
            return 20;
        else if(timeLimit.equals("15分钟"))
            return 15;
        return 20;
    }

    class SetClassTask extends AsyncTask<Void,Void,Integer>{

        ProgressDialogUtils pd=new ProgressDialogUtils();

        @Override
        public void onPreExecute(){
            pd.showProgressDialog(SetClass.this,"新建课堂","新建中");
        }

        @Override
        public Integer doInBackground(Void ... Params){
            try{
                socket=new Socket(SocketUtils.ip,SocketUtils.port);
                socket.setSoTimeout(5*1000);
                if(socket==null){
                    return -1;
                }
                SharedPreferences sp=getSharedPreferences("userInfo",MODE_PRIVATE);
                int teacherNum=sp.getInt("userid",1);
                int limit=getTimeLimit();
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF("setClass");
                outputStream.writeUTF(classname);
                outputStream.writeInt(teacherNum);
                outputStream.writeInt(limit);
                int result=inputStream.readInt();
                if(result==-1){
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                    return 0;
                }
                else if(result==-2){
                    socket.close();
                    inputStream.close();
                    outputStream.close();
                    return -2;
                }
                else{
                    int classnum=inputStream.readInt();
                    //add this class to the phone sqlite
                    DBUtils dbUtils=new DBUtils(SetClass.this,
                            "userInfo.db",null,2);
                    SQLiteDatabase db=dbUtils.getWritableDatabase();
                    db.execSQL("insert into TeacherClass(className,timeLimit) values(?,?)",
                            new String[]{classname,String.valueOf(limit)});
                    //set this class for the first choice
                    SharedPreferences.Editor editor=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
                    editor.putString("firstClass",classname);
                    editor.commit();
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
                    Toast.makeText(SetClass.this,"新建课堂成功！",Toast.LENGTH_SHORT).show();
                    //update class listView
                    break;
                case -1:
                    Toast.makeText(SetClass.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(SetClass.this,"新建课堂失败！",Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(SetClass.this,"该课堂名称已存在，请更改课堂名称后重试！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN  &&
                getCurrentFocus()!=null &&
                getCurrentFocus().getWindowToken()!=null) {

            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, event)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] location = {0, 0};
            v.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getRawX() > left && event.getRawX() < right
                    && event.getRawY() > top && event.getRawY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
