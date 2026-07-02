package com.funny.gif.memes.func.download;

import java.io.File;

public class TaskInfo {

    private String url;
    private String fileName;
    private String path;
    private String tempPath;

    public TaskInfo(String fileName, String path, String url) {
        this.url = url;
        this.fileName = fileName;
        this.path = path;
    }
    public TaskInfo(String fileName, String path, String url, String tempPath) {
        this.url = url;
        this.fileName = fileName;
        this.path = path;
        this.tempPath = tempPath;
    }



    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean exists() {
        return new File(path, fileName).exists();
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }
}
