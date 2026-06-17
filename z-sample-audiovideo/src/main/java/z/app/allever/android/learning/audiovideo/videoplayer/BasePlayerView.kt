package z.app.allever.android.learning.audiovideo.videoplayer

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import z.app.allever.android.learning.audiovideo.videoplayer.BasePlayerHandler
import app.allever.android.lib.media.core.model.MediaItem

open class BasePlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    constructor(
        basePlayerHandler: BasePlayerHandler,
        context: Context,
        attrs: AttributeSet? = null
    ) : this(context, attrs)

    protected var mMediaBean: MediaItem? = null


}