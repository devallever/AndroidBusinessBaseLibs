package com.allever.video.editor.function.download;

public interface IDownload {
    void start();

    void pause();

    void cancel();

    boolean isDownloaded();

    String getUrl();

    int getStatus();

    void addListener(DownloadCallback callback);

    class Status {
        public static int UNKNOWN = -1;
        public static int INIT = 0;
        public static int DOWNLOADING = 1;
        public static int PAUSE = 2;
        public static int COMPLETED = 3;
        public static int CANCEL = 4;
    }
}
