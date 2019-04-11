package com.tizz.signin.activity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.tizz.signin.MajorAdapter;
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

public class StuRegister extends AppCompatActivity implements View.OnClickListener {

    private EditText userName;
    private EditText stuID;
    private EditText userPsw;
    private EditText userRepsw;
    private TextView title;
    private String name,psw,repsw,stuid;
    private Button yes;
    private LinearLayout back;
    private Spinner spMajor;
    private String major=null;
    private ArrayList<String> majors=new ArrayList<>();
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private DBUtils dbUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stu_register);
        initView();
    }

    private void initView(){
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("学生注册");
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        userName=(EditText) findViewById(R.id.et_name);
        stuID=(EditText) findViewById(R.id.et_idnum);
        userPsw=(EditText) findViewById(R.id.et_psw);
        userRepsw=(EditText)findViewById(R.id.et_repsw);
        yes=(Button)findViewById(R.id.bt_register);
        yes.setOnClickListener(this);
        spMajor=(Spinner)findViewById(R.id.sp_major);
        initAdapter();
        spMajor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                major=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public void onClick(View view){
        if(view==yes){
            if(inputHasNull() || !majorChoosen()){
                Toast.makeText(StuRegister.this, "先完善注册信息！", Toast.LENGTH_SHORT).show();
            }
            else{
                if(pswNotSame()){
                    Toast.makeText(StuRegister.this, "前后密码不一致！", Toast.LENGTH_SHORT).show();
                }
                else{
                    new RegTask().execute();
                }
            }
        }
        else if(view==back){
            Intent intent=new Intent();
            StuRegister.this.setResult(RESULT_CANCELED,intent);
            StuRegister.this.finish();
        }
    }


    private boolean inputHasNull(){
        name=userName.getText().toString();
        psw=userPsw.getText().toString();
        repsw=userRepsw.getText().toString();
        stuid=stuID.getText().toString();
        if(name.equals("") || psw.equals("")  || repsw.equals("") ||stuid.equals(""))
            return true;
        return false;
    }

    private boolean pswNotSame(){
        if(psw.equals(repsw))
            return false;
        return true;
    }

    private boolean majorChoosen(){
        if(major==null)
            return false;
        return true;
    }

    class RegTask extends AsyncTask<Void,Void,Integer> {

        ProgressDialogUtils pd=new ProgressDialogUtils();

        public void onPreExecute(){
            pd.showProgressDialog(StuRegister.this,"注册","注册中");
        }

        @Override
        public Integer doInBackground(Void... params){
            try{
                Socket socket=new Socket(SocketUtils.ip,6000);
                socket.setSoTimeout(5*1000);
                if(socket==null)
                    return -1;
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF("studentRegister");
                outputStream.writeUTF(name);
                outputStream.writeUTF(psw);
                outputStream.writeUTF(stuid);
                outputStream.writeUTF(major);
                String result=inputStream.readUTF();
                if(result.equals("succed")){
                    int userid=inputStream.readInt();
                    SharedPreferences.Editor editor=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
                    editor.putString("name",name);
                    editor.putString("psw",psw);
                    editor.putString("stuid",stuid);
                    editor.putString("major",major);
                    editor.putInt("userid",userid);
                    editor.putBoolean("isStudent",true);
                    editor.putBoolean("isLogined",false);
                    editor.commit();

                    //create sqlite table
                    dbUtils=new DBUtils(StuRegister.this,"userInfo.db",null,2);
                    dbUtils.getWritableDatabase();
                    return 1;
                }
                socket.close();
                inputStream.close();
                outputStream.close();
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
                    Intent intent=new Intent(StuRegister.this,Login.class);
                    intent.putExtra("name",name);
                    StuRegister.this.setResult(RESULT_OK,intent);
                    StuRegister.this.finish();
                    break;
                case -1:
                    Toast.makeText(StuRegister.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(StuRegister.this,"注册失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void initAdapter(){
        majors.add("哲学");
        majors.add("经济学");
        majors.add("法学");
        majors.add("教育学");
        majors.add("文学");
        majors.add("历史学");
        majors.add("理学");
        majors.add("工学");
        majors.add("农学");
        majors.add("医学");
        majors.add("军事学");
        majors.add("管理学");
        majors.add("艺术学");
        majors.add("院系专业");

        MajorAdapter majorAdapter=new MajorAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,majors);
        spMajor.setAdapter(majorAdapter);
        spMajor.setSelection(majors.size()-1,true);
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
