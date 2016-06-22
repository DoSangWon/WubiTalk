package com.example.kw784.wubitalk;

class CustomList { //이것도 커스텀 리스트뷰 값을 받아서 자장 부분



    private String name;

    private String write;

    private String datetime;

    //ImageView photo;



    public CustomList(String _name, String _write, String _datetime){

        this.name = _name;

        this.write = _write;

        this.datetime = _datetime;

        //this.photo = _photo;

    }



    public String getName() {

        return name;

    }



    public String getWrite() {

        return write;

    }

    public String getDateTime() {

        return datetime;

    }



//            public ImageView getPhoto() {

//                return photo;

//            }



}