package com.allever.video.editor.ui.dialog

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.android.absbase.utils.ResourcesUtils
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.R

class SaveDialog(context: Context) : BaseDialog(context), View.OnClickListener {

    companion object {
        private val TAG = SaveDialog::class.java.simpleName
    }

    private lateinit var mLl480P: ViewGroup
    private lateinit var mLl720P: ViewGroup
    private lateinit var mLl1080P: ViewGroup

//    private lateinit var mIv480P: ImageView
//    private lateinit var mIv720P: ImageView
//    private lateinit var mIv1080P: ImageView

//    private lateinit var mTvPrevious: TextView
//    private lateinit var mTvSure: TextView

    private var mPosition = 0

    private var quality: Quality? = null

    var callback: Callback? = null

    /**
     * @param Profile
     * H.264有四种画质级别,分别是baseline, extended, main, high：
     * 1、Baseline Profile：基本画质。支持I/P 帧，只支持无交错（Progressive）和CAVLC；
     * 2、Extended profile：进阶画质。支持I/P/B/SP/SI 帧，只支持无交错（Progressive）和CAVLC；(用的少)
     * 3、Main profile：主流画质。提供I/P/B 帧，支持无交错（Progressive）和交错（Interlaced），也支持CAVLC 和CABAC 的支持；
     * 4、High profile：高级画质。在main Profile 的基础上增加了8×8内部预测、自定义量化、 无损视频编码和更多的YUV 格式；
     * @param preset 指定的编码速度越慢，获得的压缩效率就越高  (default "medium")   ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo
     * @param crf  恒定速率因子  在H.264和H.265中，CRF的范围从0到51（如QP）。23是x264的良好默认值，
     *              28是x265的默认值。18（或x265为24）应在视觉上透明; 任何更低的东西可能只是浪费文件大小     link https://slhck.info/video/2017/03/01/rate-control.html
     *              如果您想保持最佳质量而不关心文件大小，请使用此模式。
     * @param size  https://ffmpeg.org/ffmpeg-utils.html#video-size-syntax
     */
    data class Quality(
        val id: Int,
        val name: String,
        val size: String,
        val width: Int,
        val height: Int,
        val profile: String,
        val preset: String,
        val crf: String
    )

    override fun initDefaultView(context: Context?) {
        super.initDefaultView(context)
        setContentView(R.layout.ve_save_vedio_dialog)

        initView()
        setCancelable(false)
    }

    private fun initView() {
        mRootView = findViewById(R.id.root_view)
        mLl480P = findViewById(R.id.ll_480p_container)
        mLl720P = findViewById(R.id.ll_720p_container)
        mLl1080P = findViewById(R.id.ll_1080p_container)

//        mIv480P = findViewById(R.id.iv_480p)
//        mIv720P = findViewById(R.id.iv_720p)
//        mIv1080P = findViewById(R.id.iv_1080p)

//        mTvPrevious = findViewById(R.id.tv_cancel)
//        mTvSure = findViewById(R.id.tv_sure)

        mLl480P.setOnClickListener(this)
        mLl720P.setOnClickListener(this)
        mLl1080P.setOnClickListener(this)
//        mTvPrevious.setOnClickListener(this)
//        mTvSure.setOnClickListener(this)
    }

    /**
     * 1080i对应的分辨率是1920*1080i
    1080P对应的分辨率是1920*1080P
    720P对应的分辨率是1280*720
    480P对应的分辨率是720*480
    360P对应的分辨率是640*360
     */
    override fun onClick(v: View?) {
        when (v) {
            mLl480P -> {
                mPosition = 0
//                updateState(mIv480P, mIv720P, mIv1080P)
                quality = Quality(
                    mPosition,
                    ResourcesUtils.getString(R.string.save_video_dialog_item_480_p),
                    "hd480",
                    852,
                    480,
                    "-profile:v baseline -level 3.0",
                    "ultrafast",
                    "30"
                )
            }
            mLl720P -> {
                mPosition = 1
//                updateState(mIv720P, mIv1080P, mIv480P)
                quality = Quality(
                    mPosition,
                    ResourcesUtils.getString(R.string.save_video_dialog_item_720_p),
                    "hd720",
                    1280,
                    720,
                    "-profile:v main -level 4.2",
                    "ultrafast",
                    "23"
                )
            }
            mLl1080P -> {
                mPosition = 2
//                updateState(mIv1080P, mIv480P, mIv720P)
//                quality = Quality(mPosition,ResourcesUtils.getString(R.string.save_video_dialog_item_1080_p),1920,1080,"-profile:v high -level 5.1","veryslow","18")
                quality = Quality(
                    mPosition,
                    ResourcesUtils.getString(R.string.save_video_dialog_item_1080_p),
                    "hd1080",
                    1920,
                    1080,
                    "-profile:v main -level 4.2",
                    "ultrafast",
                    "18"
                )
            }

//            mTvPrevious -> {
//                dismiss()
//            }
//
//            mTvSure -> {
//                if(quality != null){
//                    dismiss()
//                    callback?.onSaveClick(quality!!)
//                }else{
//                    ToastUtils.show(ResourcesUtils.getString(R.string.save_video_dialog_title_tip))
//                }
//            }
        }

        if ((mPosition == 1 || mPosition == 2)
            && ConfigManager.purchaseSubSize < 1
        ) {
            mRootView.postDelayed({
            }, 300)
        } else {
            mRootView.postDelayed({
                dismiss()
                callback?.onSaveClick(quality!!)
            }, 300)
        }
    }

//    private fun updateState(selectedView: ImageView, unSelectView1: ImageView, unSelectView2: ImageView) {
//        selectedView.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.icon_resolution_select_on, null))
//        unSelectView1.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.icon_resolution_select_off, null))
//        unSelectView2.setImageDrawable(ResourcesCompat.getDrawable(context.resources, R.drawable.icon_resolution_select_off, null))
//    }

    public interface Callback {
        fun onSaveClick(quality: Quality)
    }
}