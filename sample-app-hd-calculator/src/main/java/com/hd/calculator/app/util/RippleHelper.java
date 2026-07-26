package com.hd.calculator.app.util;

import android.view.View;

public class RippleHelper {
    public static void setOnClickListener(View view, Runnable action) {


        view.post(new Runnable() {
            @Override
            public void run() {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int viewWidth = view.getWidth();
//                        LogUtils.d("screenWidth = " + DisplayUtils.getScreenWidth());
//                        LogUtils.d("viewWidth = " + viewWidth);
                        long delay = (long) (viewWidth * (200f / (DisplayUtils.getScreenWidth())));
                        if (delay < 100) {
                            delay = 100;
                        }
                        if (view.getForeground() == null) {
                            delay = 0;
                        }
//                        LogUtils.d("delay = " + delay);
                        view.postDelayed(action, delay);
                    }
                });

            }
        });

    }
}
