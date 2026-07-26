package com.hd.calculator.app.business.model;

import com.hd.calculator.app.function.network.post.PostTableData;

import java.util.List;

public class LocalUnUploadTableData {
    private List<PostTableData> list;

    public List<PostTableData> getList() {
        return list;
    }

    public void setList(List<PostTableData> list) {
        this.list = list;
    }
}
