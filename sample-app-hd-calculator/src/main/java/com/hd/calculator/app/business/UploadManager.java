package com.hd.calculator.app.business;

import com.hd.calculator.app.function.network.response.UseTableResponseData;

import java.util.Map;

public class UploadManager {

    //static inner class
    private static class SingletonHolder {
        static final UploadManager INSTANCE = new UploadManager();
    }

    public static UploadManager getIns() {
        return SingletonHolder.INSTANCE;
    }

    public interface TableUseCallback {
        void onResult(Map<Integer, UseTableResponseData> usedTableOrderMap);
    }

    public interface CheckTableUsedCallback {
        void onResult(boolean canUse);
    }
}
