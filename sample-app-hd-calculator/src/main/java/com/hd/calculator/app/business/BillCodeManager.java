package com.hd.calculator.app.business;

import com.hd.calculator.app.function.store.StoreManager;
import com.hd.calculator.app.util.LogUtils;
import com.tencent.mmkv.MMKV;

import java.util.Calendar;

public class BillCodeManager {

    private static final String KEY_COUNT = "bill_daily_counter";
    private static final String KEY_LAST_RESET_DATE = "bill_last_reset_date";
    private static final int RESET_HOUR = 7; // 早上7点重置

    public static BillCodeManager getIns() {
        return SingletonHolder.INSTANCE;
    }

    public void reduceCount() {
        int current = getCurrentCount();
        if (current > 0) {
            StoreManager.getIns().putInt(KEY_COUNT, current - 1);
        }
    }

    /**
     * 获取当前值并自增
     */
    public int getAndIncrement() {
        checkDailyReset();

        // 原子自增操作（线程安全）
        int current = getCurrentCount();
        int newCount = current + 1;
        StoreManager.getIns().putInt(KEY_COUNT, newCount);
        return newCount;
    }

    /**
     * 获取当前值（不自增）
     */
    public int getCurrentCount() {
        checkDailyReset();
        return StoreManager.getIns().getInt(KEY_COUNT, 0);
    }

    /**
     * 检查重置条件（每日7点重置）
     */
    public void checkDailyReset() {
        Calendar now = Calendar.getInstance();
        int today = now.get(Calendar.DAY_OF_YEAR);
        int currentYear = now.get(Calendar.YEAR);

        // 检查是否需要重置
        boolean shouldReset = shouldReset(
                StoreManager.getIns().getInt(KEY_LAST_RESET_DATE, -1),
                StoreManager.getIns().getInt(KEY_LAST_RESET_DATE + "_year", -1),
                today,
                currentYear,
                now.get(Calendar.HOUR_OF_DAY)
        );


        if (shouldReset) {
            LogUtils.log("重置账单号");
            // MMKV 支持事务操作
            MMKV kv = StoreManager.getIns().getDefaultMMKV();
            kv.lock(); // 多线程写操作前加锁
            try {
                kv.encode(KEY_COUNT, 0);
                kv.encode(KEY_LAST_RESET_DATE, today);
                kv.encode(KEY_LAST_RESET_DATE + "_year", currentYear);
                kv.commit(); // 同步提交保证立即写入
            } finally {
                kv.unlock(); // 确保解锁
            }
        } else {
            LogUtils.log("不重置虚拟桌号");
        }
    }

    private boolean shouldReset(int lastDay, int lastYear, int today, int currentYear, int currentHour) {
        // 1. 从未初始化过
        if (lastDay == -1 || lastYear == -1) return true;

        // 2. 跨年处理
        if (currentYear != lastYear) return true;

        // 3. 同一年内日期变化，且当前时间超过7点
        return (today != lastDay) && (currentHour >= RESET_HOUR);
    }

    /**
     * 调试用：强制重置计数器（非必须）
     */
    public void resetForDebug() {
        Calendar now = Calendar.getInstance();
        StoreManager.getIns().putInt(KEY_COUNT, 0);
        StoreManager.getIns().putInt(KEY_LAST_RESET_DATE, now.get(Calendar.DAY_OF_YEAR));
        StoreManager.getIns().putInt(KEY_LAST_RESET_DATE + "_year", now.get(Calendar.YEAR));
    }

    public String getDisplayBillCode(int billCode) {
        return "RE-" + billCode;
    }

    //inner static class
    private static class SingletonHolder {
        private static final BillCodeManager INSTANCE = new BillCodeManager();
    }
}
