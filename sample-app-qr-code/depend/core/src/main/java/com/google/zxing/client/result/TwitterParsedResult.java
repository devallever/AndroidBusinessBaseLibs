package com.google.zxing.client.result;

public class TwitterParsedResult extends ParsedResult {
    private String mUserId;

    protected TwitterParsedResult(String userId) {
        super(ParsedResultType.TWITTER);

        mUserId = userId;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(50);
        result.append(mUserId);
        return result.toString();
    }

    public String getUserId() {
        return mUserId;
    }

    @Override
    public String revertRawData() {
        return String.format("twitter://user?screen_name=%s", mUserId);
    }

    public static ParsedResult build(String userId) {
        return new TwitterParsedResult(userId);
    }
}


