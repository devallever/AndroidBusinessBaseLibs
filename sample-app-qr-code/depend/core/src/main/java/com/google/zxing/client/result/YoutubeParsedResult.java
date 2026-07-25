package com.google.zxing.client.result;

public class YoutubeParsedResult extends ParsedResult {
    private String mUserId;
    private String mUrl;

    protected YoutubeParsedResult() {
        super(ParsedResultType.YOUTUBE);
        mUserId = null;
    }

    protected YoutubeParsedResult(String userId) {
        super(ParsedResultType.YOUTUBE);

        mUserId = userId;
        mUrl = null;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String getDisplayResult() {
        if (mUserId != null) {
            return mUserId;
        }
        return mUrl;
    }

    public String getUserId() {
        return mUserId;
    }

    @Override
    public String revertRawData() {
        //https://www.youtube.com/channel/UCy8-vp4NblZvyBpU6sqA3ow
//        return String.format("https://www.youtube.com/results?search_query=%s", mUserId);
//        return String.format("vnd.youtube://youtube.com/results?search_query=%s", mUserId);
        if (mUserId != null) {
            return String.format("%s%s", YoutubeResultParser.SEARCH_PREFIX, mUserId);
        }
        return mUrl;
    }

    public static ParsedResult build(String userId) {
        return new YoutubeParsedResult(userId);
    }
}


