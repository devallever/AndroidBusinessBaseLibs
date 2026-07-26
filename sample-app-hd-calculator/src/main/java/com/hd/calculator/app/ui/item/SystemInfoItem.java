package com.hd.calculator.app.ui.item;

public class SystemInfoItem {
    //title
    private String title;
    //desc
    private String desc;

    public SystemInfoItem(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
