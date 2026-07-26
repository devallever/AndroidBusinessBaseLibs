package com.hd.calculator.app.constant.log;

import java.util.HashMap;
import java.util.Map;

/***
 * 操作类型
 */
public class ActionType {
    //出单
    public static final int MAKE_ORDER = 1;
    //转移桌
    public static final int TRANSFER = 2;
    //输入boss密码后强制使用
    public static final int FORCE_USE_TABLE_BOSS_PASSWORD = 3;
    //删除桌
    public static final int DELETE_ORDER = 4;
    //减少菜品
    public static final int REDUCE_DISHES = 5;
    //买单结账
    public static final int BILL = 6;
    //拆分账单
    public static final int SPLIT_BILL = 7;
    //分桌
    public static final int SPLIT_TABLE = 8;
    //上传离线数据
    public static final int UPLOAD_OFFLINE_DATA = 9;

    //typeMap
    private static Map<Integer, String> typeMap = new HashMap<>();

    static {
        typeMap.put(MAKE_ORDER, "出单");
        typeMap.put(TRANSFER, "转移桌");
        typeMap.put(FORCE_USE_TABLE_BOSS_PASSWORD, "输入boss密码后强制使用");
        typeMap.put(DELETE_ORDER, "删除桌");
        typeMap.put(REDUCE_DISHES, "减少菜品");
        typeMap.put(BILL, "买单结账");
        typeMap.put(SPLIT_BILL, "拆分账单");
        typeMap.put(SPLIT_TABLE, "分桌");
        typeMap.put(UPLOAD_OFFLINE_DATA, "上传离线数据");

//        typeMap.put(DINE_IN, "堂食");
//        typeMap.put(TAKE_OUT, "外带");
//        typeMap.put(CHOOSE_TABLE, "选桌");
//        typeMap.put(INPUT_TABLE_NUM, "输入桌号");
//        typeMap.put(ORDER, "出单");
//        typeMap.put(BILL, "结账");
//        typeMap.put(UPDATE_DISHES_COUNT, "修改菜品数量");
//        typeMap.put(ADD_DISHES, "添加菜品");
//        typeMap.put(INPUT_DISHES, "输入菜品");
//        typeMap.put(SPLIT_TABLE, "拆桌");
//        typeMap.put(SPLIT_BILL, "拆账单");
//        typeMap.put(ORDER_PRINT, "出单和打印");
//        typeMap.put(ORDER_NOT_PRINT, "出单不打印");
//        typeMap.put(CANCEL_UPDATE, "取消修改");
//        typeMap.put(TRANSFER_TABLE, "转移桌");
//        typeMap.put(RECOVER_TABLE, "恢复桌");
//        typeMap.put(DELETE_TABLE, "删除桌");
//        typeMap.put(PRINT_ORDER, "打印订单");
//        typeMap.put(PRINT_ORDER_FAIL, "打印订单失败");
    }

    public static String getTypeName(int type) {
        return typeMap.get(type);
    }
}
