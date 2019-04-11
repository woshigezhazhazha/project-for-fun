package com.tizz.signin.activity;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.R;
import com.tizz.signin.utils.App;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class TeacherRegister extends AppCompatActivity implements View.OnClickListener {

    private EditText name;
    private EditText psw;
    private EditText repsw;
    private Button reg;
    private LinearLayout back;
    private String uName;
    private String uPsw;
    private String uRePsw;
    private TextView title;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private DBUtils dbUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_register);
        App.getInstance().addActivity(this);
        initView();
    }

    private void initView(){
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("教师注册");
        name=(EditText)findViewById(R.id.et_name);
        psw=(EditText)findViewById(R.id.et_psw);
        repsw=(EditText)findViewById(R.id.et_repsw);
        reg=(Button)findViewById(R.id.btn_reg);
        reg.setOnClickListener(this);
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_reg:
                if(inputHasNull()){
                    Toast.makeText(TeacherRegister.this,"先完善注册信息！",Toast.LENGTH_SHORT)
                            .show();
                }
                else{
                    if(pswNotSame()){
                        Toast.makeText(TeacherRegister.this, "前后密码不一致！", Toast.LENGTH_SHORT)
                                .show();
                    }
                    else{
                        new RegTask().execute();
                    }
                }
                break;
            case R.id.ll_back:
                Intent intent=new Intent();
                TeacherRegister.this.setResult(RESULT_CANCELED,intent);
                TeacherRegister.this.finish();
                break;
        }
    }

    private boolean inputHasNull(){
        uName=name.getText().toString();
        uPsw=psw.getText().toString();
        uRePsw=repsw.getText().toString();
        if(uName.equals("")||uPsw.equals("")||uRePsw.equals(""))
            return true;
        return false;
    }

    private boolean pswNotSame(){
        if(uRePsw.equals(uPsw))
            return false;
        return true;
    }

    class RegTask extends AsyncTask<Void,Void,Integer> {

        ProgressDialogUtils pd=new ProgressDialogUtils();

        public void onPreExecute(){
            pd.showProgressDialog(TeacherRegister.this,"注册","注册中");
        }

        @Override
        public Integer doInBackground(Void ... params){
            try{
                Socket socket=new Socket(SocketUtils.ip,6000);
                socket.setSoTimeout(5*1000);
                if(socket==null)
                    return -1;
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                outputStream.writeUTF("teacherRegister");
                outputStream.writeUTF(uName);
                outputStream.writeUTF(uPsw);
                outputStream.writeUTF(uRePsw);
                String result=inputStream.readUTF();
                if(result.equals("succed")){
                    int userid=inputStream.readInt();
                    SharedPreferences.Editor editor=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
                    editor.putString("name",uName);
                    editor.putString("psw",uPsw);
                    editor.putInt("userid",userid);
                    editor.putBoolean("isStudent",false);
                    editor.putBoolean("isLogined",false);
                    editor.commit();

                    //create sqlite table
                    dbUtils=new DBUtils(TeacherRegister.this,"userInfo.db",null,2);
                    SQLiteDatabase db=dbUtils.getWritableDatabase();
                    dbUtils.setNewTable(db);
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
                    Intent intent=new Intent();
                    intent.putExtra("name",uName);
                    TeacherRegister.this.setResult(RESULT_OK,intent);
                    TeacherRegister.this.finish();
                    break;
                case -1:
                    Toast.makeText(TeacherRegister.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(TeacherRegister.this,"注册失败！",Toast.LENGTH_SHORT).show();
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
