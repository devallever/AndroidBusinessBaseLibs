package com.google.zxing.client.result;

public class ViberParsedResult extends ParsedResult {
    private String mUserId;

    protected ViberParsedResult(String userId) {
        super(ParsedResultType.VIBER);

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
        return String.format("viber://add?number=%s", mUserId);
    }

    public static ParsedResult build(String userId) {
        return new ViberParsedResult(userId);
    }
}


