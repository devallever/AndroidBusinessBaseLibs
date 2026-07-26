package com.hd.calculator.app.function.printer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PC858Encoder {

    private static final Map<Character, Byte> CHAR_TO_PC858 = createCharToByteMap();
    private static final char EURO_SYMBOL = '€';
    private static final byte DEFAULT_CHAR = 0x20; // 空格用于替换不支持的字符

    // 创建字符到PC858字节的映射
    private static Map<Character, Byte> createCharToByteMap() {
        Map<Character, Byte> map = new HashMap<>();

        // 西欧语言字符 (128-175)
        addMapping(map, 128, 'Ç', 'ü', 'é', 'â', 'ä', 'à', 'å', 'ç', 'ê', 'ë', 'è', 'ï', 'î');
        addMapping(map, 142, 'ì', 'Ä', 'Å', 'É', 'æ', 'Æ', 'ô', 'ö', 'ò', 'û', 'ù', 'ÿ', 'Ö');
        addMapping(map, 156, 'Ü', '¢', '£', '¥', '₧', 'ƒ', 'á', 'í', 'ó', 'ú', 'ñ', 'Ñ', 'ª');
        addMapping(map, 170, 'º', '¿', '⌐', '¬', '½', '¼', '¡', '«', '»');

        // 框线/图形符号 (176-223)
        addMapping(map, 176, '░', '▒', '▓', '│', '┤', '╡', '╢', '╖', '╕', '╣', '║', '╗', '╝');
        addMapping(map, 189, '╜', '╛', '┐', '└', '┴', '┬', '├', '─', '┼', '╞', '╟', '╚', '╔');
        addMapping(map, 202, '╩', '╦', '╠', '═', '╬', '╧', '╨', '╤', '╥', '╙', '╘', '╒', '╓');
        addMapping(map, 215, '╫', '╪', '┘', '┌', '█', '▄', '▌', '▐', '▀');

        // 希腊字母/数学符号 (224-255)
        addMapping(map, 224, 'α', 'ß', 'Γ', 'π', 'Σ', 'σ', 'µ', 'τ', 'Φ', 'Θ', 'Ω', 'δ', '∞');
        addMapping(map, 237, 'φ', 'ε', '∩', '≡', '±', '≥', '≤');
        addMapping(map, 244, EURO_SYMBOL, '⌠', '⌡', '÷', '≈', '°', '∙', '·', '√', 'ⁿ', '²', '■');

        return Map.copyOf(map);
    }

    // 辅助方法：批量添加字符映射
    private static void addMapping(Map<Character, Byte> map, int startCode, char... chars) {
        for (int i = 0; i < chars.length; i++) {
            map.put(chars[i], (byte) (startCode + i));
        }
    }

    /**
     * 将字符串编码为PC858字节序列
     * @param input 包含扩展字符的字符串
     * @return PC858编码的字节数组
     */
    public static byte[] encodeToPC858(String input) {
        if (input == null || input.isEmpty()) return new byte[0];

        byte[] result = new byte[input.length()];

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 处理ASCII字符
            if (c < 128) {
                result[i] = (byte) c;
            }
            // 特殊处理欧元符号
            else if (c == EURO_SYMBOL) {
                result[i] = (byte) 0xF4; // 244 in decimal
            }
            // 处理其他扩展字符
            else if (CHAR_TO_PC858.containsKey(c)) {
                result[i] = CHAR_TO_PC858.get(c);
            }
            // 处理不支持的字符（用空格替代）
            else {
                result[i] = DEFAULT_CHAR;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // 测试示例
        String testStr = "Euro: € | Graphic: █ | Greek: Ω | Currency: £";
        byte[] encoded = encodeToPC858(testStr);

        System.out.println("Original: " + testStr);
        System.out.println("Encoded: " + Arrays.toString(encoded));

        // 验证特定字符的编码
        System.out.println("€ encoded as: " + (encoded[5] & 0xFF)); // 244
        System.out.println("█ encoded as: " + (encoded[17] & 0xFF)); // 219
        System.out.println("Ω encoded as: " + (encoded[27] & 0xFF)); // 234
        System.out.println("£ encoded as: " + (encoded[41] & 0xFF)); // 156
    }
}