package com.funny.gif.memes.bean;

import org.litepal.crud.LitePalSupport;

public class LikedItem extends LitePalSupport {
    private String gifId;
    private String data;
    private int type;

    public String getGifId() {
        return gifId;
    }

    public void setGifId(String gifId) {
        this.gifId = gifId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
