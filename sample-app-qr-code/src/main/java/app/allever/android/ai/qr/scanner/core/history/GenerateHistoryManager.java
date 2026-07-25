package app.allever.android.ai.qr.scanner.core.history;

import android.content.Context;

import com.google.zxing.client.android.history.HistoryManager;

/**
 * @author allever
 */
public class GenerateHistoryManager extends HistoryManager {

    private static final String DB_NAME = "qrcode_create_history.db";

    public GenerateHistoryManager(Context context) {
        super(context, DB_NAME);
    }
}
