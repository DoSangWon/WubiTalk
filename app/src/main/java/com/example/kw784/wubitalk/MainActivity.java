package com.example.kw784.wubitalk;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static { System.loadLibrary("KISACrypto"); }//KISA 암호화 라이브러리 호출
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    ListView Friendlist;//친구목록
    ListView Talklist;//대화목록
    ListView Setting; //셋팅
    ListView Setting_switch; //스위치가 달린 셋팅

    // XMPP 연결과 관련된 변수들-------------------------------------┐
    static XMPPTCPConnection connection;
    Handler mHandler = new Handler();
    String id;
    String pw;
    String HOST = "sangwon.iptime.org";
    int PORT = 5222;
    static Roster roster;
    //---------------------------------------------------------------┘

    // 친구목록과 매핑되는 배열 및 어댑터----------------------------┐
    ArrayAdapter<String> adapter_roster;
    ArrayList<String> arr_friend = new ArrayList<String>();
    //---------------------------------------------------------------┘

    // 대화목록과 매핑되는 배열 및 어댑터----------------------------┐
    ChatListViewAdapter mChatListViewAdapter;
    ArrayList<String> arr_chatlist_destid;
    ArrayList<String> arr_chatlist_message;
    List<ChatListViewItem> chatDataArr;
    //---------------------------------------------------------------┘

    // 데이터 베이스 ------------------------------------------------┐
    private DbOpenHelper mDbOpenHelper;
    private SQLiteDatabase sqlDB;
    //---------------------------------------------------------------┘

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       Intent intent = getIntent();
        id=intent.getStringExtra("id");
        pw=intent.getStringExtra("pw");

        adapter_roster = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr_friend);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mDbOpenHelper = new DbOpenHelper(this);
        connect();
        //Toast.makeText(MainActivity.this, id+"님, 환영합니다!", Toast.LENGTH_SHORT).show();
    }


    // 데이터 베이스 -------------------------------------------------------------------------------------------------------------------------------------------------------------┐
    public void writeDB_log(int direction, String fromid, String message){         // 모든 메시지 송신 및 수신 시 채팅로그 테이블에 입력하기 위한 함수
        sqlDB = mDbOpenHelper.getWritableDatabase();
        sqlDB.execSQL("INSERT INTO chatlog (direction, fromid, message) VALUES ("+ direction + ", '" + fromid + "', '" + message + "');");
        sqlDB.close();
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

    public ArrayList<String> readDB_log(int column){          // 채팅로그를 DB로부터 불러오는 함수, 1열 : 송수신 구분 정보 / 2열 : 상대 아이디 / 3열 : 마지막 송신 메시지 / 0열 ID : 순서정렬용 인덱스
        sqlDB = mDbOpenHelper.getReadableDatabase();
        ArrayList<String> dataArr = new ArrayList<String>();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM chatlog ORDER BY ID;", null);

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

    public ArrayList<String> readDB_list(int column){       // 채팅목록을 DB로부터 불러오는 함수, 0열 : 상대 아이디 / 1열 : 마지막 송신 메시지
        sqlDB = mDbOpenHelper.getReadableDatabase();
        ArrayList<String> dataArr = new ArrayList<String>();
        Cursor cursor;
        cursor = sqlDB.rawQuery("SELECT * FROM chatlist;", null);

        if(cursor.moveToFirst()) {
            do{
                if(column == 0)
                    dataArr.add(cursor.getString(0));
                if(column == 1)
                    dataArr.add(cursor.getString(1));
            }
            while (cursor.moveToNext()) ;
        }

        cursor.close();
        sqlDB.close();

        return dataArr;
    }

    public void deleteDB_log(String destId){
        sqlDB = mDbOpenHelper.getWritableDatabase();
        sqlDB.execSQL("DELETE FROM chatlog WHERE fromid = '" + destId + "'");
        sqlDB.close();
    }

    public void deleteDB_list(String destId){
        sqlDB = mDbOpenHelper.getWritableDatabase();
        sqlDB.execSQL("DELETE FROM chatlist WHERE fromid = '" + destId + "'");
        sqlDB.close();
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------┘

    public void connect() {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
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
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, id + "님, 환영합니다!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (XMPPException e) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                } catch (SmackException e) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                } catch (IOException e) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    });
                    e.printStackTrace();
                }

                // 친구목록을 불러오는 기능---------------------------------------------------------
                roster = connection.getRoster();
                roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                final Collection<RosterEntry> entries = roster.getEntries();
                arr_friend.clear();
                for (RosterEntry entry : entries) {
                    if (entry.toString().indexOf(':') != -1) {
                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf(':')));
                    } else {
                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf('@')));
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter_roster.notifyDataSetChanged();
                        }
                    });
                }
                //----------------------------------------------------------------------------------


                // 친구목록 갱신 리스너-------------------------------------------------------------
                roster.addRosterListener(new RosterListener() {

                    // 추가발생 시==================================================================
                    @Override
                    public void entriesAdded(Collection<String> collection) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "친구가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                Roster roster_temp = connection.getRoster();
                                final Collection<RosterEntry> entries_temp = roster_temp.getEntries();
                                arr_friend.clear();
                                for (RosterEntry entry : entries_temp) {
                                    if (entry.toString().indexOf(':') != -1) {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf(':')));
                                    } else {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf('@')));
                                    }
                                    adapter_roster.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    //==============================================================================

                    // 갱신발생 시==================================================================
                    @Override
                    public void entriesUpdated(Collection<String> collection) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "친구정보가 갱신되었습니다.", Toast.LENGTH_SHORT).show();
                                Roster roster_temp = connection.getRoster();
                                final Collection<RosterEntry> entries_temp = roster_temp.getEntries();
                                arr_friend.clear();
                                for (RosterEntry entry : entries_temp) {
                                    if (entry.toString().indexOf(':') != -1) {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf(':')));
                                    } else {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf('@')));
                                    }
                                    adapter_roster.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    //==============================================================================

                    // 삭제발생 시==================================================================
                    @Override
                    public void entriesDeleted(Collection<String> collection) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "친구삭제를 완료하였습니다.", Toast.LENGTH_SHORT).show();
                                Roster roster_temp = connection.getRoster();
                                final Collection<RosterEntry> entries_temp = roster_temp.getEntries();
                                arr_friend.clear();
                                arr_friend.add("delete");
                                for (RosterEntry entry : entries_temp) {
                                    if (entry.toString().indexOf(':') != -1) {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf(':')));
                                    } else {
                                        arr_friend.add(entry.toString().substring(0, entry.toString().indexOf('@')));
                                    }
                                    adapter_roster.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    //==============================================================================

                    @Override
                    public void presenceChanged(Presence presence) {

                    }
                });
                //----------------------------------------------------------------------------------


                // 메시지 패킷 수신 리스너
                PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
                connection.addPacketListener(new PacketListener() {
                    @Override
                    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
                        final Message message = (Message) packet;
                        if (message.getBody() != null) {
                            String fromName = StringUtils.parseBareAddress(message.getFrom());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, message.getFrom().substring(0, message.getFrom().indexOf('@')) + "님으로부터 메시지가 도착했습니다.", Toast.LENGTH_SHORT).show();
                                    writeDB_list(message.getFrom().substring(0, message.getFrom().indexOf('@')), message.getBody());
                                    writeDB_log(1 ,message.getFrom().substring(0, message.getFrom().indexOf('@')), message.getBody());

                                    chatDataArr.clear();

                                    arr_chatlist_destid = readDB_list(0);
                                    arr_chatlist_message = readDB_list(1);

                                    for(int i=0; i<arr_chatlist_destid.size(); i++) {
                                        chatDataArr.add(new ChatListViewItem(arr_chatlist_destid.get(i), arr_chatlist_message.get(i)));
                                    }

                                    mChatListViewAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }
                }, filter);
            }
        });
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int uid = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (uid == R.id.log) {
            try {
                connection.disconnect();
                Intent logout = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(logout);
                finish();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            return true;
        }
        if(uid==R.id.addUser){
            Intent adduser = new Intent(MainActivity.this,AddUserActivity.class);
            adduser.putExtra("ID",id);
            adduser.putExtra("PW",pw);
            startActivity(adduser);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public class RedFragment extends Fragment {//친구목록

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.layout_tab_0, container, false);

            Friendlist = (ListView) view.findViewById(R.id.FriendsList);
            Friendlist.setAdapter(adapter_roster);

            Friendlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("dest", arr_friend.get(position).toString());
                    intent.putExtra("myId", MainActivity.this.id);
                    intent.putExtra("pw", pw);
                    startActivity(intent);
                }
            });

            Friendlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    getMenuInflater().inflate(R.menu.menu_friendlist, popup.getMenu());
                    final int index = position;
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.itemDelete:
                                    RosterEntry rosterEntry = roster.getEntry(arr_friend.get(index)+"@sangwon.iptime.org");
                                    try {
                                        roster.removeEntry(rosterEntry);
                                        arr_friend.remove(index);
                                        Toast.makeText(getActivity(), "선택한 친구를 목록에서 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                    } catch (SmackException.NotLoggedInException e) {
                                        e.printStackTrace();
                                    } catch (SmackException.NoResponseException e) {
                                        e.printStackTrace();
                                    } catch (XMPPException.XMPPErrorException e) {
                                        e.printStackTrace();
                                    } catch (SmackException.NotConnectedException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case R.id.itemDelCancel:
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            return view;
        }
    }
    public class BlueFragment extends Fragment {//대화목록

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.layout_tab_1, container, false);
            Talklist = (ListView) view.findViewById(R.id.TalkList);

            chatDataArr = new ArrayList<>();

            arr_chatlist_destid = readDB_list(0);
            arr_chatlist_message = readDB_list(1);

            for(int i=0; i<arr_chatlist_destid.size(); i++) {
                chatDataArr.add(new ChatListViewItem(arr_chatlist_destid.get(i), arr_chatlist_message.get(i)));
            }

            mChatListViewAdapter = new ChatListViewAdapter(getActivity(), chatDataArr);
            Talklist.setAdapter(mChatListViewAdapter);
            mChatListViewAdapter.notifyDataSetChanged();

            Talklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("dest", arr_chatlist_destid.get(position).toString());
                    intent.putExtra("myId", MainActivity.this.id);
                    intent.putExtra("pw", pw);
                    startActivityForResult(intent, 1);
                }
            });

            Talklist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    getMenuInflater().inflate(R.menu.menu_chatlist, popup.getMenu());
                    final int index = position;
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.itemOk:
                                    deleteDB_list(arr_chatlist_destid.get(index));
                                    deleteDB_log(arr_chatlist_destid.get(index));
                                    chatDataArr.remove(index);
                                    mChatListViewAdapter.notifyDataSetChanged();
                                    Toast.makeText(getActivity(), "선택한 대화방에서 퇴장하였습니다.", Toast.LENGTH_SHORT).show();
                                    break;
                                case R.id.itemCancel:
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                    return false;
                }
            });

            return view;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data){
           super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode){
                case 1:
                    chatDataArr.clear();

                    arr_chatlist_destid = readDB_list(0);
                    arr_chatlist_message = readDB_list(1);

                    for(int i=0; i<arr_chatlist_destid.size(); i++) {
                        chatDataArr.add(new ChatListViewItem(arr_chatlist_destid.get(i), arr_chatlist_message.get(i)));
                    }

                    mChatListViewAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
    public class GreenFragment extends Fragment {//환경설정

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
// Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.layout_tab_2, container, false);
            Setting = (ListView) view.findViewById(R.id.Setting);
            Setting_switch = (ListView) view.findViewById(R.id.Setting_Switch);

            ArrayList<String> dataArr = new ArrayList<String>();
            List<ListViewItem> settingDataArr = new ArrayList<>();

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, dataArr);
            Setting.setAdapter(adapter);

            dataArr.add("버전정보");

            adapter.notifyDataSetChanged();

            Setting.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {    // 버전정보를 클릭한 경우
                        Intent intent = new Intent(getActivity(), VersionInfoActivity.class);
                        startActivity(intent);
                    }
                }
            });

            settingDataArr.add(new ListViewItem("친구 요청 설정", "on일 경우 모든 요청에 대해 자동으로 수락합니다.", 1));
            ListViewAdapter mListViewAdapter = new ListViewAdapter(getActivity(), settingDataArr, roster);
            Setting_switch.setAdapter(mListViewAdapter);

            return view;
        }
    }



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return new RedFragment();
            else if(position==1)
                return new BlueFragment();
            else
                return new GreenFragment();
        }


        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "친구목록";
                case 1:
                    return "대화목록";
                case 2:
                    return "환경설정";
            }
            return null;
        }
    }
}
