package com.hd.calculator.app.util;

import com.hd.calculator.app.MyApp;

public class DisplayUtils {
    //dp转px
    public static int dp2px(float dpValue) {
        return (int) (dpValue * MyApp.context.getResources().getDisplayMetrics().density + 0.5f);
    }

    //sp转px
    public static int sp2px(float spValue) {
        return (int) (spValue * MyApp.context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    public static int getScreenWidth() {
        return MyApp.context.getResources().getDisplayMetrics().widthPixels;
    }
}
