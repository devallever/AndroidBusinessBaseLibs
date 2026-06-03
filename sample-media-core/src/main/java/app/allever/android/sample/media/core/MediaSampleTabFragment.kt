package app.allever.android.sample.media.core

import androidx.fragment.app.Fragment
import app.allever.android.lib.common.TabFragment
import app.allever.android.lib.common.TabViewModel
import app.allever.android.lib.common.databinding.FragmentTabBinding

class MediaSampleTabFragment : TabFragment<FragmentTabBinding, TabViewModel>() {
    override fun getTabTitles(): MutableList<String> = mutableListOf("MediaSample")

    override fun getFragments(): MutableList<Fragment> = mutableListOf(MediaSampleFragment())
}