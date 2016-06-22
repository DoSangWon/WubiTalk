package com.example.kw784.wubitalk;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by kw784 on 2016-05-27.
 */
public class LogoActivity extends Activity{
    SharedPreferences setting;
    SharedPreferences.Editor editor;
    String id;
    String pw;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();


        boolean isRunIntro = getIntent().getBooleanExtra("intro", true);
        if(isRunIntro) {
            beforeIntro();
        } else {
            afterIntro(savedInstanceState);
        }


    }
    private void beforeIntro() {
        // 약 2초간 인트로 화면을 출력.
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(setting.getBoolean("autochk",false)) {
                    id = setting.getString("ID", "");
                    pw = setting.getString("PW", "");
                    Intent autoIntent = new Intent(LogoActivity.this,MainActivity.class);
                    autoIntent.putExtra("id",id);
                    autoIntent.putExtra("pw",pw);
                    startActivity(autoIntent);

                }else {
                    Intent intent = new Intent(LogoActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("intro", false);
                    startActivity(intent);
                }



                    // 액티비티 이동시 페이드인/아웃 효과를 보여준다. 즉, 인트로
                //    화면에 부드럽게 사라진다.
                overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                finish();
            }
        }, 2000);
    }

    // 인트로 화면 이후.
    private void afterIntro(Bundle savedInstanceState) {
        // 기본 테마를 지정한다.
        setTheme(R.style.IntroTheme);
        setContentView(R.layout.activity_login);
    }

}
