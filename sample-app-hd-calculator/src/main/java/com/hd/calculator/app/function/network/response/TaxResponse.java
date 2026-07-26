package com.hd.calculator.app.function.network.response;

public class TaxResponse {
    private int code;
    private String msg;
    private TaxData data;

    public Integer getCode() {
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

    public TaxData getData() {
        return data;
    }

    public void setData(TaxData data) {
        this.data = data;
    }
}
