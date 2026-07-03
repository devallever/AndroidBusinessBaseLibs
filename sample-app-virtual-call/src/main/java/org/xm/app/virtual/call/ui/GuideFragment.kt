package org.xm.app.virtual.call.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allever.app.virtual.call.R
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.base.AbstractFragment

class GuideFragment : AbstractFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =
            LayoutInflater.from(App.context).inflate(R.layout.vc_fragment_guide, container, false)

        return view
    }

}