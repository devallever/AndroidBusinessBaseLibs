package app.android.gp.ai.translator.db

import androidx.annotation.Keep
import org.litepal.crud.LitePalSupport

@Keep
class TTS : LitePalSupport() {
    var content: String = ""
    var tl: String = ""
    var path: String = ""
}