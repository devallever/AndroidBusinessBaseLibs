package app.allever.android.sample.audiovideo.android.base

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

abstract class BasePlayerKernal<T>: IPlayerKernal<T> {

    override var mPlayer: T? = null

    protected val TAG = this::class.java.simpleName

    protected var mListener: IPlayerKernal.IListener? = null

    protected val mMainHandler = Handler(Looper.getMainLooper())
}