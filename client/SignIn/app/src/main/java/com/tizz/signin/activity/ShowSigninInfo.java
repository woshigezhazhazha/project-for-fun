package com.tizz.signin.activity;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tizz.signin.SpAdapter;
import com.tizz.signin.R;
import com.tizz.signin.utils.ArraylistUtils;
import com.tizz.signin.utils.DBUtils;
import com.tizz.signin.utils.ProgressDialogUtils;
import com.tizz.signin.utils.SocketUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ShowSigninInfo extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout back;
    private TextView title;
    private Button check;
    private ListView checkList;
    private Spinner checkType;
    private Spinner checkClass;
    private String stringKind;
    private String stringClass;
    private ArrayList<String> kindList=new ArrayList<>();
    private ArrayList<String> classList=new ArrayList<>();
    private ArrayList<String> infoShow=new ArrayList<>();
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    //for sql
    private int year;
    private int month;
    private int day;
    private String major;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_signin_info);
        initView();
    }

    private void initView(){
        back=(LinearLayout)findViewById(R.id.ll_back);
        back.setOnClickListener(this);
        check=(Button)findViewById(R.id.btn_check);
        check.setOnClickListener(this);
        title=(TextView)findViewById(R.id.tv_common);
        title.setText("签到记录");
        checkList=(ListView)findViewById(R.id.lv_checkList);
        checkClass=(Spinner)findViewById(R.id.sp_checkClass);
        checkType=(Spinner)findViewById(R.id.sp_checkKind);
        initClaaAdapter();
        checkClass();
        initKindAdapter();
        checkClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stringClass=parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        checkType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stringKind=parent.getItemAtPosition(position).toString();

                if(stringKind.equals("学院查询")){

                    Spinner spinner=new Spinner(ShowSigninInfo.this);
                    ArrayList<String> majors=new ArrayList<>();
                    majors= ArraylistUtils.getArrayList();
                    SpAdapter majorAdapter=new SpAdapter(ShowSigninInfo.this,
                            R.layout.support_simple_spinner_dropdown_item,majors);
                    spinner.setAdapter(majorAdapter);
                    spinner.setSelection(majors.size()-1,true);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            major=parent.getItemAtPosition(position).toString();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    final AlertDialog alertDialog=new AlertDialog.Builder(ShowSigninInfo.this)
                            .setTitle("选择专业")
                            .setView(spinner)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(major==null){
                                        Toast.makeText(ShowSigninInfo.this,"请选择专业！",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    alertDialog.show();


                }
                else if(stringKind.equals("时间查询")){
                    final DatePicker datePicker=new DatePicker(ShowSigninInfo.this);
                    datePicker.setMaxDate(System.currentTimeMillis());
                    final AlertDialog alertDialog=new AlertDialog.Builder(ShowSigninInfo.this)
                            .setTitle("选择时间")
                            .setView(datePicker)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    year=datePicker.getYear();
                                    month=datePicker.getMonth()+1;
                                    day=datePicker.getDayOfMonth();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initClaaAdapter(){
        DBUtils dbUtils=new DBUtils(ShowSigninInfo.this,"userInfo.db",null,2);
        SQLiteDatabase db=dbUtils.getWritableDatabase();
        Cursor cursor=db.query("TeacherClass",
                null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String name=cursor.getString(cursor.getColumnIndex("className"));
                classList.add(name);
            }while (cursor.moveToNext());
        }
        cursor.close();
        classList.add("选择课堂");
        SpAdapter classAdapter=new SpAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,classList);
        checkClass.setAdapter(classAdapter);
        checkClass.setSelection(classList.size()-1,true);
    }

    private void initKindAdapter(){
        kindList.add("详细信息");
        kindList.add("学生汇总");
        kindList.add("学院查询");
        kindList.add("时间查询");
        kindList.add("查询方式");
        SpAdapter kindAdapter=new SpAdapter(this,
                R.layout.support_simple_spinner_dropdown_item,kindList);
        checkType.setAdapter(kindAdapter);
        checkType.setSelection(kindList.size()-1,true);
    }

    private void checkClass(){
        if(classList.size()==1){
            Toast.makeText(ShowSigninInfo.this,"尚无课堂信息！",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void clearListView(){
        //clear old data
        if(infoShow.size()!=0){
            infoShow.clear();
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<>(
                ShowSigninInfo.this,android.R.layout.simple_list_item_1,infoShow
        );
        checkList.setAdapter(adapter);
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.ll_back:
                ShowSigninInfo.this.finish();
                break;
            case R.id.btn_check:
                clearListView();
                //check if the input info is complete
                if(!spinnerHasInfo()){
                    Toast.makeText(ShowSigninInfo.this,"请先完善查询条件!",
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                new ShowSigninInfoTask().execute();
                break;
        }
    }

    public boolean spinnerHasInfo(){
        if(stringClass!=null && stringKind!=null)
            return true;
        return false;
    }

    class ShowSigninInfoTask extends AsyncTask<Void,Void,Integer> {

        ProgressDialogUtils pd=new ProgressDialogUtils();

        @Override
        public void onPreExecute(){
            pd.showProgressDialog(ShowSigninInfo.this,"查询","查询中");
        }

        @Override
        public Integer doInBackground(Void ... Params){
            try{
                socket=new Socket(SocketUtils.ip,SocketUtils.port);
                socket.setSoTimeout(5*1000);
                if(socket==null){
                    return -1;
                }
                inputStream=new DataInputStream(socket.getInputStream());
                outputStream=new DataOutputStream(socket.getOutputStream());
                if(stringKind.equals("学生汇总")){
                    outputStream.writeUTF("checkStudent");
                    outputStream.writeUTF(stringClass);
                    String result=inputStream.readUTF();
                    while(!result.equals("###the sigin info for all students is over!!!")){
                        infoShow.add(result);
                        result=inputStream.readUTF();
                    }
                    if(infoShow.size()==0)
                        return -7;
                    else
                        return 4;
                }
                else if(stringKind.equals("详细信息")){
                    outputStream.writeUTF("checkAllInfo");
                    outputStream.writeUTF(stringClass);
                    String result=inputStream.readUTF();
                    while(!result.equals("###the sigin info for all info is over!!!")){
                        infoShow.add(result);
                        result=inputStream.readUTF();
                    }
                    if(infoShow.size()==0)
                        return -4;
                    else
                        return 1;
                }
                else if(stringKind.equals("学院查询")){
                    if(major!=null){
                        outputStream.writeUTF("checkMajor");
                        outputStream.writeUTF(stringClass);
                        outputStream.writeUTF(major);
                        String result=inputStream.readUTF();
                        while(!result.equals("###the sigin info for this major is over!!!")){
                            infoShow.add(result);
                            result=inputStream.readUTF();
                        }
                        if(infoShow.size()==0)
                            return -5;
                        else
                            return 2;
                    }
                    else
                        return -3;
                }
                else if(stringKind.equals("时间查询")){
                    outputStream.writeUTF("checkTime");
                    outputStream.writeUTF(stringClass);
                    outputStream.writeInt(year);
                    outputStream.writeInt(month);
                    outputStream.writeInt(day);
                    String result=inputStream.readUTF();
                    while(!result.equals("###the sigin info for this time is over!!!")){
                        infoShow.add(result);
                        result=inputStream.readUTF();
                    }
                    if(infoShow.size()==0)
                        return -6;
                    else
                        return 3;
                }
                socket.close();
                outputStream.close();
                inputStream.close();
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
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(
                            ShowSigninInfo.this,android.R.layout.simple_list_item_1,infoShow
                    );
                    checkList.setAdapter(adapter);
                    break;
                case 2:
                    ArrayAdapter<String> adapter2=new ArrayAdapter<>(
                            ShowSigninInfo.this,android.R.layout.simple_list_item_1,infoShow
                    );
                    checkList.setAdapter(adapter2);
                    break;
                case 3:
                    ArrayAdapter<String> adapter3=new ArrayAdapter<>(
                            ShowSigninInfo.this,android.R.layout.simple_list_item_1,infoShow
                    );
                    checkList.setAdapter(adapter3);
                    break;
                case 4:
                    ArrayAdapter<String> adapter4=new ArrayAdapter<>(
                            ShowSigninInfo.this,android.R.layout.simple_list_item_1,infoShow
                    );
                    checkList.setAdapter(adapter4);
                    break;
                case -1:
                    Toast.makeText(ShowSigninInfo.this,"无法连接网络!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    Toast.makeText(ShowSigninInfo.this,"没有选择学院!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -4:
                    Toast.makeText(ShowSigninInfo.this,"该课堂暂无签到信息!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -5:
                    Toast.makeText(ShowSigninInfo.this,"没有该学院的签到信息!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -6:
                    Toast.makeText(ShowSigninInfo.this,"没有该时间的签到信息!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case -7:
                    Toast.makeText(ShowSigninInfo.this,"该课堂暂无签到信息!",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
