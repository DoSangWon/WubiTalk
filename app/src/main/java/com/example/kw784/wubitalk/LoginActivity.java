package com.example.kw784.wubitalk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by kw784 on 2016-05-28.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    String id ;
    String pw ;
    EditText eid;
    EditText epw;
    Button login;
    Button join;
    CheckBox autochk;
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
         eid = (EditText) findViewById(R.id.etUserName);
         epw = (EditText) findViewById(R.id.etPass);
         login = (Button) findViewById(R.id.btnSingIn);
         join = (Button) findViewById(R.id.btnjoin);
         autochk = (CheckBox) findViewById(R.id.atlogin);
        setting = getSharedPreferences("setting", 0);//세팅이라는 이름의 설정리스트 생성
        editor = setting.edit();
        /*자동로그인 구현부분. 자동로그인 체크박스가 해제되어있으면, 체크한다.*/
        if(setting.getBoolean("autochk",false)){
            eid.setText(setting.getString("ID",""));
            epw.setText(setting.getString("PW",""));
            autochk.setChecked(true);

        }

        login.setOnClickListener( this);
        join.setOnClickListener( this);

    }

        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btnSingIn:
                   if(autochk.isChecked()) {
                       id = eid.getText().toString();
                       pw = epw.getText().toString();
                       editor.putString("ID", id);
                       editor.putString("PW", pw);
                       editor.putBoolean("autochk",true);
                       editor.commit();
                   }else{
                       id = eid.getText().toString();
                       pw = epw.getText().toString();
                       editor.clear();
                       editor.commit();
                   }
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("id", id);
                    intent.putExtra("pw", pw);
                    startActivity(intent);
                    finish();
                    break;
                case R.id.btnjoin:
                    Intent jointent = new Intent(LoginActivity.this,CreateAccount.class);
                    startActivity(jointent);
                    break;
            }
        }
    }


