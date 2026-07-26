package com.hd.calculator.app;

import android.annotation.SuppressLint;
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
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.NetworkUtils;

import app.allever.android.lib.core.app.App;
import app.allever.android.lib.core.helper.CoroutineHelper;
import app.allever.android.lib.core.helper.ExecutorHelper;


public class MyApp{
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static Handler mainH = new Handler(Looper.getMainLooper());

    private static boolean isInit = false;

    public static void onCreate() {
        if (isInit) {
            return;
        }
        context = App.context;
        ExecutorHelper.INSTANCE.getCacheExecutor().execute(new Runnable() {
            @Override
            public void run() {
                StoreManager.getIns().init();
                NetworkRepository.getInstance().init();
                DataBaseRepository.getInstance().init();
                DataSyncManager.getInstance().syncData();
                TakeoutTableManager.getIns().checkDailyReset();
                BillCodeManager.getIns().checkDailyReset();
                NetworkUtils.init(App.context);
                BillManager.getIns().generateYesterdayBill();


                if (NetworkUtils.isNetworkAvailable()) {
//            ToastUtils.show("网络已连接");
                    LogUtils.log("网络可用：" + NetworkUtils.getNetworkType());
                } else {
                    LogUtils.log("网络不可用");
//            ToastUtils.show("网络已断开");
                }
                isInit = true;
            }
        });

    }
}
