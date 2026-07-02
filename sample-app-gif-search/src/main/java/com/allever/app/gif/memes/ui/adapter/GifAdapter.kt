package com.allever.app.gif.memes.ui.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.ext.getString
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.ext.toast
import app.allever.android.lib.core.helper.ShareHelper
import app.allever.android.lib.core.util.FileUtils
import com.allever.app.gif.memes.R
import com.allever.app.gif.memes.ui.adapter.bean.GifItem
import com.allever.app.gif.memes.ui.search.SearchActivity
import com.allever.app.gif.memes.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.app.gif.memes.ui.widget.recycler.BaseViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.funny.gif.memes.app.Global
import com.funny.gif.memes.bean.event.LikeEvent
import com.funny.gif.memes.func.download.DownloadCallback
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.func.download.TaskInfo
import com.funny.gif.memes.util.DBHelper
import com.funny.gif.memes.util.MD5
import com.funny.gif.memes.util.copyToAlbum
import org.greenrobot.eventbus.EventBus
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File

class GifAdapter(context: Context, resId: Int, data: MutableList<GifItem>) :
    BaseRecyclerViewAdapter<GifItem>(context, resId, data) {

    @SuppressLint("SetTextI18n")
    override fun bindHolder(holder: BaseViewHolder, position: Int, item: GifItem) {
        //debug
//        holder.getView<View>(R.id.llDebugContainer)
//            ?.let { ViewHelper.setVisible(it, BuildConfig.DEBUG) }
        val tvLoadUrl = holder.getView<TextView>(R.id.tvUrl)
        val tvSize = holder.getView<TextView>(R.id.tvSize)
        val tvTempPath = holder.getView<TextView>(R.id.tvFilePath)
        val tvStatus = holder.getView<TextView>(R.id.tvStatus)

        val gifUrl = item.url
        val fileName = MD5.getMD5Str(item.id.toString()) + ".gif"
        val tempPath = "${Global.tempDir}${File.separator}$fileName"
        val cachePath = "${Global.cacheDir}${File.separator}$fileName"
        val savePath = "${Global.saveDir}${File.separator}$fileName"
        var drawable: GifDrawable? = null
        var downloaded = FileUtils.checkExist(tempPath)

        val gifImageView = holder.getView<GifImageView>(R.id.gifImageView)
        val ivPlay = holder.getView<ImageView>(R.id.ivPlay)
        val ivRetry = holder.getView<ImageView>(R.id.ivRetry)
        val progressLoading = holder.getView<ProgressBar>(R.id.progressCircle)
        val downloadProgressBar = holder.getView<ProgressBar>(R.id.downloadProgress)

        val tvTitle = holder.getView<TextView>(R.id.tvTitle)
        tvTitle?.text = item.title
        val tvDisplayName = holder.getView<TextView>(R.id.tvDisplayName)
        val displayName = item.nickname
        tvDisplayName?.text = "@$displayName"


        val ivHeader = holder.getView<ImageView>(R.id.ivHeader)
        Glide.with(App.context).load(item.avatar).into(ivHeader!!)

        val ivLike = holder.getView<ImageView>(R.id.ivLike)
        val ivShare = holder.getView<ImageView>(R.id.ivShare)
        val ivDownload = holder.getView<ImageView>(R.id.ivDownload)
        val ivMore = holder.getView<ImageView>(R.id.ivMore)
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
                tvStatus?.text = "状态：开始下载"
                progressLoading?.visibility = VISIBLE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = GONE
                gifImageView?.visibility = GONE
            }

            override fun onConnected(totalLength: Long) {
                tvStatus?.text = "状态：已连接"
                downloadProgressBar?.max = 100
                gifImageView?.visibility = GONE
            }

            override fun onProgress(current: Long, totalLength: Long, taskInfo: TaskInfo) {
                if (taskInfo.url != gifImageView?.tag) {
                    return
                }
                val percent = ((current / totalLength.toFloat()) * 100).toInt()
                downloadProgressBar?.progress = percent

                log("${taskInfo.url} -> 进度: $percent")
                tvStatus?.text = "状态：下载中: $current"
                if (percent == 100) {
                    handleFinishDownload(
                        taskInfo,
                        gifImageView,
                        downloadProgressBar,
                        tvStatus,
                        tempPath,
                        cachePath,
                        progressLoading,
                        ivPlay,
                        ivRetry
                    )
                }
            }

            override fun onPause(taskInfo: TaskInfo?) {
                tvStatus?.text = "状态：暂停下载"

                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE

            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                handleFinishDownload(
                    taskInfo,
                    gifImageView,
                    downloadProgressBar,
                    tvStatus,
                    tempPath,
                    cachePath,
                    progressLoading,
                    ivPlay,
                    ivRetry
                )
            }

            override fun onError(e: Exception?, taskInfo: TaskInfo?) {
                if (taskInfo?.url != gifImageView?.tag) {
                    return
                }

                //check again
                if (FileUtils.checkExist(taskInfo?.tempPath)) {
                    downloadProgressBar?.progress = 100
                    tvStatus?.text = "状态：已下载"
                    Glide.with(App.context)
                        .load(tempPath)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(
                            DiskCacheStrategy.NONE
                        )
                        .into(gifImageView!!)

                    progressLoading?.visibility = GONE
                    ivPlay?.visibility = GONE
                    ivRetry?.visibility = GONE
                    gifImageView?.visibility = VISIBLE

                    return
                }

                log("${taskInfo?.url} -> 状态：下载出错")
                tvStatus?.text = "状态：下载出错"
                log("")
                progressLoading?.visibility = GONE
                ivPlay?.visibility = GONE
                ivRetry?.visibility = VISIBLE
                gifImageView?.visibility = GONE
            }

        }

        gifImageView?.setOnClickListener {

        }

        ivPlay?.setOnClickListener {
            ivPlay.visibility = GONE
            if (FileUtils.checkExist(tempPath)) {
                progressLoading?.visibility = GONE
                ivRetry?.visibility = GONE
                drawable?.start()
            } else {
                val task = TaskInfo(fileName, Global.cacheDir, gifUrl, tempPath)
                DownloadManager.getInstance().start(task, downloadCallback, true)
            }
        }

        ivRetry?.setOnClickListener {
            val task = TaskInfo(fileName, Global.cacheDir, gifUrl, tempPath)
            DownloadManager.getInstance().start(task, downloadCallback)
        }


        val liked = DBHelper.isLiked(item.id.toString())
        if (liked) {
            ivLike?.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
        } else {
            ivLike?.colorFilter = null
        }

        ivLike?.setOnClickListener {
            val liked = DBHelper.isLiked(item.id.toString())
            val likeEvent = LikeEvent()
            likeEvent.id = item.id.toString()
            likeEvent.type = item.type
            likeEvent.dataBean = item
            if (liked) {
                ivLike.colorFilter = null
                DBHelper.unLiked(item.id.toString())
                likeEvent.isLiked = false
            } else {
                ivLike.setColorFilter(mContext.resources.getColor(R.color.default_theme_color))
                DBHelper.liked(item.id.toString(), item)
                likeEvent.isLiked = true
            }

            EventBus.getDefault().post(likeEvent)
        }

        ivShare?.setOnClickListener {
            if (FileUtils.checkExist(tempPath)) {
                ShareHelper.shareImage(mContext, tempPath)
            } else {
                toast(R.string.file_not_found)
            }
        }

        ivDownload?.setOnClickListener {

            Global.createDir()
            if (FileUtils.checkExist(savePath)) {
                toast(R.string.already_download)
                return@setOnClickListener
            }


            if (FileUtils.checkExist(tempPath)) {
                tempPath.copyToAlbum(App.context, Global.SAVE_ALBUM)
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
            val title = item.title
            var searchContent: String = ""
            if (title.contains("GIF")) {
                val titleArray = title.split("GIF")
                if (titleArray.isNotEmpty()) {
                    searchContent = titleArray[0]
                }
            }

            log("搜索关键字： $searchContent")
            SearchActivity.start(App.context, searchContent)
        }

        tvDisplayName?.setOnClickListener {
            val title = item.nickname
            if (TextUtils.isEmpty(title)) {
                return@setOnClickListener
            }
            log("搜索关键字： $title")
            SearchActivity.start(App.context, title)
        }


        log("load url = $gifUrl")
        tvLoadUrl?.text = gifUrl
        tvSize?.text = item.size.toString()

        if (FileUtils.checkExist(tempPath)) {
            val fileSize = FileUtils.getFileSize(tempPath)
            downloadProgressBar?.progress = 100
            tvStatus?.text = "状态：已下载"
            Glide.with(App.context)
                .load(tempPath)
                .skipMemoryCache(true)
                .diskCacheStrategy(
                    DiskCacheStrategy.NONE
                )
                .into(gifImageView!!)

            progressLoading?.visibility = GONE
            ivPlay?.visibility = GONE
            ivRetry?.visibility = GONE
            gifImageView.visibility = VISIBLE


            if (fileSize >= item.size) {
                return
            } else {
                log("fileSize = $fileSize")
                log("gifSize = ${item.size}")
            }
        }

        val task = TaskInfo(fileName, Global.cacheDir, gifUrl, tempPath)
        if (gifUrl.isNotEmpty()) {
            gifImageView?.tag = gifUrl
            DownloadManager.getInstance().start(task, downloadCallback, true)
        }

    }

    private fun handleFinishDownload(
        taskInfo: TaskInfo?,
        gifImageView: GifImageView?,
        downloadProgressBar: ProgressBar?,
        tvStatus: TextView?,
        tempPath: String,
        cachePath: String,
        progressLoading: ProgressBar?,
        ivPlay: ImageView?,
        ivRetry: ImageView?
    ) {
        if (taskInfo?.url != gifImageView?.tag) {
            return
        }
        downloadProgressBar?.progress = 100
        gifImageView?.visibility = VISIBLE
        tvStatus?.text = "状态：下载完成"
        log("${taskInfo?.url} -> 状态：下载完成")
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
        progressLoading?.visibility = GONE
        ivPlay?.visibility = GONE
        ivRetry?.visibility = GONE
    }

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.adapterPosition
        log("回收view： $position")
        val imageView = holder.getView<GifImageView>(R.id.gifImageView)
        if (imageView != null) {
            Glide.with(mContext).clear(imageView)
        }
//        val gifUrl = mData[position].images.fixed_height.url
//        DownloadManager.getInstance().pause(gifUrl)
    }

    companion object {
        private const val STATUS_PLAY = 0
        private const val STATUS_PAUSE = 1
        private const val STATUS_RETRY = 2
        private const val STATUS_HIDE = 4
    }
}