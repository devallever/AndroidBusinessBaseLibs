package z.app.allever.android.sample.ui.ui.dragclose

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding
import app.allever.android.lib.core.ui.EmptyFragment

class DragCloseMainFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles() = mutableListOf(
        "DragCloseHelper", "DragPhotoView"
    )

    override fun getFragments(): MutableList<Fragment> = mutableListOf(
        DragCloseFragment(),
        EmptyFragment("DragPhotoView方式"),
    )
}