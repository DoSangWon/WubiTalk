package com.example.kw784.wubitalk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sinn5 on 2016-06-04.
 * DB에 액세스하고 생성여부 및 버젼정보를 파악하여 테이블들을 생성해주는 클래스입니다.
 */
public class DbOpenHelper extends SQLiteOpenHelper {
    public DbOpenHelper(Context context) {
        super(context, "wubiDB", null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DataBases.CreateDB._CREATE_LOG);
        db.execSQL(DataBases.CreateDB._CREATE_LIST);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS chatlog;");
        db.execSQL("DROP TABLE IF EXISTS chatlist;");
        onCreate(db);
    }
}