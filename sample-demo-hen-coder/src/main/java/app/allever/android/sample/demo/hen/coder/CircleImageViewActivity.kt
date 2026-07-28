package app.allever.android.sample.demo.hen.coder

import android.os.Bundle
import app.allever.android.lib.core.base.AbstractActivity

class CircleImageViewActivity: AbstractActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hc_activity_circle_image_view)
        adaptStatusBar(findViewById(R.id.civ))
    }
}