package com.google.zxing.client.android;

import android.content.Context;

public class DeviceUtils {
    //static dp2px
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
