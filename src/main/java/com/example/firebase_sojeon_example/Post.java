package com.example.firebase_sojeon_example;

import java.util.Date;

public class Post {
    private  String key;
    private String txt ,title;
    private long createDate,updateDate;

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(long updateDate) {
        this.updateDate = updateDate;
    }

    public String getTitle() {
        if(txt!=null) {
            if (txt.indexOf("\n")> -1){
                return txt.substring(0,txt.indexOf("\n"));
            }else{
                return txt;
                }
        }
        else return txt;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
