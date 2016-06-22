package com.example.kw784.wubitalk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

/**
 * Created by kw784 on 2016-06-01.
 */
public class AddUserActivity extends Activity implements View.OnClickListener {
    Button btn_adduser;
    EditText addUserID;
    EditText addUserName;

    String addname;
    //XMPPTCPConnection connection;
    Handler mHandler = new Handler();
    String addid;
    String id;
    String pw;
    String HOST = "sangwon.iptime.org";
    int PORT = 5222;
    //Roster roster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);
        addUserID = (EditText) findViewById(R.id.addUserId);
        addUserName = (EditText) findViewById(R.id.addUserName);
        btn_adduser = (Button) findViewById(R.id.btn_addUser);
        addid = addUserID.getText().toString();
        addname = addUserName.getText().toString();
        Intent intent = getIntent();
        id=intent.getStringExtra("ID");
        pw=intent.getStringExtra("PW");

       btn_adduser.setOnClickListener(this);




    }

    public void addUser(){
        Thread t3 = new Thread(new Runnable() {

            @Override
            public void run() {
                /*
                Context context = getApplicationContext();
                SmackAndroid.init(context);
                ConnectionConfiguration ConnectionConfiguration = new ConnectionConfiguration(HOST, PORT);
                ConnectionConfiguration.setDebuggerEnabled(true);
                ConnectionConfiguration.setSecurityMode(org.jivesoftware.smack.ConnectionConfiguration.SecurityMode.disabled);
                connection = new XMPPTCPConnection(ConnectionConfiguration);

                try {
                    connection.connect();

                } catch (SmackException.ConnectionException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
                try {
                    connection.login(id, pw);
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */



                // 친구추가
                //roster = MainActivity.connection.getRoster();

                //roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                try {
                    MainActivity.roster.createEntry(addid+"@"+HOST, addname, null);
                    Presence subscribe = new Presence(Presence.Type.subscribe);
                    subscribe.setTo(addid+"@"+HOST);
                    MainActivity.connection.sendPacket(subscribe);
                } catch (SmackException.NotLoggedInException e) {
                    e.printStackTrace();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
        t3.start();
    }

    @Override
    public void onClick(View v) {
        addid = addUserID.getText().toString();
        addname = addUserName.getText().toString();
        addUser();
        Toast.makeText(AddUserActivity.this, addname+"님에게 친구 신청하였습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
