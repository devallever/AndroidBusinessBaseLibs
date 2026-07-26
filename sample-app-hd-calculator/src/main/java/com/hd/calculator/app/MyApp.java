package com.hd.calculator.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hd.calculator.app.business.BillCodeManager;
import com.hd.calculator.app.business.BillManager;
import com.hd.calculator.app.business.TakeoutTableManager;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.network.NetworkRepository;
import com.hd.calculator.app.function.store.StoreManager;
import com.hd.calculator.app.function.sync.DataSyncManager;
import com.hd.calculator.app.util.EventUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.NetworkUtils;
import com.hd.calculator.app.util.ToastUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.umeng.commonsdk.UMConfigure;


public class MyApp extends Application {
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static Handler mainH = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        StoreManager.getIns().init();
        NetworkRepository.getInstance().init();
        DataBaseRepository.getInstance().init();
        DataSyncManager.getInstance().syncData();
        TakeoutTableManager.getIns().checkDailyReset();
        BillCodeManager.getIns().checkDailyReset();
        NetworkUtils.init(this);
        BillManager.getIns().generateYesterdayBill();

        CrashReport.initCrashReport(getApplicationContext(), "3ac5c24438", true);
        EventUtils.init();

        if (NetworkUtils.isNetworkAvailable()) {
//            ToastUtils.show("网络已连接");
            LogUtils.log("网络可用：" + NetworkUtils.getNetworkType());
        } else {
            LogUtils.log("网络不可用");
//            ToastUtils.show("网络已断开");
        }
    }
}
