package com.google.zxing.client.result;

public class WhatsappParsedResult extends ParsedResult {
    private String mUserId;

    protected WhatsappParsedResult(String userId) {
        super(ParsedResultType.WHATSAPP);

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
        return String.format("whatsapp://send?phone=%s", mUserId);
    }

    public static ParsedResult build(String userId) {
        return new WhatsappParsedResult(userId);
    }
}


