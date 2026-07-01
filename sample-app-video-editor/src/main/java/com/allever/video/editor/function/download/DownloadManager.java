package com.allever.video.editor.function.download;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadManager {

    private static Map<String, IDownload> sDownloadExecutorMap = new HashMap<>();

    private DownloadManager() {
    }

    private static class Holder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    public static DownloadManager getInstance() {
        return Holder.INSTANCE;
    }

    public void start(final TaskInfo taskInfo, final DownloadCallback callback, boolean forcibly) {
        IDownload download = sDownloadExecutorMap.get(taskInfo.getUrl());
        if (download == null) {
            if (!forcibly && taskInfo.exists()) {
                return;
            }
            download = new OkDownloadExecutor(taskInfo, new DownloadCallback() {

                @Override
                public void onStart() {
                    callback.onStart();
                }

                @Override
                public void onConnected(long totalLength) {
                    callback.onConnected(totalLength);
                }

                @Override
                public void onProgress(long current, long totalLength) {
                    callback.onProgress(current, totalLength);
                }

                @Override
                public void onPause(TaskInfo taskInfo) {
                    callback.onPause(taskInfo);
                }

                @Override
                public void onCompleted(TaskInfo taskInfo) {
                    callback.onCompleted(taskInfo);
                    sDownloadExecutorMap.remove(taskInfo.getUrl());
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                    sDownloadExecutorMap.remove(taskInfo.getUrl());
                }
            });

            sDownloadExecutorMap.put(taskInfo.getUrl(), download);
            download.start();
        } else {
            if (callback != null) {
                download.addListener(callback);
            }
        }

    }

    /*

        fun start(taskInfo: TaskInfo, callback: DownloadCallback? = null, forcibly: Boolean = false) {
        var download = sDownloadExecutorMap[taskInfo.url]
        if (download == null) {
            if (!forcibly && taskInfo.exists()) {
                return
            }
            download = OkDownloadExecutor(taskInfo, object : DownloadCallback {
                override fun onStart() {
                    callback?.onStart()
                }

                override fun onConnected(totalLength: Long) {
                    callback?.onConnected(totalLength)
                }

                override fun onProgress(current: Long, totalLength: Long) {
                    callback?.onProgress(current, totalLength)
                }

                override fun onPause(taskInfo: TaskInfo) {
                    callback?.onPause(taskInfo)
                }

                override fun onCompleted(taskInfo: TaskInfo) {
                    callback?.onCompleted(taskInfo)

                    sDownloadExecutorMap.remove(taskInfo.url)
                }

                override fun onError(e: Exception) {
                    callback?.onError(e)
                    sDownloadExecutorMap.remove(taskInfo.url)
                }
            })
            sDownloadExecutorMap[taskInfo.url] = download
            download.start()
        } else {
            if (callback != null) {
                download.addListener(callback)
            }
        }
    }

     */

    public void start(IDownload downloadExecutor) {
        if (downloadExecutor != null && sDownloadExecutorMap != null) {
            sDownloadExecutorMap.put(downloadExecutor.getUrl(), downloadExecutor);
            downloadExecutor.start();
        }
    }

    public void startMultiTask(List<IDownload> downloadExecutorList) {
        if (downloadExecutorList == null || sDownloadExecutorMap == null) {
            return;
        }
        for (IDownload downloadExecutor : downloadExecutorList) {
            sDownloadExecutorMap.put(downloadExecutor.getUrl(), downloadExecutor);
            downloadExecutor.start();
        }
    }

    public void startAllTask() {
        if (sDownloadExecutorMap == null) {
            return;
        }
        for (IDownload downloadExecutor : sDownloadExecutorMap.values()) {
            if (downloadExecutor != null) {
                downloadExecutor.start();
            }
        }
    }

    public void pause(String... urls) {
        pause(Arrays.asList(urls));
    }

    public void pause(IDownload downloadExecutor) {
        if (downloadExecutor != null) {
            pause(downloadExecutor.getUrl());
        }
    }

    public void pause(List<String> urlList) {
        if (urlList == null || sDownloadExecutorMap == null) {
            return;
        }
        IDownload downloadExecutor;
        for (String url : urlList) {
            downloadExecutor = sDownloadExecutorMap.get(url);
            if (downloadExecutor != null) {
                downloadExecutor.pause();
            }
        }
    }

    public void pauseAllTask() {
        if (sDownloadExecutorMap == null) {
            return;
        }
        for (IDownload downloadExecutor : sDownloadExecutorMap.values()) {
            if (downloadExecutor != null) {
                downloadExecutor.pause();
            }
        }
    }

    public void cancel(String... urls) {
        cancel(Arrays.asList(urls));
    }

    public void cancel(List<String> urlList) {
        if (urlList == null || sDownloadExecutorMap == null) {
            return;
        }
        IDownload downloadExecutor;
        for (String url : urlList) {
            downloadExecutor = sDownloadExecutorMap.get(url);
            if (downloadExecutor != null) {
                downloadExecutor.cancel();
            }
        }
    }

    public void cancel(IDownload downloadExecutor) {
        if (downloadExecutor != null) {
            cancel(downloadExecutor.getUrl());
        }
    }

    public void cancelAllTask() {
        if (sDownloadExecutorMap == null) {
            return;
        }
        for (IDownload downloadExecutor : sDownloadExecutorMap.values()) {
            if (downloadExecutor != null) {
                downloadExecutor.cancel();
            }
        }
    }

    public void removeTask(String... urls) {
        removeTask(Arrays.asList(urls));
    }

    public void removeTask(List<String> urlList) {
        if (urlList == null || sDownloadExecutorMap == null) {
            return;
        }
        for (String url : urlList) {
            sDownloadExecutorMap.remove(url);
        }
    }

    public void removeAllTask() {
        if (sDownloadExecutorMap != null) {
            sDownloadExecutorMap.clear();
        }
//        for (Map.Entry<String, IDownload> entry: sDownloadExecutorMap.entrySet()){
//            sDownloadExecutorMap.remove(entry.getKey());
//        }
    }

    public boolean isDownload(IDownload downloadExecutor) {
        if (downloadExecutor == null) {
            return false;
        }
        return downloadExecutor.isDownloaded();
    }

    public int getStatus(String url) {
        if (sDownloadExecutorMap == null) {
            return IDownload.Status.UNKNOWN;
        }
        IDownload downloadExecutor = sDownloadExecutorMap.get(url);
        if (downloadExecutor != null) {
            return downloadExecutor.getStatus();
        }
        return IDownload.Status.UNKNOWN;
    }
}
