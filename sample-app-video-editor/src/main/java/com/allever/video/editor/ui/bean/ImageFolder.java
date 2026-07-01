package com.allever.video.editor.ui.bean;


import java.util.ArrayList;

public class ImageFolder {
    /**
     * 图片的文件夹路径(包含名称)
     */
    private String dir;

    /**
     * 文件夹的名称
     */
    private String name;

    /**
     * 文件夹的名称
     */
    private String bucketId;

    /**
     * 所有ThumbnailBean
     */
    private ArrayList<ThumbnailBean> datas;

    /**
     * Photo ThumbnailBean
     */
    private ArrayList<ThumbnailBean> photoThumbnailBeans;

    /**
     * Video ThumbnailBean
     */
    private ArrayList<ThumbnailBean> videoThumbnailBeans;

    /**
     * 图片的数量
     */
    private int count;

    private int photoCount;

    private int videoCount;

    /**
     * 第一张图片的ThumbnailBean
     */
    private ThumbnailBean firstThumbnailBean;

    private boolean mNeedRefresh;

    public ImageFolder() {
        count = 0;
    }

    public ImageFolder(int count) {
        this.count = count;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setDirAndName(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public void setName(String name) {
        this.name = name;
    }

    public ThumbnailBean getFirstThumbnailBean() {
        return firstThumbnailBean;
    }

    public void setFirstImageBean(ThumbnailBean thumbnailBean) {
        this.firstThumbnailBean = thumbnailBean;
    }

    public ArrayList<ThumbnailBean> getPhotoThumbnailBeans() {
        return photoThumbnailBeans;
    }

    public void setPhotoThumbnailBeans(ArrayList<ThumbnailBean> photoThumbnailBeans) {
        this.photoThumbnailBeans = photoThumbnailBeans;
    }

    public ArrayList<ThumbnailBean> getVideoThumbnailBeans() {
        return videoThumbnailBeans;
    }

    public void setVideoThumbnailBeans(ArrayList<ThumbnailBean> videoThumbnailBeans) {
        this.videoThumbnailBeans = videoThumbnailBeans;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(int mPhotoCount) {
        this.photoCount = mPhotoCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int mVideoCount) {
        this.videoCount = mVideoCount;
    }

    public void setData(ArrayList<ThumbnailBean> data) {
        this.datas = data;
    }

    public ArrayList<ThumbnailBean> getData() {
        return datas;
    }

    public boolean isDataInit() {
        return !(datas == null);
    }

    public boolean isChecked() {
        return firstThumbnailBean.isChecked();
    }

    public void setChecked(boolean flag) {
        firstThumbnailBean.setChecked(flag);
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public boolean isNeedRefresh() {
        return mNeedRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        mNeedRefresh = needRefresh;
    }
}
