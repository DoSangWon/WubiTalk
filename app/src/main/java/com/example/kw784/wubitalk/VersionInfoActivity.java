package com.example.kw784.wubitalk;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by sinn5 on 2016-06-04.
 * 버젼 정보를 표시하는 엑티비티의 클래스입니다.
 */
public class VersionInfoActivity extends ActionBarActivity {

    TextView title;

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version_info);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = (TextView) findViewById(R.id.mytext);

        title.setText("버전 정보");
    }
}
