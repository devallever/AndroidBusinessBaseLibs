package app.allever.android.learning.audiovideo.kernel

import app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory

/**
 * MediaPlayer工厂类
 */
class AndroidPlayerFactory : AbsPlayerFactory() {
    override fun createPlayer() = AndroidPlayer()
}