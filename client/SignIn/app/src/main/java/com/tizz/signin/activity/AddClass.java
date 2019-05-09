package com.tizz.signin.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class AddClass extends AppCompatActivity implements View.OnClickListener {

    private EditText className;
    private Button addClass;
    private LinearLayout back;
    private TextView title;
    private String classname;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private ProgressDialogUtils pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        initView();
    }

    private void initView(){
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("添加课堂");
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        className=(EditText)findViewById(R.id.et_className);
        addClass=(Button)findViewById(R.id.btn_addClass);
        addClass.setOnClickListener(this);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                AddClass.this.finish();
                break;
            case R.id.btn_addClass:
                classname=className.getText().toString();
                if(classname.equals("")){
                    Toast.makeText(AddClass.this,"输入课堂名称!",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                new AddClassTask().execute();
                break;
        }
    }

    class AddClassTask extends AsyncTask<Void,Void,Integer>{

        @Override
        public void onPreExecute(){
            pd=new ProgressDialogUtils();
            pd.showProgressDialog(AddClass.this,"添加课堂","添加中");
        }

        @Override
        public Integer doInBackground(Void ... params){
             try{
                 Socket socket=new Socket(SocketUtils.ip,SocketUtils.port);
                 socket.setSoTimeout(5*5000);
                 if(socket==null)
                     return -1;

                 SharedPreferences sharedPreferences=getSharedPreferences("userInfo",MODE_PRIVATE);
                 String name=sharedPreferences.getString("name","");
                 String major=sharedPreferences.getString("major","");
                 String stuid=sharedPreferences.getString("stuid","");

                 inputStream=new DataInputStream(socket.getInputStream());
                 outputStream=new DataOutputStream(socket.getOutputStream());

                 outputStream.writeUTF("addClass");
                 outputStream.writeUTF(classname);
                 outputStream.writeUTF(name);
                 outputStream.writeUTF(stuid);
                 outputStream.writeUTF(major);

                 int result=inputStream.readInt();
                 if(result==1){
                     //add this class to the phone sqlite
                     DBUtils dbUtils=new DBUtils(AddClass.this,
                             "userInfo.db",null,2);
                     SQLiteDatabase db=dbUtils.getWritableDatabase();
                     Cursor cursor=db.rawQuery("select * from StudentClass where className=?",
                             new String[]{classname});
                     if(!cursor.moveToFirst()){
                         db.execSQL("insert into StudentClass(className) values(?)",
                                 new String[]{classname});
                         //set this class for the first class
                         SharedPreferences.Editor editor=getSharedPreferences("userInfo",MODE_PRIVATE).edit();
                         editor.putString("firstClass",classname);
                         editor.commit();
                     }
                 }

                 socket.close();
                 inputStream.close();
                 outputStream.close();
                 return result;

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
                    Toast.makeText(AddClass.this,"添加课堂成功！",Toast.LENGTH_SHORT).show();
                    //update class listView
                    break;
                case -1:
                    Toast.makeText(AddClass.this,"无法连接网络！",Toast.LENGTH_SHORT).show();
                    break;
                case 0:
                    Toast.makeText(AddClass.this,"添加课堂失败！",Toast.LENGTH_SHORT).show();
                    break;
                case -5:
                    Toast.makeText(AddClass.this,"不存在该课堂！",Toast.LENGTH_SHORT).show();
                    break;
                case -10:
                    Toast.makeText(AddClass.this,"你已添加该课堂！",Toast.LENGTH_SHORT).show();
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
