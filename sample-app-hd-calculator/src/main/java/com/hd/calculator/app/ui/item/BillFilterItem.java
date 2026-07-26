package com.hd.calculator.app.ui.item;

public class BillFilterItem {
    // id
    private long userId;
    //name
    private String name;
    //select
    private boolean select;

    public BillFilterItem(long userId, String name, boolean select) {
        this.userId = userId;
        this.name = name;
        this.select = select;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
