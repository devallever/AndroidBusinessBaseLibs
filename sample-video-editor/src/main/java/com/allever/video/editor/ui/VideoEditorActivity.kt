package com.allever.video.editor.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.*
import com.android.absbase.App
import com.android.absbase.ui.widget.drawable.TextDrawable
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.ToastUtils
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.R
import com.allever.video.editor.app.Base2Activity
import com.allever.video.editor.function.DataTemporaryStorageHelper
import com.allever.video.editor.function.Ratio
import com.allever.video.editor.function.editor.OnEffectEditListener
import com.allever.video.editor.function.editor.OnEffectSelectListener
import com.allever.video.editor.function.editor.OnEffectStateChangeListener
import com.allever.video.editor.function.editor.VideoEditorManager
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.bean.EffectBean
import com.allever.video.editor.function.editor.action.*
import com.allever.video.editor.function.editor.bean.*
import com.allever.video.editor.function.font.FontHelper
import com.allever.video.editor.function.music.MusicManager
import com.allever.video.editor.function.music.SongHelper
import com.allever.video.editor.function.music.SongInfo
import com.allever.video.editor.function.music.SongMediaPlayer
import com.allever.video.editor.function.online.LocalDataBean
import com.allever.video.editor.function.online.OnlineManager
import com.allever.video.editor.function.save.CommandHelper
import com.allever.video.editor.function.save.VideoMaker
import com.allever.video.editor.function.sticker.InsideStickerTool
import com.allever.video.editor.function.sticker.StickerManger
import com.allever.video.editor.function.timeline.TimeLineController
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.dialog.DialogUtils
import com.allever.video.editor.ui.dialog.SaveDialog
import com.allever.video.editor.ui.widget.*
import com.allever.video.editor.ui.widget.gesture.CustomGestureFrameLayout
import com.allever.video.editor.utils.*
import com.allever.video.editor.utils.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class VideoEditorActivity : Base2Activity(),
    View.OnClickListener,
    TimeLineController.TimeDispatchEventByControllerListener,
    OnEffectStateChangeListener,
    ActionController.OnActionListener,
    ActionController.OnAddActionListener,
    IApplyAction,
    CustomGestureFrameLayout.OnVideoPreparedListener,
    OnEffectEditListener,
    OnEffectSelectListener,
    SaveDialog.Callback {


    companion object {
        private val TAG = VideoEditorActivity::class.java.name
        private const val REQUEST_CODE_PRIVATE = 10001
        private const val REQUEST_CODE_PICK_MUSIC = 10002
        private const val REQUEST_CODE_PICK_ALBUM = 10003
        private const val RESPONSE_DATA = "pick_data"
        private const val RESPONSE_DATA_TEMP_STORAGE_KEY = "pick_data_temp_storage_key"
        private const val INTENT_KEY_REPLACE_EFFECT_ID = "replace_effect_id"


        private const val BOTTOM_FUN_TYPE_NOMAL = 0
        private const val BOTTOM_FUN_TYPE_MUSIC = 1000
        private const val BOTTOM_FUN_TYPE_MUSIC_LIST = 1001
        private const val BOTTOM_FUN_TYPE_MUSIC_VOLUME = 1002
        private const val BOTTOM_FUN_TYPE_MUSIC_TRIM = 1003
        private const val BOTTOM_FUN_TYPE_FORMAT = 1100
        private const val BOTTOM_FUN_TYPE_TEXT = 1200
        private const val BOTTOM_FUN_TYPE_TEXT_FONT = 1201
        private const val BOTTOM_FUN_TYPE_TEXT_COLOR = 1202
        private const val BOTTOM_FUN_TYPE_TEXT_OPACITY = 1203
        private const val BOTTOM_FUN_TYPE_TEXT_BACKGROUND = 1204
        private const val BOTTOM_FUN_TYPE_STICKER = 1300
        private const val BOTTOM_FUN_TYPE_TRIM = 1400

        private const val RESTORE_OR_REVERT_ALPHA_CAN = 1F
        private const val RESTORE_OR_REVERT_ALPHA_CAN_NOT = 0.2F

        @IntDef(
            value = [
                BOTTOM_FUN_TYPE_NOMAL,
                BOTTOM_FUN_TYPE_MUSIC,
                BOTTOM_FUN_TYPE_MUSIC_LIST,
                BOTTOM_FUN_TYPE_MUSIC_VOLUME,
                BOTTOM_FUN_TYPE_MUSIC_TRIM,
                BOTTOM_FUN_TYPE_FORMAT,
                BOTTOM_FUN_TYPE_TEXT,
                BOTTOM_FUN_TYPE_TEXT_FONT,
                BOTTOM_FUN_TYPE_TEXT_COLOR,
                BOTTOM_FUN_TYPE_TEXT_OPACITY,
                BOTTOM_FUN_TYPE_TEXT_BACKGROUND,
                BOTTOM_FUN_TYPE_STICKER,
                BOTTOM_FUN_TYPE_TRIM
            ]
        )
        @Retention(AnnotationRetention.SOURCE)
        annotation class BottomFunType


        @JvmStatic
        fun startActivity(context: Context, dataKey: String) {
            val intent = Intent(context, VideoEditorActivity::class.java)
            intent.putExtra(RESPONSE_DATA_TEMP_STORAGE_KEY, dataKey)
            context.startActivity(intent)
        }
    }

    private lateinit var mTitlePanel: ViewGroup
    private lateinit var mEffectEditLayout: EffectEditLayout
    private lateinit var mBottomFunLayout: LinearLayout
    private lateinit var mBottomBarSecondContainer: RelativeLayout
    private lateinit var mBottomFunSecondLayout: LinearLayout
    private lateinit var mBottomFunSecondScrollView: IndicativeHorizontalScrollView
    private lateinit var mBottomHorizontalScrollView: IndicativeHorizontalScrollView
    private lateinit var mFunctionContainer: ViewGroup
    private lateinit var mRevertAndRestoreLayout: ViewGroup
    private lateinit var mBtnRevert: ImageView
    private lateinit var mBtnRestore: ImageView
    private lateinit var mBtnPlay: ImageView
    private lateinit var mBtnFullScreen: ImageView
    private lateinit var mBtnBack: ImageView
    private lateinit var mBtnSave: TextView
    private lateinit var mSaveDialog: SaveDialog

    /**
     * 拖拽布局
     */
    private lateinit var mCustomGestureFrameLayout: CustomGestureFrameLayout

    private val mMainToneColor = ResourcesUtils.getColor(R.color.main_color_tone)

    private var mLoadView: LoadingView? = null
    private var mSavingVideoView: SavingVideoView? = null

    //底部Item
    private var mBottomTabFuns = arrayListOf<BottomTabHolder>()
    private var mBottomItemMusicHolder: BottomTabHolder? = null
    private var mBottomItemTextHolder: BottomTabHolder? = null
    private var mBottomItemStickerHolder: BottomTabHolder? = null
    private var mBottomItemFormatHolder: BottomTabHolder? = null
    private var mBottomItemTrimHolder: BottomTabHolder? = null

    /**
     * 底部音乐本地和在线数据
     */
    private var mBottomTabMusicDataFuns = arrayListOf<BottomTabHolder>()
    private var mBottomItemMusicLocalHolder: BottomTabHolder? = null

    // 暂时只支持添加一个音乐
    private var mBottomItemMusicNeedReplaceBean: SoundBean? = null

    /**
     * 底部音乐功能item
     */
    private var mBottomTabMusicFuns = arrayListOf<BottomTabHolder>()
    private var mBottomItemMusicMusicHolder: BottomTabHolder? = null
    private var mBottomItemMusicVolumeHolder: BottomTabHolder? = null
    private var mBottomItemMusicTrimHolder: BottomTabHolder? = null
    private var mBottomItemMusicDelHolder: BottomTabHolder? = null
    private var mBottomMusicInfoLayout: RelativeLayout? = null
    private var mBottomMusicInfoPlayView: ImageView? = null
    private var mBottomItemMusicVolumeLayout: ViewGroup? = null
    private var mBottomItemMusicOriginalSeekBar: CircleSeekBar? = null
    private var mBottomItemMusicSoundtrackSeekBar: CircleSeekBar? = null
    private var mBottomItemMusicOriginalTextView: TextView? = null
    private var mBottomItemMusicSoundtrackTextView: TextView? = null
    private var mCurrentOriginalVolume = 128
    private var mCurrentSoundtrackVolume = 128
    private var mBottomItemMusicTrimView: SoundTrimView? = null
    private var mBottomItemMusicFunShowing = false
    private var mBottomItemMusicVolumeShowing = false

    /**
     * 底部尺寸功能item
     */
    private var mBottomTabSizeFuns = arrayListOf<BottomTabHolder>()
    private var mBottomTabSizeShowing = false
    private var mBottomTabSizeCurrentSelectHolder: BottomTabHolder? = null
    private var mCurrentRatio = Ratio.RATIO_ORIGINAL

    /**
     * 底部字体功能item
     */
    private var mBottomTabFontFuns = arrayListOf<BottomTabHolder>()
    private var mBottomItemTextEditHolder: BottomTabHolder? = null
    private var mBottomItemTextFontHolder: BottomTabHolder? = null
    private var mBottomItemTextColorHolder: BottomTabHolder? = null
    private var mBottomItemTextOpacityHolder: BottomTabHolder? = null
    private var mBottomItemTextAlignHolder: BottomTabHolder? = null
    private var mBottomItemTextBackgroundHolder: BottomTabHolder? = null
    private var mBottomItemTextDelHolder: BottomTabHolder? = null
    private var mBottomItemTextCurrentSelectColor: ColorView? = null
    private var mBottomItemTextCurrentSelectBackground: ColorView? = null
    private var mBottomItemCurrentSelectFont: FontView? = null
    private var mBottomItemTextOpacityLayout: ViewGroup? = null
    private var mBottomItemTextOpacitySeekBar: CircleSeekBar? = null
    private var mBottomItemTextOpacityTextView: TextView? = null
    private var mCurrentTextOpacity = 100
    private var mBottomItemTextFunShowing = false

    /**
     * 底部sticker功能item
     */
    private var mBottomTabStickerFuns = arrayListOf<BottomTabHolder>()
    private var mCurrentStickerPackageName: String? = null

    /**
     * 底部trim功能item
     */
    private var mBottomTabTrimFuns = arrayListOf<BottomTabHolder>()
    private var mBottomItemTrimEffectListView: EffectListView? = null
    private var mBottomItemTrimAdd: BottomTabHolder? = null
    private var mBottomItemTrimCurrentSelectBean: EffectBean? = null
    private var mBottomItemTrimSecondShowing = false
    private var mBottomItemTrimSingleTrimShowing = false
    private var mBottomItemTrimView: MediaTrimView? = null

    private var mBottomItemBackHoler: BottomTabHolder? = null
    private var mBottomItemFun1Holer: BottomTabHolder? = null
    private var mBottomItemFun2Holer: BottomTabHolder? = null

    private val mBottomFunMargin = DeviceUtils.dip2pxF(10f)
    private val mBottomFunBtnWidth =
        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_btn_width)
    private val mBottomFunBackWidth =
        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_second_btn_width)

    private var mBitmapListList = mutableListOf<MutableList<Bitmap>>()


    private lateinit var mVideoEditorManager: VideoEditorManager

    /**
     * 媒体资源准备完成
     */
    private var initFinished = false

    private var mCurrentSelectEffectBean: EffectBean? = null

    private var mCurrentSelectedSoundBean: SoundBean? = null

    /**
     * 纪录内置和已下载的Sticker数，用于下载完成后交换位置
     */
    private var mLocalStickerCount = 0

    /**
     * 纪录内置和已下载的Music数，用于下载完成后交换位置
     * 从1 开始，第一个时本地选择本地音乐
     */
    private var mLocalMusicCount = 1

    private var mFullScreenMode = false

    private var mBottomTabStickerLastSelectHolder: BottomTabHolder? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_edit)
        mVideoEditorManager = VideoEditorManager(this)
        mVideoEditorManager.addListener(this)
        initView()
        initVideoData()
    }

    private fun initView() {
        mTitlePanel = findViewById(R.id.title_panel)
        mEffectEditLayout = findViewById(R.id.effect_edit)
        mEffectEditLayout.onEffectStateChangeListener = this
        mEffectEditLayout.onEffectSelectListener = this
        mEffectEditLayout.onEffectEditListener = this
        mBottomFunLayout = findViewById(R.id.bottom_fun_layout)
        mBottomBarSecondContainer = findViewById(R.id.bottom_bar_second_container)
        mBottomFunSecondLayout = findViewById(R.id.bottom_fun_second_layout)
        mBottomFunSecondScrollView = findViewById(R.id.bottom_fun_second_scroll_view)
        mBottomHorizontalScrollView = findViewById(R.id.bottom_fun_scroll_view)
        mFunctionContainer = findViewById(R.id.rl_function_container)

        mRevertAndRestoreLayout = findViewById(R.id.revert_and_restore)
        mBtnRevert = findViewById(R.id.iv_revert)
        mBtnRevert.setOnClickListener(this)
        mBtnRevert.alpha =
            RESTORE_OR_REVERT_ALPHA_CAN_NOT
        mBtnRestore = findViewById(R.id.iv_restore)
        mBtnRestore.setOnClickListener(this)
        mBtnRestore.alpha =
            RESTORE_OR_REVERT_ALPHA_CAN_NOT
        mBtnPlay = findViewById(R.id.iv_play)
        mBtnPlay.setOnClickListener(this)
        mBtnFullScreen = findViewById(R.id.iv_full_screen)
        mBtnFullScreen.setOnClickListener(this)
        mBtnBack = findViewById(R.id.back)
        mBtnBack.setOnClickListener(this)
        mBtnSave = findViewById(R.id.save)
        mBtnSave.setOnClickListener(this)


        mCustomGestureFrameLayout = findViewById(R.id.edit_gesture_container)
        mCustomGestureFrameLayout.onAddActionListener = this
        mCustomGestureFrameLayout.onVideoPreparedListener = this
        mCustomGestureFrameLayout.onEffectSelectListener = this
        mCustomGestureFrameLayout.onEffectEditListener = this
        showBottomLayoutFun()
        showBottomSecondLayout(false)

        mVideoEditorManager.addListener(mEffectEditLayout)
        mVideoEditorManager.onActionListener = this

        updateRevertAndRestoreState()

        showLoadingAnim(true)

        mSaveDialog = SaveDialog(this)
        mSaveDialog.setCancelable(true)
        mSaveDialog.callback = this

        mEffectEditLayout.onItemListener = object : EffectEditLayout.OnItemListener {
            override fun onItemRangeMoved() {
                updateState()
                showBottomTrimSecond(mBottomItemTrimSecondShowing, null)
            }
        }
    }

    override fun playFrameStart() {
        mBtnPlay.setImageResource(R.drawable.icon_edit_pause)
    }

    override fun playFramePause() {
        mBtnPlay.setImageResource(R.drawable.icon_edit_play)
    }

    override fun playFrameEnd() {
        mBtnPlay.setImageResource(R.drawable.icon_edit_play)
        mVideoEditorManager.pause(mCustomGestureFrameLayout)
    }

    override fun frameAtTime(
        currentPlayTimeReferenceOffset: Long,
        currentPlayTimeReferenceStart: Long,
        auto: Boolean
    ) {
        mVideoEditorManager.play(currentPlayTimeReferenceStart, mCustomGestureFrameLayout, auto)
    }

    private fun showLoadingAnim(show: Boolean) {
        if (mLoadView == null && show) {
            val viewStub = findViewById<ViewStub>(R.id.loading_view)
            val loadingView = viewStub.inflate() as LoadingView
            mLoadView = loadingView
        }
        if (show) {
            mLoadView?.startLoading()
        } else {
            mLoadView?.stopLoading()
        }
    }

    private fun showSavingAnim(show: Boolean, needCloseImmediately: Boolean = false) {
        if (mSavingVideoView == null && show) {
            val viewStub = findViewById<ViewStub>(R.id.saving_view)
            val savingView = viewStub.inflate() as SavingVideoView
            savingView.setCancelClickListener(View.OnClickListener {
                showSavingAnim(false, true)
            })
            mSavingVideoView = savingView
        }

        if (show) {
            mSavingVideoView?.startAnimation()
            mSavingVideoView?.startProgressSmooth(10_000, 5_000)
        } else {
            if (needCloseImmediately) {
                mSavingVideoView?.stopAnimation()
                VideoMaker.stopExecute()
            } else {
                mSavingVideoView?.endProgressSmooth(Runnable {
                    mSavingVideoView?.stopAnimation()
                })
            }
        }
    }

    private fun showBottomBack(show: Boolean, listener: View.OnClickListener? = this) {
        if (mBottomItemBackHoler == null) {
            mBottomItemBackHoler =
                BottomTabHolder.getHolderBy(
                    this,
                    R.id.bottom_tab_item_back,
                    R.string.video_edit_bottom_item_back,
                    R.drawable.icon_edit_bar_back,
                    listener
                )
        }
        mBottomItemBackHoler?.setOnClickListener(listener)
        if (show) {
            mBottomItemBackHoler?.setVisibility(View.VISIBLE)
        } else {
            mBottomItemBackHoler?.setVisibility(View.GONE)
        }
    }

    /**
     * 用于扩展的功能按钮,根据各自场景而定
     */
    private fun showBottomRightFun1(
        show: Boolean,
        @StringRes labelId: Int? = null,
        @DrawableRes iconId: Int? = null,
        clickListener: View.OnClickListener? = null
    ) {
        if (mBottomItemFun1Holer == null) {
            mBottomItemFun1Holer =
                BottomTabHolder.getHolderBy(
                    this,
                    R.id.bottom_tab_item_right_1,
                    labelId,
                    iconId,
                    clickListener
                )
        }
        mBottomItemFun1Holer?.setOnClickListener(clickListener)
        if (labelId != null) {
            mBottomItemFun1Holer?.setText(labelId)
        }
        if (iconId != null) {
            mBottomItemFun1Holer?.setImageResource(iconId)
        }
        mBottomItemFun1Holer?.show(show)
    }

    /**
     * 用于扩展的功能按钮,根据各自场景而定
     */
    private fun showBottomRightFun2(
        show: Boolean,
        @StringRes labelId: Int? = null,
        @DrawableRes iconId: Int? = null,
        clickListener: View.OnClickListener? = null
    ) {
        if (mBottomItemFun2Holer == null) {
            mBottomItemFun2Holer =
                BottomTabHolder.getHolderBy(
                    this,
                    R.id.bottom_tab_item_right_2,
                    labelId,
                    iconId,
                    clickListener
                )
        }
        mBottomItemFun2Holer?.setOnClickListener(clickListener)
        if (labelId != null) {
            mBottomItemFun2Holer?.setText(labelId)
        }
        if (iconId != null) {
            mBottomItemFun2Holer?.setImageResource(iconId)
        }
        mBottomItemFun2Holer?.show(show)
    }

    private fun updateBottomLayout(
        bottomTabHolders: List<BottomTabHolder>, listGravity: Int = Gravity.CENTER,
        itemWidth: Int? = null,
        itemHeight: Int? = null
    ) {
        mBottomFunLayout.removeAllViews()
        mBottomHorizontalScrollView.setScrollable(true)
        for (bth in bottomTabHolders) {
            val layout = bth.layout
            if (layout != null) {
                var lp = layout.layoutParams
                if (lp == null) {
                    val itemWidth = itemWidth ?: mBottomFunBtnWidth.toInt()
                    val itemHeight = itemHeight ?: LinearLayout.LayoutParams.MATCH_PARENT
                    val tmpLp = lp as? LinearLayout.LayoutParams
                        ?: LinearLayout.LayoutParams(itemWidth, itemHeight)
                    tmpLp.gravity = Gravity.CENTER_VERTICAL
                    lp = tmpLp
                }
                mBottomFunLayout.addView(layout, lp)
            }
        }
        val lp = mBottomFunLayout.layoutParams
        if (lp is FrameLayout.LayoutParams) {
            lp.gravity = listGravity
        }
    }

    private fun updateBottomLayoutByView(
        views: List<View>, listGravity: Int = Gravity.CENTER,
        itemWidth: Int? = null,
        itemHeight: Int? = null
    ) {
        mBottomFunLayout.removeAllViews()
        mBottomHorizontalScrollView.setScrollable(true)
        for (view in views) {
            var lp = view.layoutParams
            if (lp == null) {
                val itemWidth = itemWidth ?: mBottomFunBtnWidth.toInt()
                val itemHeight = itemHeight ?: LinearLayout.LayoutParams.MATCH_PARENT
                val tmpLp = lp as? LinearLayout.LayoutParams
                    ?: LinearLayout.LayoutParams(itemWidth, itemHeight)
                tmpLp.gravity = Gravity.CENTER_VERTICAL
                lp = tmpLp
            }
            mBottomFunLayout.addView(view, lp)
        }
        mBottomHorizontalScrollView.scrollTo(0, 0)
        val lp = mBottomFunLayout.layoutParams
        if (lp is FrameLayout.LayoutParams) {
            lp.gravity = listGravity
        }
    }

    private fun showBottomSecondLayout(show: Boolean) {
        val secondContainerLp = mBottomBarSecondContainer.layoutParams
        secondContainerLp.height = DeviceUtils.dip2px(70f)
        mBottomBarSecondContainer.layoutParams = secondContainerLp

        mBottomFunSecondScrollView.setScrollable(true)
        if (show) {
            mBottomBarSecondContainer.visibility = View.VISIBLE
        } else {
            mBottomBarSecondContainer.visibility = View.GONE
        }
    }

    private fun showBottomLayoutByType(
        type: Int,
        backType: Int = BOTTOM_FUN_TYPE_NOMAL,
        arg1: Any? = null,
        arg2: Any? = null,
        arg3: Any? = null
    ) {
        when (type) {
            BOTTOM_FUN_TYPE_NOMAL -> {
                showBottomLayoutFun()
            }
            BOTTOM_FUN_TYPE_MUSIC -> {
                showBottomMusicFun(arg1 as Boolean, arg2 as SoundBean?)
            }
            BOTTOM_FUN_TYPE_MUSIC_LIST -> {
                showBottomMusicList(arg1 as Boolean, arg2 as Boolean, arg3 as SoundBean?)
            }
            BOTTOM_FUN_TYPE_MUSIC_VOLUME -> {

            }
            BOTTOM_FUN_TYPE_MUSIC_TRIM -> {

            }
            BOTTOM_FUN_TYPE_FORMAT -> {
                showBottomSizeFun(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_TEXT -> {
                showBottomTextFun(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_TEXT_FONT -> {
                showBottomTextFont(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_TEXT_COLOR -> {
                showBottomTextColor(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_TEXT_OPACITY -> {
                showBottomTextOpacity(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_STICKER -> {
                showBottomStickerFun(arg1 as Boolean)
            }
            BOTTOM_FUN_TYPE_TRIM -> {
                showBottomTrimFun(arg1 as Boolean)
            }


        }
    }

    private fun showBottomLayoutFun() {
        if (mBottomTabFuns.isEmpty()) {
            mBottomItemMusicHolder =
                BottomTabHolder.getHolderInflater(
                    this,
                    R.layout.edit_bottom_item_view,
                    R.string.video_edit_bottom_item_music,
                    R.drawable.icon_edit_music,
                    this
                )
            mBottomItemFormatHolder =
                BottomTabHolder.getHolderInflater(
                    this,
                    R.layout.edit_bottom_item_view,
                    R.string.video_edit_bottom_item_format,
                    R.drawable.icon_edit_size,
                    this
                )
            mBottomItemTextHolder =
                BottomTabHolder.getHolderInflater(
                    this,
                    R.layout.edit_bottom_item_view,
                    R.string.video_edit_bottom_item_text,
                    R.drawable.icon_edit_font,
                    this
                )
            mBottomItemStickerHolder =
                BottomTabHolder.getHolderInflater(
                    this,
                    R.layout.edit_bottom_item_view,
                    R.string.video_edit_bottom_item_sticker,
                    R.drawable.icon_edit_sticker,
                    this
                )
            mBottomItemTrimHolder =
                BottomTabHolder.getHolderInflater(
                    this,
                    R.layout.edit_bottom_item_view,
                    R.string.video_edit_bottom_item_trim,
                    R.drawable.icon_edit_trim,
                    this
                )
            mBottomTabFuns.add(mBottomItemMusicHolder!!)
            mBottomTabFuns.add(mBottomItemFormatHolder!!)
            mBottomTabFuns.add(mBottomItemTextHolder!!)
            mBottomTabFuns.add(mBottomItemStickerHolder!!)
            mBottomTabFuns.add(mBottomItemTrimHolder!!)
        }
        showBottomBack(false, null)
        updateBottomLayout(mBottomTabFuns)
    }


    private fun showBottomMusicList(
        show: Boolean,
        replace: Boolean = false,
        songBean: SoundBean? = null
    ) {
        if (show) {
            val listener = View.OnClickListener {
                when (it.id) {
                    mBottomItemMusicLocalHolder?.getLayoutId() -> {
                        bottomMusicInfoPlayOrPause(false, songBean)
                        val bundle = if (replace && songBean != null) {
                            val b = Bundle()
                            b.putInt(INTENT_KEY_REPLACE_EFFECT_ID, songBean.id)
                            b
                        } else null
                        LocalMusicActivity.startActivityForResult(
                            this@VideoEditorActivity,
                            REQUEST_CODE_PICK_MUSIC,
                            bundle
                        )
                    }
                    mBottomItemBackHoler?.getLayoutId() -> {
                        if (replace) {
                            showBottomMusicFun(true, songBean)
                        } else {
                            showBottomMusicList(false)
                        }
                    }
                }
            }

            mBottomItemMusicLocalHolder?.setOnClickListener(listener)
            //需要更新监听，否则(replace && songBean != null) 总是false,
            if (mBottomTabMusicDataFuns.isEmpty()) {
                //本地
                mBottomItemMusicLocalHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_music_local,
                        R.drawable.icon_edit_music_local,
                        listener
                    )
                mBottomTabMusicDataFuns.add(mBottomItemMusicLocalHolder!!)
//                mBottomItemMusicLocalHolder?.setOnClickListener(listener)

                //内置
                val insideMusicListener = View.OnClickListener {
                    val holder = it.tag as BottomTabHolder
                    val path = holder.obj as String
                    val songInfo = SongInfo.createFromPath(path)
                    pickMusic(songInfo, mBottomItemMusicNeedReplaceBean?.id)
                }
                val insideMusicResources = MusicManager.getInsideMusicResource()
                val textColor = ResourcesCompat.getColor(
                    resources,
                    R.color.video_edit_bottom_online_music_text_color,
                    null
                )
                for (insideMusicResource in insideMusicResources) {
                    val width =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_item_music_online_width)
                            .toInt()
                    val insideHolder =
                        BottomTabHolder.getHolderInflater(
                            this,
                            R.layout.edit_bottom_item_music_online_view,
                            clickListener = insideMusicListener
                        )
                    val margin =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_item_music_online_margin)
                            .toInt()
                    insideHolder.setText(insideMusicResource.title ?: "")
                    insideHolder.textView?.setTextColor(textColor)
                    insideHolder.textView?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    val iconBitmap = AssetsUtil.toBitmap(insideMusicResource.iconPath)
                    if (iconBitmap != null) {
                        insideHolder.setImageBitmap(iconBitmap)
                    }
                    insideHolder.obj = insideMusicResource.cachePath
                    val lp = LinearLayout.LayoutParams(width, width)
                    lp.gravity = Gravity.CENTER
                    lp.leftMargin = margin
                    lp.rightMargin = margin
                    insideHolder.layout?.layoutParams = lp
                    mBottomTabMusicDataFuns.add(insideHolder)
                    mLocalMusicCount++
                }

                //下载
                val onlineMusicClickListener = View.OnClickListener {
                    val holder = it.tag as BottomTabHolder
                    val data = holder.obj as LocalDataBean
                    val existUnzipFiles = MusicManager.checkUnzipFileExist(data)
                    if (existUnzipFiles) {
                        pickMusic(
                            MusicManager.getUnzipSondInfo(data),
                            mBottomItemMusicNeedReplaceBean?.id
                        )
                    } else {
                        if (data.downloaded) {
                            //解压
                            if (MusicManager.unzipDownloadMusicFile(data)) {
                                pickMusic(
                                    MusicManager.getUnzipSondInfo(data),
                                    mBottomItemMusicNeedReplaceBean?.id
                                )
                            } else {
                                ToastUtils.show(getString(R.string.download_error_tips))
                            }
                        } else {
                            //下载
                            val downloadMusicCallback =
                                object : OnlineManager.DownloadResourceCallback {
                                    override fun onConnected() {
                                        holder.circleProgressBar?.maxProgress = 100
                                        holder.circleProgressBar?.setProgress(0)
                                    }

                                    override fun onCompleted() {
                                        //解压
                                        if (MusicManager.unzipDownloadMusicFile(data)) {
                                            holder.circleProgressBarContainer?.visibility =
                                                View.GONE
                                            holder.downloadImageView?.visibility = View.GONE
                                            holder.mask?.visibility = View.GONE
                                            holder.crown?.visibility = View.GONE

                                            //刷新界面
                                            val from = mBottomTabMusicDataFuns.indexOf(holder)
                                            val to = mLocalMusicCount
                                            Collections.swap(mBottomTabMusicDataFuns, from, to)
                                            mLocalMusicCount++

                                            //如果当前展示的功能区是music，则刷新界面
                                            if (mBottomItemMusicFunShowing) {
                                                updateBottomLayout(
                                                    mBottomTabMusicDataFuns,
                                                    Gravity.START
                                                )
                                            }

                                            ToastUtils.show("${data.name} ${getString(R.string.download_finish_tips)}")
                                            //pickMusic(MusicManager.getUnzipSondInfo(data), mBottomItemMusicNeedReplaceBean?.id)

                                        } else {
                                            ToastUtils.show(getString(R.string.download_error_tips))
                                        }
                                    }

                                    override fun onProgress(progress: Int) {
                                        holder.progressText?.text = progress.toString()
                                        holder.circleProgressBar?.setProgress(progress)
                                    }

                                    override fun onFailed() {
                                        if (data.isNeedBuy) {
                                            holder.crown?.visibility = View.VISIBLE
                                        } else {
                                            holder.downloadImageView?.visibility = View.VISIBLE
                                        }
                                        holder.circleProgressBarContainer?.visibility = View.GONE
                                        holder.mask?.visibility = View.GONE
                                    }

                                }
                            if (!data.isNeedBuy || ConfigManager.purchaseSubSize > 0) {
                                holder.circleProgressBarContainer?.visibility = View.VISIBLE
                                holder.mask?.visibility = View.VISIBLE
                                holder.crown?.visibility = View.GONE
                                holder.downloadImageView?.visibility = View.GONE
                                MusicManager.downloadMusic(data, downloadMusicCallback)
                            } else {
                            }
                        }
                    }
                }

                val downloadedBottomHolders = arrayListOf<BottomTabHolder>()
                val unDownloadBottomHolders = arrayListOf<BottomTabHolder>()
                val localMusicDatas = MusicManager.getLocalMusicDatas()
                for (it in localMusicDatas.iterator()) {
                    val pkg = it.key
                    val localMusicData = it.value
                    val width =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_item_music_online_width)
                            .toInt()
                    val margin =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_item_music_online_margin)
                            .toInt()
                    val online =
                        BottomTabHolder.getHolderInflater(
                            this,
                            R.layout.edit_bottom_item_music_online_view,
                            clickListener = onlineMusicClickListener
                        )
                    online.setText(localMusicData.name)
                    online.textView?.setTextColor(textColor)
                    online.textView?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    online.obj = localMusicData
                    val lp = LinearLayout.LayoutParams(width, width)
                    lp.gravity = Gravity.CENTER
                    lp.leftMargin = margin
                    lp.rightMargin = margin
                    online.layout?.layoutParams = lp

                    val existUnzipFiles = MusicManager.checkUnzipFileExist(localMusicData)
                    if (existUnzipFiles) {
                        //加载下载zip包的icon
                        val musicIconFilePath = MusicManager.getUnzipIconPath(localMusicData)
                        val bitmap = BitmapFactory.decodeFile(musicIconFilePath)
                        if (bitmap != null) {
                            online.setImageBitmap(bitmap)
                        }
                        downloadedBottomHolders.add(online)
                        mLocalMusicCount++
                    } else {
                        //是否下载
                        if (localMusicData.downloaded) {
                            //解压
                            if (MusicManager.unzipDownloadMusicFile(localMusicData)) {
                                val unzipIconPath = MusicManager.getUnzipIconPath(localMusicData)
                                val bitmap = BitmapFactory.decodeFile(unzipIconPath)
                                if (bitmap != null) {
                                    online.setImageBitmap(bitmap)
                                }
                            } else {
                                ImageLoader.loadImage(
                                    localMusicData.getDefaultSmallImgUrl(),
                                    R.drawable.icon_album_default,
                                    online.imageView
                                )
                            }
                            downloadedBottomHolders.add(online)
                            mLocalMusicCount++
                        } else {
                            //加载网络图
                            if (localMusicData.isNeedBuy) {
                                online.crown?.visibility = View.VISIBLE
                                online.downloadImageView?.visibility = View.GONE
                            } else {
                                online.crown?.visibility = View.GONE
                                online.downloadImageView?.visibility = View.VISIBLE
                            }
                            ImageLoader.loadImage(
                                localMusicData.getDefaultSmallImgUrl(),
                                R.drawable.icon_album_default,
                                online.imageView
                            )
                            unDownloadBottomHolders.add(online)
                        }
                    }
                }
                mBottomTabMusicDataFuns.addAll(downloadedBottomHolders)
                mBottomTabMusicDataFuns.addAll(unDownloadBottomHolders)
            }
            showBottomBack(show, listener)
            updateBottomLayout(mBottomTabMusicDataFuns, Gravity.START)
        } else {
            showBottomLayoutFun()
        }
    }

    private fun showBottomMusicFun(show: Boolean, songBean: SoundBean?) {
        if (show) {
            val listener = View.OnClickListener {
                when (it?.id) {
                    mBottomItemBackHoler?.getLayoutId() -> {
                        showBottomMusicFun(false, songBean)
                    }
                    mBottomItemMusicMusicHolder?.getLayoutId() -> {
                        showBottomMusicList(true, true, songBean)
                    }
                    mBottomItemMusicVolumeHolder?.getLayoutId() -> {
                        showBottomMusicVolume(true, songBean)
                    }
                    mBottomItemMusicTrimHolder?.getLayoutId() -> {
                        showBottomMusicTrim(true, songBean)
                    }
                    mBottomItemMusicDelHolder?.getLayoutId() -> {
                        if (songBean != null) {
                            deleteEffect(songBean)
                        }
                    }
                }
            }
            if (mBottomTabMusicFuns.isEmpty()) {
                mBottomItemMusicMusicHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_music,
                        R.drawable.icon_edit_music,
                        listener
                    )
                mBottomItemMusicVolumeHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_music_volume,
                        R.drawable.icon_edit_music_volume,
                        listener
                    )
                mBottomItemMusicTrimHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_trim,
                        R.drawable.icon_edit_trim,
                        listener
                    )
                mBottomItemMusicDelHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_del,
                        R.drawable.icon_edit_del,
                        listener
                    )
                mBottomTabMusicFuns.add(mBottomItemMusicMusicHolder!!)
                mBottomTabMusicFuns.add(mBottomItemMusicVolumeHolder!!)
                mBottomTabMusicFuns.add(mBottomItemMusicTrimHolder!!)
                mBottomTabMusicFuns.add(mBottomItemMusicDelHolder!!)
            }
            mBottomItemMusicMusicHolder?.setOnClickListener(listener)
            mBottomItemMusicVolumeHolder?.setOnClickListener(listener)
            mBottomItemMusicTrimHolder?.setOnClickListener(listener)
            mBottomItemMusicDelHolder?.setOnClickListener(listener)

            showBottomBack(show, listener)
            updateBottomLayout(mBottomTabMusicFuns)
            showBottomMusicInfoInSecond(true, songBean)
        } else {
            showBottomMusicInfoInSecond(false, songBean)
            showBottomLayoutFun()
        }
        mBottomItemMusicFunShowing = show
    }

    private fun bottomMusicInfoPlayOrPause(play: Boolean? = null, effectBean: SoundBean? = null) {
        val needPlay = play ?: (effectBean?.player?.isPlaying() != true)
        if (needPlay) {
            effectBean?.play(effectBean.videoTime.dstStartTime, null, true)
            mBottomMusicInfoPlayView?.let {
                it.setImageResource(R.drawable.icon_edit_music_index_pause)
//                val magnify = 10000
//                var toDegrees = 360f
//                var duration = 1000L
//                toDegrees *= magnify
//                duration *= magnify
//                val animation = RotateAnimation(0f, toDegrees,
//                        Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f)
//                animation.duration = duration
//                animation.repeatCount = Animation.INFINITE
//                animation.interpolator = LinearInterpolator()
//                it.clearAnimation()
//                it.startAnimation(animation)
                it
            }
            mCurrentSelectedSoundBean = effectBean
        } else {
            effectBean?.pause(null)
            mBottomMusicInfoPlayView?.let {
                it.setImageResource(R.drawable.icon_edit_music_index_play)
//                it.clearAnimation()
                null
            }
        }
    }

    private fun showBottomMusicInfoInSecond(show: Boolean, effectBean: SoundBean? = null) {
        mBottomFunSecondLayout.removeAllViews()
        showBottomSecondLayout(show)
        if (show) {
            val songBean = effectBean ?: return
            val layout = mBottomMusicInfoLayout.let {
                if (it == null) {
                    val layout =
                        layoutInflater.inflate(R.layout.layout_music_info, null) as RelativeLayout
                    mBottomMusicInfoLayout = layout
                    layout
                } else it
            }
            layout.visibility = View.VISIBLE
            val playView = layout.findViewById(R.id.play_and_pause) as ImageView
            mBottomMusicInfoPlayView = playView
            val musicTitleView = layout.findViewById(R.id.title) as TextView
            val musicTimeView = layout.findViewById(R.id.time) as TextView
            songBean.player?.addOnPlayerListener(object : SongMediaPlayer.OnPlayerListener {
                override fun onPrepared() {
                }

                override fun onCompletion() {
                    bottomMusicInfoPlayOrPause(false, effectBean)
                }

                override fun onError(err: String) {
                }

                override fun onProgress(time: Int) {
                    musicTimeView.text = SongHelper.formatTime(time.toLong())
                }
            })

            val listener = View.OnClickListener {
                when (it.id) {
                    R.id.play_and_pause -> {
                        bottomMusicInfoPlayOrPause(null, effectBean)
                    }
                    R.id.title -> {

                    }
                    R.id.time -> {

                    }
                }
            }
            playView.setOnClickListener(listener)
            musicTitleView.setOnClickListener(listener)
            musicTimeView.setOnClickListener(listener)

            musicTitleView.text = songBean.name
            musicTimeView.text = SongHelper.formatTime(songBean.dstDuration)

            val secondContainerLp = mBottomBarSecondContainer.layoutParams
            secondContainerLp.height = DeviceUtils.dip2px(40f)
            mBottomBarSecondContainer.layoutParams = secondContainerLp

            layout.tag = songBean
            val lp = LinearLayout.LayoutParams(
                DeviceUtils.SCREEN_WIDTH_PX,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            mBottomFunSecondLayout.addView(layout, lp)
        } else {
            mBottomMusicInfoLayout?.visibility = View.GONE
            bottomMusicInfoPlayOrPause(false, effectBean)
        }
    }

    private fun showBottomMusicVolume(show: Boolean, effectBean: SoundBean? = null) {
        if (show) {
            val listener = object : View.OnClickListener,
                OnSeekBarChangeListener {
                override fun onClick(v: View?) {
                    when (v?.id) {
                        mBottomItemBackHoler?.getLayoutId() -> {
                            showBottomMusicVolume(false, effectBean)
                        }
                    }
                }

                override fun onProgressChanged(
                    seekBar: CustomNumSeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (seekBar == mBottomItemMusicOriginalSeekBar) {
//                        val text = resources.getString(R.string.video_edit_bottom_item_music_original_volume, "$progress")
//                        mBottomItemMusicOriginalTextView?.text = text
                        mCurrentOriginalVolume = progress
//                        mVideoEditorManager.volume = progress
                        mVideoEditorManager.setVideoVolume(progress, mCustomGestureFrameLayout)
                    } else if (seekBar == mBottomItemMusicSoundtrackSeekBar) {
//                        val text = resources.getString(R.string.video_edit_bottom_item_music_soundtrack, "$progress")
//                        mBottomItemMusicSoundtrackTextView?.text = text
                        mCurrentSoundtrackVolume = progress
                        effectBean?.volume = progress
                    }
                }

                private var prevProgress: Int? = null
                override fun onStartTrackingTouch(seekBar: CustomNumSeekBar?) {
                    if (seekBar == mBottomItemMusicOriginalSeekBar) {
                        prevProgress = mVideoEditorManager.volume
                    } else if (seekBar == mBottomItemMusicSoundtrackSeekBar) {
                        if (effectBean != null) {
                            prevProgress = effectBean.volume
                        }
                    }
                }

                override fun onStopTrackingTouch(seekBar: CustomNumSeekBar?) {
                    val multiEffectAction =
                        MultiEffectAction(effectBean?.clone())
                    multiEffectAction.type = ActionType.TYPE_VOLUME_CHANGE
                    if (seekBar == mBottomItemMusicOriginalSeekBar) {
                        multiEffectAction.obj = arrayListOf(mCurrentOriginalVolume, null)
                        multiEffectAction.prevObj = arrayListOf(prevProgress, null)
                    } else if (seekBar == mBottomItemMusicSoundtrackSeekBar) {
                        multiEffectAction.obj = arrayListOf(null, effectBean?.volume)
                        multiEffectAction.prevObj = arrayListOf(null, prevProgress)
                    }
                    addAction(multiEffectAction)
                }

            }
            if (mBottomItemMusicVolumeLayout == null) {
                val viewGroup =
                    layoutInflater.inflate(R.layout.layout_music_volume, null) as ViewGroup
                mBottomItemMusicVolumeLayout = viewGroup
                mBottomItemMusicOriginalSeekBar = viewGroup.findViewById(R.id.seekbar_original)
                mBottomItemMusicSoundtrackSeekBar = viewGroup.findViewById(R.id.seekbar_soundtrack)
                mBottomItemMusicOriginalSeekBar?.setOnSeekBarChangeListener(listener)
                mBottomItemMusicSoundtrackSeekBar?.setOnSeekBarChangeListener(listener)
                mBottomItemMusicOriginalTextView = viewGroup.findViewById(R.id.textview_original)
                mBottomItemMusicSoundtrackTextView =
                    viewGroup.findViewById(R.id.textview_soundtrack)

                val margin =
                    ResourcesUtils.getDimension(R.dimen.video_edit_bottom_text_font_opacity_margin)
                val width = DeviceUtils.SCREEN_WIDTH_PX - margin * 2 - mBottomFunBackWidth
                val lp =
                    LinearLayout.LayoutParams(width.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                viewGroup.layoutParams = lp
            }
            mBottomItemMusicOriginalSeekBar?.progress = mVideoEditorManager.volume
            mBottomItemMusicSoundtrackSeekBar?.progress = effectBean?.volume ?: 128


            showBottomBack(show, listener)
            updateBottomLayoutByView(arrayListOf(mBottomItemMusicVolumeLayout!!), Gravity.CENTER)
            mBottomHorizontalScrollView.setScrollable(false)
        } else {
            showBottomMusicFun(true, effectBean)
        }
        mBottomItemMusicVolumeShowing = show
    }

    private fun showBottomMusicTrim(show: Boolean, effectBean: SoundBean? = null) {
        if (show) {
            val listener = object : View.OnClickListener, SoundTrimView.OnTimeChangeLintener {
                override fun onClick(v: View?) {
                    when (v?.id) {
                        mBottomItemBackHoler?.getLayoutId() -> {
                            showBottomMusicVolume(false, effectBean)
                        }
                    }
                }

                override fun timeStart(start: Boolean, end: Boolean) {
                    mBottomHorizontalScrollView.setScrollable(false)
                }

                override fun timeEnd(start: Boolean, end: Boolean) {
                    mBottomHorizontalScrollView.setScrollable(true)
                }

                override fun timeMove(start: Boolean, end: Boolean, offsetTime: Long) {
                    if (effectBean != null) {
                        if (start) {
                            effectBean.videoTime.srcStartTime += offsetTime
                            effectBean.videoTime.dstEndTime -= offsetTime
                        } else if (end) {
                            effectBean.videoTime.srcEndTime += offsetTime
                            effectBean.videoTime.dstEndTime += offsetTime
                        }
                        updateState()
                        mBottomItemMusicTrimView?.arrowColor =
                            ResourcesUtils.getColor(R.color.white)
                    }
                }
            }
            val layout = mBottomItemMusicTrimView.let {
                if (it == null) {
                    val soundTrimView = SoundTrimView(this)
                    mBottomItemMusicTrimView = soundTrimView
                    soundTrimView.duration = 13500
                    soundTrimView
                } else it
            }
            layout.arrowColor = ResourcesUtils.getColor(R.color.white)
            layout.onTimeChangeLintener = listener
            if (effectBean != null) {
                layout.duration = effectBean.dstDuration
                layout.startTime = effectBean.videoTime.srcStartTime
                layout.endTime = effectBean.videoTime.srcEndTime
            }

            showBottomBack(show, listener)
            updateBottomLayoutByView(arrayListOf(layout), Gravity.START)
            showBottomMusicInfoInSecond(true, effectBean)
        } else {
            showBottomMusicFun(true, effectBean)
        }
    }

    private fun updateRatioState(holder: BottomTabHolder? = null) {
        if (holder != null) {
            if (holder != mBottomTabSizeCurrentSelectHolder) {
                mBottomTabSizeCurrentSelectHolder?.imageView?.clearColorFilter()
                holder.imageView?.setColorFilter(mMainToneColor)
                mBottomTabSizeCurrentSelectHolder = holder
            }
        } else if (mBottomTabSizeShowing) {
            val currentRatio = mCustomGestureFrameLayout.getRatio()
            mBottomTabSizeFuns.find {
                val ratio = it.obj as? Ratio
                ratio != null && Ratio.equals(currentRatio, ratio)
            }?.also {
                updateRatioState(it)
            }
        }
    }

    private fun showBottomSizeFun(show: Boolean) {
        if (show) {
            if (mBottomTabSizeFuns.isEmpty()) {
                val listener = View.OnClickListener {
                    if (it.tag is BottomTabHolder) {
                        val holder = it.tag as BottomTabHolder
                        val ratio = holder.obj as Ratio
                        mCurrentRatio = ratio
                        mCustomGestureFrameLayout.setRatio(ratio)
                        updateRatioState(holder)
                    }
                }
                val ratios = Ratio.RATIOS

                for (ratio in ratios) {
                    val holder =
                        BottomTabHolder.getHolderInflater(
                            this,
                            R.layout.edit_bottom_item_view,
                            null,
                            ratio.drawableId,
                            listener
                        )
                    holder.setText(ratio.scaleFactorString)
                    // 看下需不需要考虑对按钮做状态处理
                    if (Ratio.equals(mCustomGestureFrameLayout.getRatio(), ratio)) {
                        updateRatioState(holder)
                    }
                    holder.obj = ratio
                    mBottomTabSizeFuns.add(holder)
                }
            }

            showBottomBack(show)
            updateBottomLayout(mBottomTabSizeFuns, Gravity.LEFT)
        } else {
            showBottomLayoutFun()
        }
        mBottomTabSizeShowing = show
    }

    private fun updateTextBackgroundState() {
        val holder = mBottomItemTextBackgroundHolder ?: return
        val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
        if (effectBean != null) {
            when (effectBean.backgroundType) {
                TextBean.TEXT_BACKGROUND_TYPE_NOMAL -> {
                    holder.setImageResource(R.drawable.icon_edit_font_fonttype_1)
                }
                TextBean.TEXT_BACKGROUND_TYPE_INVERTED -> {
                    holder.setImageResource(R.drawable.icon_edit_font_fonttype_2)
                }
                TextBean.TEXT_BACKGROUND_TYPE_GRAY_BOTTOM -> {
                    holder.setImageResource(R.drawable.icon_edit_font_fonttype_3)
                }
            }
        } else {
            holder.setImageResource(R.drawable.icon_edit_font_fonttype_1)
        }
    }

    private fun showBottomTextFun(show: Boolean) {

        if (show) {
            val listener = View.OnClickListener {
                when (it.id) {
                    mBottomItemTextEditHolder?.getLayoutId() -> {
                        mCustomGestureFrameLayout.doubleClick()
                    }
                    mBottomItemTextColorHolder?.getLayoutId() -> {
                        showBottomTextColor(true)
                    }
                    mBottomItemTextFontHolder?.getLayoutId() -> {
                        showBottomTextFont(true)
                    }
                    mBottomItemTextBackgroundHolder?.getLayoutId() -> {
                        val effectBean =
                            mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                        if (effectBean != null) {
                            val prevType = effectBean.backgroundType
                            val nextType = when (effectBean.backgroundType) {
                                TextBean.TEXT_BACKGROUND_TYPE_NOMAL -> {
                                    TextBean.TEXT_BACKGROUND_TYPE_INVERTED
                                }
                                TextBean.TEXT_BACKGROUND_TYPE_INVERTED -> {
                                    TextBean.TEXT_BACKGROUND_TYPE_GRAY_BOTTOM
                                }
                                TextBean.TEXT_BACKGROUND_TYPE_GRAY_BOTTOM -> {
                                    TextBean.TEXT_BACKGROUND_TYPE_NOMAL
                                }
                                else -> {
                                    TextBean.TEXT_BACKGROUND_TYPE_NOMAL
                                }
                            }
                            effectBean.backgroundType = nextType

                            val multiEffectAction =
                                SingleEffectAction(
                                    effectBean
                                )
                            multiEffectAction.type = ActionType.TYPE_TEXT_BACKGROUND
                            multiEffectAction.prevObj = prevType
                            multiEffectAction.obj = nextType
                            addAction(multiEffectAction)

                            mCustomGestureFrameLayout.changeBeanState(effectBean)
                        }
                        updateTextBackgroundState()
                    }
                    mBottomItemTextOpacityHolder?.getLayoutId() -> {
                        showBottomTextOpacity(true)
                    }
                    mBottomItemTextAlignHolder?.getLayoutId() -> {
                        val effectBean =
                            mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                        if (effectBean != null) {
                            val allGravities = effectBean.allGravities
                            for (index in 0 until allGravities.size) {
                                if (effectBean.gravity == allGravities[index]) {
                                    val newIndex = (index + 1) % allGravities.size

                                    val prevGravity = effectBean.gravity
                                    val currentGravity = allGravities[newIndex]
                                    effectBean.gravity = currentGravity

                                    val multiEffectAction =
                                        SingleEffectAction(
                                            effectBean
                                        )
                                    multiEffectAction.type = ActionType.TYPE_TEXT_ALIGN
                                    multiEffectAction.prevObj = prevGravity
                                    multiEffectAction.obj = currentGravity
                                    addAction(multiEffectAction)

                                    mBottomItemTextAlignHolder?.setImageResource(effectBean.allGravityDrawable[newIndex])
                                    break
                                }
                            }
                            mCustomGestureFrameLayout.changeBeanState(effectBean)
                        }
                    }
                    mBottomItemTextDelHolder?.getLayoutId() -> {
                        val effectBean =
                            mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                        if (effectBean != null) {
                            deleteEffect(effectBean, Runnable {
                                showBottomTextFun(false)
                            })
                        }
                    }
                }
            }
            if (mBottomTabFontFuns.isEmpty()) {
                //返回按钮、文本输入按钮、字体样式按钮、字体颜色按钮、字体透明度按钮、对齐按钮、字体背景按钮
                mBottomItemTextEditHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_edit,
                        R.drawable.icon_edit_font_edit,
                        listener
                    )
                mBottomItemTextFontHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_font,
                        R.drawable.icon_edit_font_fonts,
                        listener
                    )
                mBottomItemTextColorHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_color,
                        R.drawable.icon_edit_font_color,
                        listener
                    )
                mBottomItemTextOpacityHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_opacity,
                        null,
                        listener
                    )
                mBottomItemTextAlignHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_align,
                        R.drawable.icon_edit_font_left,
                        listener
                    )
                mBottomItemTextBackgroundHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_font_background,
                        R.drawable.icon_edit_font_fonttype_1,
                        listener
                    )
                mBottomItemTextDelHolder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        R.string.video_edit_bottom_item_del,
                        R.drawable.icon_edit_del,
                        listener
                    )

                mBottomTabFontFuns.add(mBottomItemTextEditHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextFontHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextColorHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextOpacityHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextAlignHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextBackgroundHolder!!)
                mBottomTabFontFuns.add(mBottomItemTextDelHolder!!)

            }
            // TODO: 获得当前透明度
            val textDrawable = TextDrawable(this)
            textDrawable.text = "$mCurrentTextOpacity"
            textDrawable.setTextColor(
                ResourcesUtils.getColor(
                    R.color.video_edit_bottom_text_opacity_text_color,
                    Color.WHITE
                )
            )
            textDrawable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            mBottomItemTextOpacityHolder?.setImageDrawable(textDrawable)
            updateTextBackgroundState()
            showBottomBack(show)
            updateBottomLayout(mBottomTabFontFuns, Gravity.LEFT)
        } else {
            showBottomLayoutFun()
        }
        mBottomItemTextFunShowing = show
    }

    //
    private fun showBottomTextFont(show: Boolean) {
        if (show) {
            val listener = View.OnClickListener {
                when (it.id) {
                    mBottomItemBackHoler?.getLayoutId() -> {
                        showBottomTextFun(true)
                    }
                    else -> {
                        if (it is FontView) {
                            val localDataBean = it.fontBean as LocalDataBean
                            if (localDataBean.isBuildin) {
                                changeFontState(it, it.fontBean?.assetName ?: "")
                            } else {
                                //线上字体点击事件逻辑
                                if (FontHelper.checkExistTTF(localDataBean)) {
                                    //改变字体
                                    changeFontState(
                                        it,
                                        FontHelper.getTtfFontPath(localDataBean),
                                        true
                                    )
                                } else {
                                    if (localDataBean.downloaded) {
                                        //已下载 -> 解压
                                        if (FontHelper.unzipDownloadFontFile(localDataBean)) {
                                            changeFontState(
                                                it,
                                                FontHelper.getTtfFontPath(localDataBean),
                                                true
                                            )
                                        } else {
                                            ToastUtils.show(getString(R.string.download_error_tips))
                                        }
                                    } else {
                                        val holder = it.tag as BottomTabHolder
                                        val bean = holder.obj as LocalDataBean
                                        val downloadFontCallback =
                                            object : OnlineManager.DownloadResourceCallback {
                                                override fun onConnected() {
                                                    holder.circleProgressBar?.maxProgress = 100
                                                    holder.circleProgressBar?.setProgress(0)
                                                }

                                                override fun onCompleted() {
                                                    holder.circleProgressBarContainer?.visibility =
                                                        View.GONE
                                                    holder.downloadImageView?.visibility = View.GONE
                                                    holder.mask?.visibility = View.GONE
                                                    holder.crown?.visibility = View.GONE
                                                    if (FontHelper.unzipDownloadFontFile(
                                                            localDataBean
                                                        )
                                                    ) {
                                                        changeFontState(
                                                            it,
                                                            FontHelper.getTtfFontPath(localDataBean),
                                                            true
                                                        )
                                                    } else {
                                                        ToastUtils.show(getString(R.string.download_error_tips))
                                                    }
                                                }

                                                override fun onProgress(progress: Int) {
                                                    holder.progressText?.text = progress.toString()
                                                    holder.circleProgressBar?.setProgress(progress)
                                                }

                                                override fun onFailed() {
                                                    ToastUtils.show(getString(R.string.download_error_tips))
                                                    holder.circleProgressBarContainer?.visibility =
                                                        View.GONE
                                                    holder.mask?.visibility = View.GONE
                                                    if (bean.isNeedBuy) {
                                                        holder.downloadImageView?.visibility =
                                                            View.GONE
                                                        holder.crown?.visibility = View.VISIBLE
                                                    } else {
                                                        holder.downloadImageView?.visibility =
                                                            View.VISIBLE
                                                        holder.crown?.visibility = View.GONE
                                                    }
                                                }
                                            }

                                        if (!localDataBean.isNeedBuy || ConfigManager.purchaseSubSize > 0) {
                                            holder.circleProgressBarContainer?.visibility =
                                                View.VISIBLE
                                            holder.mask?.visibility = View.VISIBLE
                                            holder.crown?.visibility = View.GONE
                                            holder.downloadImageView?.visibility = View.GONE
                                            FontHelper.downloadFont(
                                                localDataBean,
                                                downloadFontCallback
                                            )
                                        } else {
                                        }

                                    }
                                }

                            }
                        }
                    }
                }
            }

            val bottomTabTextFonts = arrayListOf<BottomTabHolder>()
            val fontList = FontHelper.getFontList()
            val defaultSelect = 0
            val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
            for (i in fontList.indices) {
                val bean = fontList[i]
                val holder =
                    BottomTabHolder.getHolderInflater(
                        this,
                        R.layout.edit_bottom_item_view,
                        null,
                        null,
                        null
                    )
                holder.textView?.visibility = View.GONE
                holder.imageView?.visibility = View.GONE
                holder.fontView?.visibility = View.VISIBLE
                holder.fontView?.setColor(
                    R.color.video_edit_bottom_text_font_background_normal,
                    R.color.video_edit_bottom_text_font_background_select
                )
                holder.obj = bean
                holder.fontView?.tag = holder
                holder.fontView?.setOnClickListener(listener)

                val sel = if (effectBean != null) {
                    if (bean.isBuildin) {
                        TextUtils.equals(effectBean.fontName, bean.assetName)
                    } else {
                        if (!bean.downloaded) {
                            false
                        } else {
                            TextUtils.equals(
                                effectBean.localFontPath,
                                FontHelper.getTtfFontPath(bean)
                            )
                        }

                    }
                } else defaultSelect == i
                if (sel) {
                    mBottomItemCurrentSelectFont = holder.fontView
                    holder.fontView?.updateData(bean, true)
                } else {
                    holder.fontView?.updateData(bean, false)
                }

                if (!bean.isBuildin && !bean.downloaded) {
                    if (bean.isNeedBuy) {
                        holder.downloadImageView?.visibility = View.GONE
                        holder.crown?.visibility = View.VISIBLE
                    } else {
                        holder.downloadImageView?.visibility = View.VISIBLE
                        holder.crown?.visibility = View.GONE
                    }
                } else {
                    holder.downloadImageView?.visibility = View.GONE
                }

                bottomTabTextFonts.add(holder)
            }

            showBottomBack(show, listener)
            updateBottomLayout(bottomTabTextFonts, Gravity.START)
        } else {
            showBottomTextFun(true)
            mBottomItemCurrentSelectFont = null
        }
    }

    /***
     *
     */
    private fun changeFontState(fontView: FontView, path: String, local: Boolean = false) {
        mBottomItemCurrentSelectFont?.setSel(false)
        fontView.setSel(true)
        mBottomItemCurrentSelectFont = fontView
        val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
        if (effectBean != null) {
            val prevLocalFontPath = effectBean.localFontPath
            val prevFontName = effectBean.fontName
            val currentLocalFontPath: String?
            val currentFontName: String?
            if (local) {
                effectBean.localFontPath = path
                currentLocalFontPath = path
                currentFontName = null
                effectBean.fontName = ""
            } else {
                effectBean.fontName = path
                currentLocalFontPath = null
                //解决点击了下载的字体后，从编辑区选中字体特效，选中内置字体不生效
                effectBean.localFontPath = ""
                currentFontName = path
            }

            val multiEffectAction =
                MultiEffectAction(effectBean.clone())
            multiEffectAction.type = ActionType.TYPE_FONT_CHANGE
            multiEffectAction.obj = arrayListOf(currentLocalFontPath, currentFontName)
            multiEffectAction.prevObj = arrayListOf(prevLocalFontPath, prevFontName)
            addAction(multiEffectAction)


            mCustomGestureFrameLayout.changeBeanState(effectBean)
        }
    }

    private fun showBottomColor(
        colorListener: View.OnClickListener,
        backListener: View.OnClickListener? = null,
        defaultSelectColor: Int? = null
    ): ColorView? {
        val listener = View.OnClickListener {
            when (it.id) {
                mBottomItemBackHoler?.getLayoutId() -> {
                    backListener?.onClick(it)
                }
                else -> {
                    colorListener.onClick(it)
                }
            }
        }
        var currentSelectView: ColorView? = null
        val width = ResourcesUtils.getDimension(R.dimen.video_edit_bottom_text_color_width).toInt()
        val margin =
            ResourcesUtils.getDimension(R.dimen.video_edit_bottom_text_color_margin).toInt()
        val uncheckedRadius = DeviceUtils.dip2px(30f) / 2
        val checkedOuterRadius = DeviceUtils.dip2px(30f) / 2
        val checkedOuterWidth = DeviceUtils.dip2px(2f)
        val checkedInsideRadius = DeviceUtils.dip2px(15f) / 2
        val views = arrayListOf<View>()
        val localColors = App.getContext().resources.getIntArray(R.array.colors)
        val currentSelectColor = defaultSelectColor ?: 0xffffffff.toInt()
        for (color in localColors) {
            val view = ColorView(this)
            views.add(view)
            view.visibility = View.VISIBLE
            view.setRadius(
                uncheckedRadius,
                checkedOuterRadius,
                checkedOuterWidth,
                checkedInsideRadius
            )
            if (color == 0 || color == 0xff000000.toInt()) {
                view.setColor(color, Color.parseColor("#1b1b1b"))
            } else {
                view.setColor(color)
            }
            view.setOnClickListener(listener)
            val lp = LinearLayout.LayoutParams(width, width)
            lp.marginStart = margin
            lp.marginEnd = margin
            view.layoutParams = lp

            if (currentSelectColor == color) {
                mBottomItemTextCurrentSelectColor = view
                view.setChecked(true)
                currentSelectView = view
            } else {
                view.setChecked(false)
            }
        }

        showBottomBack(true, listener)
        updateBottomLayoutByView(views, Gravity.LEFT, itemWidth = width)
        return currentSelectView
    }

    private fun showBottomTextColor(show: Boolean) {
        if (show) {
            val listener = View.OnClickListener {
                when (it.id) {
                    mBottomItemBackHoler?.getLayoutId() -> {
                        showBottomTextFun(true)
                    }
                    else -> {
                        if (it is ColorView) {
                            mBottomItemTextCurrentSelectColor?.setChecked(false)
                            it.setChecked(true)
                            mBottomItemTextCurrentSelectColor = it
                            val color = it.color
                            val effectBean =
                                mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                            if (effectBean != null) {
                                val prevColor = effectBean.textColor
                                val currentColor = color
                                effectBean.textColor = color
                                effectBean.hintColor = color

                                val multiEffectAction =
                                    SingleEffectAction(
                                        effectBean
                                    )
                                multiEffectAction.type = ActionType.TYPE_COLOR
                                multiEffectAction.obj = currentColor
                                multiEffectAction.prevObj = prevColor
                                addAction(multiEffectAction)

                                mCustomGestureFrameLayout.changeBeanState(effectBean)
                            }
                        }
                    }
                }
            }
            val textBean = mCurrentSelectEffectBean as? TextBean
            mBottomItemTextCurrentSelectColor =
                showBottomColor(listener, listener, textBean?.textColor)
        } else {
            showBottomTextFun(true)
            mBottomItemTextCurrentSelectColor = null
        }
    }

    private fun showBottomTextOpacity(show: Boolean) {
        if (show) {
            val listener = object : View.OnClickListener,
                OnSeekBarChangeListener {
                override fun onClick(v: View?) {
                    when (v?.id) {
                        mBottomItemBackHoler?.getLayoutId() -> {
                            showBottomTextOpacity(false)
                        }
                    }
                }

                override fun onProgressChanged(
                    seekBar: CustomNumSeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val text = resources.getString(
                        R.string.video_edit_bottom_item_font_opacity_text,
                        "$progress"
                    )
                    mCurrentTextOpacity = progress
                    mBottomItemTextOpacityTextView?.text = text

                    val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                    if (effectBean != null) {
                        effectBean.alpha = progress
                        mCustomGestureFrameLayout.changeBeanState(effectBean)
                    }
                }

                private var prevAlpha = 100
                override fun onStartTrackingTouch(seekBar: CustomNumSeekBar?) {
                    val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                    if (effectBean != null) {
                        prevAlpha = effectBean.alpha
                    }
                }

                override fun onStopTrackingTouch(seekBar: CustomNumSeekBar?) {
                    val effectBean = mCustomGestureFrameLayout.getCurrentEditorBean() as? TextBean
                    if (effectBean != null) {
                        val multiEffectAction =
                            SingleEffectAction(
                                effectBean
                            )
                        multiEffectAction.type = ActionType.TYPE_OPACITY
                        multiEffectAction.obj = effectBean.alpha
                        multiEffectAction.prevObj = prevAlpha
                        addAction(multiEffectAction)
                    }
                }

            }
            if (mBottomItemTextOpacityLayout == null) {
                val viewGroup =
                    layoutInflater.inflate(R.layout.layout_text_opacity, null) as ViewGroup
                mBottomItemTextOpacitySeekBar = viewGroup.findViewById(R.id.seekbar)
                mBottomItemTextOpacitySeekBar?.setOnSeekBarChangeListener(listener)
                mBottomItemTextOpacityTextView = viewGroup.findViewById(R.id.textview)
                mBottomItemTextOpacityLayout = viewGroup
                val margin =
                    ResourcesUtils.getDimension(R.dimen.video_edit_bottom_text_font_opacity_margin)
                val width = DeviceUtils.SCREEN_WIDTH_PX - margin * 2 - mBottomFunBackWidth
                val lp =
                    LinearLayout.LayoutParams(width.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                viewGroup.layoutParams = lp
            }
            val textBean = mCurrentSelectEffectBean as? TextBean
            mBottomItemTextOpacitySeekBar?.progress = textBean?.alpha ?: 100


            showBottomBack(show, listener)
            updateBottomLayoutByView(arrayListOf(mBottomItemTextOpacityLayout!!), Gravity.CENTER)
            mBottomHorizontalScrollView.setScrollable(false)
        } else {
            showBottomTextFun(true)
        }
    }

    private var mShowStickerFun = false
    private fun showBottomStickerFun(show: Boolean) {
        if (show) {
            mShowStickerFun = true
            val listener = View.OnClickListener {
                val holder = it.tag as BottomTabHolder
                if (holder == mBottomItemBackHoler) {
                    mShowStickerFun = false
                    showBottomStickerFun(false)
                    return@OnClickListener
                }
                showBottomStickerSecond(true, holder.obj as String)
                updateStickerSelectState(holder)
            }
            if (mBottomTabStickerFuns.isEmpty()) {
                //内置
                for (packageName in StickerManger.insideStickerPackageNames) {
                    val stickerInsideResource = StickerManger.getInsideSticker(packageName)
                        ?: continue
                    val holder =
                        BottomTabHolder.getHolderInflater(
                            this,
                            R.layout.edit_bottom_item_view,
                            null,
                            null,
                            listener
                        )
                    holder.textView?.visibility = View.GONE
                    if (stickerInsideResource.iconResId != 0) {
                        holder.setImageResource(stickerInsideResource.iconResId)
                    }
                    holder.obj = packageName
                    mBottomTabStickerFuns.add(holder)
                    mLocalStickerCount++
                }

                //apk包资源
                val downloadedBottomHolders = arrayListOf<BottomTabHolder>()
                val unDownloadBottomHolders = arrayListOf<BottomTabHolder>()
                val localStickerDatas = StickerManger.getLocalStickerDatas()
                for (it in localStickerDatas.iterator()) {
                    val pkg = it.key
                    val localStickerData = it.value
                    if (localStickerData.downloaded) {
                        //已下载
                        val unInstallStickerResource =
                            StickerManger.getUnInstallSticker(localStickerData)
                        val holder =
                            BottomTabHolder.getHolderInflater(
                                this,
                                R.layout.edit_bottom_item_view,
                                null,
                                null,
                                listener
                            )
                        holder.textView?.visibility = View.GONE
                        val iconId = unInstallStickerResource?.iconId
                        if (iconId != null && iconId != 0) {
                            val bitmap = BitmapFactory.decodeResource(
                                unInstallStickerResource.resource,
                                iconId
                            )
                            holder.setImageBitmap(bitmap)
                        }
                        holder.obj = pkg
                        downloadedBottomHolders.add(holder)
                        mLocalStickerCount++
                    } else {
                        // 未下载
                        val holder =
                            BottomTabHolder.getHolderInflater(
                                this,
                                R.layout.edit_bottom_item_view,
                                null,
                                null,
                                null
                            )
                        val downloadStickerCallback =
                            object : OnlineManager.DownloadResourceCallback {
                                override fun onConnected() {
                                    holder.circleProgressBar?.maxProgress = 100
                                    holder.circleProgressBar?.setProgress(0)
                                }

                                override fun onCompleted() {
                                    //局部刷新
                                    StickerManger.addUnInstallStickerRes(localStickerData)
                                    holder.circleProgressBarContainer?.visibility = View.GONE
                                    holder.downloadImageView?.visibility = View.GONE
                                    holder.mask?.visibility = View.GONE
                                    holder.crown?.visibility = View.GONE
                                    holder.obj = pkg
                                    holder.setOnClickListener(listener)

                                    //交换，最新下载的放在已下载的最后面
                                    val from = mBottomTabStickerFuns.indexOf(holder)
                                    val to = mLocalStickerCount
                                    Collections.swap(mBottomTabStickerFuns, from, to)
                                    mLocalStickerCount++

                                    //如果当前的功能区是展示Sticker，则刷新
                                    if (mShowStickerFun) {
                                        showBottomStickerSecond(true, pkg)
                                        updateStickerSelectState(holder)
                                        updateBottomLayout(mBottomTabStickerFuns, Gravity.START)
                                    }

                                    ToastUtils.show("${localStickerData.name} ${getString(R.string.download_finish_tips)}")

                                }

                                override fun onProgress(progress: Int) {
                                    holder.progressText?.text = progress.toString()
                                    holder.circleProgressBar?.setProgress(progress)
                                }

                                override fun onFailed() {
                                    if (localStickerData.isNeedBuy) {
                                        holder.crown?.visibility = View.VISIBLE
                                    } else {
                                        holder.downloadImageView?.visibility = View.VISIBLE
                                    }
                                    holder.circleProgressBarContainer?.visibility = View.GONE
                                    holder.mask?.visibility = View.GONE
                                    holder.crown?.visibility = View.VISIBLE

                                    ToastUtils.show(getString(R.string.download_error_tips))
                                }

                            }
                        val clickListener = View.OnClickListener {
                            if (!localStickerData.isNeedBuy || ConfigManager.purchaseSubSize > 0) {
                                holder.circleProgressBarContainer?.visibility = View.VISIBLE
                                holder.mask?.visibility = View.VISIBLE
                                holder.crown?.visibility = View.GONE
                                holder.downloadImageView?.visibility = View.GONE
                                StickerManger.downloadSticker(
                                    localStickerData,
                                    downloadStickerCallback
                                )
                            } else {
                            }
                        }
                        holder.setOnClickListener(clickListener)
                        holder.textView?.visibility = View.GONE

                        if (localStickerData.isNeedBuy) {
                            holder.crown?.visibility = View.VISIBLE
                            holder.downloadImageView?.visibility = View.GONE
                        } else {
                            holder.crown?.visibility = View.GONE
                            holder.downloadImageView?.visibility = View.VISIBLE
                        }

                        ImageLoader.loadImage(
                            localStickerData.getDefaultSmallImgUrl(),
                            R.drawable.icon_album_default,
                            holder.imageView
                        )
                        unDownloadBottomHolders.add(holder)
                    }
                }

                mBottomTabStickerFuns.addAll(downloadedBottomHolders)
                mBottomTabStickerFuns.addAll(unDownloadBottomHolders)
            }

            showBottomBack(show, listener)
            updateBottomLayout(mBottomTabStickerFuns, Gravity.START)
            showBottomStickerSecond(true, StickerManger.insideStickerPackageNames[0])
            updateStickerSelectState(mBottomTabStickerFuns[0])
        } else {
            showBottomLayoutFun()
            showBottomStickerSecond(false)
            updateStickerSelectState(reset = true)
            mShowStickerFun = false
        }
    }

    private fun showBottomStickerSecond(show: Boolean, packageName: String? = null) {
        mCurrentStickerPackageName = if (show) {
            if (mCurrentStickerPackageName === packageName)
                return
            packageName
        } else {
            null
        }
        mBottomFunSecondLayout.removeAllViews()
        showBottomSecondLayout(show)
        if (show) {
            val listener = View.OnClickListener {
                val obj = it.tag
                pickSticker(obj)
            }
            val packageName = packageName ?: return
            //内置
            val sir = StickerManger.getInsideSticker(packageName)
            if (sir != null && sir.isAssertRes) {
                val width: Int
                val marginTopBottom: Int
                var stickerSize = 0
                if (sir.packageName == InsideStickerTool.STICKER_PKG_NAME_EMOJI) {
                    //emoji
                    width =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_width_emoji)
                            .toInt()
                    marginTopBottom =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_margin_top_emoji)
                            .toInt()
                    stickerSize =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_sticker_emoji_size)
                            .toInt()
                } else {
                    //other sitcker
                    width =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_width_other)
                            .toInt()
                    marginTopBottom =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_margin_top_other)
                            .toInt()
                    stickerSize =
                        ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_sticker_other_size)
                            .toInt()
                }

                sir.paths?.map {
                    val bitmap =
                        AssetsUtil.toScaleBitmap(it, stickerSize, stickerSize) ?: return@map
                    addImageViewToBottomFunSecondLayout(
                        bitmap,
                        it,
                        listener,
                        width,
                        marginTopBottom
                    )
                }
            }

            //apk资源
            val unInstallStickerResource = StickerManger.getUnInstallSticker(packageName)
            if (unInstallStickerResource != null && unInstallStickerResource.exist) {
                val width =
                    ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_width_other)
                        .toInt()
                val marginTopBottom =
                    ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_margin_top_other)
                        .toInt()
                val stickerSize =
                    ResourcesUtils.getDimension(R.dimen.video_edit_bottom_sticker_second_item_sticker_other_size)
                        .toInt()
                if (unInstallStickerResource.resource != null) {
                    val pkg = unInstallStickerResource.packageName
                    for (resId in unInstallStickerResource.resIds) {
                        val bitmap = AssetsUtil.toScaleBitmap(
                            unInstallStickerResource.resource!!,
                            pkg,
                            resId,
                            stickerSize,
                            stickerSize
                        ) ?: return
                        addImageViewToBottomFunSecondLayout(
                            bitmap,
                            bitmap,
                            listener,
                            width,
                            marginTopBottom
                        )
                    }
                }
            }
        }
    }


    private fun updateStickerSelectState(holder: BottomTabHolder? = null, reset: Boolean = false) {
        if (holder != null) {
            mBottomTabStickerLastSelectHolder?.layout?.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.black,
                    null
                )
            )
            holder.layout?.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.video_edit_bottom_selected_background_color,
                    null
                )
            )
            mBottomTabStickerLastSelectHolder = holder
        }

        if (reset) {
            mBottomTabStickerLastSelectHolder?.layout?.setBackgroundColor(
                ResourcesCompat.getColor(
                    resources,
                    R.color.black,
                    null
                )
            )
        }
    }

    private fun addImageViewToBottomFunSecondLayout(
        bitmap: Bitmap,
        tag: Any?,
        listener: View.OnClickListener,
        width: Int,
        marginTopOrBottom: Int
    ) {
        val imageView = ImageView(this@VideoEditorActivity)
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageBitmap(bitmap)
        imageView.tag = tag
        imageView.setOnClickListener(listener)
        val lp = LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT)
        lp.topMargin = marginTopOrBottom
        lp.bottomMargin = marginTopOrBottom
        mBottomFunSecondLayout.addView(imageView, lp)
    }

    override fun onResume() {
        super.onResume()
        if (initFinished) {
            mVideoEditorManager.play(
                mVideoEditorManager.getCurrentTimelineIndex(),
                mCustomGestureFrameLayout,
                false
            )
        }
    }

    override fun onPause() {
        super.onPause()
        if (mVideoEditorManager.isPlaying()) {
            mVideoEditorManager.pause(mCustomGestureFrameLayout)
        }

        bottomMusicInfoPlayOrPause(false, mCurrentSelectedSoundBean)
    }

    private fun showBottomTrimFun(show: Boolean) {
        if (show) {
            val listener = View.OnClickListener {
                val holder = it.tag as BottomTabHolder
                when (holder) {
                    mBottomItemBackHoler -> {
                        showBottomTrimFun(false)
                        return@OnClickListener
                    }
                    mBottomItemFun1Holer -> {
                        // del
                        mBottomItemTrimCurrentSelectBean?.let { bean ->
                            deleteEffect(bean, Runnable {
                                showBottomTrimFun(true)
                            })
                        }
                    }
                    mBottomItemFun2Holer -> {
                        // trim
                        if (mBottomItemTrimCurrentSelectBean != null) {
                            showBottomTrimSingleTrim(true, mBottomItemTrimCurrentSelectBean)
                        }
                    }
                    mBottomItemTrimAdd -> {
                        // add
                    }
                    else -> {
                    }
                }
            }
            showBottomRightFun2(
                show,
                R.string.video_edit_bottom_item_trim,
                R.drawable.icon_edit_trim,
                listener
            )
            showBottomRightFun1(
                show,
                R.string.video_edit_bottom_item_del,
                R.drawable.icon_edit_del,
                listener
            )

            showBottomBack(show, listener)
            updateBottomLayout(ArrayList(), Gravity.START)
            showBottomTrimSecond(true, listener)
        } else {
            showBottomRightFun1(false)
            showBottomRightFun2(false)
            showBottomTrimSecond(false)
            showBottomLayoutFun()
        }
    }

    private fun showBottomTrimSecond(show: Boolean, listener: View.OnClickListener? = null) {
        if (mBottomItemTrimSecondShowing == show) {
            return
        }
        mBottomFunSecondLayout.removeAllViews()
        showBottomSecondLayout(show)

        if (show) {
            val primaryBeans = mVideoEditorManager.primaryBeans.beans.filter {
                it.state != EffectBean.STATE_DELETE && it.primary
            }
            val layout = mBottomItemTrimEffectListView.let {
                if (it == null) {
                    val view = EffectListView(this)
                    mBottomItemTrimEffectListView = view
                    view
                } else it
            }
            layout.onItemListener = object : EffectListView.OnItemListener {
                override fun onClick(effectBean: EffectBean) {
                    mBottomItemTrimCurrentSelectBean = effectBean
                    onSelectSecondaryEffect(effectBean)
                }

                override fun onAdd() {
                    requestAddEffect()
                }

                override fun onItemRangeMoved(beans: List<EffectBean>, from: Int, to: Int) {
                    changeEffectPosition(beans)
                }
            }
            layout.setData(primaryBeans)
            val temp = mBottomItemTrimCurrentSelectBean
            if (mCurrentSelectEffectBean == null && temp != null) {
                onSelectSecondaryEffect(temp)
            } else {
                layout.currentSelectEffectBean = mCurrentSelectEffectBean
                mBottomItemTrimCurrentSelectBean = mCurrentSelectEffectBean
                val lp = LinearLayout.LayoutParams(
                    DeviceUtils.SCREEN_WIDTH_PX,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                mBottomFunSecondLayout.addView(layout, lp)
            }
            mBottomFunSecondScrollView.setScrollable(false)
        }
        mBottomItemTrimSecondShowing = show
    }

    private fun showBottomTrimSingleTrim(show: Boolean, effectBean: EffectBean? = null) {
        if (show) {
            val listener = object : View.OnClickListener, MediaTrimView.OnTimeChangeLintener {
                override fun onClick(v: View?) {
                    val holder = v?.tag as? BottomTabHolder
                    when (holder) {
                        mBottomItemBackHoler -> {
                            showBottomTrimSingleTrim(false)
                        }
                        else -> {

                        }
                    }
                }

                override fun timeStart(start: Boolean, end: Boolean) {
                    mBottomHorizontalScrollView.setScrollable(false)
                }

                override fun timeMove(start: Boolean, end: Boolean, offsetTime: Long) {
                }

                override fun timeEnd(start: Boolean, end: Boolean, startTime: Long, endTime: Long) {
                    mBottomHorizontalScrollView.setScrollable(true)
                    if (effectBean != null && (start || end)) {
                        trimEffectInSelf(effectBean, startTime, endTime)
                    }
                }
            }
            showBottomTrimFun(false)
            showBottomBack(show, listener)

            if (mBottomItemTrimCurrentSelectBean != null) {
                val layout = mBottomItemTrimView.let {
                    if (it == null) {
                        val trimView = MediaTrimView(this)
                        mBottomItemTrimView = trimView
                        trimView
                    } else it
                }
                layout.arrowColor = ResourcesUtils.getColor(R.color.white)
                layout.onTimeChangeLintener = listener
                layout.effectBean = effectBean
                updateBottomLayoutByView(arrayListOf(layout), Gravity.START)
            }

        } else {
            mBottomItemTrimView?.clear()
            showBottomTrimFun(true)
        }
        mBottomItemTrimSingleTrimShowing = show
    }

    private fun createEffectBean(thumbnailBean: ThumbnailBean, offset: Long): EffectBean? {
        return when (thumbnailBean.type) {
            MediaTypeUtil.TYPE_OTHER_IMAGE, MediaTypeUtil.TYPE_JPG, MediaTypeUtil.TYPE_PNG -> {
                val bean = ImageBean.getBean(thumbnailBean, offset)
                bean
            }
            MediaTypeUtil.TYPE_VIDEO -> {
                val bean = VideoBean.getBean(thumbnailBean, offset)
                bean
            }
            else -> {
                null
            }
        }
    }

    private fun initVideoData(async: Boolean = true) {
        //        val beans = intent?.getParcelableArrayListExtra<ThumbnailBean>(RESPONSE_DATA)
        val key = intent?.getStringExtra(RESPONSE_DATA_TEMP_STORAGE_KEY) ?: return

        if (async) {
            object : AsyncTask<Void, Void, Unit>() {
                override fun doInBackground(vararg params: Void) {
                    initVideoData(false)
                }

                override fun onPostExecute(result: Unit?) {
                }
            }.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
            return
        }

        val beans = DataTemporaryStorageHelper.get(key) as? ArrayList<ThumbnailBean>
        var offset = 0L
        val effectBeans = beans?.mapNotNull {
            val bean = createEffectBean(it, offset)
            if (bean != null) {
                offset += bean.dstDuration + 1
            }
            bean
        }
        val effectList = EffectListBean()
        if (effectBeans?.isNotEmpty() == true) {
            effectList.addAll(effectBeans)
        }
        mVideoEditorManager.setEffectListBean(effectList)

        val initViewRunnable = Runnable {
            mCustomGestureFrameLayout.setEffectListBean(
                mVideoEditorManager.primaryBeans,
                mVideoEditorManager.secondaryBeans
            )
            mEffectEditLayout.setData(
                mVideoEditorManager.primaryBeans,
                mVideoEditorManager.secondaryBeans
            )
        }

        if (isMainThread) {
            initViewRunnable.run()
        } else {
            post(initViewRunnable)
        }
    }

    private fun addEffectBean(thumbnailBean: ThumbnailBean) {
        val bean = createEffectBean(thumbnailBean, 0L) ?: return
        mVideoEditorManager.addPrimaryBeans(bean)
        updateState()
    }

    /**
     * 添加音乐
     */
    private fun pickMusic(songInfo: SongInfo?, replaceId: Int? = null) {
        if (songInfo == null) {
            return
        }
        val timelineIndex = mVideoEditorManager.getCurrentTimelineIndex()
        if (replaceId == null || replaceId == -1) {
            songInfo.let {
                val soundBean = SoundBean.getBean(it, timelineIndex)
                mBottomItemMusicNeedReplaceBean = soundBean
                mVideoEditorManager.addSecondaryBeans(soundBean)
                mEffectEditLayout.add(soundBean, timelineIndex)
                mEffectEditLayout.selectEffectBean(soundBean)
                showBottomMusicFun(true, soundBean)
                null
            }
        } else {
            val bean = mVideoEditorManager.getBean(replaceId) as? SoundBean
            if (bean != null) {
                val prevSongInfo = bean.soundInfo?.clone()
                bean.replace(songInfo)

                mBottomItemMusicNeedReplaceBean = bean
                showBottomMusicFun(true, bean)
                bottomMusicInfoPlayOrPause(false, bean)

                val singleEffectAction =
                    SingleEffectAction(bean)
                singleEffectAction.type = ActionType.TYPE_REPLACE
                singleEffectAction.prevObj = prevSongInfo
                singleEffectAction.obj = songInfo
                addAction(singleEffectAction)
            }
        }
    }

    /**
     * 添加贴纸
     */
    private fun pickSticker(obj: Any) {
        val timelineIndex = mVideoEditorManager.getCurrentTimelineIndex()
        val stickerBean = StickerBean.getBean(obj, timelineIndex)
        mVideoEditorManager.addSecondaryBeans(stickerBean)
        mCustomGestureFrameLayout.addSecondaryEffect(stickerBean)
        mCustomGestureFrameLayout.selectEffect(stickerBean)
        mEffectEditLayout.add(stickerBean, timelineIndex)
        mEffectEditLayout.selectEffectBean(stickerBean)
    }

    /**
     * 添加文本
     */
    private fun addEffectTextBean() {
        mVideoEditorManager.pause(mCustomGestureFrameLayout)
        val timelineIndex = mVideoEditorManager.getCurrentTimelineIndex()
        val textBean = TextBean.getBean("", timelineIndex)
        mVideoEditorManager.addSecondaryBeans(textBean)
        mCustomGestureFrameLayout.addSecondaryEffect(textBean)
        mCustomGestureFrameLayout.selectEffect(textBean)
        mEffectEditLayout.add(textBean, timelineIndex)
        mEffectEditLayout.selectEffectBean(textBean)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PRIVATE) {
            if (resultCode == RESULT_OK && data != null) {
//                val extras = data.extras
//                if (extras != null) {
//                    val beans = extras.get(RESPONSE_DATA) as? ArrayList<ThumbnailBean>
//
//                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_MUSIC) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                var songInfo =
                    data.getParcelableExtra(LocalMusicActivity.INTENT_KEY_RESPONSE_DATA) as? SongInfo
                val path = if (songInfo == null) {
                    val uri = data.data
                    uri.toString()
                } else {
                    songInfo.path
                }
                if (songInfo == null) {
                    songInfo = SongInfo.createFromPath(this, path)
                }
                val replaceId = data.getBundleExtra(LocalMusicActivity.INTENT_KEY_EXTRA_INFO)
                    ?.getInt(INTENT_KEY_REPLACE_EFFECT_ID, -1)
                    ?: -1
                pickMusic(songInfo, replaceId)
            }
        } else if (requestCode == REQUEST_CODE_PICK_ALBUM) {
            val beans = data?.getParcelableArrayListExtra<ThumbnailBean>(
                AlbumActivity.INTENT_KEY_PICKED_DATA
            )
            val videoList = beans?.filter {
                it.type == MediaTypeUtil.TYPE_VIDEO
            }
            if (videoList?.size ?: 0 > 0) {
                showLoadingAnim(true)
                initFinished = false
            }
            beans?.mapNotNull {
                addEffectBean(it)
                if (mBottomItemTrimSecondShowing) {
                    showBottomTrimFun(true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mVideoEditorManager.destroy()
        mSavingVideoView?.destroy()
    }

    override fun finish() {
        val runnable = Runnable {
            bottomMusicInfoPlayOrPause(false, mCurrentSelectedSoundBean)
            super.finish()
        }
        if (mVideoEditorManager.canActionRevert() || mVideoEditorManager.canActionRestore()) {
            val okListener = {
                runnable.run()
                Unit
            }
            DialogUtils.show(
                this, R.string.video_edit_dialog_exit_editor_tips,
                R.string.video_edit_dialog_exit_editor_desc,
                R.string.video_edit_dialog_exit_editor_cancel,
                R.string.video_edit_dialog_exit_editor_discard,
                okListener = okListener
            )
        } else {
            runnable.run()
        }
    }

    override fun onTimelineStart() {
        mVideoEditorManager.onTimelineStart()
    }

    override fun onTimelineOffset(timeOffset: Long) {
        mVideoEditorManager.onTimelineOffset(timeOffset)
    }

    override fun onTimelinePause() {
        mVideoEditorManager.onTimelinePause()
    }

    override fun onActionStateChange() {
        updateState()
        updateRatioState()
    }

    override fun applyAction(action: Action?) {
        if (action != null) {
            when (action) {
                is SingleEffectAction -> {
                    when (action.type) {
                        ActionType.TYPE_REPLACE -> {
                            val effectBean = action.effectBean
                            if (effectBean != null) {
                                val soundBean =
                                    mVideoEditorManager.getBean(effectBean.id) as? SoundBean
                                if (soundBean != null) {
                                    val dstInfo = action.currentObj as? SongInfo
                                    if (dstInfo != null) {
                                        soundBean.replace(dstInfo)

                                        if (mBottomItemMusicFunShowing) {
                                            showBottomMusicFun(true, soundBean)
                                            bottomMusicInfoPlayOrPause(false, soundBean)
                                        }
                                    }
                                }
                            }
                        }
                        ActionType.TYPE_TEXT_BACKGROUND -> {
                            val effectBean = action.effectBean as? TextBean
                            if (effectBean != null) {
                                effectBean.backgroundType = action.currentObj as Int
                                if (mBottomItemTextFunShowing) {
                                    updateTextBackgroundState()
                                }
                            }
                        }
                        ActionType.TYPE_COLOR -> {
                            val textBean = action.effectBean as? TextBean
                            if (textBean != null) {
                                textBean.textColor = action.currentObj as Int
                                textBean.hintColor = action.currentObj as Int
                            }
                        }
                        ActionType.TYPE_TEXT_ALIGN -> {
                            val textBean = action.effectBean as? TextBean
                            if (textBean != null) {
                                textBean.gravity = action.currentObj as Int
                            }
                        }
                        ActionType.TYPE_OPACITY -> {
                            val textBean = action.effectBean as? TextBean
                            if (textBean != null) {
                                textBean.alpha = action.currentObj as Int
                            }
                        }
                    }
                }
                is MultiEffectAction -> {
                    when (action.type) {
                        ActionType.TYPE_VOLUME_CHANGE -> {
                            val soundBean = action.effectBean as? SoundBean
                            val originalVolume = action.currentObj?.get(0) as? Int
                            val soundTrackVolume = action.currentObj?.get(1) as? Int
                            if (originalVolume != null) {
                                mCurrentOriginalVolume = originalVolume
                                mVideoEditorManager.volume = originalVolume
                            }
                            if (soundTrackVolume != null) {
                                mCurrentSoundtrackVolume = soundTrackVolume
                                soundBean?.volume = soundTrackVolume
                            }
                            if (mBottomItemMusicVolumeShowing) {
                                showBottomMusicFun(true, soundBean)
                            }
                        }
                    }
                }
            }
            mCustomGestureFrameLayout.applyAction(action)

            if (mBottomItemTrimSingleTrimShowing) {
                showBottomTrimSingleTrim(true, mBottomItemTrimCurrentSelectBean)
            }
            if (mBottomItemTrimSecondShowing) {
                showBottomTrimFun(true)
            }

        }
    }

    override fun onVideoPrepared() {
        initFinished = true
        mVideoEditorManager.play(
            mVideoEditorManager.getCurrentTimelineIndex(),
            mCustomGestureFrameLayout,
            false
        )
        showLoadingAnim(false)
    }

    override fun addAction(action: Action) {
        mVideoEditorManager.action(action)
    }

    private fun selectEffect(effectBean: EffectBean?) {
        mCurrentSelectEffectBean = effectBean

        showBottomTrimFun(false)
        showBottomTextFun(false)
        showBottomMusicFun(false, null)
        showBottomStickerFun(false)


        showBottomTrimFun(effectBean?.primary == true)
        when (effectBean) {
            is VideoBean -> {
            }
            is ImageBean -> {
            }
            is TextBean -> {
                showBottomTextFun(true)
            }
            is SoundBean -> {
                showBottomMusicFun(true, effectBean)
            }
        }
    }

    private fun updateState() {
        mVideoEditorManager.update()
        mEffectEditLayout.updateState()
        mCustomGestureFrameLayout.updateState()
        if (initFinished) {
            mVideoEditorManager.play(
                mVideoEditorManager.getCurrentTimelineIndex(),
                mCustomGestureFrameLayout,
                false
            )
        }


        if (mVideoEditorManager.isPlaying()) {
            playFrameStart()
        } else {
            playFramePause()
        }
        updateRevertAndRestoreState()
    }

    /**
     * 选择主特效
     */
    override fun onSelectPrimaryEffect(effectBean: EffectBean) {
        selectEffect(effectBean)
        mEffectEditLayout.selectEffectBean(effectBean)
        mCustomGestureFrameLayout.selectEffect(effectBean)
    }

    /**
     * 选择副特效
     */
    override fun onSelectSecondaryEffect(effectBean: EffectBean) {
        selectEffect(effectBean)
        mEffectEditLayout.selectEffectBean(effectBean)
        mCustomGestureFrameLayout.selectEffect(effectBean)
    }

    override fun onNoSelect() {
        selectEffect(null)
        mEffectEditLayout.selectEffectBean(null)
        mCustomGestureFrameLayout.selectEffect(null)
    }

    override fun onDragTimeLineViewEndUp() {
        mVideoEditorManager.play(
            mVideoEditorManager.getCurrentTimelineIndex(),
            mCustomGestureFrameLayout,
            false
        )
    }

    /**
     * 裁剪特效在时间轴上的时间
     */
    override fun trimEffectInTimeLine(effectBean: EffectBean, startTime: Long, endTime: Long?) {
        mVideoEditorManager.moveDstByBean(effectBean, startTime, endTime)
        updateState()
    }

    override fun trimSecondEffectInTimeLine() {
        updateState()
    }

    /**
     * 裁剪特效自身的时间
     */
    override fun trimEffectInSelf(effectBean: EffectBean, startTime: Long, endTime: Long) {
        mVideoEditorManager.cropSrcTimeByBean(effectBean, startTime, endTime)

        updateState()
    }

    override fun addEffect(effectBean: EffectBean, primary: Boolean) {
        mVideoEditorManager.addBeans(effectBean, primary)
    }

    override fun requestAddEffect() {
        AlbumActivity.startActivity(
            this@VideoEditorActivity,
            AlbumActivity.KEY_PICK,
            mVideoEditorManager.getVideoCount(),
            REQUEST_CODE_PICK_ALBUM
        )
    }

    /**
     * 删除特效
     */
    override fun deleteEffect(effectBean: EffectBean) {
        mVideoEditorManager.remove(effectBean)
        mCustomGestureFrameLayout.removeEffectView(effectBean)
        if (mBottomItemTrimSecondShowing) {
            showBottomTrimFun(true)
        }
        updateState()
    }

    private fun deleteEffect(effectBean: EffectBean, runnable: Runnable? = null) {
        val okListener = {
            deleteEffect(effectBean)
            runnable?.run()
            Unit
        }
        DialogUtils.show(
            this, R.string.video_edit_dialog_del_effect_desc,
            -1,
            R.string.video_edit_dialog_btn_cancel,
            R.string.video_edit_dialog_btn_yes,
            okListener = okListener
        )
    }

    override fun changeEffectPosition(beans: List<EffectBean>) {
        val ids = beans.map {
            it.id
        }
        val swap = mVideoEditorManager.swapBeans(ids)
        if (swap) {
            updateState()
        }
    }

    private fun updateRevertAndRestoreState() {
        if (mVideoEditorManager.canActionRevert()) {
//            mBtnRevert.visibility = View.VISIBLE
            mBtnRevert.alpha =
                RESTORE_OR_REVERT_ALPHA_CAN
        } else {
//            mBtnRevert.visibility = View.INVISIBLE
            mBtnRevert.alpha =
                RESTORE_OR_REVERT_ALPHA_CAN_NOT
        }
        if (mVideoEditorManager.canActionRestore()) {
//            mBtnRestore.visibility = View.VISIBLE
            mBtnRestore.alpha =
                RESTORE_OR_REVERT_ALPHA_CAN
        } else {
//            mBtnRestore.visibility = View.INVISIBLE
            mBtnRestore.alpha =
                RESTORE_OR_REVERT_ALPHA_CAN_NOT
        }
    }

    private fun changeFullScreenMode() {
        mFullScreenMode = !mFullScreenMode
        if (mFullScreenMode) {
            mCustomGestureFrameLayout.prohibitEditing = true
            mCustomGestureFrameLayout.selectEffect(null)
            mEffectEditLayout.selectEffectBean(null)

            mFunctionContainer.visibility = View.GONE
            mBtnFullScreen.setImageResource(R.drawable.icon_edit_contract)
            mRevertAndRestoreLayout.visibility = View.GONE
            mTitlePanel.visibility = View.GONE
        } else {
            mCustomGestureFrameLayout.prohibitEditing = false
            mFunctionContainer.visibility = View.VISIBLE
            mBtnFullScreen.setImageResource(R.drawable.icon_edit_pusher)
            mRevertAndRestoreLayout.visibility = View.VISIBLE
            mTitlePanel.visibility = View.VISIBLE
        }
    }

    override fun onSaveClick(quality: SaveDialog.Quality) {
//        val needShowRateGuide = RateGuide.Builder().addAction(1).checkShow(this)
//        if (!needShowRateGuide) {
//            loadSaveAd()
//        }
        val dstFile = FolderHelper.getOutputMediaFile(
            this.applicationContext,
            FolderHelper.MEDIA_TYPE_DYNAMIC,
            false
        )
        object : AsyncTask<Void, Void, CommandHelper.Result?>() {
            override fun onPreExecute() {
                super.onPreExecute()
                showSavingAnim(true)
            }

            override fun doInBackground(vararg params: Void?): CommandHelper.Result? {
                val result = VideoMaker.mergeVideo2(
                    mVideoEditorManager.primaryBeans,
                    mVideoEditorManager.secondaryBeans,
                    mCustomGestureFrameLayout,
                    dstFile.absolutePath,
                    quality
                )
                return result
            }

            override fun onPostExecute(result: CommandHelper.Result?) {
                super.onPostExecute(result)
                var toSharePage = false
                val snapshot = when (result?.result) {
                    CommandHelper.RESULT_ID_SUCCESS -> {
                        var bitmap = mVideoEditorManager.snapshot(100, mCustomGestureFrameLayout)
                        bitmap = if (bitmap == null) {
                            MediaThumbnailUtil.createPreViewVideoThumbnail(
                                dstFile.absolutePath,
                                MediaStore.Images.Thumbnails.MINI_KIND
                            )
                        } else {
                            MediaThumbnailUtil.extractThumbnail(
                                bitmap,
                                mCustomGestureFrameLayout.videoRect.width(),
                                mCustomGestureFrameLayout.videoRect.height(),
                                0
                            )
                        }
                        showSavingAnim(false)
                        toSharePage = true
                        bitmap
                    }
                    CommandHelper.RESULT_ID_CANCEL -> {
                        null
                    }
                    else -> {
                        showSavingAnim(false)
                        null
                    }
                }

                val runnable = if (toSharePage) {
                    Runnable {
                        //解决保存视频后无法搜索到视频
                        //发送广播通知系统刷新数据库
                        val path = dstFile.absolutePath
                        sendBroadcast(
                            Intent(
                                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(File(path))
                            )
                        )
                        ShareActivity.startActivity(
                            this@VideoEditorActivity,
                            snapshot,
                            path,
                            forceRateGuide = false
                        )
                    }
                } else null

                runnable?.run()

//                if (needShowRateGuide || !showSaveAd(runnable)) {
//                    runnable?.run()
//                }
            }
        }.executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            mBottomItemBackHoler?.getLayoutId() -> {
                // TODO: 这里可能需要判断上级目录是谁
                showBottomLayoutFun()
            }
            mBottomItemMusicHolder?.getLayoutId() -> {
                val selectBean = mBottomItemMusicNeedReplaceBean?.also {
                    mCustomGestureFrameLayout.selectEffect(it)
                    mEffectEditLayout.selectEffectBean(it)
                }
                showBottomMusicList(true, selectBean != null, selectBean)
            }
            mBottomItemFormatHolder?.getLayoutId() -> {
                showBottomSizeFun(true)
            }
            mBottomItemStickerHolder?.getLayoutId() -> {
                showBottomStickerFun(true)
            }
            mBottomItemTextHolder?.getLayoutId() -> {
                addEffectTextBean()
                showBottomTextFun(true)
            }
            mBottomItemTrimHolder?.getLayoutId() -> {
                showBottomTrimFun(true)
            }
            R.id.back -> {
                finish()
            }
            R.id.save -> {
                //暂停视频
                mVideoEditorManager.pause(mCustomGestureFrameLayout)
                //暂停音乐
                bottomMusicInfoPlayOrPause(false, mCurrentSelectedSoundBean)
                mSaveDialog.show()
            }
            R.id.iv_revert -> {
                if (mVideoEditorManager.canActionRevert()) {
                    mVideoEditorManager.actionRevert(this, this)
                    mVideoEditorManager.play(
                        mVideoEditorManager.getCurrentTimelineIndex(),
                        mCustomGestureFrameLayout,
                        false
                    )
                }
            }
            R.id.iv_restore -> {
                if (mVideoEditorManager.canActionRestore()) {
                    mVideoEditorManager.actionRestore(this, this)
                    mVideoEditorManager.play(
                        mVideoEditorManager.getCurrentTimelineIndex(),
                        mCustomGestureFrameLayout,
                        false
                    )
                }
            }
            R.id.iv_play -> {
                if (mVideoEditorManager.isPlaying()) {
                    mVideoEditorManager.pause(mCustomGestureFrameLayout)
                } else {
                    mEffectEditLayout.selectEffectBean(null)
                    mCustomGestureFrameLayout.selectEffect(null)
                    mVideoEditorManager.play(
                        mVideoEditorManager.getCurrentTimelineIndex(),
                        mCustomGestureFrameLayout,
                        true
                    )
                    bottomMusicInfoPlayOrPause(false, mCurrentSelectedSoundBean)
                }
            }
            R.id.iv_full_screen -> {
                changeFullScreenMode()
            }
        }
    }

    override fun onBackPressed() {
        if (mSavingVideoView?.visibility == View.VISIBLE) {
            showSavingAnim(false, true)
        } else {
            super.onBackPressed()
        }
    }

//    private var fullScreenAdProvider: FullScreenAdProvider? = null
//    private fun loadSaveAd() {
//        if (fullScreenAdProvider == null) {
//            fullScreenAdProvider = FullScreenAdProvider()
//        }
//
//        fullScreenAdProvider?.loadAd("editor_save", AdConstant.AdVirtualUnitID.UNITID_SAVE)
//    }
//
//    private fun showSaveAd(runnable: Runnable?): Boolean {
//        return fullScreenAdProvider?.showAd(this, runnable) == true
//    }
//
//    private fun destroySaveAd() {
//        fullScreenAdProvider?.destroyAd()
//    }
}