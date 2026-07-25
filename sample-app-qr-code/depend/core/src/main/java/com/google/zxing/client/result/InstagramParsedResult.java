package com.google.zxing.client.result;

public class InstagramParsedResult extends ParsedResult {
    private String mUserId;

    protected InstagramParsedResult(String userId) {
        super(ParsedResultType.INSTAGRAM);

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
        return String.format("instagram://user?username=%s", mUserId);
    }

    public static ParsedResult build(String userId) {
        return new InstagramParsedResult(userId);
    }
}


