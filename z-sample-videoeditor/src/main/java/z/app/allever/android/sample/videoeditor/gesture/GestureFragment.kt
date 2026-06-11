package z.app.allever.android.sample.videoeditor.gesture

import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.databinding.FragmentTabBinding
import app.allever.android.lib.core.ui.EmptyFragment
import app.allever.android.lib.mvvm.base.BaseViewModel

class GestureFragment : TabFragment<FragmentTabBinding, BaseViewModel>() {
    override fun getTabTitles() = mutableListOf(
        "GestureImageView",
        "GestureFrameLayout",
        "CustomGestureFrameLayout"
    )

    override fun getFragments() = mutableListOf(
        GestureImageViewFragment(),
        GestureFrameLayoutFragment(),
        EmptyFragment("CustomGestureFrameLayout")
    )

}