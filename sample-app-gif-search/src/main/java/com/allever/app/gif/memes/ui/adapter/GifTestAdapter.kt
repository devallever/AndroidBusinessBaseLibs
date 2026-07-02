package com.allever.app.gif.memes.ui.adapter

import android.content.Context
import android.os.Environment
import com.allever.app.gif.memes.R
import com.funny.gif.memes.bean.DataBean
import com.funny.gif.memes.func.download.DownloadCallback
import com.funny.gif.memes.func.download.DownloadManager
import com.funny.gif.memes.func.download.TaskInfo
import com.funny.gif.memes.util.MD5
import com.allever.lib.common.app.App
import com.allever.lib.common.ui.widget.recycler.BaseRecyclerViewAdapter
import com.allever.lib.common.ui.widget.recycler.BaseViewHolder
import com.allever.lib.common.util.FileUtil
import com.allever.lib.common.util.FileUtils
import com.allever.lib.common.util.log
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.File
import java.lang.Exception

class GifTestAdapter(context: Context, resId: Int, data: MutableList<DataBean>) :
    BaseRecyclerViewAdapter<DataBean>(context, resId, data) {

    private val tempDir = App.context.cacheDir.absolutePath + File.separator + "gif"
    private val fileDir =
        Environment.getExternalStorageDirectory().absoluteFile.path + File.separator + App.context.packageName + File.separator + "gifFunny"


    override fun bindHolder(holder: BaseViewHolder, position: Int, item: DataBean) {
        val url = item.images.fixed_height_small.url
//        val url = item.images.preview_webp.url
        log("load url = $url")
        holder.setText(R.id.tvUrl, url)
        holder.setText(R.id.tvSize, item.images.fixed_height.size)

        val gifImageView = holder.getView<GifImageView>(R.id.ivGif)


        val fileName = MD5.getMD5Str(item.id) + ".gif"

        val filePath = "$fileDir${File.separator}$fileName"
        val downloaded = FileUtils.checkExist(filePath)
        if (downloaded) {
            holder.setText(R.id.tvStatus, "状态：已下载")
            val drawable = GifDrawable(File(filePath))
            gifImageView?.setImageDrawable(drawable)
//            Glide.with(mContext).load(File(filePath)).into(holder.getView<ImageView>(R.id.ivGif)!!)
            return
        }


        val tempFilePath = "$tempDir${File.separator}$fileName"

        val task = TaskInfo(fileName, tempDir, url)
        holder.setText(R.id.tvFilePath, "临时路径：$tempFilePath")

//        return
        DownloadManager.getInstance().start(task, object : DownloadCallback {
            override fun onStart() {
                holder.setText(R.id.tvStatus, "状态：开始下载")
            }

            override fun onConnected(totalLength: Long) {
                holder.setText(R.id.tvStatus, "状态：取消下载")
                holder.setMax(R.id.downloadProgress, totalLength.toInt())
            }

            override fun onProgress(current: Long, totalLength: Long, taskInfo: TaskInfo) {
                holder.setProgress(R.id.downloadProgress, current.toInt())
                holder.setText(R.id.tvStatus, "状态：下载中 - $current")
            }

            override fun onPause(taskInfo: TaskInfo?) {
                holder.setText(R.id.tvStatus, "状态：暂停下载")
            }

            override fun onCompleted(taskInfo: TaskInfo?) {
                holder.setText(R.id.tvStatus, "状态：下载完成")
                FileUtil.createNewFile(filePath, false)
//                com.android.absbase.utils.FileUtils.copyFile(tempFilePath, filePath, true)
                FileUtil.copyFile(File(tempFilePath), File(filePath))
//                Glide.with(mContext).load(File(filePath)).into(holder.getView<ImageView>(R.id.ivGif)!!)
                val drawable = GifDrawable(File(filePath))
                gifImageView?.setImageDrawable(drawable)

            }

            override fun onError(e: Exception?, taskInfo: TaskInfo?) {
                holder.setText(R.id.tvStatus, "状态：下载出错")
            }

        }, true)


    }
}