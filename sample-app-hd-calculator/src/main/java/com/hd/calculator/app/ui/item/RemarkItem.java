package com.hd.calculator.app.ui.item;

public class RemarkItem {
    private String remark;
    private boolean select;

    public RemarkItem() {
    }

    public RemarkItem(String remark) {
        this.remark = remark;
    }


    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
