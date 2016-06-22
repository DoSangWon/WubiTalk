package com.example.kw784.wubitalk;

import android.provider.BaseColumns;

/**
 * Created by sinn5 on 2016-06-04.
 * 데이터베이스 생성 시 테이블 생성에 대한 쿼리를 정의해놓은 클래스입니다.
 */
public final class DataBases {

    public static final class CreateDB implements BaseColumns {
        public static final String FROMID = "fromid";
        public static final String MESSAGE = "message";
        public static final String _TABLENAME_LOG = "chatlog";
        public static final String _TABLENAME_LIST = "chatlist";
        public static final String _CREATE_LOG =
                "create table "+_TABLENAME_LOG+"("
                        +"ID integer primary key autoincrement, "
                        +"direction integer not null, "
                        +FROMID+" text not null , "
                        +MESSAGE+" text not null);";
        // direction : 송수신 구분, 0:송신 / 1:수신

        public static final String _CREATE_LIST =
                "create table "+_TABLENAME_LIST+"("
                        +FROMID+" text primary key, "
                        +MESSAGE+" text not null);";
    }
}
