package com.example.kw784.wubitalk;

/**
 * Created by sinn5 on 2016-06-06.
 * 대화목록 커스텀 리스트뷰의 아이템
 */
public class ChatListViewItem {
    private String destId;
    private String message;

    public ChatListViewItem(String destId, String message){
        this.destId = destId;
        this.message = message;
    }

    public String getDestId() {
        return destId;
    }

    public void setDestId(String destId) {
        this.destId = destId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
