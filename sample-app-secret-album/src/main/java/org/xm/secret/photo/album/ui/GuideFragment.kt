package org.xm.secret.photo.album.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.base.AbstractFragment
import org.xm.secret.photo.album.R

class GuideFragment: AbstractFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(App.context).inflate(R.layout.fragment_guide, container, false)
        return view
    }

}