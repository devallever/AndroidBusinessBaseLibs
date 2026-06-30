package com.allever.video.editor.function.emoji;

import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import com.android.absbase.App;

import com.allever.video.editor.utils.AssetsUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EmojiUtils {
    private static final String EMOJI_ASSERT_DIR = "emoji";
    private static Map<String, String> sEmojiMaps = new HashMap<>();

    static {
        try {
            String[] files = App.getContext().getAssets().list(EMOJI_ASSERT_DIR);
            for (String file : files) {
                String[] cols = file.split("\\.");
                sEmojiMaps.put(cols[0].toLowerCase(), EMOJI_ASSERT_DIR + File.separator + file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static String getEmojiAssertPath(String uni) {
        String path = sEmojiMaps.get(uni.toLowerCase());
        return path;
    }

    @Nullable
    public static InputStream getEmojiInputStream(String uni) {
        String emojiAssertPath = getEmojiAssertPath(uni);
        return AssetsUtil.INSTANCE.toInputStream(emojiAssertPath);
    }

    @Nullable
    public static Bitmap getEmojiBitmap(String uni) {
        String emojiAssertPath = getEmojiAssertPath(uni);
        return AssetsUtil.INSTANCE.toBitmap(emojiAssertPath);
    }
}
