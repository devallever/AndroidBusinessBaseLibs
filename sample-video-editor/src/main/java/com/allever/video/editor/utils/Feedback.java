package com.allever.video.editor.utils;

import android.content.Context;

import com.android.absbase.utils.ToastUtils;

/**
 *
 */

public class Feedback {

    public static void feedback(Context context) {
        ToastUtils.INSTANCE.show("feedback");
    }

}
