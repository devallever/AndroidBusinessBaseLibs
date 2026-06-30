package com.allever.video.editor.utils;

public class EncryptConstant {
    public static String decodeBase64(String content) {
        String result = "";
        try {
            result = new String(Base64.decode(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
