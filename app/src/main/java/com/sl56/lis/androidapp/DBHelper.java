package com.sl56.lis.androidapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Josion on 2017/3/21.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String name = "lis"; //数据库名称
    private static final int version = 1; //数据库版本
    public DBHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Meta (key varchar(64) , value varchar(64))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }
    //查询整张表数据
    public Cursor Fetch(String criteria) throws Exception{
            String sqlStr = "SELECT * FROM Meta WHERE key = '" + criteria+"'";
            return (getReadableDatabase().rawQuery(sqlStr, null));
    }
    public void Inert(String key,String value){
        ContentValues cv = new ContentValues();
        cv.put("key",key);
        cv.put("value",value);
        getWritableDatabase().insert("Meta",null,cv);
    }
    public void Update(String key,String value){
        String sql = "update Meta set value = '"+value+"' where key = '"+key+"'";
        getWritableDatabase().execSQL(sql);
    }
}
