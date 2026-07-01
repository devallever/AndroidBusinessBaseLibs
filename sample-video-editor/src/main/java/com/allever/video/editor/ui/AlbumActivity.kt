package com.allever.video.editor.ui

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import app.allever.android.lib.core.app.App.Companion.mainHandler
import app.allever.android.lib.core.ext.log
import app.allever.android.lib.core.permission.PermissionCore
import app.allever.android.lib.core.permission.internal.PermissionHelper
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.android.absbase.utils.TimeUtils
import com.android.absbase.utils.ToastUtils
import com.android.permissions.compat.PermissionCallbacks
import com.android.permissions.compat.PermissionManager
import com.allever.video.editor.ConfigManager
import com.allever.video.editor.R
import com.allever.video.editor.app.Base2Activity
import com.allever.video.editor.function.DataTemporaryStorageHelper
import com.allever.video.editor.function.online.OnlineDataManager
import com.allever.video.editor.ui.PreviewActivity.Companion.EXTRA_RESULT_CHECK
import com.allever.video.editor.ui.PreviewActivity.Companion.EXTRA_RESULT_POSITION
import com.allever.video.editor.ui.adapter.SelectAlbumAdapter
import com.allever.video.editor.ui.adapter.SelectedAdapter
import com.allever.video.editor.ui.bean.ImageFolder
import com.allever.video.editor.ui.bean.ThumbnailBean
import com.allever.video.editor.ui.widget.BulingBulingDrawable
import com.allever.video.editor.ui.widget.tab.TabLayout
import com.allever.video.editor.utils.*
import java.io.File


class AlbumActivity : Base2Activity(), TabLayout.OnTabSelectedListener, View.OnClickListener,
    AlbumFragment.Callback,
    SelectedAdapter.OptionListener, SelectAlbumAdapter.OptionListener,
    DragHelper.DragStateCallback {

    companion object {
        private val TAG = AlbumActivity::class.java.simpleName

        const val INTENT_KEY_PICKED_DATA = "picked_data"
        const val KEY_PICK = "dlakjsot"

        private const val REQUEST_CODE_PREVIEW = 0x01
        private const val RC_PERMISSION = 0x01

        private const val EXTRA_KEY = "llasjp"
        private const val EXTRA_VIDEO_KEY = "videosadw"

        private const val ANIMATION_DURATION = 200L

        fun startActivity(context: Context) {
            val intent = Intent(context, AlbumActivity::class.java)
            context.startActivity(intent)
        }

        fun startActivity(context: Activity, key: String, videoCount: Int, requestCode: Int) {
            val intent = Intent(context, AlbumActivity::class.java)
            intent.putExtra(EXTRA_KEY, key)
            intent.putExtra(EXTRA_VIDEO_KEY, videoCount)
            context.startActivityForResult(intent, requestCode)
        }
    }

    /***
     * 顶部控件
     */
    private lateinit var mTabs: TabLayout
    private lateinit var mViewPager: androidx.viewpager.widget.ViewPager
    private lateinit var mFragmentPageAdapter: FragmentPageAdapter
    private lateinit var mIvSelectAlbum: ImageView
    private lateinit var mAlbumListContainer: ViewGroup
    private lateinit var mTvAlbumTitle: TextView
    private lateinit var mLlAlbumTitleContainer: ViewGroup
    private lateinit var mIvSetting: ImageView
    private lateinit var mIvBack: ImageView
    private lateinit var mIvBilling: ImageView

    /***
     * 底部控件
     */
    private lateinit var mRecyclerViewSelected: androidx.recyclerview.widget.RecyclerView
    private lateinit var mSelectedAdapter: SelectedAdapter
    private lateinit var mSelectedContainer: ViewGroup
    private lateinit var mTvSelectCount: TextView
    private lateinit var mTvSelectTips: TextView
    private lateinit var mTvStart: TextView
    private lateinit var mRlStartContainer: ViewGroup

    /***
     * 选择相册
     */
    private lateinit var mRecyclerViewSelectAlbum: androidx.recyclerview.widget.RecyclerView
    private lateinit var mSelectAlbumAdapter: SelectAlbumAdapter

    /**
     * 单选模式下的Add控件
     */
    private lateinit var mBtnNextContainer: ViewGroup

    //fragment数据的数据集
    private var mFragmentDataMap = mutableMapOf<TabModel.Tab, MutableList<ThumbnailBean>>()
    //底部选中的数据
    private var mSelectedData = mutableListOf<ThumbnailBean>()
    //All数据
    private var mAllData = mutableListOf<ThumbnailBean>()
    //Video数据
    private var mVideoData = mutableListOf<ThumbnailBean>()
    //Photo数据
    private var mPhotoData = mutableListOf<ThumbnailBean>()
    //相册列表数据
    private var mAlbumData = mutableListOf<ImageFolder>()

    private var mFragments = mutableListOf<androidx.fragment.app.Fragment>()

    private var mSelectAlbumContainerAnimShow: Animator? = null
    private var mSelectAlbumContainerAnimHide: Animator? = null
    private var mIvArrowRotateAnimUp: Animator? = null
    private var mIvArrowRotateAnimDown: Animator? = null

//    private var mBtnNextAnimShow: Animator? = null
//    private var mBtnNextAnimHide: Animator? = null

//    private var mEnterFromLauncher: Boolean? = true

    private var mSingleMode = false
    private var mPickMode = false

    private var firstPressedBackTime: Long = 0

    private var mIsNeedRefresh = true

    private var mLastSingleModeData: ThumbnailBean? = null

    private var mMediaChangeObserver: ContentObserver? = null

    private var currentVideoCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ve_activity_album)
        adaptStatusBar(findViewById(R.id.rl_title_bar))

        val key = intent?.getStringExtra(EXTRA_KEY)
