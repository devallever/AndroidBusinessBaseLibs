package com.allever.video.editor.utils;

import android.content.Context;

import com.android.absbase.utils.ToastUtils;
import com.allever.video.editor.BuildConfig;
import com.allever.video.editor.R;

/**
 *
 */

public class Feedback {

    public static void feedback(Context context) {
        ToastUtils.INSTANCE.show("feedback");
    }

}
