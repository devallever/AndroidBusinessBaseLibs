package com.step.wincash.init;


public interface Constance {
    /**
     * @description: 广告部分测试环境配置
     */
    String BIGO_APPID = "10182906";
    String BIGO_REWARD_ID = "10182906-10017534";
    String BIGO_REWARD_ID1 = "11255971-115661210020";

    String MAX_SDK_ID = "tRLgvBNKtFZuwWs2XqXJt_3X9yyl7oCA-1N-LBASDS9GDGrDaMunCbzHMWK63bVl_NmwB5g0k5sCUD6BAEgmda";
    String MAX_INTER_ID = "619f75501965fcec";
    String MAX_INTER_ID1 = "21f81e81c1ebafe7";
    String MAX_REWARD_ID = "a14ece4d7e570a3e";
    String MAX_REWARD_ID1 = "b7a278733c8ae6c6";

    String ADMOB_APPLICATION_ID = "ca-app-pub-3940256099942544~3347511713"; //需要和AndroidManifest中 APPLICATION_ID一致
    String ADMOB_SPLASH_ID = "ca-app-pub-3940256099942544/9257395921";
    String ADMOB_INTER_ID = "ca-app-pub-3940256099942544/1033173712";
    String ADMOB_REWARD_ID = "ca-app-pub-3940256099942544/5224354917";
    String ADMOB_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110";
    String ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/9214589741";

    String KWAI_APP_ID = "899999";
    String KWAI_TOKEN = "EaCw0AipSYyvf3E7";
    String KWAI_REWARD_ID = "8999996001";
    String KWAI_REWARD_ID1 = "8999996002";
    String KWAI_REWARD_ID2 = "8999996003";

    // AdJust
    String AdJUST_ID = "3ytvg8mos4ow";
    String ADJUST_FB_APP_ID = "1157954929621028";

    //APP部分
    String TRUE_PACKAGE_NAME = "com.pandastep.wincash";
    String APP_ID = "1981659150681194497";
    String HOST_URL = "http://192.168.50.241:2888";
    String AES_IV = "dgHLDMKfBxdDjuzd";
    String AES_KEY = "RgSUPDZEjPZLWUuW";
    String PUBLIC_KEY = "BGh1caVkDfpzuM7TBqAiM1bdoB2txJPWoLey+VfMomVS2LK7ffCnjcahH7zCGgTVK0Ucxkw+3mJ+y2Mrlvt84JQ=";

    //数数
    String TA_APP_ID = "32c487ff9f2d4ca5a5815785f4500953";
    String TA_SERVER_API = "http://pssu.abutayfour.com";

    //FP配置 广告id
    String FP_ADID_KEY = "panda_step_adid";
    String FP_PRICE_KEY = "panda_step_price";

    //配置，自定义的
    String FP_NOTIFY_ID= "panda_step_notification_key";  //通知栏fpkey
    //配置，自定义的
    String FP_STEP_KEY = "step_settings_key";

    String FP_DEVICES_INFO = "panda_step_device_info"; //手机设备信息

    //配置 admob
    String FP_ADMOB_KEY = "panda_step_admob_key";

    //配置 adJust
    String FP_ADJUST_KEY = "panda_step_adjust_key";

    //Okspin
    String OKSPINE = "panda_step";
    String OKSPIN_URL = "https://s.gamifyspace.com/tml?pid=19104&appk=3U9ceSqQSl6P0hGw5T7odBOEPXPKJDEX&did={gaid}";

    //设置相关
    String EMAIL = "contactus@abutayfour.com";
    String PRIVACY_URL = "http://www.baidu.com";

    //广告价值上报接口
    String UPLOAD_API = "/iap/E1YqAI/QoAv35GsK";//实际接口：/iap/upData/adRevenue
    //配置
    String FP_API = "/iap/8MGS/thyQ/97k9";  //实际接口：/iap/conf/json/fpV2
    //提现记录
    String RECORD_API = "/iap/upObBZLg/X032";//实际接口：/iap/withdraw/list
    //提现
    String CASH_API = "/iap/upObBZLg/0qVd";//实际接口：/iap/withdraw/cash
    //信息收集接口
    String DEVICES_API = "/iap/xd/EDt/0J4bVE8yHo"; //实际接口：/iap/up/iaa/deviceInfo

    String LINK_I = "https://s.gamifyspace.com/tml?pid=19104&appk=3U9ceSqQSl6P0hGw5T7odBOEPXPKJDEX&did={gaid}";
}