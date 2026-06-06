package app.allever.android.learning.audiovideo.render

import android.content.Context
import app.allever.android.learning.audiovideo.render.internal.AbsRenderFactory
import app.allever.android.learning.audiovideo.render.internal.IRenderView

class IJKRenderFactory : AbsRenderFactory() {
    override fun createRender(context: Context): IRenderView {
        return IJKRenderView(context)
    }
}