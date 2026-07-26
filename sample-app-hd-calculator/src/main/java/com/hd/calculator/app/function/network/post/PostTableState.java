package com.hd.calculator.app.function.network.post;

public class PostTableState {
    private int tableCode;
    private long userId;
    private boolean inuse;
    private boolean force;

    public PostTableState() {
    }

    public PostTableState(int tableCode, long userId) {
        this.tableCode = tableCode;
        this.userId = userId;
        this.inuse = true;
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

    public boolean isInuse() {
        return inuse;
    }

    public void setInuse(boolean inuse) {
        this.inuse = inuse;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }
}
