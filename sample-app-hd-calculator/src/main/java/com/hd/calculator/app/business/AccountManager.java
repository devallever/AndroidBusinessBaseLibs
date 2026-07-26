package com.hd.calculator.app.business;

import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.util.ToastUtils;

public class AccountManager {

    private AccountEntity loginAccount;

    public static AccountManager getIns() {
        return AccountManagerHolder.instance;
    }

    public synchronized void updateAccount(AccountEntity accountEntity) {
        loginAccount = accountEntity;
    }

    public void toastNoPermission() {
        ToastUtils.show("no permission");
    }

    public synchronized AccountEntity getAccount() {
        return loginAccount;
    }

    public boolean isBoss() {
        return loginAccount.isBoss();
    }

    public boolean canReduceDishes() {
        return loginAccount.isReduceDishesCountPermission() || isBoss();
    }

    public boolean canTransformTable() {
        return loginAccount.isTransTablePermission() || isBoss();
    }

    public boolean canDeleteTable() {
        return loginAccount.isDeleteTablePermission() || isBoss();
    }

    public boolean canRestoreTable() {
        return loginAccount.isRestoreTablePermission() || isBoss();
    }

    public boolean canCancelOrder() {
        return loginAccount.isCancelOrderPermission() || isBoss();
    }

    public boolean canViewDailyBill() {
        return loginAccount.isViewDailyBillPermission() || isBoss();
    }

    public boolean canLocalMode() {
        return loginAccount.isLocalModePermission() || isBoss();
    }

    private static class AccountManagerHolder {
        public final static AccountManager instance = new AccountManager();
    }

}
