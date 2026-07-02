package com.funny.gif.memes.bean;

import com.google.gson.annotations.SerializedName;

public class ImagesBean {

    /**
     * downsized_large : {"height":"358","size":"188159","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif","width":"480"}
     * fixed_height_small_still : {"height":"100","size":"3748","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100_s.gif","width":"134"}
     * original : {"frames":"5","hash":"e0bac96c34756364d462a26b480745e3","height":"358","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.mp4","mp4_size":"62245","size":"188159","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.webp","webp_size":"52204","width":"480"}
     * fixed_height_downsampled : {"height":"200","size":"45661","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_d.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_d.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_d.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_d.webp","webp_size":"23492","width":"268"}
     * downsized_still : {"height":"358","size":"59366","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy_s.gif","width":"480"}
     * fixed_height_still : {"height":"200","size":"9610","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_s.gif","width":"268"}
     * downsized_medium : {"height":"358","size":"188159","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif","width":"480"}
     * downsized : {"height":"358","size":"188159","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif","width":"480"}
     * preview_webp : {"height":"258","size":"47310","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.webp","width":"346"}
     * original_mp4 : {"height":"358","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.mp4","mp4_size":"62245","width":"480"}
     * fixed_height_small : {"height":"100","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.mp4","mp4_size":"5667","size":"17864","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.webp","webp_size":"14720","width":"134"}
     * fixed_height : {"height":"200","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.mp4","mp4_size":"15426","size":"45661","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.webp","webp_size":"32656","width":"268"}
     * downsized_small : {"height":"358","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-downsized-small.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-downsized-small.mp4","mp4_size":"62245","width":"480"}
     * preview : {"height":"320","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.mp4","mp4_size":"19888","width":"429"}
     * fixed_width_downsampled : {"height":"149","size":"29478","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_d.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_d.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_d.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_d.webp","webp_size":"15636","width":"200"}
     * fixed_width_small_still : {"height":"75","size":"2725","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w_s.gif","width":"100"}
     * fixed_width_small : {"height":"75","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.mp4","mp4_size":"4300","size":"13017","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.webp","webp_size":"11162","width":"100"}
     * original_still : {"height":"358","size":"59366","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy_s.gif","width":"480"}
     * fixed_width_still : {"height":"149","size":"6091","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_s.gif","width":"200"}
     * looping : {"mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-loop.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-loop.mp4","mp4_size":"1836696"}
     * fixed_width : {"height":"149","mp4":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.mp4","mp4_size":"9495","size":"29478","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.gif","webp":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.webp","webp_size":"23332","width":"200"}
     * preview_gif : {"height":"150","size":"48857","url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.gif","width":"201"}
     * 480w_still : {"url":"https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/480w_s.jpg?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=480w_s.jpg","width":"480","height":"358"}
     */

    private DownsizedLargeBean downsized_large;
    private FixedHeightSmallStillBean fixed_height_small_still;
    private OriginalBean original;
    private FixedHeightDownsampledBean fixed_height_downsampled;
    private DownsizedStillBean downsized_still;
    private FixedHeightStillBean fixed_height_still;
    private DownsizedMediumBean downsized_medium;
    private DownsizedBean downsized;
    private PreviewWebpBean preview_webp;
    private OriginalMp4Bean original_mp4;
    private FixedHeightSmallBean fixed_height_small;
    private FixedHeightBean fixed_height;
    private DownsizedSmallBean downsized_small;
    private PreviewBean preview;
    private FixedWidthDownsampledBean fixed_width_downsampled;
    private FixedWidthSmallStillBean fixed_width_small_still;
    private FixedWidthSmallBean fixed_width_small;
    private OriginalStillBean original_still;
    private FixedWidthStillBean fixed_width_still;
    private LoopingBean looping;
    private FixedWidthBean fixed_width;
    private PreviewGifBean preview_gif;
    @SerializedName("480w_still")
    private _$480wStillBean _$480w_still;

    public DownsizedLargeBean getDownsized_large() {
        return downsized_large;
    }

    public void setDownsized_large(DownsizedLargeBean downsized_large) {
        this.downsized_large = downsized_large;
    }

    public FixedHeightSmallStillBean getFixed_height_small_still() {
        return fixed_height_small_still;
    }

    public void setFixed_height_small_still(FixedHeightSmallStillBean fixed_height_small_still) {
        this.fixed_height_small_still = fixed_height_small_still;
    }

    public OriginalBean getOriginal() {
        return original;
    }

    public void setOriginal(OriginalBean original) {
        this.original = original;
    }

