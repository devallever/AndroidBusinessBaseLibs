package z.app.allever.android.learning.audiovideo.render

import android.content.Context
import z.app.allever.android.learning.audiovideo.render.internal.AbsRenderFactory
import z.app.allever.android.learning.audiovideo.render.internal.IRenderView

class SurfaceRenderFactory : AbsRenderFactory() {

    override fun createRender(context: Context): IRenderView {
        return SurfaceRenderView(context)
    }
}