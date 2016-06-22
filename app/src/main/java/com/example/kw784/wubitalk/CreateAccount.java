package com.example.kw784.wubitalk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kw784 on 2016-05-28.
 */
public class CreateAccount extends Activity implements View.OnClickListener {
    EditText eid ;
    EditText epw;
    EditText ecpw;
    Button join;
    String HOST = "sangwon.iptime.org";
    int PORT = 5222;

    XMPPTCPConnection connection;

    String id;
    String pw;
    String cpw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);
        eid = (EditText) findViewById(R.id.joinID);
        epw = (EditText) findViewById(R.id.joinPW);
        ecpw = (EditText) findViewById(R.id.confirmjoinPW);
        join = (Button) findViewById(R.id.btnSingIn);
        join.setOnClickListener (this);


    }
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnSingIn:
                id = eid.getText().toString();
                pw = epw.getText().toString();
                cpw = ecpw.getText().toString();
                //아이디 중복체크, 비밀번호 일치하는지를 체크하는 소스코드 작성해야 함.
                if(pw.equals(cpw)) {
                    create();
                    Toast.makeText(CreateAccount.this, id+"계정이 생성 되었습니다!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    Toast.makeText(CreateAccount.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    public void create() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                Context context = getApplicationContext();
                SmackAndroid.init(context);
                ConnectionConfiguration ConnectionConfiguration = new ConnectionConfiguration(HOST, PORT);
                ConnectionConfiguration.setDebuggerEnabled(true);
                ConnectionConfiguration.setSecurityMode(org.jivesoftware.smack.ConnectionConfiguration.SecurityMode.disabled);
                connection = new XMPPTCPConnection(ConnectionConfiguration);
                AccountManager am = AccountManager.getInstance(connection);
                try {
                    connection.connect();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                try {
                    am.createAccount(id,pw);
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }



}