    public FixedHeightDownsampledBean getFixed_height_downsampled() {
        return fixed_height_downsampled;
    }

    public void setFixed_height_downsampled(FixedHeightDownsampledBean fixed_height_downsampled) {
        this.fixed_height_downsampled = fixed_height_downsampled;
    }

    public DownsizedStillBean getDownsized_still() {
        return downsized_still;
    }

    public void setDownsized_still(DownsizedStillBean downsized_still) {
        this.downsized_still = downsized_still;
    }

    public FixedHeightStillBean getFixed_height_still() {
        return fixed_height_still;
    }

    public void setFixed_height_still(FixedHeightStillBean fixed_height_still) {
        this.fixed_height_still = fixed_height_still;
    }

    public DownsizedMediumBean getDownsized_medium() {
        return downsized_medium;
    }

    public void setDownsized_medium(DownsizedMediumBean downsized_medium) {
        this.downsized_medium = downsized_medium;
    }

    public DownsizedBean getDownsized() {
        return downsized;
    }

    public void setDownsized(DownsizedBean downsized) {
        this.downsized = downsized;
    }

    public PreviewWebpBean getPreview_webp() {
        return preview_webp;
    }

    public void setPreview_webp(PreviewWebpBean preview_webp) {
        this.preview_webp = preview_webp;
    }

    public OriginalMp4Bean getOriginal_mp4() {
        return original_mp4;
    }

    public void setOriginal_mp4(OriginalMp4Bean original_mp4) {
        this.original_mp4 = original_mp4;
    }

    public FixedHeightSmallBean getFixed_height_small() {
        return fixed_height_small;
    }

    public void setFixed_height_small(FixedHeightSmallBean fixed_height_small) {
        this.fixed_height_small = fixed_height_small;
    }

    public FixedHeightBean getFixed_height() {
        return fixed_height;
    }

    public void setFixed_height(FixedHeightBean fixed_height) {
        this.fixed_height = fixed_height;
    }

    public DownsizedSmallBean getDownsized_small() {
        return downsized_small;
    }

    public void setDownsized_small(DownsizedSmallBean downsized_small) {
        this.downsized_small = downsized_small;
    }

    public PreviewBean getPreview() {
        return preview;
    }

    public void setPreview(PreviewBean preview) {
        this.preview = preview;
    }

    public FixedWidthDownsampledBean getFixed_width_downsampled() {
        return fixed_width_downsampled;
    }

    public void setFixed_width_downsampled(FixedWidthDownsampledBean fixed_width_downsampled) {
        this.fixed_width_downsampled = fixed_width_downsampled;
    }

    public FixedWidthSmallStillBean getFixed_width_small_still() {
        return fixed_width_small_still;
    }

    public void setFixed_width_small_still(FixedWidthSmallStillBean fixed_width_small_still) {
        this.fixed_width_small_still = fixed_width_small_still;
    }

    public FixedWidthSmallBean getFixed_width_small() {
        return fixed_width_small;
    }

    public void setFixed_width_small(FixedWidthSmallBean fixed_width_small) {
        this.fixed_width_small = fixed_width_small;
    }

    public OriginalStillBean getOriginal_still() {
        return original_still;
    }

    public void setOriginal_still(OriginalStillBean original_still) {
        this.original_still = original_still;
    }

    public FixedWidthStillBean getFixed_width_still() {
        return fixed_width_still;
    }

    public void setFixed_width_still(FixedWidthStillBean fixed_width_still) {
        this.fixed_width_still = fixed_width_still;
    }

    public LoopingBean getLooping() {
        return looping;
    }

    public void setLooping(LoopingBean looping) {
        this.looping = looping;
    }

    public FixedWidthBean getFixed_width() {
        return fixed_width;
    }

    public void setFixed_width(FixedWidthBean fixed_width) {
        this.fixed_width = fixed_width;
    }

    public PreviewGifBean getPreview_gif() {
        return preview_gif;
    }

    public void setPreview_gif(PreviewGifBean preview_gif) {
        this.preview_gif = preview_gif;
    }

    public _$480wStillBean get_$480w_still() {
        return _$480w_still;
    }

    public void set_$480w_still(_$480wStillBean _$480w_still) {
        this._$480w_still = _$480w_still;
    }

    public static class DownsizedLargeBean {
        /**
         * height : 358
         * size : 188159
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif
         * width : 480
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedHeightSmallStillBean {
        /**
         * height : 100
         * size : 3748
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100_s.gif
         * width : 134
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class OriginalBean {
        /**
         * frames : 5
         * hash : e0bac96c34756364d462a26b480745e3
         * height : 358
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.mp4
         * mp4_size : 62245
         * size : 188159
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.webp
         * webp_size : 52204
         * width : 480
         */

