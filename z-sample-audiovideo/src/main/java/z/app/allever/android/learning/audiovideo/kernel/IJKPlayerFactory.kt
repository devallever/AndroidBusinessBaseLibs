package z.app.allever.android.learning.audiovideo.kernel

import z.app.allever.android.learning.audiovideo.kernel.internal.AbsPlayerFactory

/**
 * IJKPlayer工厂类
 */
class IJKPlayerFactory : AbsPlayerFactory() {
    override fun createPlayer() = IJKPlayer()
}