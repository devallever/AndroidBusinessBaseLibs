package com.allever.video.editor.ui.dialog

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.android.absbase.ui.dialog.BaseDialog
import com.allever.video.editor.R

class RetainDialog2(context: Context) : BaseDialog(context), View.OnClickListener {

    private var mRetainTitle: TextView? = null
    private var mRetainDescription: TextView? = null
    private lateinit var mImageView1: View
    private lateinit var mImageView2: View
    private lateinit var mImageView3: View
    private lateinit var mImageView4: View
    private lateinit var mImageView5: View
    private lateinit var mImageView6: View
    private lateinit var mImageView7: View
    private lateinit var mImageView9: View
    private lateinit var mImageView8: View
    private lateinit var mBtnFree: Button
    //    private var mBtnQuit: Button? = null
    private lateinit var mIvClose: ImageView

    private lateinit var mAnimTargets: MutableList<View>

    var onRetainListener: OnRetainListener? = null

    override fun initDefaultView(context: Context) {
        super.initDefaultView(context)
        setContentView(R.layout.dialog_retain)

        mRetainTitle = findViewById(R.id.tv_retain_title)
        mRetainDescription = findViewById(R.id.tv_retain_description)

        mImageView1 = findViewById(R.id.iv_premium_dialog_item_iv_container_01)
        mImageView2 = findViewById(R.id.iv_premium_dialog_item_iv_container_02)
        mImageView3 = findViewById(R.id.iv_premium_dialog_item_iv_container_03)
        mImageView4 = findViewById(R.id.iv_premium_dialog_item_iv_container_04)
        mImageView5 = findViewById(R.id.iv_premium_dialog_item_iv_container_05)
        mImageView6 = findViewById(R.id.iv_premium_dialog_item_iv_container_06)
        mImageView7 = findViewById(R.id.iv_premium_dialog_item_iv_container_07)
        mImageView8 = findViewById(R.id.iv_premium_dialog_item_iv_container_08)
        mImageView9 = findViewById(R.id.iv_premium_dialog_item_iv_container_09)

        mAnimTargets = mutableListOf()
        mAnimTargets.add(mImageView1)
        mAnimTargets.add(mImageView2)
        mAnimTargets.add(mImageView3)
        mAnimTargets.add(mImageView4)
        mAnimTargets.add(mImageView5)
        mAnimTargets.add(mImageView6)
        mAnimTargets.add(mImageView7)
        mAnimTargets.add(mImageView8)
        mAnimTargets.add(mImageView9)

        mBtnFree = findViewById(R.id.btn_try_for_free)
        mBtnFree.setOnClickListener(this)
        mIvClose = findViewById(R.id.iv_close)
        mIvClose.setOnClickListener(this)
//        mBtnQuit = findViewById(R.id.btn_quit)
//        mBtnQuit?.setOnClickListener(this)

        this.setCancelable(false)
        initAnimalView()
    }

    private fun initAnimalView() {
        val duration = 500
        val pvh1 = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)
        val pvh2 = PropertyValuesHolder.ofFloat("scaleX", 0f, 1.1f, 1f)
        val pvh3 = PropertyValuesHolder.ofFloat("scaleY", 0f, 1.1f, 1f)

        for (i in 0 until mAnimTargets.count()){
            val startDelay = 500 * i
            val animator = ObjectAnimator.ofPropertyValuesHolder(mAnimTargets[i], pvh1, pvh2, pvh3).setDuration(duration.toLong())
            animator.startDelay = startDelay.toLong()
            animator.start()
        }
    }

    fun updateTitle(title: String?, visibility: Int) {
        if (title != null) {
            mRetainTitle?.text = title
        }
        mRetainTitle?.visibility = visibility
    }

    fun updateDescription(description: String?, visibility: Int) {
        if (description != null) {
            mRetainDescription?.text = description
        }
        mRetainDescription?.visibility = visibility
    }

    fun updateFreeButton(text: String?, visibility: Int) {
        if (text != null) {
            mBtnFree.text = text
        }
        mBtnFree.visibility = visibility
    }

//    fun updateQuitButton(text: String?, visibility: Int) {
//        if (text != null) {
//            mBtnQuit?.text = text
//        }
//        mBtnQuit?.visibility = visibility
//    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_try_for_free -> {
                onRetainListener?.onSureClick()
                dismiss()
            }

            R.id.iv_close -> {
                onRetainListener?.onQuitClick()
                dismiss()
            }
//            R.id.btn_quit -> {
//                onRetainListener?.onQuitClick()
//                dismiss()
//            }
            else -> {
            }
        }
    }

    interface OnRetainListener {
        fun onSureClick()

        fun onQuitClick()
    }
}