        private String frames;
        private String hash;
        private String height;
        private String mp4;
        private String mp4_size;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getFrames() {
            return frames;
        }

        public void setFrames(String frames) {
            this.frames = frames;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedHeightDownsampledBean {
        /**
         * height : 200
         * size : 45661
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_d.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_d.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_d.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_d.webp
         * webp_size : 23492
         * width : 268
         */

        private String height;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class DownsizedStillBean {
        /**
         * height : 358
         * size : 59366
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy_s.gif
         * width : 480
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedHeightStillBean {
        /**
         * height : 200
         * size : 9610
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200_s.gif
         * width : 268
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class DownsizedMediumBean {
        /**
         * height : 358
         * size : 188159
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif
         * width : 480
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class DownsizedBean {
        /**
         * height : 358
         * size : 188159
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.gif
         * width : 480
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class PreviewWebpBean {
        /**
         * height : 258
         * size : 47310
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.webp
         * width : 346
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class OriginalMp4Bean {
        /**
         * height : 358
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy.mp4
         * mp4_size : 62245
         * width : 480
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedHeightSmallBean {
        /**
         * height : 100
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.mp4
         * mp4_size : 5667
         * size : 17864
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100.webp
         * webp_size : 14720
         * width : 134
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedHeightBean {
        /**
         * height : 200
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.mp4
         * mp4_size : 15426
         * size : 45661
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200.webp
         * webp_size : 32656
         * width : 268
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class DownsizedSmallBean {
        /**
         * height : 358
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-downsized-small.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-downsized-small.mp4
         * mp4_size : 62245
         * width : 480
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class PreviewBean {
        /**
         * height : 320
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.mp4
         * mp4_size : 19888
         * width : 429
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedWidthDownsampledBean {
        /**
         * height : 149
         * size : 29478
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_d.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_d.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_d.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_d.webp
         * webp_size : 15636
         * width : 200
         */

        private String height;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedWidthSmallStillBean {
        /**
         * height : 75
         * size : 2725
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w_s.gif
         * width : 100
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedWidthSmallBean {
        /**
         * height : 75
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.mp4
         * mp4_size : 4300
         * size : 13017
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/100w.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=100w.webp
         * webp_size : 11162
         * width : 100
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class OriginalStillBean {
        /**
         * height : 358
         * size : 59366
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy_s.gif
         * width : 480
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class FixedWidthStillBean {
        /**
         * height : 149
         * size : 6091
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w_s.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w_s.gif
         * width : 200
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class LoopingBean {
        /**
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-loop.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-loop.mp4
         * mp4_size : 1836696
         */

        private String mp4;
        private String mp4_size;

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }
    }

    public static class FixedWidthBean {
        /**
         * height : 149
         * mp4 : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.mp4?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.mp4
         * mp4_size : 9495
         * size : 29478
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.gif
         * webp : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/200w.webp?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=200w.webp
         * webp_size : 23332
         * width : 200
         */

        private String height;
        private String mp4;
        private String mp4_size;
        private String size;
        private String url;
        private String webp;
        private String webp_size;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getMp4() {
            return mp4;
        }

        public void setMp4(String mp4) {
            this.mp4 = mp4;
        }

        public String getMp4_size() {
            return mp4_size;
        }

        public void setMp4_size(String mp4_size) {
            this.mp4_size = mp4_size;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWebp() {
            return webp;
        }

        public void setWebp(String webp) {
            this.webp = webp;
        }

        public String getWebp_size() {
            return webp_size;
        }

        public void setWebp_size(String webp_size) {
            this.webp_size = webp_size;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class PreviewGifBean {
        /**
         * height : 150
         * size : 48857
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/giphy-preview.gif?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=giphy-preview.gif
         * width : 201
         */

        private String height;
        private String size;
        private String url;
        private String width;

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }
    }

    public static class _$480wStillBean {
        /**
         * url : https://media2.giphy.com/media/SsCLe5ozI3C3wRHL9r/480w_s.jpg?cid=089931dcae6a0aa7c63a87d522a182820d2f725c6c5dd4f7&rid=480w_s.jpg
         * width : 480
         * height : 358
         */

        private String url;
        private String width;
        private String height;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getWidth() {
            return width;
        }

        public void setWidth(String width) {
            this.width = width;
        }

        public String getHeight() {
            return height;
        }

        public void setHeight(String height) {
            this.height = height;
        }
    }
}
