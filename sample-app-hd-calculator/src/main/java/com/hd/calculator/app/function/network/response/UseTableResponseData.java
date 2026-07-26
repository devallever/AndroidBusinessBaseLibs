package com.hd.calculator.app.function.network.response;

import com.hd.calculator.app.function.network.post.TableUseData;

public class UseTableResponseData {

    private long id;
    private int tableCode;
    private long userId;
    private TableUseData tableData;
    private int useStatus;
    private String createTime;
    private String updateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public int getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(int useStatus) {
        this.useStatus = useStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
