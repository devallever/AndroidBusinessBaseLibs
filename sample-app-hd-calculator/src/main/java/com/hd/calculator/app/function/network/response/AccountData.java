package com.hd.calculator.app.function.network.response;

public class AccountData {
    private long id;
    private String username;
    private String password;
    private boolean moveTable;
    private boolean cancelTable;
    private boolean recoverTable;
    private boolean cancelFoodDrinks;
    private boolean cancelBill;
    private boolean viewDayBill;
    private boolean localMode;
    private boolean onlineSync;

    private boolean boss;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMoveTable() {
        return moveTable;
    }

    public void setMoveTable(boolean moveTable) {
        this.moveTable = moveTable;
    }

    public boolean isCancelTable() {
        return cancelTable;
    }

    public void setCancelTable(boolean cancelTable) {
        this.cancelTable = cancelTable;
    }

    public boolean isRecoverTable() {
        return recoverTable;
    }

    public void setRecoverTable(boolean recoverTable) {
        this.recoverTable = recoverTable;
    }

    public boolean isCancelFoodDrinks() {
        return cancelFoodDrinks;
    }

    public void setCancelFoodDrinks(boolean cancelFoodDrinks) {
        this.cancelFoodDrinks = cancelFoodDrinks;
    }

    public boolean isCancelBill() {
        return cancelBill;
    }

    public void setCancelBill(boolean cancelBill) {
        this.cancelBill = cancelBill;
    }

    public boolean isViewDayBill() {
        return viewDayBill;
    }

    public void setViewDayBill(boolean viewDayBill) {
        this.viewDayBill = viewDayBill;
    }

    public boolean isLocalMode() {
        return localMode;
    }

    public void setLocalMode(boolean localMode) {
        this.localMode = localMode;
    }

    public boolean isOnlineSync() {
        return onlineSync;
    }

    public void setOnlineSync(boolean onlineSync) {
        this.onlineSync = onlineSync;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }
}