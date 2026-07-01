package com.allever.video.editor.function.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;

import java.io.File;

public class OkDownloadExecutor implements IDownload {

    private static final String TAG = OkDownloadExecutor.class.getSimpleName();

    private String fileName;
    private String path;
    private String url;
    private int status = Status.INIT;

    private DownloadTask mTask;
    private TaskInfo mTaskInfo;
    private DownloadCallback mCallback;

    private DownloadListener mDownloadListener;

    public OkDownloadExecutor(TaskInfo taskInfo, DownloadCallback downloadCallback) {
        mTaskInfo = taskInfo;
        this.fileName = taskInfo.getFileName();
        this.path = taskInfo.getPath();
        this.url = taskInfo.getUrl();
        mCallback = downloadCallback;
    }

    public OkDownloadExecutor(String fileName, String path, String url, DownloadCallback downloadCallback) {
        this.fileName = fileName;
        this.path = path;
        this.url = url;
        mCallback = downloadCallback;
        mTaskInfo = new TaskInfo(fileName, path, url);
    }

    private void init() {
        mDownloadListener = new DownloadListener1() {
            @Override
            public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
                if (mCallback != null) {
                    mCallback.onStart();
                }
            }

            @Override
            public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
            }

            @Override
            public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
                if (mCallback != null) {
                    mCallback.onConnected(totalLength);
                }
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
                if (mCallback != null) {
                    mCallback.onProgress(currentOffset, totalLength);
                }
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
                if (mCallback != null && cause == EndCause.COMPLETED) {
                    mCallback.onCompleted(mTaskInfo);
                    setStatus(Status.COMPLETED);
                }
                if (mCallback != null && cause == EndCause.ERROR) {
                    mCallback.onError(realCause);
                    setStatus(Status.UNKNOWN);
                }

                if (mCallback != null && cause == EndCause.CANCELED) {
                    setStatus(Status.PAUSE);
                    mCallback.onPause(mTaskInfo);
                }
            }
        };

        mTask = new DownloadTask.Builder(url, path, fileName)
                .setFilename(fileName)
                // the minimal interval millisecond for callback progress
                .setMinIntervalMillisCallbackProcess(100)
                // do re-download even if the task has already been completed in the past.
                //false: 会重新下载， true: 跳过
                .setPassIfAlreadyCompleted(false)
                .build();
    }

    @Override
    public void start() {
        if (mTask == null) {
            init();
        }
        if (status != Status.DOWNLOADING) {
            mTask.enqueue(mDownloadListener);
            setStatus(Status.DOWNLOADING);
        }
    }

    @Override
    public void pause() {
        if (mTask != null) {
            //OkDownloader没有暂停，cancel用作暂停
            mTask.cancel();
            setStatus(Status.PAUSE);
        }
    }

    @Override
    public void cancel() {
        if (mTask != null) {
            mTask.cancel();
            setStatus(Status.CANCEL);
        }
    }

    @Override
    public boolean isDownloaded() {
        File target = new File(path, fileName);
        return target.exists();
//        StatusUtil.Status status = StatusUtil.getStatus(mTask);
//        return status == StatusUtil.Status.COMPLETED;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void addListener(DownloadCallback callback) {

    }

    private void setStatus(int status) {
        Log.d(TAG, "setStatus: status = " + status);
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }


}
