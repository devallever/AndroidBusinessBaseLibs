package com.funny.gif.memes.func.download;

public interface DownloadCallback {
    void onStart();

    void onConnected(long totalLength);

    void onProgress(long current, long totalLength, TaskInfo taskInfo);

    void onPause(TaskInfo taskInfo);

    void onCompleted(TaskInfo taskInfo);

    void onError(Exception e, TaskInfo taskInfo);
}
