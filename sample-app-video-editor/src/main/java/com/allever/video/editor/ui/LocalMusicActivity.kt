package com.allever.video.editor.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.R
import com.allever.video.editor.app.Base2Activity
import com.allever.video.editor.function.music.SongHelper
import com.allever.video.editor.function.music.SongInfo
import com.allever.video.editor.function.music.SongMediaPlayer
import com.allever.video.editor.utils.TimeUtils
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.ToastUtils
import com.android.permissions.compat.PermissionCallbacks
import com.android.permissions.compat.PermissionManager
import java.io.File
import java.util.LinkedList

class LocalMusicActivity : Base2Activity(), View.OnClickListener {
    private lateinit var mBack: ImageView
    private lateinit var mScaningLayout: ViewGroup
    private lateinit var mNoDataLayout: ViewGroup

    private lateinit var mNoDataTextView: TextView
    private lateinit var mNoDataLocusImageView: ImageView
    private lateinit var mRecycleView: androidx.recyclerview.widget.RecyclerView
    private lateinit var mScanProgress: TextView
    private lateinit var mScanBtnContainer: ViewGroup
    private lateinit var mScanBtn: TextView

    private var mAdapter = LocalMusicAdapter()
    private var mSongInfos = LinkedList<SongInfo>()

    private var mRescan = false

    private val mPermissionManager = PermissionManager.getProxy()
    private val mPermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val RC_PERMISSION = 0x01

    private val WHAT_UPDATE_SCAN_START = 0
    private val WHAT_UPDATE_SCAN_PROGRESS = 1
    private val WHAT_UPDATE_SCAN_END = 2
    private val WHAT_ADD_DATA = 3
    private val WHAT_GONE_SCAN_ANIMATION = 4
    private val mHander = Handler(Looper.getMainLooper(), Handler.Callback {
        when (it.what) {
            WHAT_UPDATE_SCAN_START -> {
                showScanProgress(true)
                showBtnScan(false)
            }
            WHAT_UPDATE_SCAN_PROGRESS -> {
                mScanProgress.text = it.obj as String
            }
            WHAT_UPDATE_SCAN_END -> {
                showScanProgress(false)
                showScanLayout(false)
                showBtnScan(true)
                checkDataState()

//                val info = resources.getString(R.string.local_music_scan_result_toast, "${mSongInfos.size}")
                val info = resources.getString(R.string.local_music_scan_result_toast)
                ToastUtils.show(this@LocalMusicActivity, info)
            }
            WHAT_ADD_DATA -> {
                addData(it.obj as SongInfo)
            }
            WHAT_GONE_SCAN_ANIMATION -> {
                showScanLayout(false)
            }
        }

        false
    })

