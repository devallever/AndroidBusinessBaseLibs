package com.hd.calculator.app.function.network.response;

public class TableHoldStateResponse {

    private int code;
    private String msg;
    private TableHoldStateData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public TableHoldStateData getData() {
        return data;
    }

    public void setData(TableHoldStateData data) {
        this.data = data;
    }
}
