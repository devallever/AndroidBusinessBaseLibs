package z.app.allever.android.learning.audiovideo.kernel

import z.app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory

/**
 * MediaPlayer工厂类
 */
class AndroidPlayerFactory : AbsPlayerFactory() {
    override fun createPlayer() = AndroidPlayer()
}