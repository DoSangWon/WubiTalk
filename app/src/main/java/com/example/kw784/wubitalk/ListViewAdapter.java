package com.example.kw784.wubitalk;

/**
 * Created by sinn5 on 2016-06-01.
 * 환경설정의 친구 요청 설정에 사용할 CustumListView의 Adapter클래스입니다.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.jivesoftware.smack.Roster;

import java.util.List;

public class ListViewAdapter extends BaseAdapter{
    private Context mContext;
    private List<ListViewItem> mSettingDataList;
    private LayoutInflater mLayoutInflater;
    Roster roster;

    public ListViewAdapter(Context context, List<ListViewItem> settingDataList, Roster roster){
        this.mContext = context;
        this.mSettingDataList = settingDataList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.roster = roster;
    }

    @Override
    public int getCount() {
        return mSettingDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSettingDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = mLayoutInflater.inflate(R.layout.listview_item, null);
        }

        TextView name = (TextView)view.findViewById(R.id.textView1);
        TextView desc = (TextView)view.findViewById(R.id.textView2);
        Switch sw = (Switch)view.findViewById(R.id.switch1);

        final ListViewItem settingdata = mSettingDataList.get(position);

        name.setText(settingdata.getName());
        desc.setText(settingdata.getDesc());

        sw.setChecked(true);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settingdata.setSwitch_state(1);
                    roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                } else {
                    settingdata.setSwitch_state(0);
                    roster.setSubscriptionMode(Roster.SubscriptionMode.reject_all);
                }
            }
        });


        return view;
    }
}
