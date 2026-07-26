package com.hd.calculator.app.util;

public class StringUtils {

    /**
     * 判断字符串是否能转换成整型
     *
     * @param input 输入字符串
     * @return true 可转换，false 不可转换
     */
    public static boolean canConvertToInt(String input) {
        if (input == null || input.isEmpty()) {
            return false; // 空值检测
        }

        // 去除两端空格
        String trimStr = input.trim();
        if (trimStr.isEmpty()) {
            return false;
        }

        // 符号检测（允许开头的+/-）
        int startIndex = 0;
        boolean isNegative = false;
        if (trimStr.charAt(0) == '-') {
            isNegative = true;
            startIndex = 1;
        } else if (trimStr.charAt(0) == '+') {
            startIndex = 1;
        }

        // 纯符号（仅"+"或"-"）
        if (startIndex >= trimStr.length()) {
            return false;
        }

        // 逐字符检测
        long value = 0; // 使用long避免计算溢出
        for (int i = startIndex; i < trimStr.length(); i++) {
            char c = trimStr.charAt(i);
            if (c < '0' || c > '9') {
                return false; // 非法字符
            }

            // 累加计算（在溢出前判断）
            int digit = c - '0';
            if (isNegative) {
                long temp = value * 10 - digit;
                if (temp < Integer.MIN_VALUE) {
                    return false; // 越界（负向）
                }
                value = (int) temp;
            } else {
                long temp = value * 10 + digit;
                if (temp > Integer.MAX_VALUE) {
                    return false; // 越界（正向）
                }
                value = (int) temp;
            }
        }
        return true;
    }

    public static int safeConvertToInt(String content) {
        if (StringUtils.isInteger(content)) {
            return Integer.parseInt(content);
        }
        return 0;
    }

    public static boolean isInteger(String content) {
        return content.matches("[0-9]+");
    }
}
