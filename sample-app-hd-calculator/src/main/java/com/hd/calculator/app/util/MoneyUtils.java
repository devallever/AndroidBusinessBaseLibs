package com.hd.calculator.app.util;

import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {
    public static String formatMoney(float money) {
        // 使用德国Locale（欧元、逗号小数点、符号后置）
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        // 禁用千位分隔符（分组）
        formatter.setGroupingUsed(false);
        return formatter.format(money);
    }

    //将120,00 转换为120.00
    public static float formatMoney(String money) {
        return Float.parseFloat(money.replace(",", "."));
    }
}
