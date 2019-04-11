package com.tizz.signin.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBUtils extends SQLiteOpenHelper {

    public static final String createTeaClassTable="create table TeacherClass(" +
            "className text," +
            "timeLimit text)";

    public static final String createStudentClassTable="create table StudentClass(" +
            "className text," +
            "teacherName text)";

    public static final String createStudentSigninTable="create table StudentSignin(" +
            "className text," +
            "signinTime text)";

    public DBUtils(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(createStudentClassTable);
        db.execSQL(createStudentSigninTable);
        db.execSQL(createTeaClassTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
    }

    public void setNewTable(SQLiteDatabase db){
        db.execSQL("drop table if exists TeacherClass");
        db.execSQL("drop table if exists StudentClass");
        db.execSQL("drop table if exists StudentSignin");
        onCreate(db);
    }
}
