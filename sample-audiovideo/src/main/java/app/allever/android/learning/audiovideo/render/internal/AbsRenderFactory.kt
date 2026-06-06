package app.allever.android.learning.audiovideo.render.internal

import android.content.Context
import app.allever.android.learning.audiovideo.render.internal.IRenderView

abstract class AbsRenderFactory {
    companion object {
        inline fun <reified F> create(): F = F::class.java.newInstance()
    }

    abstract fun createRender(context: Context): IRenderView
}