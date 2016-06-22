package com.example.kw784.wubitalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sinn5 on 2016-06-06.
 */
public class ChatListViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatListViewItem> mChatDataList;
    private LayoutInflater mLayoutInflater;

    public ChatListViewAdapter(Context context, List<ChatListViewItem> chatDataList){
        this.mContext = context;
        this.mChatDataList = chatDataList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mChatDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mChatDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = mLayoutInflater.inflate(R.layout.chat_listview_item, null);
        }

        TextView destId = (TextView)view.findViewById(R.id.textView_Chat1);
        TextView message = (TextView)view.findViewById(R.id.textView_Chat2);

        final ChatListViewItem chatData = mChatDataList.get(position);

        destId.setText(chatData.getDestId());
        message.setText(chatData.getMessage());

        return view;
    }
}
