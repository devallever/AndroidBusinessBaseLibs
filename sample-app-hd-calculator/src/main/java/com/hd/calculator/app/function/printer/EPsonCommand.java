package com.hd.calculator.app.function.printer;

public class EPsonCommand {
    public static byte[] INIT = {27,64};//初始化打印机 ESC @
    public static byte[] CLOSE_CHINESE = {28,46};//关闭中文字库
    public static byte[] OPEN_CHINESE = {28,33};//选择汉字打印模式
    public static byte[] CHINESE_CODE_MODE = {28,38}; //选择汉字字符模式
    public static byte[] SET_CODE_PC858 = {27,116,19};//设置代码页 PC858（n = 19）
}