    private fun showScanProgress(show: Boolean) {
        if (show) {
            if (mScanProgress.visibility == View.VISIBLE) {
                return
            }
        } else {
            if (mScanProgress.visibility == View.GONE) {
                return
            }
        }
        val formY = if (show) -1.0f else 0.0f
        val toY = if (show) 0.0f else -1.0f
        val animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, formY,
                Animation.RELATIVE_TO_SELF, toY)
        animation.duration = 200
        animation.interpolator = AccelerateInterpolator()
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                if (!show) {
                    mScanProgress.visibility = View.GONE
                }
            }

            override fun onAnimationStart(animation: Animation?) {
                if (show) {
                    mScanProgress.visibility = View.VISIBLE
                }
            }

        })
        mScanProgress.startAnimation(animation)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_music)
        adaptStatusBar(findViewById(R.id.top_panel))

        mBack = findViewById(R.id.back)
        mBack.setOnClickListener(this)
        mNoDataLayout = findViewById(R.id.no_data_layout)
        mScaningLayout = findViewById(R.id.scaning_layout)

        mNoDataTextView = findViewById(R.id.no_data_title)
        mNoDataLocusImageView = findViewById(R.id.no_music_locus)
        mScanProgress = findViewById(R.id.scan_progress)
        mScanBtnContainer = findViewById(R.id.fl_scan_container)
        mScanBtn = findViewById(R.id.tv_scan)
        mScanBtn.setOnClickListener(this)

        mRecycleView = findViewById(R.id.recycler_view)
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
        mRecycleView.layoutManager = layoutManager
        mRecycleView.adapter = mAdapter
        mAdapter.onItemListener = onItemListener

        mNoDataTextView.text = resources.getText(R.string.local_music_no_data)
    }

    override fun onStart() {
        super.onStart()

        if (ConfigManager.firstScanLocalMusic) {
            ConfigManager.firstScanLocalMusic = false
            rescan()
        } else {
            updateData(false, true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun finish() {
        super.finish()
        SongHelper.stopGlobalSearchAsync()
        mAdapter.release()
    }

    private fun setResult(songInfo: SongInfo) {
        val intent = Intent()
        val bundle = getIntent().getBundleExtra(INTENT_KEY_EXTRA_INFO) ?: Bundle()
        intent.putExtra(INTENT_KEY_RESPONSE_DATA, songInfo)
        intent.putExtra(INTENT_KEY_EXTRA_INFO, bundle)
        intent.data = Uri.parse(songInfo.path)
        setResult(RESULT_OK, intent)
    }

    private fun checkStoragePermission(): Boolean {
        try {
            val sdcardPath = Environment.getExternalStorageDirectory().absolutePath
            val file = File(sdcardPath)
            return file.canRead()
        } catch (e: Throwable) {

        }
        return false
    }

    private fun requestStoragePermission() {
        mPermissionManager.requestPermission(this,
                resources.getString(R.string.tips_ration_storate),
                RC_PERMISSION,
                *mPermissions)
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        mPermissionManager.onRequestPermissionResult(requestCode, permissions, grantResults, this)
//    }

//    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
//        if (mPermissionManager.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//            updateData(false, true)
//        }
//    }

//    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
//        if (mPermissionManager.isPermissionPermanentlyDenied(this, *mPermissions)) {
//            mPermissionManager.jumpToSettingDialog(this)
//        }
//    }

    private fun rescan() {
        mRescan = true
        mSongInfos.clear()
        mAdapter.setData(mSongInfos)
        updateData(true, false)

        showScanLayout(true)
        mHander.sendEmptyMessageDelayed(WHAT_GONE_SCAN_ANIMATION, 4000)
    }

    private fun addData(songInfo: SongInfo) {
        mSongInfos.add(songInfo)
        checkDataState()
        mAdapter.add(songInfo)
    }

    private val onSongSearchListener = object : SongHelper.OnSongSearchListener {
        override fun searchStart() {
            mHander.sendEmptyMessage(WHAT_UPDATE_SCAN_START)
        }

        override fun progress(path: String) {
            Message.obtain(mHander, WHAT_UPDATE_SCAN_PROGRESS, path).sendToTarget()
        }

        override fun search(songInfo: SongInfo) {
            Message.obtain(mHander, WHAT_ADD_DATA, songInfo).sendToTarget()
        }

        override fun searchEnd(songInfos: List<SongInfo>) {
            mHander.sendEmptyMessage(WHAT_UPDATE_SCAN_END)
        }
    }

    private fun updateData(rescan: Boolean = false, showNoData: Boolean = true) {
        if (showNoData) {
            checkDataState()
        }
        val hasPermission = if (!mPermissionManager.hasPermissions(this, *mPermissions)) {
            requestStoragePermission()
            false
        } else if (Build.VERSION.SDK_INT <= 23) {
            // 6.0以下的版本出现用户手动设置项关掉权限, 上接口仍然判断为有权限, 而且不管有没有勾选权限,判断是否永久拒绝都为拒绝
            val hasPermission = checkStoragePermission()
            if (!hasPermission) {
                mPermissionManager.jumpToSettingDialog(this)
            }
            hasPermission
        } else true
        if (hasPermission) {
            val allSongInfo = SongHelper.getAllSongInfo(this, rescan, onSongSearchListener)
            if (!allSongInfo.isEmpty()) {
                mSongInfos = LinkedList(allSongInfo)
                mAdapter.setData(mSongInfos)
            }
        }
    }

    private fun showScanLayout(show: Boolean) {
        if (show) {
            showNoDataLayout(false)
            mScaningLayout.visibility = View.VISIBLE

            val magnify = 10000
            var toDegrees = 360f
            var duration = 1000L
            toDegrees *= magnify
            duration *= magnify
            val animation = RotateAnimation(0f, toDegrees,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f)
            animation.duration = duration
            animation.repeatCount = Animation.INFINITE
            animation.interpolator = LinearInterpolator()
            mNoDataLocusImageView.startAnimation(animation)
        } else {
            mNoDataLocusImageView.clearAnimation()
            mScaningLayout.visibility = View.GONE
        }
    }

    private fun showNoDataLayout(show: Boolean) {
        if (show) {
            showScanLayout(false)
            mNoDataLayout.visibility = View.VISIBLE
            mRecycleView.visibility = View.GONE
        } else {
            mNoDataLayout.visibility = View.GONE
            mRecycleView.visibility = View.VISIBLE
        }
    }

    private fun checkDataState() {
        if (mSongInfos.isEmpty()) {
            showNoDataLayout(true)
        } else {
            showNoDataLayout(false)
        }
    }

    private fun showBtnScan(show: Boolean) {
        if (show) {
            mScanBtnContainer.visibility = View.VISIBLE
        } else {
            mScanBtnContainer.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_scan -> {
                rescan()
            }

            R.id.back -> {
                finish()
            }
        }
    }

    private val onItemListener = object :
        LocalMusicAdapter.OnItemListener {

        override fun play(songInfo: SongInfo) {

        }

        override fun add(songInfo: SongInfo) {
            setResult(songInfo)
            finish()
        }
    }

    private class LocalMusicAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
        var onItemListener: OnItemListener? = null
        private var data = ArrayList<ItemStateInfo>()
        private val unknownArtist = ResourcesUtils.getString(R.string.music_info_unknown_artist)

        private var songPlayer = SongMediaPlayer()
        private var currentPlayItemStateInfo: ItemStateInfo? = null
        private val onPlayerListener = object : SongMediaPlayer.OnPlayerListener {

            fun reset() {
                songPlayer.reset()
            }

            fun play() {
                val isi = currentPlayItemStateInfo
                if (isi != null) {
                    songPlayer.play()
                    isi.playing = true
                    notifyDataSetChanged()
                    onItemListener?.play(isi.songInfo)
                }
            }

            fun pause() {
                val isi = currentPlayItemStateInfo
                if (isi != null) {
                    songPlayer.pause()
                    isi.playing = false
                    notifyItemChanged(isi.position, isi.position)
                }
            }

            fun load(isi: ItemStateInfo) {
                songPlayer.reset()
                val prevISI = currentPlayItemStateInfo
                if (prevISI != null && prevISI != isi) {
                    prevISI.playing = false
                    prevISI.loaded = false
                    notifyItemChanged(prevISI.position, prevISI.position)
                    songPlayer.reset()
                }
                currentPlayItemStateInfo = isi
                if (isi.loaded) {
                    onPrepared()
                } else {
                    songPlayer.load(isi.songInfo.path)
                }
            }

            override fun onPrepared() {
                currentPlayItemStateInfo?.loaded = true
                play()
            }

            override fun onCompletion() {
                val isi = currentPlayItemStateInfo
                if (isi != null) {
                    isi.playing = false
                    notifyItemChanged(isi.position, isi.position)
                }
            }

            override fun onError(err: String) {
            }

            override fun onProgress(time: Int) {


            }
        }


        init {
            songPlayer.addOnPlayerListener(onPlayerListener)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_local_music_item, parent, false)
            return LocalMusicViewHolder(
                view
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            val itemStateInfo = data[position]
            val holder = holder as LocalMusicViewHolder
            holder.itemStateInfo = itemStateInfo
            holder.currentPosition = position
            holder.titleTextView.text = itemStateInfo.songInfo.title
            holder.tvDuration.text = TimeUtils.formatTime(itemStateInfo.songInfo.duration)

            if (itemStateInfo.playing) {
                holder.btnPlayAndPause.setImageResource(R.drawable.icon_edit_music_online_pause)
                val magnify = 10000
                var toDegrees = 360f
                var duration = 1000L
                toDegrees *= magnify
                duration *= magnify
                val animation = RotateAnimation(0f, toDegrees,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                animation.duration = duration
                animation.repeatCount = Animation.INFINITE
                animation.interpolator = LinearInterpolator()
                holder.btnPlayAndPause.startAnimation(animation)
            } else {
                holder.btnPlayAndPause.setImageResource(R.drawable.icon_edit_music_online_play)
                holder.btnPlayAndPause.clearAnimation()
            }
            holder.btnPlayAndPause.setOnClickListener {
                if (!itemStateInfo.loaded) {
                    onPlayerListener.load(itemStateInfo)
                } else {
                    if (itemStateInfo.playing) {
                        onPlayerListener.pause()
                    } else {
                        onPlayerListener.play()
                    }
                }
            }
            holder.btnAdd.setOnClickListener {
                onItemListener?.add(itemStateInfo.songInfo)
            }
        }

        fun setData(songInfos: LinkedList<SongInfo>) {
            onPlayerListener.reset()
            data = songInfos.mapIndexed { index, songInfo ->
                ItemStateInfo(
                    songInfo,
                    index
                )
            } as ArrayList<ItemStateInfo>
            notifyDataSetChanged()
        }

        fun add(songInfo: SongInfo) {
            val size = data.size
            data.add(
                ItemStateInfo(
                    songInfo,
                    size
                )
            )
            notifyItemInserted(size)
        }

        fun release() {
            songPlayer.release()
        }

        data class ItemStateInfo(val songInfo: SongInfo, var position: Int, var playing: Boolean = false, var loaded: Boolean = false)

        private class LocalMusicViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            var currentPosition = -1
            val btnPlayAndPause: ImageView = itemView.findViewById(R.id.play_and_pause)
            val titleTextView: TextView = itemView.findViewById(R.id.title)
            val tvDuration: TextView = itemView.findViewById(R.id.duration)
            val btnAdd: ImageView = itemView.findViewById(R.id.add)
            var itemStateInfo: ItemStateInfo? = null
        }

        interface OnItemListener {
            fun play(songInfo: SongInfo)
            fun add(songInfo: SongInfo)
        }
    }

    companion object {
        const val INTENT_KEY_RESPONSE_DATA = "pick_data"
        const val INTENT_KEY_EXTRA_INFO = "extra_info"
        fun startActivity(context: Context) {
            val intent = Intent(context, LocalMusicActivity::class.java)
            context.startActivity(intent)
        }

        fun startActivityForResult(activity: Activity, requestCode: Int, bundle: Bundle? = null) {
            val intent = Intent(activity, LocalMusicActivity::class.java)
            if (bundle != null) {
                intent.putExtra(INTENT_KEY_EXTRA_INFO, bundle)
            }
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
