package com.example.kw784.wubitalk;

/**
 * Created by sinn5 on 2016-06-01.
 * 환경설정의 친구 요청 설정에 대한 CustumListView의 Item 클래스입니다.
 */
public class ListViewItem {
    private String name;
    private String desc;
    private int switch_state;

    public ListViewItem(String name, String desc, int switch_state){
        this.name = name;
        this.desc = desc;
        this.switch_state = switch_state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSwitch_state() {
        return switch_state;
    }

    public void setSwitch_state(int switch_state) {
        this.switch_state = switch_state;
    }
}
