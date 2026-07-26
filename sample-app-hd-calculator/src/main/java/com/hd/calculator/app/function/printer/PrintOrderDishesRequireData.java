package com.hd.calculator.app.function.printer;

public class PrintOrderDishesRequireData {
    private int count;
    private String dishedCode;
    private String name;

    private String remark = "";
    public PrintOrderDishesRequireData() {
    }

    public PrintOrderDishesRequireData(int count, String dishedCode, String name, String remark) {
        this.count = count;
        this.dishedCode = dishedCode;
        this.name = name;
        this.remark = remark;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDishedCode() {
        return dishedCode;
    }

    public void setDishedCode(String dishedCode) {
        this.dishedCode = dishedCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
