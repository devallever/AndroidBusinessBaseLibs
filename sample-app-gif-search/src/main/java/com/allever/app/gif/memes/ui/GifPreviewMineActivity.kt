package com.allever.app.gif.memes.ui

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.allever.app.gif.memes.R
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.event.DownloadFinishEvent
import com.funny.gif.memes.func.download.DownloadManager
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import com.funny.gif.memes.util.MD5
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.base.AbstractActivity
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.util.FileUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.funny.gif.memes.util.copyToAlbum
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifImageView
import java.io.File

class GifPreviewMineActivity : AbstractActivity() {

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
        }


        val gifId = item.id
        val gifUrl = item.url
        val headerUrl = item.avatar
        val userName = item.nickname
        val title = item.title ?: ""


//        val gifUrl = item.images.fixed_height.url
        val fileName = FileUtils.getFileName(item.url)
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"

        val downloadProgressBar = findViewById<ProgressBar>(R.id.downloadProgress)
        downloadProgressBar?.progress = 100

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        ViewHelper.setVisible(tvTitle, false)
        tvTitle?.text = title
        val tvDisplayName = findViewById<TextView>(R.id.tvDisplayName)
        ViewHelper.setVisible(tvDisplayName, false)
        val displayName = userName ?: ""
        tvDisplayName?.text = "@$displayName"


        val ivHeader = findViewById<ImageView>(R.id.ivHeader)
        ViewHelper.setVisible(ivHeader, false)

        val ivLike = findViewById<ImageView>(R.id.ivLike)
        ViewHelper.setVisible(ivLike, false)
        val ivDownload = findViewById<ImageView>(R.id.ivDownload)
        val ivMore = findViewById<ImageView>(R.id.ivMore)
        ViewHelper.setVisible(ivMore, false)
        if (FileUtils.checkExist(savePath)) {
            ivDownload?.setColorFilter(
                App.context.resources.getColor(R.color.gray_66),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            ivDownload?.colorFilter = null
        }


        gifImageView?.setOnClickListener {

        }

        ivDownload?.setOnClickListener {
            Global.createDir()
            if (FileUtils.checkExist(savePath)) {
                toast(R.string.already_download)
                return@setOnClickListener
            }


            if (FileUtils.checkExist(item.url)) {
                item.url.copyToAlbum(App.context, Global.SAVE_ALBUM)
                toast("${getString(R.string.already_save_to)}\n$savePath")
                ivDownload.setColorFilter(
                    App.context.resources.getColor(R.color.gray_66),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                toast(R.string.file_not_found)
            }
        }


        log("load url = $gifUrl")

        if (!item.url.startsWith("http")) {
            return
        }
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
            val intent = Intent(context, GifPreviewMineActivity::class.java)
            intent.putExtra(EXTRA_DATA_BEAN_JSON, dataJson)
            context.startActivity(intent)
        }
    }
}