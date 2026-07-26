package com.hd.calculator.app.function.network.response;

import java.util.List;

public class AccountResponse {
    private int code;
    private String msg;
    private List<AccountData> data;

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

    public List<AccountData> getData() {
        return data;
    }

    public void setData(List<AccountData> data) {
        this.data = data;
    }


}
