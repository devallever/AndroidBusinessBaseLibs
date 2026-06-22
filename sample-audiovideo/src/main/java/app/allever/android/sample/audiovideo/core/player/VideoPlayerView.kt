package app.allever.android.sample.audiovideo.core.player

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import app.allever.android.lib.media.core.model.MediaItem
import app.allever.android.sample.audiovideo.databinding.VideoPlayerViewBinding

class VideoPlayerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs){

    private var binding: VideoPlayerViewBinding =
        VideoPlayerViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var mMediaBean: MediaItem? = null

}