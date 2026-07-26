package com.hd.calculator.app.ui.item;

public class BillSortItem {
    //sort type
    private int sortType;
    private String sortName;

    private boolean select;

    private String orderCondition;

    public BillSortItem(int sortType, String sortName, boolean select, String orderCondition) {
        this.sortType = sortType;
        this.sortName = sortName;
        this.select = select;
        this.orderCondition = orderCondition;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String sortName) {
        this.sortName = sortName;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }


    public String getOrderCondition() {
        return orderCondition;
    }

    public void setOrderCondition(String orderCondition) {
        this.orderCondition = orderCondition;
    }
}
