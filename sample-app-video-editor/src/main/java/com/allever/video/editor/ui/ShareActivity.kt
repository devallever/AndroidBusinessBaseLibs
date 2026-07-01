package com.allever.video.editor.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import com.allever.video.editor.R
import com.allever.video.editor.app.Base2Activity
import com.allever.video.editor.function.media.MediaTypeUtil
import com.allever.video.editor.function.share.ShareHelper
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.utils.AsyncTask
import com.allever.video.editor.utils.MediaThumbnailUtil
import java.io.File
import java.lang.ref.WeakReference

class ShareActivity : Base2Activity(), View.OnClickListener {

    companion object {
        private const val EXTRA_BITMAP = "lsdj"
        private const val EXTRA_PATH = "sjklda"
        private const val EXTRA_FORCE_RATE_GUIDE = "efjeleifdjhf"

        private var bitmapWeak = WeakReference<Bitmap?>(null)

        fun startActivity(context: Context, bitmap: Bitmap?, path: String, forceRateGuide: Boolean = false) {
            val intent = Intent(context, ShareActivity::class.java)
            // bitmap太大是无法使用Intent
//            intent.putExtra(EXTRA_BITMAP, bitmap)
            bitmapWeak = WeakReference(bitmap)
            intent.putExtra(EXTRA_PATH, path)
            intent.putExtra(EXTRA_FORCE_RATE_GUIDE, forceRateGuide)
            context.startActivity(intent)
        }
    }

    private lateinit var mIvBack: View
    private lateinit var mDisplayContainer: View
    private lateinit var mIvDisplay: ImageView
    private lateinit var mIvHome: View

    private lateinit var mIvShareFaceBook: ImageView
    private lateinit var mIvShareIns: ImageView
    private lateinit var mIvShareTikTok: ImageView
    private lateinit var mIvShareYoutube: ImageView
    private lateinit var mIvShareMore: ImageView

    private var mBitmap: Bitmap? = null
    private var mPath: String? = null
    private var mForceRateGuide = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        getIntentData()

        initView()
    }

    private fun getIntentData() {
        val intent = this.intent
        mPath = intent.getStringExtra(EXTRA_PATH)
        mBitmap = intent.getParcelableExtra(EXTRA_BITMAP) ?: bitmapWeak.get()
        mForceRateGuide = intent.getBooleanExtra(EXTRA_FORCE_RATE_GUIDE, false)
    }

    private fun initView() {

        mIvBack = findViewById(R.id.iv_back)
        mIvBack.setOnClickListener(this)

        mIvHome = findViewById(R.id.iv_home)
        mIvHome.setOnClickListener(this)

        mDisplayContainer = findViewById(R.id.fl_display_container)
        mDisplayContainer.setOnClickListener(this)

        mIvDisplay = findViewById(R.id.iv_display)
        setBitmap()
        mIvShareFaceBook = findViewById(R.id.iv_share_facebook)
        mIvShareIns = findViewById(R.id.iv_share_ins)
        mIvShareTikTok = findViewById(R.id.iv_share_tiktok)
        mIvShareYoutube = findViewById(R.id.iv_share_youtube)
        mIvShareMore = findViewById(R.id.iv_share_more)
        mIvShareFaceBook.setOnClickListener(this)
        mIvShareIns.setOnClickListener(this)
        mIvShareTikTok.setOnClickListener(this)
        mIvShareYoutube.setOnClickListener(this)
        mIvShareMore.setOnClickListener(this)
    }

    private fun setBitmap() {
        if (mBitmap == null) {
            object : AsyncTask<Void, Void, Bitmap?>() {
                override fun doInBackground(vararg params: Void?): Bitmap? {
                    val path = mPath
                    path ?: return null
                    return MediaThumbnailUtil.createPreViewVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND)
                }

                override fun onPostExecute(result: Bitmap?) {
                    super.onPostExecute(result)
                    if (result != null) {
                        mIvDisplay.setImageBitmap(result)
                    }
                }
            }.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
        } else {
            mIvDisplay.setImageBitmap(mBitmap)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            mIvBack -> {
                finish()
            }

            mIvHome -> {
                val intent = Intent(this, AlbumActivity::class.java)
                //Activity已经在当前的Task中运行，因此，不再是重新启动一个这个Activity的实例，
                // 而是在这个Activity上方的所有Activity都将关闭，
                // 然后这个Intent会作为一个新的Intent投递到老的Activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }

            mDisplayContainer -> {
                //
                val thumbnailBeanList = mutableListOf<ThumbnailBean>()
                val thumbnailBean = ThumbnailBean()
                thumbnailBean.path = mPath
                thumbnailBean.type = MediaTypeUtil.TYPE_VIDEO
                thumbnailBean.uri = Uri.fromFile(File(mPath))
                thumbnailBean.isAutoPlay = true
                thumbnailBeanList.add(thumbnailBean)
                val arrayListData = ArrayList<ThumbnailBean>()
                arrayListData.addAll(thumbnailBeanList)
                PreviewActivity.startActivity(
                    this,
                    arrayListData,
                    0,
                    true
                )
            }

            mIvShareFaceBook -> {
                ShareHelper.shareToFacebook(this, mPath, ShareHelper.SHARE_CONTENT_TYPE_VEDIO)
            }
            mIvShareIns -> {
                ShareHelper.shareToIns(context = this, path = mPath, type = ShareHelper.SHARE_CONTENT_TYPE_VEDIO)
            }

            mIvShareTikTok -> {
                ShareHelper.shareToTikTok(this, mPath, ShareHelper.SHARE_CONTENT_TYPE_VEDIO)
            }

            mIvShareYoutube -> {
                ShareHelper.shareToYoutube(this, mPath, ShareHelper.SHARE_CONTENT_TYPE_VEDIO)
            }

            mIvShareMore -> {
                ShareHelper.shareMore(this, mPath, ShareHelper.SHARE_CONTENT_TYPE_VEDIO)
            }
        }
    }
}