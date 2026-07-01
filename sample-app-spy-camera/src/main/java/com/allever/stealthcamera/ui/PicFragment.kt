package com.allever.stealthcamera.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.allever.android.lib.core.base.AbstractFragment
import org.xm.stealth.camera.R
import com.bumptech.glide.Glide

/**
 * Created by Allever on 18/5/16.
 */

class PicFragment : AbstractFragment {
    private var mImgPath: String? = null

    constructor()
    @SuppressLint("ValidFragment")
    constructor(imgPath: String) {
        mImgPath = imgPath
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.spy_fragment_pic, container, false)
        val iv = view.findViewById<ImageView>(R.id.id_fg_pic_iv)
        Glide.with(requireActivity()).load(mImgPath).into(iv)
        return view
    }

}
