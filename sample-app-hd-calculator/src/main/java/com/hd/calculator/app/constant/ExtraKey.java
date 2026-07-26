package com.hd.calculator.app.constant;

public class ExtraKey {

    public static final int REQUEST_CODE_CHOOSE_TABLE = 1;
    public static final int REQUEST_CODE_CHOOSE_DISHES = 2;
    public static final int REQUEST_CODE_INPUT_BOSS_PWD = 3;
    public static final int REQUEST_CODE_MODIFY_DISHES_COUNT = 4;
    public static final int REQUEST_CODE_ADD_DISHES = 0x06;
    public static final int REQUEST_CODE_SPLIT_TABLE = 0x07;
    public static final int REQUEST_CODE_SPLIT_PAY = 0x08;
    public static final int REQUEST_CODE_PAYMENT = 0x09;
    public static final int REQUEST_CODE_MODIFY_PWD = 0x10;
    public static final int REQUEST_CODE_PAY_CONFIRM = 0x11;
    public static final int REQUEST_CODE_SELECT_USER_LOGIN = 0x12;


    //EXTRA_KEY
    public static final String TABLE_CODE = "TABLE_CODE";
    public static final String SPLIT_TARGET_TABLE_CODE = "SPLIT_TARGET_TABLE_CODE";
    public static final String ORDER_ID = "ORDER_ID";
    public static final String ORIGIN_ORDER_ID = "ORIGIN_ORDER_ID";
    public static final String ORDER_TYPE = "ORDER_TYPE";
    public static final String AMOUNT = "AMOUNT";
    public static final String PAY_TYPE = "PAY_TYPE";
    public static final String DISHES_NAME = "DISHES_NAME";
    public static final String DISHES_COUNT = "DISHES_COUNT";
    public static final String DISHES_IS_ORDERED = "DISHES_IS_ORDERED";
    public static final String CHOOSE_TABLE_FROM = "CHOOSE_TABLE_FROM";
    public static final String CHOOSE_DISHES_FROM = "CHOOSE_DISHES_FROM";
    public static final String DISHES_ITEM_LIST = "DISHES_ITEM_LIST";
    public static final String FROM_MAIN = "FROM_MAIN";

    //ACTIVITY RESULT
    public static final String RESULT_TABLE_CODE = "RESULT_TABLE_CODE";
    public static final String RESULT_DISHES_COUNT = "RESULT_DISHES_COUNT";
    public static final String BOSS_PWD_BUNDLE = "BOSS_PWD_BUNDLE";

    public static final String BUNDLE_PWD_ACTION = "BUNDLE_PWD_ACTION";
    public static final int BOSS_PWD_ACTION_HANDLE_CLICK_MAIN_TABLE = 1;
    public static final int BOSS_PWD_ACTION_HANDLE_CLICK_UNPAID_TABLE = 2;
}
