package com.allever.video.editor.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import android.view.View
import android.widget.ImageView
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.ToastUtils
import com.allever.video.editor.R
import com.allever.video.editor.app.Base2Activity
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.widget.LineIndicator

class PreviewActivity : Base2Activity(), View.OnClickListener {
    private var mViewPager: ViewPager? = null
    private var mIvBack: ImageView? = null
    private var mIvSelect: ImageView? = null

    private var mPagerAdapter: PreviewFragmentPagerAdapter? = null

    var mThumbnailBeanList: MutableList<ThumbnailBean> = mutableListOf()
    private var mPosition: Int = 0

    private var mFromSave = false

    companion object {

        public const val EXTRA_RESULT_POSITION = "ntenkmasf"
        public const val EXTRA_RESULT_CHECK = "ketnasmdn"

        private const val EXTRA_THUMBNAIL_LIST = "sadtas"
        private const val EXTRA_POSITION = "dfgdfg"
        private const val EXTRA_FROM_SAVE = "snassfla"

        @JvmStatic
        fun startActivity(
            context: Activity,
            thumbnailBeanList: ArrayList<ThumbnailBean>,
            position: Int,
            fromSave: Boolean = false,
            requestCode: Int = -1
        ) {
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_THUMBNAIL_LIST, thumbnailBeanList)
            intent.putExtra(EXTRA_POSITION, position)
            intent.putExtra(EXTRA_FROM_SAVE, fromSave)
            context.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        getIntentData()

        initView()
    }

    private fun getIntentData() {
        val intent = this.intent

        mThumbnailBeanList.addAll(intent.getParcelableArrayListExtra(EXTRA_THUMBNAIL_LIST)!!)
        mPosition = intent.getIntExtra(EXTRA_POSITION, 0)
        mFromSave = intent.getBooleanExtra(EXTRA_FROM_SAVE, false)
    }

    private fun initView() {
        mIvBack = findViewById(R.id.iv_back)
        mIvBack?.setOnClickListener(this)

        mIvSelect = findViewById(R.id.iv_select)
        mIvSelect?.setOnClickListener(this)
        if (mFromSave) {
            mIvSelect?.visibility = View.GONE
        } else {
            mIvSelect?.visibility = View.VISIBLE
        }

        mViewPager = findViewById(R.id.id_vp_image)
        mPagerAdapter = PreviewFragmentPagerAdapter(
            supportFragmentManager,
            mThumbnailBeanList
        )
        mViewPager?.adapter = mPagerAdapter

        mPagerAdapter?.notifyDataSetChanged()

        if (checkOutOfBoundary()) {
            return
        }
        mViewPager?.currentItem = mPosition
        val thumbnailBean = mThumbnailBeanList[mPosition]
        updateSelecctIcon(thumbnailBean)

        mViewPager?.addOnPageChangeListener(object : LineIndicator.OnPageChangeListener,
            ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (checkOutOfBoundary()) {
                    return
                }
                val fragment = mPagerAdapter?.currentFragment as? PreviewFragment
                fragment?.pause()
                mPosition = position
                if (checkOutOfBoundary()) {
                    return
                }
                val thumbnailBean = mThumbnailBeanList[position]
                updateSelecctIcon(thumbnailBean)
            }

            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}
            override fun onPageChange(page: Int) {}
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.iv_back -> {
                finish()
            }

            R.id.iv_select -> {
                if (checkOutOfBoundary()) {
                    return
                }
                val thumbnailBean = mThumbnailBeanList[mPosition]
                val selected = thumbnailBean.isChecked
                thumbnailBean.isChecked = !selected
                updateSelecctIcon(thumbnailBean)
                postDelayed(Runnable {
                    val intent = Intent()
                    intent.putExtra(EXTRA_RESULT_POSITION, mPosition)
                    intent.putExtra(EXTRA_RESULT_CHECK, thumbnailBean.isChecked)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }, 300)
            }
        }
    }

    private fun updateSelecctIcon(thumbnailBean: ThumbnailBean) {
        if (thumbnailBean.isChecked) {
            mIvSelect?.setImageResource(R.drawable.icon_album_select)
        } else {
            mIvSelect?.setImageResource(R.drawable.icon_album_unselected)
        }
    }

    private fun checkOutOfBoundary(): Boolean {
        val result = mPosition !in 0 until mThumbnailBeanList.size
        if (result) {
            ToastUtils.show(ResourcesUtils.getString(R.string.preview_boundary_error_tips))
        }
        return result
    }

}