//        mSingleMode = key == KEY_PICK
        mPickMode = key == KEY_PICK

        currentVideoCount = intent?.getIntExtra(EXTRA_VIDEO_KEY, 0) ?: 0

        initView()

        initAnim()

        mReceiver = AlbumBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(mReceiver, intentFilter)

    }

    private fun initView() {
//        val adContainer = findViewById<FrameLayout>(R.id.ad_container)
//        mFlowAdView = FlowAdView.newEntranceAdView(adContainer)
////            flowAdView.setBackgroundResource(R.color.default_background)
//        mFlowAdView.setAdLayoutId(R.layout.sc_layout_banner_small_inapp)
//        mFlowAdView.setData(mAdItemBean)
//        mFlowAdView.setTitleLayoutVisibility(View.GONE)


        mIvSelectAlbum = findViewById(R.id.iv_select_album)
        mIvSelectAlbum.setOnClickListener(this)
        mIvSetting = findViewById(R.id.iv_setting)
        mIvSetting.setOnClickListener(this)
        mIvBack = findViewById(R.id.iv_back)
        mIvBack.setOnClickListener(this)
        mIvBilling = findViewById(R.id.iv_billing)
        mIvBilling.setOnClickListener(this)
        mIvBilling.setImageDrawable(
            BulingBulingDrawable(
                resources,
                resources.getDrawable(R.drawable.icon_setting_premium)
            )
        )

        mTvStart = findViewById(R.id.select_start)
        mTvStart.setOnClickListener(this)

        mRlStartContainer = findViewById(R.id.rl_start_container)

        mTvAlbumTitle = findViewById(R.id.tv_album_title)
        mLlAlbumTitleContainer = findViewById(R.id.ll_select_album_container)
        mLlAlbumTitleContainer.setOnClickListener(this)

        mAlbumListContainer = findViewById(R.id.fl_select_album_container)
        mAlbumListContainer.setOnClickListener(this)

        mTvSelectCount = findViewById(R.id.select_count)
        mTvSelectTips = findViewById(R.id.tv_select_tips)

        mBtnNextContainer = findViewById(R.id.fl_next_container)
        mBtnNextContainer.setOnClickListener(this)

        mSelectedContainer = findViewById(R.id.ll_selected_container)


        //Tab
        val tabCount = TabModel.tabCount
        mTabs = findViewById(R.id.tl_tab)
        for (i in 0 until tabCount) {
            val tabModel = TabModel.getTab(i)
            val tabView = getTabView(tabModel, i)
            val tab = mTabs.newTab()
                .setCustomView(tabView)
            tab.tag = tabModel
            mTabs.addTab(tab)
        }
        mViewPager = findViewById(R.id.view_pager)
        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabs))
        mTabs.setOnTabSelectedListener(this)
        mTabs.setSelectedTabIndicatorWidth(ResourcesUtils.resources.getDimension(R.dimen.album_selected_tab_indicator_width).toInt())
        mTabs.setSelectedTabIndicatorHeight(ResourcesUtils.resources.getDimension(R.dimen.album_selected_tab_indicator_height).toInt())
        val indicatorColor = ResourcesUtils.resources.getColor(R.color.album_tab_indicator_color)
        mTabs.setSelectedTabIndicatorColor(indicatorColor)
        mTabs.needDrawSelectedIndicator(true)

        //viewpager
        mViewPager = findViewById(R.id.view_pager)

        // 先清除现有的fragment
        // 原因: 其他界面崩溃后,该界面会出现唤醒的情况, 而onCreate内部会重建所有的fragment,导致界面刷新异常的问题
        val fragments = supportFragmentManager.fragments
        if (fragments.isNotEmpty()) {
            val beginTransaction = supportFragmentManager.beginTransaction()
            fragments?.map {
                beginTransaction.remove(it)
            }
            beginTransaction.commitAllowingStateLoss()
        }

        for (i in 0 until tabCount) {
            val fragment = AlbumFragment()
            fragment.callback = this
            fragment.type = TabModel.getTab(i)
            mFragments.add(fragment)
        }
        mFragmentDataMap[TabModel.Tab.ALL] = mAllData
        mFragmentDataMap[TabModel.Tab.VIDEO] = mVideoData
        mFragmentDataMap[TabModel.Tab.PICTURE] = mPhotoData

        mFragmentPageAdapter =
            FragmentPageAdapter(supportFragmentManager, mFragments)
        mViewPager.adapter = mFragmentPageAdapter
        mViewPager.currentItem = 0

        //底部选中显示图片的RecyclerView
        mRecyclerViewSelected = findViewById(R.id.recycler_view_selected)
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        layoutManager.orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        mRecyclerViewSelected.layoutManager = layoutManager
        mSelectedAdapter = SelectedAdapter(
            this,
            R.layout.ve_item_selected,
            mSelectedData
        )
        mSelectedAdapter.setOptionListener(this)
        mRecyclerViewSelected.adapter = mSelectedAdapter
        val firstItemMarginLeft = DeviceUtils.dip2px(10f)
        val lastItemMarginRight = firstItemMarginLeft
        mRecyclerViewSelected.addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
                val pos = parent.getChildLayoutPosition(view)
                if (pos == 0) {
                    outRect.left = firstItemMarginLeft
                } else if (pos + 1 == mSelectedData.size) {
                    outRect.right = lastItemMarginRight
                }
            }
        })
        DragHelper.bind(mRecyclerViewSelected, mSelectedData, this)

        //选择相册的RecyclerView
        mRecyclerViewSelectAlbum = findViewById(R.id.recycler_view_album)
        mRecyclerViewSelectAlbum.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        mSelectAlbumAdapter = SelectAlbumAdapter(
            this,
            R.layout.ve_item_select_album,
            mAlbumData
        )
        mSelectAlbumAdapter.setOptionListener(this)
        mRecyclerViewSelectAlbum.adapter = mSelectAlbumAdapter

        if (mPickMode) {
            //mSelectedContainer.visibility = View.GONE
            mRlStartContainer.visibility = View.GONE
            //显示返回按钮
            mIvBack.visibility = View.VISIBLE
            //隐藏设置按钮
            mIvSetting.visibility = View.GONE
            mIvBilling.visibility = View.GONE
            mRecyclerViewSelected.visibility = View.GONE
        } else {
            //默认显示
        }

    }

    private fun initAnim() {
        mSelectAlbumContainerAnimShow = ObjectAnimator.ofFloat(mAlbumListContainer, "alpha", 0f, 1f)
        mSelectAlbumContainerAnimShow?.duration =
            ANIMATION_DURATION
        mSelectAlbumContainerAnimShow?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {
                mAlbumListContainer.visibility = View.VISIBLE
            }
        })


        mSelectAlbumContainerAnimHide = ObjectAnimator.ofFloat(mAlbumListContainer, "alpha", 1f, 0f)
        mSelectAlbumContainerAnimHide?.duration =
            ANIMATION_DURATION
        mSelectAlbumContainerAnimHide?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                mAlbumListContainer.visibility = View.GONE
            }
        })

        mIvArrowRotateAnimUp = ObjectAnimator.ofFloat(mIvSelectAlbum, "rotation", 0f, 180f)
        mIvArrowRotateAnimUp?.duration =
            ANIMATION_DURATION

        mIvArrowRotateAnimDown = ObjectAnimator.ofFloat(mIvSelectAlbum, "rotation", 180f, 360f)
        mIvArrowRotateAnimDown?.duration =
            ANIMATION_DURATION
    }

    override fun onStart() {
        super.onStart()
        if (mPickMode && GlobalData.albumData.isNotEmpty()) {
            //默认显示第一个相册的数据
            val firstImageFolder = GlobalData.albumData[0]
            updateData(firstImageFolder)
            updateFragmentUI()
            mSelectAlbumAdapter.mData = GlobalData.albumData
            mSelectAlbumAdapter.notifyDataSetChanged()
            return
        }

        getFolderDataTask().executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
        OnlineDataManager.getInstance().download()
    }

    override fun onResume() {
        super.onResume()
        if (mSelectedData.size > 0) {
            currentVideoCount = 0
            mSelectedData.map {
                if (it.type == MediaTypeUtil.TYPE_VIDEO) {
                    currentVideoCount++
                }
            }
        }
        if (mMediaChangeObserver == null) {
            mMediaChangeObserver = object : ContentObserver(mainHandler) {
                override fun onChange(selfChange: Boolean) {
                    getFolderDataTask().executeOnExecutor(AsyncTask.DATABASE_THREAD_EXECUTOR)
                }
            }
            val contentResolver = contentResolver
            contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                false,
                mMediaChangeObserver!!
            )
            contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                false,
                mMediaChangeObserver!!
            )
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(mMediaChangeObserver!!)
        unregisterReceiver(mReceiver)

        if (mPickMode) {
            for (thumbnail in mSelectedData) {
                thumbnail.isChecked = false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_PREVIEW -> {
                    if (data == null) {
                        return
                    }

                    val position = data.getIntExtra(EXTRA_RESULT_POSITION, -1)
                    val isCheck = data.getBooleanExtra(EXTRA_RESULT_CHECK, false)

                    if (position == -1) {
                        return
                    }

                    val tab = TabModel.selectedTab
                    val thumbnailBeanList = mFragmentDataMap[tab] ?: return
                    val targetData = thumbnailBeanList[position]
                    targetData.isChecked = isCheck

                    if (isCheck) {
                        mSelectedData.add(targetData)
                    } else {
                        mSelectedData.remove(targetData)
                    }

                    updateFragmentUI(targetData)
                    updateSelectedPanelUI()
                }
            }
        }
    }
    private fun showSelectAlbumContainer(show: Boolean) {
        if (show) {
            mSelectAlbumContainerAnimShow?.start()
            mIvArrowRotateAnimUp?.start()
        } else {
            mSelectAlbumContainerAnimHide?.start()
            mIvArrowRotateAnimDown?.start()
        }
    }

    private fun updateFragmentUI(thumbnailBean: ThumbnailBean? = null) {
        val fragmentCount = mFragments.size
        for (i in 0 until fragmentCount) {
            val fragment = mFragments[i] as? AlbumFragment
            if (thumbnailBean == null) {
                val tab = TabModel.getTab(i)
                fragment?.updateData(mFragmentDataMap[tab])
            } else {
                fragment?.updateData(thumbnailBean)
            }
        }
    }

    private fun updateSelectedPanelUI() {
        mSelectedAdapter.notifyDataSetChanged()
        val size = mSelectedData.size
        if (size < 1) {
            mTvSelectCount.visibility = View.GONE
        } else {
            if (size == 1) {
                mTvSelectTips.text = getString(R.string.album_activity_bottom_selected_tips_text_no_select)
            } else {
                mTvSelectTips.text = getString(R.string.album_activity_bottom_selected_tips_text_selected)
            }
            mTvSelectCount.visibility = View.VISIBLE
            mTvSelectCount.text = size.toString()
        }

        if (size == 0) {
            mRecyclerViewSelected.visibility = View.GONE
            val background = mTvStart.background
            if (background is GradientDrawable) {
                background.setColor(ResourcesUtils.getColor(R.color.album_activity_bottom_btn_add_no_select_background_color))
            }
            mTvStart.setTextColor(ResourcesUtils.getColor(R.color.album_activity_bottom_no_selected_btn_start_text))
        } else {
            mRecyclerViewSelected.visibility = View.VISIBLE
            val background = mTvStart.background
            if (background is GradientDrawable) {
                background.setColor(ResourcesUtils.getColor(R.color.album_activity_bottom_btn_add_background_color))
            }
            mTvStart.setTextColor(ResourcesUtils.getColor(R.color.album_activity_bottom_selected_btn_start_text))
        }
    }

    private fun updateAddUI() {
        if (mSelectedData.isEmpty()) {
            mBtnNextContainer.visibility = View.GONE
        } else {
            val background = mBtnNextContainer.background
            if (background is GradientDrawable) {
                background.setColor(ResourcesUtils.getColor(R.color.album_activity_bottom_btn_add_background_color))
            }
            mBtnNextContainer.visibility = View.VISIBLE
        }
    }

    private fun updateData(imageFolder: ImageFolder) {
        mAllData.clear()
        mPhotoData.clear()
        mVideoData.clear()

        mAllData.addAll(imageFolder.data)
        mVideoData.addAll(imageFolder.videoThumbnailBeans)
        mPhotoData.addAll(imageFolder.photoThumbnailBeans)
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab ?: return
        mViewPager.currentItem = tab.position
        TabModel.selectedTab = tab.tag as TabModel.Tab

        for (i in 0 until mTabs.tabCount) {
            val tabAt = mTabs.getTabAt(i)
            if (tabAt != null) {
                val textView = tabAt.customView?.findViewById<TextView>(R.id.tv_tab)
                if (tabAt == tab) {
                    textView?.setTextColor(resources.getColor(R.color.album_tab_selected_color))
                } else {
                    textView?.setTextColor(resources.getColor(R.color.album_tab_un_selected_color))
                }
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    /**
     * fragment 回调， 点击图片时回调
     */
    override fun onAlbumImgItemClick(thumbnailBean: ThumbnailBean): Boolean {
        //根据thumbnailBean 的选中字段 添加或移除
        if (!mSingleMode) {
            //非单选模式，默认流程，
            if (thumbnailBean.isChecked) {
                if (thumbnailBean.type == MediaTypeUtil.TYPE_VIDEO) {
                    if (currentVideoCount > ConfigManager.maxVideoCount) {
                        ToastUtils.show(this, getString(R.string.guide_video_tips))
                        return false
                    }
                    currentVideoCount++
                }
                //添加
                mSelectedData.add(thumbnailBean)
                mRecyclerViewSelected.scrollToPosition(mSelectedData.size - 1)
            } else {
                //移除
                if (mSelectedData.contains(thumbnailBean)) {
                    mSelectedData.remove(thumbnailBean)
                    if (thumbnailBean.type == MediaTypeUtil.TYPE_VIDEO) {
                        currentVideoCount--
                    }
                }
            }

            if (mPickMode) {
                //选择模式的流程
                updateAddUI()
            } else {
                //默认流程
                updateSelectedPanelUI()
            }

            updateFragmentUI(thumbnailBean)

        } else {
            //单选模式
            mRecyclerViewSelected.visibility = View.GONE

            val mLastSingleModeData = mLastSingleModeData
            val mSelectedData = mSelectedData

            if (mLastSingleModeData == null) {
                this.mLastSingleModeData = thumbnailBean
                mSelectedData.add(thumbnailBean)
            } else {
                if (mLastSingleModeData != thumbnailBean) {
                    //刷新上一个选中
                    mLastSingleModeData.isChecked = false
                    updateFragmentUI(mLastSingleModeData)
                    mSelectedData.clear()

                    //刷新当前选中
                    thumbnailBean.isChecked = true
                    this.mLastSingleModeData = thumbnailBean
                    mSelectedData.add(thumbnailBean)
                } else {
                    thumbnailBean.isChecked = mSelectedData.size == 0
                    if (thumbnailBean.isChecked) {
                        mSelectedData.add(thumbnailBean)
                    } else {
                        mSelectedData.clear()
                    }
                }

                updateFragmentUI(thumbnailBean)

            }

            updateAddUI()

        }
        return true
    }

    /***
     * 长按缩略图回调
     */
    override fun onAlbumImgItemLongClick(thumbnailBean: ThumbnailBean) {
        val data = mFragmentDataMap[TabModel.selectedTab]
        val position = data?.indexOf(thumbnailBean) ?: 0
        val arrayListData = ArrayList<ThumbnailBean>()
        if (data != null) {
            arrayListData.addAll(data)
            PreviewActivity.startActivity(
                this,
                arrayListData,
                position,
                false,
                REQUEST_CODE_PREVIEW
            )
        }
    }

    /***
     * 底部item点击
     */
    override fun onSelectedAdapterDeleteClick(thumbnailBean: ThumbnailBean, position: Int) {
        if (thumbnailBean.type == MediaTypeUtil.TYPE_VIDEO) {
            currentVideoCount--
        }
        mSelectedData.remove(thumbnailBean)

        thumbnailBean.isChecked = false

        updateFragmentUI(thumbnailBean)
        updateSelectedPanelUI()

    }


    /***
     * 点击选择相册回调
     */
    override fun onChooseAlbumAdapterItemClick(imageFolder: ImageFolder, position: Int) {
        showSelectAlbumContainer(false)

        mTvAlbumTitle.text = imageFolder.name

        updateData(imageFolder)
        updateFragmentUI()
    }

    override fun onClick(v: View?) {
        when (v) {
            mIvSelectAlbum, mLlAlbumTitleContainer -> {
                val visibility = mAlbumListContainer.visibility
                when (visibility) {
                    View.VISIBLE -> {
                        showSelectAlbumContainer(false)
                    }
                    View.GONE -> {
                        showSelectAlbumContainer(true)
                    }
                }
            }

            mAlbumListContainer -> {
                showSelectAlbumContainer(false)
            }

            mTvStart -> {
                if (mSelectedData.isEmpty()) {
                    ToastUtils.show(this, ResourcesUtils.getString(R.string.album_activity_click_start_no_select))
                    return
                }
                val key = "album_2_editor_data"
                DataTemporaryStorageHelper.put(key, mSelectedData, TimeUtils.TimeConstant.ONE_SEC * 10)
                VideoEditorActivity.startActivity(this, key)
            }

            mIvSetting -> {
                SettingsActivity.startActivity(this)
            }

            mIvBack -> {
                if (mPickMode) {
                    val intent = Intent()
                    setResult(Activity.RESULT_OK, intent)
                }
                finish()
            }

            mIvBilling -> {
            }

            mBtnNextContainer -> {
                val intent = Intent()
                intent.putParcelableArrayListExtra(INTENT_KEY_PICKED_DATA, mSelectedData as ArrayList)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (mAlbumListContainer.visibility == View.VISIBLE) {
            showSelectAlbumContainer(false)
            return
        }

        if (!mPickMode) {
            if (System.currentTimeMillis() - firstPressedBackTime < 2000) {
                super.onBackPressed()
            } else {
                Toast.makeText(baseContext, R.string.click_again_to_exit, Toast.LENGTH_SHORT).show()
                firstPressedBackTime = System.currentTimeMillis()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun getTabView(tab: TabModel.Tab, position: Int): View {
        val view = LayoutInflater.from(this).inflate(R.layout.ve_tab_item, null)
        val tvLabel = view.findViewById<TextView>(R.id.tv_tab)
        tvLabel?.text = resources.getString(tab.labelResId)
        //修改第一个Tab为选中状态
        if (position == 0) {
            tvLabel?.setTextColor(resources.getColor(R.color.album_tab_selected_color))
        }
        return view
    }


    /**
     * 获取媒体数据的异步任务
     *
     * @return
     */
    private fun getFolderDataTask(): AsyncTask<Void, Void, java.util.ArrayList<ImageFolder>> {
        log(TAG, "getFolderDataTask")
        mIsNeedRefresh = false
        return object : AsyncTask<Void, Void, java.util.ArrayList<ImageFolder>>() {

            override fun doInBackground(vararg params: Void): java.util.ArrayList<ImageFolder>? {

                //文件夹信息
                val datas = ImageHelper.getAllFolderData(this@AlbumActivity)

                //按目录名称排序
                datas.sortWith(Comparator { arg0, arg1 -> arg0.name.compareTo(arg1.name) })

                //插入第一个数据，all
                val firstImageFolder = ImageFolder()
                firstImageFolder.dir = null
                firstImageFolder.bucketId = null
                firstImageFolder.name = getString(R.string.album_default_album_name)

                datas.add(0, firstImageFolder)

                for (i in 0 until datas.size) {
                    val imageFolder = datas[i]
                    var allThumbnailBean: ArrayList<ThumbnailBean>?
                    allThumbnailBean = if (imageFolder.bucketId == null) {
                        ImageHelper.getThumbnailBeanFromPath(this@AlbumActivity, imageFolder.dir)
                    } else {
                        //解决sdcard根目录数据重复问题，使用路径会搜索到根目录及子目录的内容，改成bucketId，
                        ImageHelper.getThumbnailBeanFromBucketId(this@AlbumActivity, imageFolder.bucketId)
                    }
                    imageFolder.data = allThumbnailBean

                    //分类
                    val photoThumbnailBean = mutableListOf<ThumbnailBean>()
                    val videoThumbnailBean = mutableListOf<ThumbnailBean>()
                    for (position in 0 until allThumbnailBean.size) {
                        val thumbnailBean = allThumbnailBean[position]
                        if (MediaTypeUtil.isImage(thumbnailBean.type)) {
                            photoThumbnailBean.add(thumbnailBean)
                        } else if (MediaTypeUtil.isVideo(thumbnailBean.type)) {
                            videoThumbnailBean.add(thumbnailBean)
                        }
                    }

                    //手动插入第一个folder数据，firstThumbnailBean字段为空，需要特殊处理
                    if (imageFolder.firstThumbnailBean == null && allThumbnailBean.size > 0) {
                        imageFolder.setFirstImageBean(allThumbnailBean[0])
                    }

                    imageFolder.photoThumbnailBeans = (photoThumbnailBean as ArrayList<ThumbnailBean>)
                    imageFolder.videoThumbnailBeans = (videoThumbnailBean as ArrayList<ThumbnailBean>)

                    imageFolder.photoCount = photoThumbnailBean.size
                    imageFolder.videoCount = videoThumbnailBean.size

                    datas[i] = imageFolder

                }

                //排除空目录
                val iterator = datas.iterator()
                while (iterator.hasNext()) {
                    val folder = iterator.next()
                    if (folder.data.isEmpty()) {
                        iterator.remove()
                    }
                }

                GlobalData.cloneAlbumData(datas)

                return datas
            }

            override fun onPostExecute(result: java.util.ArrayList<ImageFolder>?) {
                if (result == null || result.size == 0) {
                    return
                }

                mAlbumData.clear()
                mAlbumData.addAll(result)
                mSelectAlbumAdapter.notifyDataSetChanged()

                //默认显示第一个相册的数据
                val firstImageFolder = result[0]
                updateData(firstImageFolder)
                updateFragmentUI()

                //选中后，如果监听到系统相册变化，重新获取了数据，需要刷新选中的数据
                for (j in 0 until mSelectedData.size) {
                    val selectedData = mSelectedData[j]
                    for (i in 0 until mAllData.size) {
                        val thumbnailBean = mAllData[i]
                        if (selectedData.path == thumbnailBean.path) {
                            thumbnailBean.isChecked = true
                            mSelectedData.removeAt(j)
                            mSelectedData.add(j, thumbnailBean)
                            break
                        }
                    }
                }
                updateSelectedPanelUI()
            }
        }
    }

    private fun checkSubs() {
        if (ConfigManager.openAppCountInDay == 1) {
            if (ConfigManager.purchaseSubSize >= 0) {
            }
        }
    }

    //监听锁屏广播，暂停下载
    private var mReceiver: AlbumBroadcastReceiver? = null

    private inner class AlbumBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_SCREEN_OFF == intent.action) {
                OnlineDataManager.getInstance().pauseDownload()
            } else if (Intent.ACTION_USER_PRESENT == intent.action) {
                OnlineDataManager.getInstance().restartDownloadConfig()
            }
        }
    }



    override fun getData(): MutableList<out Any> {
        return mSelectedData
    }

    override fun allowDrag(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun allowSwipe(recyclerView: androidx.recyclerview.widget.RecyclerView, viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onItemRangeMoved(from: Int, to: Int) {
    }

    override fun onDragStart() {

    }

    override fun onDragEnd(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, from: Int, to: Int) {

    }
}