package com.funny.gif.memes.bean;

public class UserBean {
    /**
     * avatar_url : https://media3.giphy.com/avatars/justin/Ym9RjQrTsFKT.gif
     * banner_image : https://media3.giphy.com/channel_assets/justino/c3v3Q6fKnSeR.gif
     * banner_url : https://media3.giphy.com/channel_assets/justino/c3v3Q6fKnSeR.gif
     * profile_url : https://giphy.com/justin/
     * username : justin
     * display_name : Justin
     * is_verified : false
     */

    private String avatar_url;
    private String banner_image;
    private String banner_url;
    private String profile_url;
    private String username;
    private String display_name;
    private boolean is_verified;

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getBanner_image() {
        return banner_image;
    }

    public void setBanner_image(String banner_image) {
        this.banner_image = banner_image;
    }

    public String getBanner_url() {
        return banner_url;
    }

    public void setBanner_url(String banner_url) {
        this.banner_url = banner_url;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public void setProfile_url(String profile_url) {
        this.profile_url = profile_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public boolean isIs_verified() {
        return is_verified;
    }

    public void setIs_verified(boolean is_verified) {
        this.is_verified = is_verified;
    }
}
