package com.allever.app.gif.memes.ui

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.event.DownloadFinishEvent
import com.funny.gif.memes.bean.event.LikeEvent
import com.funny.gif.memes.func.download.DownloadCallback
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.func.download.TaskInfo
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import com.allever.app.gif.memes.ui.search.SearchActivity
import com.funny.gif.memes.util.DBHelper
import com.funny.gif.memes.util.MD5
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.util.FileUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File

class GifPreviewActivity : AbstractActivity() {

    private var mIsDownloadFinish = false

    private lateinit var item: GifItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gs_activity_gif_preview)

        init()

    }

    private fun init() {
        intent ?: return

        val dataJson = intent.getStringExtra(EXTRA_DATA_BEAN_JSON) ?: return

        try {
            item = Gson().fromJson(dataJson, GifItem::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        item ?: return

        val gifImageView = findViewById<GifImageView>(R.id.gifImageView)

        val ivShare = findViewById<ImageView>(R.id.ivShare)

        if (!item.url.startsWith("http")) {
            ivShare?.setOnClickListener {
                ShareHelper.shareImage(this, item.url)
            }

            val fileName = MD5.getMD5Str(item.id.toString()) + ".gif"
            val tempPath = "${Global.tempDir}${File.separator}$fileName"
            val url = if (FileUtils.checkExist(tempPath)) {
                tempPath
            } else {
                item.url
            }

            Glide.with(App.context)
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(
                    DiskCacheStrategy.NONE
                )
                .into(gifImageView!!)
            return
        }


        val gifId = item.id
        val gifUrl = item.url
        val headerUrl = item.avatar
        val userName = item.nickname
        val title = item.title ?: ""


//        val gifUrl = item.images.fixed_height.url
        val fileName = MD5.getMD5Str(gifId.toString()) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val ivPlay = findViewById<ImageView>(R.id.ivPlay)
        val ivRetry = findViewById<ImageView>(R.id.ivRetry)
        val progressLoading = findViewById<ProgressBar>(R.id.progressCircle)
        val downloadProgressBar = findViewById<ProgressBar>(R.id.downloadProgress)
        downloadProgressBar?.progress = 0

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle?.text = title
        val tvDisplayName = findViewById<TextView>(R.id.tvDisplayName)
        val displayName = userName ?: ""
        tvDisplayName?.text = "@$displayName"


        val ivHeader = findViewById<ImageView>(R.id.ivHeader)
        Glide.with(App.context).load(headerUrl).into(ivHeader!!)

        val ivLike = findViewById<ImageView>(R.id.ivLike)
        val ivDownload = findViewById<ImageView>(R.id.ivDownload)
        val ivMore = findViewById<ImageView>(R.id.ivMore)
        if (FileUtils.checkExist(savePath)) {
            ivDownload?.setColorFilter(
                App.context.resources.getColor(R.color.gray_66),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            ivDownload?.colorFilter = null
        }

        val downloadCallback = object : DownloadCallback {
            override fun onStart() {
                progressLoading?.visibility = View.VISIBLE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
                gifImageView?.visibility = View.GONE
            }

            override fun onConnected(totalLength: Long) {
                downloadProgressBar?.max = 100
                gifImageView?.visibility = View.GONE
            }

            override fun onProgress(current: Long, totalLength: Long,taskInfo: TaskInfo) {
                val percent = ((current / totalLength.toFloat()) * 100).toInt()
                downloadProgressBar?.progress = percent
            }

            override fun onPause(taskInfo: TaskInfo?) {
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                mIsDownloadFinish = true
                gifImageView?.visibility = View.VISIBLE
                FileUtils.createNewFile(tempPath, false)
                FileUtils.copyFile(File(cachePath), File(tempPath))
//                com.android.absbase.utils.FileUtils.copyFile(cachePath, tempPath, true)
//                val drawable = GifDrawable(tempPath)
//                gifImageView?.setImageDrawable(drawable)
                Glide.with(App.context)
                    .load(tempPath)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(
                        DiskCacheStrategy.NONE
                    )
                    .into(gifImageView!!)
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
            }

            override fun onError(e: Exception?, taskInfo: TaskInfo?) {
                progressLoading?.visibility = View.GONE
                ivPlay?.visibility = View.GONE
                ivRetry?.visibility = View.VISIBLE
                gifImageView?.visibility = View.GONE
            }

        }

        gifImageView?.setOnClickListener {

        }

        ivPlay?.setOnClickListener {
            ivPlay.visibility = View.GONE
            if (FileUtils.checkExist(tempPath)) {
                progressLoading?.visibility = View.GONE
                ivRetry?.visibility = View.GONE
                drawable?.start()
            } else {
                val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
                DownloadManager.getInstance().start(task, downloadCallback)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
            DownloadManager.getInstance().start(task, downloadCallback)
        }


        val liked = DBHelper.isLiked(gifId.toString())
        if (liked) {
            ivLike?.setColorFilter(resources.getColor(R.color.gs_default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DBHelper.isLiked(gifId.toString())
            val likeEvent = LikeEvent()
            likeEvent.id = gifId.toString()
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DBHelper.unLiked(gifId.toString())
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(resources.getColor(R.color.gs_default_theme_color))
                DBHelper.liked(gifId.toString(), item)
                likeEvent.isLiked = true
            }

            EventBus.getDefault().post(likeEvent)
        }

        ivShare?.setOnClickListener {
            if (FileUtils.checkExist(tempPath)) {
                ShareHelper.shareImage(this, tempPath)
            } else {
                toast(R.string.file_not_found)
            }
        }

        ivDownload?.setOnClickListener {
            if (FileUtils.checkExist(savePath)) {
                toast(R.string.already_download)
                return@setOnClickListener
            }
            if (FileUtils.checkExist(tempPath)) {
                FileUtils.createNewFile(savePath, false)
//                com.android.absbase.utils.FileUtils.copyFile(tempPath, savePath, true)
                FileUtils.copyFile(File(cachePath), File(tempPath))
                toast("${getString(R.string.already_save_to)}\n$savePath")
                ivDownload.setColorFilter(
                    App.context.resources.getColor(R.color.gray_66),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                toast(R.string.file_not_found)
            }

        }

        ivMore?.setOnClickListener {
            var searchContent: String = ""
            if (title?.contains("GIF") == true) {
                val titleArray = title.split("GIF")
                if (titleArray.isNotEmpty()) {
                    searchContent = titleArray[0]
                }
            }

            log("搜索关键字： $searchContent")
            SearchActivity.start(App.context, searchContent)
        }

        log("load url = $gifUrl")

        if (!item.url.startsWith("http")) {
            return
        }

        if (FileUtils.checkExist(tempPath)) {
            downloadProgressBar?.progress = 100
//            drawable = GifDrawable(tempPath)
//            gifImageView?.setImageDrawable(drawable)
            Glide.with(App.context)
                .load(tempPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(
                    DiskCacheStrategy.NONE
                )
                .into(gifImageView!!)

            progressLoading?.visibility = View.GONE
            ivPlay?.visibility = View.GONE
            ivRetry?.visibility = View.GONE
            gifImageView.visibility = View.VISIBLE

            return
        }


//        if (BuildConfig.DEBUG) {
//            return
//        }

        DownloadManager.getInstance().cancel(gifUrl)
        val task = TaskInfo(fileName, Global.cacheDir, gifUrl)
        DownloadManager.getInstance().start(task, downloadCallback)

    }


    override fun onDestroy() {
        super.onDestroy()
        if (item != null) {
            DownloadManager.getInstance().cancel(item.url)
        }

        if (mIsDownloadFinish) {
            val event = DownloadFinishEvent()
            event.id = item.id.toString()
            EventBus.getDefault().post(event)
        }
    }

    companion object {
        private const val EXTRA_DATA_BEAN_JSON = "EXTRA_DATA_BEAN_JSON"
        fun start(
            context: Context,
            dataJson: String
        ) {
            val intent = Intent(context, GifPreviewActivity::class.java)
            intent.putExtra(EXTRA_DATA_BEAN_JSON, dataJson)
            context.startActivity(intent)
        }
    }
}