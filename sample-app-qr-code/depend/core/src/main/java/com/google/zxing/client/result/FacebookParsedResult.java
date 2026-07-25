package com.google.zxing.client.result;

public class FacebookParsedResult extends ParsedResult {
    private String mUserId;

    protected FacebookParsedResult(String userId) {
        super(ParsedResultType.FACEBOOK);

        mUserId = userId;
    }

    @Override
    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(50);
        result.append(mUserId);
        return result.toString();
    }

    @Override
    public String revertRawData() {
        //fb://profile?id=
        return String.format("fb://profile/%s", mUserId);
    }

    public String getUserId() {
        return mUserId;
    }

    public static ParsedResult build(String userId) {
        return new FacebookParsedResult(userId);
    }
}


