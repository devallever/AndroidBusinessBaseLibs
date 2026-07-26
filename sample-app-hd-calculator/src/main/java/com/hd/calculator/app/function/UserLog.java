package com.hd.calculator.app.function;

import com.hd.calculator.app.function.network.NetworkRepository;
import com.hd.calculator.app.function.network.post.PostUserLog;
import com.hd.calculator.app.util.ThreadUtils;

public class UserLog {
//    public static void log(int type) {
//        ThreadUtils.runOnIoThreadDelayed(() -> {
//            GsonUtils.toJson(TypeContent.createInstance(type));
//            PostUserLog postUserLog = new PostUserLog();
//            postUserLog.setContent(GsonUtils.toJson(TypeContent.createInstance(type)));
//            postUserLog.setType(type);
//            NetworkRepository.getInstance().uploadUserLog(postUserLog, data -> { });
//        });
//    }

    public static void log(PostUserLog  postUserLog) {
        NetworkRepository.getInstance().uploadUserLog(postUserLog, data -> { });
    }
}
