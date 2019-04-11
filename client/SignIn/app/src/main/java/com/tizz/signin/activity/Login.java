package com.tizz.signin.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
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
import com.tizz.signin.utils.ProgressDialogUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Login extends AppCompatActivity implements View.OnClickListener{

    private EditText et1;
    private EditText et2;
    private Button yes;
    private TextView reg;
    private String name,psw;
    private int isStu=-1;
    private AlertDialog alertDialog;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private static boolean isStudent;
    private LinearLayout back;
    private TextView title;
    private boolean isLogined;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        App.getInstance().addActivity(this);
        initView();
    }

    private void initView(){
        reg=(TextView)findViewById(R.id.textView);
        reg.setOnClickListener(this);
        yes=(Button)findViewById(R.id.button);
        yes.setOnClickListener(this);
        et1=(EditText)findViewById(R.id.editText);
        et2=(EditText)findViewById(R.id.editText2);
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("登录");
    }

    @Override
    public void onClick(View view){
        if(view==back){
            Login.this.finish();
        }
        else if(view==reg){
            showSingleAlertDialog(view);
        }
        else if(view==yes){
            if(inputHasNull()){
                Toast.makeText(Login.this, "先完善登录信息！", Toast.LENGTH_SHORT).show();
            }
            else{
                new LoginTask().execute();
            }
        }
    }

    private boolean inputHasNull(){
        name=et1.getText().toString();
        psw=et2.getText().toString();
        if(name.equals("") || psw.equals(""))
            return true;
        return false;
    }

    public void showSingleAlertDialog(View view){
        isStu=-1;
        final String items[]={"学生","老师"};
        AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择身份");
        alertBuilder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                    isStu=0;
                else if(which==1)
                    isStu=1;
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(isStu==0){
                    alertDialog.dismiss();
                    Intent intent=new Intent(Login.this, StuRegister.class);
                    startActivityForResult(intent,100);
                }
                else if(isStu==1){
                    alertDialog.dismiss();
                    Intent intent=new Intent(Login.this, TeacherRegister.class);
                    startActivityForResult(intent,100);
                }
                else if(isStu==-1){
                    alertDialog.show();
                    Toast.makeText(Login.this,"请先选择身份！",Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        alertDialog=alertBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        switch (resultCode){
            case RESULT_CANCELED:
                break;
            case RESULT_OK:
                String data=intent.getExtras().getString("name" );
                et1.setText(data);
                Toast.makeText(Login.this,"注册成功,请登录！",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    class LoginTask extends AsyncTask<Void,Void,Integer>{

        ProgressDialogUtils pd=new ProgressDialogUtils();

        public void onPreExecute(){
            pd.showProgressDialog(Login.this,"登录","登录中");
        }

        @Override
        public Integer doInBackground(Void... params){
            SharedPreferences sharedPreferences=getSharedPreferences("userInfo",MODE_PRIVATE);
            isLogined=sharedPreferences.getBoolean("isLogined",false);
            if(isLogined)
                return -3;
            String spname=sharedPreferences.getString("name","");
            String sppsw=sharedPreferences.getString("psw","");
            //get the user's identification
            isStudent=sharedPreferences.getBoolean("isStudent",true);
            if(!spname.equals("") && spname.equals(name) && sppsw.equals(psw)){
                SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                editor.putBoolean("isLogined",true);
                editor.commit();
                return 1;
            }
            else if(!spname.equals(name))
                return -1;
            else if(!sppsw.equals(psw))
                return -2;
            else
                return 0;
        }

        @Override
        public void onPostExecute(Integer result){
            pd.finishProgressDialog();
            switch (result){
                case 1:
                    Intent intent=new Intent(Login.this,MainActivity.class);
                    intent.putExtra("isStudent",isStudent);
                    startActivity(intent);
                    break;
                case -1:
                    Toast.makeText(Login.this,"用户名错误！",Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(Login.this,"密码错误！",Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    Toast.makeText(Login.this,"你已登录！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(Login.this,"登录失败！",Toast.LENGTH_SHORT).show();
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
