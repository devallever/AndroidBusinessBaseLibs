package com.hd.calculator.app.util;


import com.hd.calculator.app.MyApp;
import com.tencent.bugly.BuglyStrategy;
import com.tencent.bugly.crashreport.BuglyLog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import java.util.HashMap;

public class EventUtils {
    public static void init() {
        String umAppKey = "69243a338560e34872f292bb";
        String umChannel = "default";
        UMConfigure.setLogEnabled(true);
        UMConfigure.preInit(MyApp.context, umAppKey, umChannel);
        UMConfigure.init(MyApp.context, umAppKey, umChannel, UMConfigure.DEVICE_TYPE_PHONE, "");
    }

    public static void logEvent(String eventId, HashMap<String, Object> map) {
        MobclickAgent.onEventObject(MyApp.context, eventId, map);
    }

    public static void logDeleteOrderEvent(long orderId, String scene) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("scene", scene);
        logEvent("deleteOrder", map);
        LogUtils.log("删除订单: " + orderId);
    }

    public static void logExceptionOrderId(long orderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("message", "订单异常，使用中，但是没有菜品");
        logEvent("exceptionOrderId", map);
        LogUtils.log("订单异常，使用中，但是没有菜品: " + orderId);
    }

}
