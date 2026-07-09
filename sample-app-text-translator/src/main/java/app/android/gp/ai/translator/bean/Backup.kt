package app.android.gp.ai.translator.bean

import androidx.annotation.Keep
import app.android.gp.ai.translator.db.History

@Keep
class Backup {
    var data: MutableList<History>? = null
}