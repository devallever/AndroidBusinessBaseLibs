package com.hd.calculator.app.function.network;

import com.hd.calculator.app.util.LogUtils;

public interface NetworkCallback<R> {
    void onSuccess(R data);

    default void onFailure(String msg) {
        LogUtils.log("Request fail: " + msg);
    }
}
