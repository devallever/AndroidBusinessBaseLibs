//package com.videoeditor.ui
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import android.view.View
//import android.widget.ImageView
//import android.widget.Toast
//import android.widget.VideoView
//import com.allever.video.editor.R
//import com.allever.video.editor.function.share.ShareImageItem
//import com.allever.video.editor.app.AppApplication
//import Base2Activity
//import com.allever.video.editor.function.share.ShareImageDialogHelp
//import com.allever.video.editor.function.share.ShareImageTools
//import com.allever.video.editor.ui.adapter.ShareItemAdapter
//import BitmapBean
//import ThumbnailBean
//import com.allever.video.editor.utils.*
//import SPDataManager
//import java.util.ArrayList
//
//class VEShareActivity : Base2Activity(), ShareItemAdapter.OptionListener, View.OnClickListener {
//    companion object {
//        private const val EXTRA_DATA = "etamjoi"
//
//        fun startActivity(context: Context, thumbnailBean: ThumbnailBean) {
//            val intent = Intent(context, VEShareActivity::class.java)
//            intent.putExtra(EXTRA_DATA, thumbnailBean)
//            context.startActivity(intent)
//        }
//    }
//
//    private lateinit var mRecyclerView: RecyclerView
//    private var mAdapter: ShareItemAdapter? = null
//    private var mData = mutableListOf<ShareImageItem.ShareImageItemData>()
//
//    private lateinit var mIvPlay: ImageView
//    private lateinit var mIvBack: ImageView
//    private lateinit var mVideoView: VideoView
//
//    private var mBitmapBean: BitmapBean? = null
//
//    private var mExtraData: ThumbnailBean? = null
//
//    private var mVideoViewHolder: VideoViewHolder? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.ve_activity_share)
//
//        getIntentData()
//
//        initView()
//
//        initData()
//    }
//
//    private fun getIntentData() {
//        mExtraData = intent?.getParcelableExtra(EXTRA_DATA)
//        mBitmapBean = BitmapBean()
//        mBitmapBean?.mDegree = mExtraData?.degree
//        mBitmapBean?.mUri = mExtraData?.uri
//        mBitmapBean?.mDate = mExtraData?.date
//        mBitmapBean?.mPath = mExtraData?.path
//        mBitmapBean?.mType = mExtraData?.type
//    }
//
//    private fun initView() {
//        mRecyclerView = findViewById(R.id.recycler_view_share)
//        val linearLayoutManager = LinearLayoutManager(this)
//        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
//        mRecyclerView.layoutManager = linearLayoutManager
//        mAdapter = ShareItemAdapter(this, R.layout.ve_item_share, mData)
//        mAdapter?.setOptionListener(this)
//        mRecyclerView.adapter = mAdapter
//
//        mIvPlay = findViewById(R.id.iv_play)
////        mIvPlay.setOnClickListener(this)
//
//        mIvBack = findViewById(R.id.iv_back)
//        mIvBack.setOnClickListener(this)
//
//        mVideoView = findViewById(R.id.video_view)
//
//        mVideoViewHolder = VideoViewHolder()
//        mVideoViewHolder?.initVideo(mVideoView, mBitmapBean?.mPath, mIvPlay)
//
//    }
//
//    private fun initData() {
//        val isImage = MediaTypeUtil.isImage(mBitmapBean?.mType ?: MediaTypeUtil.TYPE_OTHER_IMAGE)
//        val isGif = MediaTypeUtil.isGif(mBitmapBean?.mType ?: MediaTypeUtil.TYPE_OTHER_IMAGE)
//        val top3ShareTools = ShareImageTools.getTop3ShareTools(this, if (isImage) 1 else 2, true, !isGif)
//        val shareTools = ArrayList<ShareImageItem.ShareImageItemData>(top3ShareTools.subList(0, Math.min(4, top3ShareTools.size)))
//        shareTools.add(ShareImageTools.getMore(this))
//        mData.addAll(shareTools)
//        mAdapter?.notifyDataSetChanged()
//
//    }
//
//    override fun onClick(v: View?) {
//        when (v) {
//            mIvBack -> {
//                finish()
//                mVideoViewHolder?.pause()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mVideoViewHolder?.stop()
//    }
//
//    /***
//     *
//     */
//    override fun onShareItemClick(item: ShareImageItem.ShareImageItemData) {
//        val pkgName = item.getmPkgName()
//        val isImage = MediaTypeUtil.isImage(mBitmapBean?.mType ?: -1)
//        if (pkgName == null) {
//            //more
//            shareMore(mBitmapBean)
//        } else {
//            val isInstall = ShareImageTools.getAppIsInstalled(application, pkgName)
//            if (isInstall) {
//                val siid = item
//                var result = true
//                if (ShareImageTools.INSTAGRAM_SEND_PIC_TO_SHARE_PACKAGE_NAME == siid.getmPkgName()) {//Instagram不需要保存工具信息
//                    if (isImage) {
//                        result = ShareImageTools.startInstagramShareActivity(this@VEShareActivity, siid.getmPkgName(),
//                                siid.getmActivityName(), ShareImageDialogHelp.transferForIns(mBitmapBean?.mUri), MediaTypeUtil.isImage(mBitmapBean?.mType
//                                ?: -1))
//                    } else {
//                        result = ShareImageTools.startInstagramShareActivity(this@VEShareActivity, siid.getmPkgName(),
//                                siid.getmActivityName(), mBitmapBean?.mUri, MediaTypeUtil.isImage(mBitmapBean?.mType
//                                ?: -1))
//                    }
//                } else {
//                    result = ShareImageTools.startShareActivity(AppApplication.getApplication(), siid.getmPkgName(),
//                            siid.getmActivityName(), mBitmapBean!!)
//                    if (isImage) {
////                        DataManager.getInstance().recordShareImageTool(siid.getmPkgName(), siid.getmActivityName())
//                    }
//                }
//                if (result) {
////                    StatisticsUtils.statisticsCustomPreviewClick(StatisticsConstant.EVENT_FUNC_PREVIEW_VALUE_SHARE_CHANNEL + "_" + siid.getmLabel())
//                    SPDataManager.setShareCountOfDay(SPDataManager.getShareCountOfDay() + 1)
//                } else {
//                    Toast.makeText(AppApplication.getApplication(), R.string.not_install, Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(AppApplication.getApplication(), R.string.not_install, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun shareMore(bitmapBean: BitmapBean?) {
//        val isImage = MediaTypeUtil.isImage(bitmapBean?.mType ?: MediaTypeUtil.TYPE_OTHER_IMAGE)
//        ShareUtil.share(this, bitmapBean?.mUri, isImage)
//    }
//}