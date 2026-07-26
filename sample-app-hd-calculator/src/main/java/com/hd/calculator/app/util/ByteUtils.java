package com.hd.calculator.app.util;

public class ByteUtils {

    public static byte[] mergeByteArrays(byte[]... arrays) {
        // 计算总长度
        int totalLength = 0;
        for (byte[] array : arrays) {
            if (array != null) {
                totalLength += array.length;
            }
        }

        // 创建结果数组
        byte[] result = new byte[totalLength];
        int destPos = 0;

        // 逐个复制数组
        for (byte[] array : arrays) {
            if (array != null && array.length > 0) {
                System.arraycopy(array, 0, result, destPos, array.length);
                destPos += array.length;
            }
        }
        return result;
    }

}
