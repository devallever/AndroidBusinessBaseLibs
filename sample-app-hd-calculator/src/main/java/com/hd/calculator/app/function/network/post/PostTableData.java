package com.hd.calculator.app.function.network.post;

public class PostTableData {
    private int tableCode;
    private long userId;
    private TableUseData tableData;

    public int getTableCode() {
        return tableCode;
    }

    public void setTableCode(int tableCode) {
        this.tableCode = tableCode;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public TableUseData getTableData() {
        return tableData;
    }

    public void setTableData(TableUseData tableData) {
        this.tableData = tableData;
    }
}
