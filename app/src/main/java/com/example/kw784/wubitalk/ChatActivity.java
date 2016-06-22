package com.example.kw784.wubitalk;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by Daddario on 16. 5. 31..
 */
public class ChatActivity extends ActionBarActivity {
    ListView listView_chat;
    EditText edit_input;
    TextView destid;
    Button btn_send;
    ArrayList<String> arr_chat = new ArrayList<String>();
    ArrayAdapter<String> adapter_chat;
    Intent intent;
    String dest;
    //XMPPConnection connection;
    Handler mHandler;
    ChatManager chatmanager;
    Chat newChat;

    String id;
    String pw;
    String HOST = "sangwon.iptime.org";
    int PORT = 5222;

    // 데이터 베이스 ------------------------------------------------┐
    private DbOpenHelper mDbOpenHelper;
    private SQLiteDatabase sqlDB;
    //---------------------------------------------------------------┘

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mHandler = new Handler();
        intent = getIntent();

        dest = intent.getStringExtra("dest");
        destid = (TextView) findViewById(R.id.mytext);
        destid.setText(dest);

        id = intent.getStringExtra("myId");
        pw = intent.getStringExtra("pw");

        listView_chat = (ListView) findViewById(R.id.listView);
        edit_input = (EditText) findViewById(R.id.editInput);
        btn_send = (Button) findViewById(R.id.btnSend);

        adapter_chat = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, arr_chat);
        listView_chat.setAdapter(adapter_chat);

        listView_chat.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        mDbOpenHelper = new DbOpenHelper(this);

        readLog();

        connect();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send(edit_input.getText().toString());
                edit_input.setText("");
            }
        });

    }


    // 데이터 베이스 -------------------------------------------------------------------------------------------------------------------------------------------------------------┐
    public void writeDB_log(int direction, String fromid, String message){         // 모든 메시지 송신 및 수신 시 채팅로그 테이블에 입력하기 위한 함수
        sqlDB = mDbOpenHelper.getWritableDatabase();
        sqlDB.execSQL("INSERT INTO chatlog (direction, fromid, message) VALUES (" + direction + ", '" + fromid + "', '" + message + "');");
        sqlDB.close();
    }

    public ArrayList<String> readDB_log(int column, String fromid){          // 채팅로그를 DB로부터 불러오는 함수, 1열 : 송수신 구분 정보 / 2열 : 상대 아이디 / 3열 : 마지막 송신 메시지 / 0열 ID : 순서정렬용 인덱스
        sqlDB = mDbOpenHelper.getReadableDatabase();
        ArrayList<String> dataArr = new ArrayList<String>();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM chatlog WHERE fromid = '" + fromid + "' ORDER BY ID;", null);

        if(cursor.moveToFirst()) {
            do{
                if(column == 1)
                    dataArr.add(cursor.getString(1));
                if(column == 2)
                    dataArr.add(cursor.getString(2));
                if(column == 3)
                    dataArr.add(cursor.getString(3));
            }
            while (cursor.moveToNext()) ;
        }

        cursor.close();
        sqlDB.close();

        return dataArr;
    }

    public void writeDB_list(String fromid, String message){        // 메시지 수신 시 송신자id와 메시지를 채팅목록 테이블에 입력하는데에 사용하는 함수
        sqlDB = mDbOpenHelper.getWritableDatabase();
        try {       // 송신자로부터 최초 메시지 수신 시
            sqlDB.execSQL("INSERT INTO chatlist (fromid, message) VALUES ('" + fromid + "', '" + message + "');");
        }catch (SQLException e){       // 한번 이상 송신자로부터 메시지를 수신받은 경우가 있는 경우
            sqlDB.execSQL("UPDATE chatlist set message='"+message+"' where fromid = '"+fromid+"'");
        }
        sqlDB.close();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘

    public void readLog() {
        ArrayList<String> arr_log_col1 = readDB_log(1, dest);
        ArrayList<String> arr_log_col2 = readDB_log(2, dest);
        ArrayList<String> arr_log_col3 = readDB_log(3, dest);

        for(int i=0; i<arr_log_col1.size(); i++){
            if(arr_log_col1.get(i).equals("0")) {
                arr_chat.add(id + " : " + arr_log_col3.get(i));
            } else{
                arr_chat.add(arr_log_col2.get(i) + " : "  + arr_log_col3.get(i));
            }
        }

        adapter_chat.notifyDataSetChanged();
    }

    public void connect() {
        Thread t2 = new Thread(new Runnable() {
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
                chatmanager = ChatManager.getInstanceFor(MainActivity.connection);
                newChat = chatmanager.createChat(dest + "@sangwon.iptime.org", new MessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {

                    }
                });

                PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
                MainActivity.connection.addPacketListener(new PacketListener() {
                    @Override
                    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
                        Message message = (Message) packet;
                        if (message.getBody() != null) {
                            String fromName = StringUtils.parseBareAddress(message.getFrom());
                            if(fromName.substring(0, fromName.indexOf('@')).equals(dest)) {
                                arr_chat.add(fromName.substring(0, fromName.indexOf('@')) + " : " + message.getBody());
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter_chat.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }, filter);
            }
        });
        t2.start();
    }

    public void send(String msg){
        try {
            newChat.sendMessage(msg);
            arr_chat.add(id + " : " + msg);
            writeDB_log(0, dest, msg);
            writeDB_list(dest, msg);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    adapter_chat.notifyDataSetChanged();
                }
            });

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (XMPPException e) {
            e.printStackTrace();
        }

    }
}
