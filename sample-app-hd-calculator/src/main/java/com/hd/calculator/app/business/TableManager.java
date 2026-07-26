package com.hd.calculator.app.business;

import androidx.annotation.NonNull;

import com.hd.calculator.app.constant.OrderType;

public class TableManager {
    public static TableManager getIns() {
        return SingletonHolder.INSTANCE;
    }

    public String getDisplayTableName(int tableCode, int orderType) {
        if (orderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            return "T-" + tableCode;
        } else {
            return "T-A" + tableCode;
        }
    }

    public void checkTableCanUsed(int tableCode, @NonNull CheckTableUsedCallback callback) {
        if (callback != null)
        callback.onResult(true);
    }

    public void unLockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, false, finishTask);
    }

    public void lockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, true, false, finishTask);
    }

    public void forceLockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, true, true, finishTask);
    }

    /***
     *
     * @param type 用户操作类型 ActionType
     * 订单记录
     * 1.最开始能进入点餐界面，就要占用
     * 2.出单
     * 3.结账
     * 4.菜品数量变更
     * 5.输入boss密码强制覆盖
     **/
    public void postOrderRecord(int tableCode, int type, boolean force, Runnable finishTask) {
        if (finishTask != null)
        finishTask.run();
    }


    public void updateTableState(int tableCode, boolean inuse, Runnable finishTask) {
        updateTableState(tableCode, inuse, false, finishTask);
    }

    public void updateTableState(int tableCode, boolean inuse, boolean force, Runnable finishTask) {
        if (finishTask != null)
        finishTask.run();
    }

    public interface CheckTableUsedCallback {
        void onResult(boolean canUse);
    }

    //inner static class
    private static class SingletonHolder {
        private static final TableManager INSTANCE = new TableManager();
    }

}
