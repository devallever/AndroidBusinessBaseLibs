package com.hd.calculator.app.function.db.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "account")
public class AccountEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    //userid
    private long userId;
    //username
    private String userName;
    //password
    private String password;
    //transTablePermission
    private boolean transTablePermission;
    //deleteTablePermission
    private boolean deleteTablePermission;
    //restoreTablePermission
    private boolean restoreTablePermission;
    //cancelOrderPermission
    private boolean cancelOrderPermission;
    //reduceDishesCountPermission
    private boolean reduceDishesCountPermission;
    //viewDailyBillPermission
    private boolean viewDailyBillPermission;
    //localModePermission
    private boolean localModePermission;

    private boolean boss;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTransTablePermission() {
        return transTablePermission;
    }

    public void setTransTablePermission(boolean transTablePermission) {
        this.transTablePermission = transTablePermission;
    }

    public boolean isDeleteTablePermission() {
        return deleteTablePermission;
    }

    public void setDeleteTablePermission(boolean deleteTablePermission) {
        this.deleteTablePermission = deleteTablePermission;
    }

    public boolean isRestoreTablePermission() {
        return restoreTablePermission;
    }

    public void setRestoreTablePermission(boolean restoreTablePermission) {
        this.restoreTablePermission = restoreTablePermission;
    }

    public boolean isCancelOrderPermission() {
        return cancelOrderPermission;
    }

    public void setCancelOrderPermission(boolean cancelOrderPermission) {
        this.cancelOrderPermission = cancelOrderPermission;
    }

    public boolean isReduceDishesCountPermission() {
        return reduceDishesCountPermission;
    }

    public void setReduceDishesCountPermission(boolean reduceDishesCountPermission) {
        this.reduceDishesCountPermission = reduceDishesCountPermission;
    }

    public boolean isViewDailyBillPermission() {
        return viewDailyBillPermission;
    }

    public void setViewDailyBillPermission(boolean viewDailyBillPermission) {
        this.viewDailyBillPermission = viewDailyBillPermission;
    }

    public boolean isLocalModePermission() {
        return localModePermission;
    }

    public void setLocalModePermission(boolean localModePermission) {
        this.localModePermission = localModePermission;
    }

    public boolean isBoss() {
        return boss;
    }

    public void setBoss(boolean boss) {
        this.boss = boss;
    }
}
