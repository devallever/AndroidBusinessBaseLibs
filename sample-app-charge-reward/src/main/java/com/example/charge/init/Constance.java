package com.example.charge.init;


public interface Constance {

    //配置，自定义的
    String FP_WITHDRAW_KEY = "charge_reward_settings";

    String OKSPINE = "charge_reward";
    String OKSPIN_URL = "https://s.gamifyspace.com/tml?pid=19359&appk=lxuKZYXV1JEwbC6FKILiw8FO6y67If48&did={gaid}";

    //设置相关
    String EMAIL = "feedback@nextvisionstudios.com";
    String PRIVACY_URL = "https://nextvisionstudios.com/privacy.html";

    //org url
    String ORG_URL = "http://ip-api.com/json/?fields=189696";

    String HOST_URL = false ? "http://capri.nextvisionstudios.com/" : "http://192.168.50.244:2266/";

    //旧
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
    String ADJUST_FB_APP_ID = "1589039182309853";

    //APP部分
    String TRUE_PACKAGE_NAME = "com.example.sanxiao";
    String APP_ID = "1980845332057149442";
    String AES_IV = "NBKqncxOXrSXZugq";
    String AES_KEY = "chptEumRvaTSZFvV";
    String PUBLIC_KEY = "BBYnipNH8NyQf+wjDc/7iQcFlLkqN1YSI/WAOXjqjN3nWVZz6wxAvr9U2mVhidj3qGQPtX4PdgarQRQdkXeaNyc=";

    //数数
    String TA_APP_ID = "947e20f38cc545d387fcf7d496e046ec";
    String TA_SERVER_API = "http://bsbu.fireappstudios.com";

    //FP配置 广告id
    String FP_ADID_KEY = "plinko_wincash_adid";
    String FP_PRICE_KEY = "plinko_wincash_price";

    //配置，自定义的
    String FP_GAME_SETTINGS_KEY = "plinko_wincash_game_settings";

    //配置 admob
    String FP_ADMOB_KEY = "plinko_wincash_admob_key";
//    String FP_WITHDRAW_KEY = "charge_reward_settings";

    //配置 adJust
    String FP_ADJUST_KEY = "plinko_wincash_adjust_key";

    String Ad_LOAD_KEY = "plinko_adload_key";

    //Okspin
//    String OKSPINE = "cash_online_earning";
//    String OKSPIN_URL = "https://s.gamifyspace.com/tml?pid=18563&appk=5rH3loYD4MqeiXtL1IWLZSUnqAbnsx4a&did={gaid}";

    //设置相关
//    String EMAIL = "feedback@nextvisionapp.com";
//    String PRIVACY_URL = "https://nextvisionapp.com/privacy.html";

    //广告价值上报接口
    String UPLOAD_API = "/iap/WGlR78/jQNS65D1S";//实际接口：/iap/upData/adRevenue
    //配置
    String FP_API = "/iap/q7n6/BBPz/N371";  //实际接口：/iap/conf/json/fpV2
    //提现记录
    String RECORD_API = "/iap/v1rRuNBf/6gsw";//实际接口：/iap/withdraw/list
    //提现
    String CASH_API = "/iap/v1rRuNBf/K8Ym";//实际接口：/iap/withdraw/cash
}
