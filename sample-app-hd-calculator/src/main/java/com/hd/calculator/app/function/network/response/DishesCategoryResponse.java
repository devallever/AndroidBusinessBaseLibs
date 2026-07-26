package com.hd.calculator.app.function.network.response;

import java.util.List;

public class DishesCategoryResponse {
    private int code;
    private String msg;
    private List<FirstCategoryData> data;

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

    public List<FirstCategoryData> getData() {
        return data;
    }

    public void setData(List<FirstCategoryData> data) {
        this.data = data;
    }
}
