package app.allever.android.sample.audiovideo.lib.kernal

import android.os.Handler
import android.os.Looper

abstract class BasePlayerKernal<T>: IPlayerKernal<T> {

    override var mPlayer: T? = null

    protected val TAG = this::class.java.simpleName

    protected var mListener: IPlayerKernal.IListener? = null

    protected val mMainHandler = Handler(Looper.getMainLooper())
}