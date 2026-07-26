package com.hd.calculator.app.function.network.response;


import java.util.List;

public class UseTableResponse {

    private int code;
    private String msg;
    private List<UseTableResponseData> data;

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

    public List<UseTableResponseData> getData() {
        return data;
    }

    public void setData(List<UseTableResponseData> data) {
        this.data = data;
    }
}
