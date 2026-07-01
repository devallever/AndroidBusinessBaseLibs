//package com.videoeditor.ui
//
//import android.animation.Animator
//import android.animation.AnimatorSet
//import android.animation.ObjectAnimator
//import android.animation.ValueAnimator
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.content.res.Configuration
//import android.graphics.BitmapFactory
//import android.graphics.Paint
//import android.graphics.drawable.Drawable
//import android.os.Bundle
//import android.os.Handler
//import android.os.Message
//import androidx.core.content.ContextCompat
//import android.view.View
//import android.view.ViewGroup
//import android.view.ViewStub
//import android.view.animation.LinearInterpolator
//import android.widget.*
//import com.android.absbase.ui.BaseActivity
//import com.android.absbase.utils.AppUtils
//import com.android.absbase.utils.DeviceUtils
//import com.android.absbase.utils.ResourcesUtils
//import com.android.absbase.utils.TimeUtils
//import com.android.billing.compat.BillingManager
//import com.android.billing.compat.bean.PurchaseItem
//import com.android.billing.compat.bean.ResultItem
//import com.android.billing.compat.bean.SkuDetailsItem
//import com.android.billing.compat.utils.CommUtils
//import com.android.billingclient.api.BillingClient
//import com.statistics.StatisticsUtils
//import com.allever.video.editor.BuildConfig
//import com.allever.video.editor.ConfigManager
//import com.allever.video.editor.R
//import com.allever.video.editor.function.billing.Billing
//import com.allever.video.editor.function.billing.BillingProxy
//import com.allever.video.editor.function.online.OnlineManager
//import com.allever.video.editor.ui.dialog.RetainDialog2
//import BlurImageView
//import com.allever.video.editor.ui.widget.MediaView
//
//
//class BillingActivity : BaseActivity(), View.OnClickListener {
//
//    companion object {
//        const val INTENT_ENTRANCE = "Ie_dkfjkdsjkf"
//        const val INTENT_RESULT_ACTIVITY_CLASS = "IRAC_ALSDKFKKSDF"
//
//        fun startActivity(context: Context,enter:String, callback: Callback? = null) {
//            val intent = Intent(context, BillingActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
//            context.startActivity(intent)
////            //StatisticsUtils.statisics("inapp_billing_ui","enter",enter)
//            mCallback = callback
//        }
//
//        fun startActivityAndResult(context: Context, entrance: String, resultActivityClass: Class<out Activity>) {
//            val intent = Intent(context, BillingActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION)
//            intent.putExtra(INTENT_ENTRANCE, entrance)
//            intent.putExtra(INTENT_RESULT_ACTIVITY_CLASS, resultActivityClass)
//            context.startActivity(intent)
//            //StatisticsUtils.statisics("inapp_billing_ui", "enter", entrance)
//        }
//
////        private val DEFAULT_BANNER_VIDEO_URL = OnlineConstant.getUrl("ev/qr-video1.mp4")
////        private var currentBannerVideoUrl: String = DEFAULT_BANNER_VIDEO_URL
//        private var currentBannerImageUrl: String? = null
//
//        private var mCallback: Callback? = null
//
//
//        fun reloadBannerVideo(videoUrl: String?) {
////            currentBannerVideoUrl = if (videoUrl != null && videoUrl.isNotEmpty()) {
////                videoUrl
////            } else DEFAULT_BANNER_VIDEO_URL
////            OnlineManager.preloadUrl(currentBannerVideoUrl)
//        }
//
//        fun reloadBannerImage(imageUrl: String?) {
//            currentBannerImageUrl = if (imageUrl != null && imageUrl.isNotEmpty()) {
//                OnlineManager.preloadUrl(imageUrl)
//                imageUrl
//            } else null
//        }
//    }
//    private var currentMonthProductId = Billing.SUB_MONTH
//    private var currentYearProductId = Billing.SUB_YEAR
//
//    //底部按钮对应的商品id
//    private var currentDynamicProductId = Billing.SUB_MONTH
//    private var mEntrance: String = ""
//    private var mResultActivityClass: Class<out Activity>? = null
//
//
//    private val mBillingProxy: BillingProxy? = null
//    private var llBanner1: LinearLayout? = null
//    private var llMonth: LinearLayout? = null
//    private var llYear: LinearLayout? = null
//    private var ivBanner1: BlurImageView? = null
//    private var ivBanner2: BlurImageView? = null
//    private var cbMonth: CheckBox? = null
//    private var cbYear: CheckBox? = null
//    private var btnContinueBuy: TextView? = null
//    private var titleMonth: TextView? = null
//    private var despMonth: TextView? = null
//    private var titleYear: TextView? = null
//    private var despYear: TextView? = null
//    private var selectYearSubs = true
//    private var banner1Set: AnimatorSet? = null;
//    private var banner2Set: AnimatorSet? = null;
//    private var mediaView: MediaView? = null
//
//    private var premium_card_1: Drawable? = null
//    private var premium_card_2: Drawable? = null
//    private var premium_card_3: Drawable? = null
//    private var premium_card_4: Drawable? = null
//    var temp1: Drawable? = null
//    var temp2: Drawable? = null
//
//    private var mTvCheck: TextView? = null
//    private var mTvMessage: TextView? = null
//
//    /**
//     * 当前订阅有没有使用视频
//     */
//    private var mCurrentBillingBannerUseMedia = false
//
//    var viewStubPremiumView:ViewStub?= null;
//    var viewStubVipView:ViewStub?= null;
//    var viewStubVipViewOther:ViewStub?= null;
//
//
//    /**
//     * other
//     */
//    private var mOtherLlMonth: FrameLayout? = null
//    private var mOtherLlYear: FrameLayout? = null
//    private var mArrow: ImageView? = null
//    private var mYearTip:TextView? = null
//
////    private var mTaskInfo = OnlineManager.preloadUrl(currentBannerVideoUrl, object : DownloadCallback {
////
////        override fun onCompleted(taskInfo: TaskInfo) {
////            startMedia()
////        }
////
////        override fun onError(e: Exception) {
////        }
////
////    })
//
//    /**
//     * 应用商品信息
//     * 这里只对订阅做处理
//     */
//    private var skuDetails: MutableList<SkuDetailsItem>? = null
//
//    //    private var allHistorySkus = mutableListOf<String>()
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_billing)
//        intent?.also {
//            mEntrance = it.getStringExtra(INTENT_ENTRANCE) ?: ""
//            mResultActivityClass = it.getSerializableExtra(INTENT_RESULT_ACTIVITY_CLASS) as? Class<out Activity>
//        }
//
//        if (ConfigManager.purchaseSubYear && ConfigManager.subYearAutoRenew) {
//            showPremiumView()
//        } else {
//            showVipView()
//        }
//
//    }
//
//    override fun onResume() {
//        super.onResume()
////        if (ConfigManager.purchaseSubYear) {
////            showPremiumView()
////        } else {
////            showVipView()
////        }
//    }
//    /**
//     * 设置banner金额
//     */
//    @SuppressLint("SetTextI18n")
//    private fun setProductPrice() {
//        checkBottomBtnProductId()
//        var monthPrice = 0f
//        skuDetails?.forEach { item ->
//            var parentId = mBillingProxy?.getSkuId(item.productId)
//            if(parentId == null){
//                parentId = item.productId
//            }
//            if (parentId == Billing.SUB_MONTH) {
//                monthPrice = item.price_amount_micros / 1_000_000f
//            }
//        }
////        val skuDetailsItem = mBillingProxy?.getSkuDetailsItem(SUB_PRE_MONTH)
////        if(allHistorySkus.contains(SUB_PRE_MONTH) && skuDetailsItem!=null){
////            skuDetails?.add(skuDetailsItem)
////        }
////        skuDetails?.forEach { item ->
////            var parentId = mBillingProxy?.getSkuId(item.productId)
////            if(parentId == null){
////                parentId = item.productId
////            }
////            if (parentId == SUB_PRE_MONTH) {
////                monthPrice = item.price_amount_micros / 1_000_000f
////            }
////        }
////        skuDetails?.forEach {  item ->
////            if(item.type == BillingClient.SkuType.SUBS){
////                var parentId = mBillingProxy?.getSkuId(item.productId)
////                if(parentId == null){
////                    parentId = item.productId
////                }
////                when(parentId){
////                    //SUB_MONTH,SUB_PRE_MONTH
////                    Billing.SUB_MONTH -> {
////                        //先判断之前是否有购买过商品
////                        if (mBillingProxy?.getRenewSkuId() != null) {
////                            val itemDetail =  mBillingProxy?.getSkuDetails(currentDynamicProductId)
////                            if (itemDetail != null) {
////                                currentMonthProductId = itemDetail.productId
////                                val period = itemDetail.priceType
////                                val price = itemDetail.singlePrice
////                                if (ConfigManager.firstEnterGpStyle == Billing.STYLE_TYLE) {
////                                    titleMonth?.text = getString(R.string.vip_month_tip, price)
////                                    despMonth?.text = getString(R.string.vip_month_tip2, price)
////                                } else {
////                                    despMonth?.text = getString(R.string.gp_month_desp, price)
////                                    mYearTip?.text = getString(R.string.gp_bottom_tip2, period, price, period)
////                                }
////                            }
////                        } else {
////                            //走配置流程,价格应该是替换后的商品价格
////                            currentMonthProductId = Billing.getReplaceSku(item.productId)
////                            val replaceSkuDetailsItem = skuDetails?.find { it.productId == currentMonthProductId }
////                            val newPrice = if (replaceSkuDetailsItem != null) { replaceSkuDetailsItem.price } else { item.price }
////                            val price = CommUtils.formatPrice(newPrice, 1, item.price_amount_micros, item.price_currency_code, 2)
////                            if(ConfigManager.firstEnterGpStyle == Billing.STYLE_TYLE){
////                                titleMonth?.text = getString(R.string.vip_month_tip,price);
////                                despMonth?.text = getString(R.string.vip_month_tip2,price);
////                            }else{
////                                despMonth?.text = getString(R.string.gp_month_desp,price);
////                                mYearTip?.text  = getString(R.string.gp_bottom_tip,price);
////                            }
////                            val skuConfig = Billing.getSkuConfig(Billing.SUB_MONTH)
////                            if (skuConfig != null) {
////                                val title = Billing.getString(skuConfig.title)
////                                if (title != null && title.isNotEmpty()) {
////                                    titleMonth?.text = String.format(title, price)
////                                }
////                                val desc = Billing.getString(skuConfig.desc)
////                                if (desc != null && desc.isNotEmpty()) {
////                                    despMonth?.text = String.format(desc, price)
////                                }
////                                if (Billing.bottomDesc.isNotEmpty()) {
////                                    mYearTip?.text = String.format(Billing.bottomDesc, price)
////                                }
////                                if (skuConfig.replaceSku.isNotEmpty()) {
////                                    currentMonthProductId = skuConfig.replaceSku
////                                }
////                            }
////                        }
////                    }
////                    //SUB_YEAR,SUB_PRE_YEAR
////                    Billing.SUB_YEAR ->{
////                        currentYearProductId = Billing.getReplaceSku(item.productId)
////                        val replaceSkuDetailsItem = skuDetails?.find { it.productId == currentYearProductId }
////                        val price = if (replaceSkuDetailsItem != null) { replaceSkuDetailsItem.price } else { item.price }
////                        val titlePriceStr = CommUtils.formatPrice(price, 12, item.price_amount_micros, item.price_currency_code, 2)
//////                        val descPriceStr = CommUtils.formatPrice(price, 1, item.price_amount_micros, item.price_currency_code, 2)
////                        if(ConfigManager.firstEnterGpStyle == Billing.STYLE_TYLE){
////                            titleYear?.text = getString(R.string.vip_year_tip, titlePriceStr);
////                            val savePrice = CommUtils.formatPrice(price, 12, item.price_amount_micros, item.price_currency_code, 2, monthPrice)
////                            despYear?.text = getString(R.string.vip_year_tip2, savePrice);
////                        }else{
////                            despYear?.text = getString(R.string.gp_year_desp, titlePriceStr)
////                        }
////                        val skuConfig = Billing.getSkuConfig(Billing.SUB_YEAR)
////                        if (skuConfig != null) {
////                            val title = Billing.getString(skuConfig.title)
////                            if (title != null && title.isNotEmpty()) {
////                                titleYear?.text = String.format(title, titlePriceStr)
////                            }
////                            val desc = Billing.getString(skuConfig.desc)
////                            if (desc != null && desc.isNotEmpty()) {
////                                despYear?.text = String.format(desc, titlePriceStr)
////                            }
////                            if (skuConfig.replaceSku.isNotEmpty()) {
////                                currentYearProductId = skuConfig.replaceSku
////                            }
////                        }
////                   }
////                }
////            }
////        }
//    }
//    private fun initPrivate() {
//        mTvCheck = findViewById<TextView>(R.id.tv_check_privacy)
//        mTvMessage = findViewById<TextView>(R.id.tv_privacy_pre)
//        val onClickListener = View.OnClickListener {
//            AppUtils.startWebView(this, BuildConfig.PRIVACY_URL)
//        }
//        //土耳其语言为倒装  特殊处理
//        val layoutDirection = resources.configuration.layoutDirection
//        //if(getResources().getConfiguration().locale.getLanguage().endsWith("tr")) {
//        if (layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) {
//            mTvMessage?.setText(R.string.privacy_message)
//            mTvCheck?.setText(R.string.privacy_message_pre)
//            mTvMessage?.paint?.flags = Paint.UNDERLINE_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
//            mTvMessage?.setOnClickListener(onClickListener)
//        } else {
//            mTvCheck?.paint?.flags = Paint.UNDERLINE_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
//            mTvCheck?.setOnClickListener(onClickListener)
//        }
//    }
//
//    private fun initMediaView() {
//        mediaView = findViewById<MediaView>(R.id.mediaView)
//        startMedia()
//    }
//
//    private fun startMedia() {
//        val mediaView = mediaView ?: return
//        val banner = when (ConfigManager.firstEnterGpStyle) {
//            Billing.STYLE_TYLE -> R.drawable.premium_banner
//            Billing.STYLE_OTHER_TYLE -> R.drawable.premium_banner_other
//            else -> R.drawable.premium_banner_other
//        }
//        val url = currentBannerImageUrl
//        val bitmap = if (url != null) {
//            val taskInfo = OnlineManager.preloadUrl(url, null)
//            if (taskInfo.exists()) {
//                val path = taskInfo.path
//                BitmapFactory.decodeFile(path)
//            } else null
//        } else null
//        if (bitmap != null) {
//            mediaView.setDefaultImage(bitmap)
//        } else {
//            mediaView.setDefaultImage(banner)
//        }
////        if (ConfigManager.billingBannerUseMedia) {
////            if (mTaskInfo.exists()) {
////                mCurrentBillingBannerUseMedia = true
////                mediaView.visibility = View.VISIBLE
////                mediaView.setDataSource(mTaskInfo.path)
////                mediaView.isLooping = true
////                mediaView.start()
////            }
////        }
//    }
//
//    private fun showPremiumView(visible:Boolean = true) {
//        if(viewStubPremiumView == null){
//            viewStubPremiumView = findViewById<ViewStub>(R.id.viewstub_premium)
//            viewStubPremiumView?.inflate()
//            findViewById<View>(R.id.iv_close)?.setOnClickListener(this)
//        }
//        if(visible){
//            viewStubPremiumView?.visibility = View.VISIBLE
//        }else{
//            viewStubPremiumView?.visibility = View.GONE
//        }
//    }
//    private fun startConnectGp(){
//        mBillingProxy?.setOnStartSetupListener(object :BillingManager.OnStartSetupFinishedListener{
//            override fun onSetupError(p0: Int) {}
//
//            override fun onSetupFail(p0: ResultItem?) {}
//
//            override fun onSetupSuccess() {
//                // fix bug:每次进来重新query,避免出现配置新增的sku无法获取到的问题
////                skuDetails = mBillingProxy?.getDiscountSkuDetails().toMutableList()
////                allHistorySkus = mBillingProxy?.getHitstorySkus().toMutableList()
////                if(skuDetails == null || skuDetails?.size == 0){
//                    mBillingProxy?.querySubsSkuDetails(object : BillingManager.OnQueryFinishedListener{
//                        override fun onQueryError() {}
//
//                        override fun onQueryFail(p0: ResultItem?) {}
//
//                        override fun onQuerySuccess(p0: String?, p1: MutableList<SkuDetailsItem>?) {
//                            skuDetails = mBillingProxy?.getDiscountSkuDetails().toMutableList()
//                            setProductPrice()
//                            val str  = arrayListOf<String>()
//                            p1?.map {
//                                str.add(it.productId)
//                                str.add(it.price)
//                                str.add("price_currency_code")
//                                str.add(it.price_currency_code)
//                                null
//                            }
//                            if(str.isEmpty()){
//                                str.add("error")
//                                str.add("nothing")
//                            }
//                            //StatisticsUtils.statisics("inapp_billing_d",*str.toTypedArray())
//                        }
//                    })
////                }else{
////                    setProductPrice()
////                }
//            }
//        })
//
//        mBillingProxy?.setPurchaseFinishedListener(object :BillingManager.OnPurchaseFinishedListener{
//            override fun onPurchaseSuccess(p0: MutableList<PurchaseItem>?) {
//                showPremiumView(true)
//                showVipView(false)
//            }
//
//            override fun onPurchaseError() {
//            }
//
//            override fun onPurchaseFail(p0: ResultItem?) {
//            }
//        })
//        mBillingProxy?.startConnect(this)
//    }
//    private fun showVipView(visible:Boolean = true) {
//        if( ConfigManager.firstEnterGpStyle == 0){
//            // 新样式效果较好,暂时固定新样式
////            ConfigManager.firstEnterGpStyle = (Random().nextInt(2) + 1)
//            ConfigManager.firstEnterGpStyle = Billing.STYLE_OTHER_TYLE
//        }
//        when(ConfigManager.firstEnterGpStyle){
//            Billing.STYLE_TYLE ->{
//                if(viewStubVipView == null){
//                    startConnectGp()
//                    viewStubVipView = findViewById<ViewStub>(R.id.viewstub_vip)
//                    viewStubVipView?.inflate()
//                    initView()
//                    initListener()
//                    showSpaceView()
//                    initPrivate()
//                }
//                if(visible){
//                    viewStubVipView?.visibility = View.VISIBLE
//                }else{
//                    viewStubVipView?.visibility = View.GONE
//                }
//            }
//            Billing.STYLE_OTHER_TYLE ->{
//                if(viewStubVipViewOther == null){
//                    startConnectGp()
//                    viewStubVipViewOther = findViewById<ViewStub>(R.id.viewstub_vip_other)
//                    viewStubVipViewOther?.inflate()
//                    initOtherView()
//                    initOtherListener()
//                    showSpaceOtherView()
//                    initPrivate()
//                }
//                if(visible){
//                    viewStubVipViewOther?.visibility = View.VISIBLE
//                }else{
//                    viewStubVipViewOther?.visibility = View.GONE
//                }
//            }
//        }
////        //StatisticsUtils.statisics("inapp_billing_ui", "activity", "${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
//
//        initMediaView()
//    }
//    private fun showSpaceOtherView() {
//        if(DeviceUtils.getScreenHeightDp() < 600){
//            findViewById<View>(R.id.v_space1).visibility = View.GONE
//            val layoutParams1 = findViewById<LinearLayout>(R.id.ll_bottom_privacy).layoutParams
//            if(layoutParams1 != null){
//                val marginLayoutParams =  layoutParams1 as ViewGroup.MarginLayoutParams
//                marginLayoutParams.topMargin = DeviceUtils.dip2px(6F)
//            }
//        }
//    }
//    private fun showSpaceView() {
//        if(DeviceUtils.getScreenHeightDp() < 600){
//            findViewById<View>(R.id.v_space1).visibility = View.GONE
//            val layoutParams1 = findViewById<FrameLayout>(R.id.ll_buy).layoutParams
//            if(layoutParams1 != null){
//                val marginLayoutParams =  layoutParams1 as ViewGroup.MarginLayoutParams
//                marginLayoutParams.topMargin = DeviceUtils.dip2px(20F)
//            }
//        }else{
//            val layoutParams1 = findViewById<LinearLayout>(R.id.ll_bottom_privacy).layoutParams
//            if(layoutParams1 != null){
//                val marginLayoutParams =  layoutParams1 as ViewGroup.MarginLayoutParams
//                marginLayoutParams.topMargin = DeviceUtils.dip2px(10F)
//            }
//        }
//    }
//    private val MESSAGE_ANIMAL_START = 1001
//    private var animalTime = 500L //动画的时间
//    private var delayTime = 2000L  //中间间隔延迟时间
//    private var turn = true   //反转
//    private var flag = true   //反转
//    private var radiusMax = 25     //最大模糊半径
//    private var radiusMin = 0      //最小模糊半径
//    private var scaleMax = 1f      //最大缩放
//    private var scaleMin = 0.7f    //最小缩放
//    private var scale = 1f         //真实缩放
//    private var radius = 1         //真实模糊半径
//    private var count = 0           //AnimatedValue更新次数
//    private var mValueAnimator: ValueAnimator? = null
//    private fun initView() {
//        llBanner1 = findViewById(R.id.ll_banner1)
//        ivBanner1 = findViewById(R.id.iv_banner1)
//        ivBanner2 = findViewById(R.id.iv_banner2)
//        llMonth = findViewById(R.id.ll_month)
//        llYear = findViewById(R.id.ll_year)
//        cbMonth = findViewById(R.id.cb_month)
//        btnContinueBuy = findViewById(R.id.btn_continue_buy)
//        cbYear = findViewById(R.id.cb_year)
//        titleMonth = findViewById(R.id.tv_month_title)
//        titleYear = findViewById(R.id.tv_year_title)
//        despMonth = findViewById(R.id.tv_month_desp)
//        despYear = findViewById(R.id.tv_year_desp)
//        mArrow = findViewById(R.id.iv_arrow)
//        llYear?.background = ContextCompat.getDrawable(this, R.drawable.premium_check_box_selected)
//        titleMonth?.text = getString(R.string.vip_month_tip,Billing.SUB_MONTH_PRICE);
//        despMonth?.text = getString(R.string.vip_month_tip2,Billing.SUB_MONTH_PRICE);
//        titleYear?.text = getString(R.string.vip_year_tip,Billing.SUB_YEAR_PRICE);
//        despYear?.text = getString(R.string.vip_year_tip2,Billing.SUB_YEAR_PRICE_SAVE);
//
//        premium_card_1 = ContextCompat.getDrawable(this, R.drawable.premium_card_2)
//        premium_card_2 = ContextCompat.getDrawable(this, R.drawable.premium_card_4)
//        premium_card_3 = ContextCompat.getDrawable(this, R.drawable.premium_card_3)
//        premium_card_4 = ContextCompat.getDrawable(this, R.drawable.premium_card_1)
//
//        ivBanner1?.setDrawable(premium_card_1)
//        ivBanner2?.setDrawable(premium_card_2)
//        temp1 = premium_card_1
//        temp2 = premium_card_2
//
//        if (ConfigManager.purchaseSubMonth) {
//            llMonth?.visibility = View.GONE
//            btnContinueBuy?.text = getString(R.string.premium_start_upgrade)
//            selectYearSubs = true
//            findViewById<View>(R.id.v_space2)?.visibility=View.VISIBLE
//            findViewById<View>(R.id.v_space)?.visibility=View.VISIBLE
//        } else {
//            llMonth?.visibility = View.VISIBLE
//            btnContinueBuy?.text = getString(R.string.premium_start_buy)
//            findViewById<View>(R.id.v_space2)?.visibility=View.GONE
//            findViewById<View>(R.id.v_space)?.visibility=View.GONE
//        }
//        initAnimal()
//        initOtherAnimal()
//    }
//    private fun initOtherView() {
//        mOtherLlMonth = findViewById(R.id.ll_other_month)
//        mOtherLlYear = findViewById(R.id.ll_other_year)
//        mArrow = findViewById(R.id.iv_arrow)
//        mYearTip = findViewById(R.id.tv_year_tip)
//        titleMonth = findViewById(R.id.tv_month_title)
//        titleYear = findViewById(R.id.tv_year_title)
//        despMonth = findViewById(R.id.tv_month_desp)
//        despYear = findViewById(R.id.tv_year_desp)
//
//        despYear?.text = getString(R.string.gp_year_desp, Billing.SUB_YEAR_PRE_MONTH_OTHER)
//        mYearTip?.text = getString(R.string.gp_bottom_tip, Billing.SUB_MONTH_PRICE)
//
//        if (DeviceUtils.SCREEN_WIDTH_PX == 1080 && DeviceUtils.SCREEN_HEIGHT_PX > 1920) {
//            val bottomSeparate = findViewById<View>(R.id.bottom_separate)
//            bottomSeparate?.visibility = View.VISIBLE
//        }
//
//        //续订UI逻辑
//        if(ConfigManager.purchaseSubYear){
//            mOtherLlYear?.visibility = View.GONE
//            val layoutParams = mOtherLlMonth?.layoutParams as? ViewGroup.MarginLayoutParams
//            layoutParams?.bottomMargin = (ResourcesUtils.getDimension(R.dimen.premium_btn_height) / DeviceUtils.SCREEN_DENSITY).toInt()
//            titleMonth?.text = getString(R.string.premium_start_renew)
//        }else if(ConfigManager.purchaseSubMonth && !ConfigManager.subMonthAutoRenew){
//            titleYear?.text = getString(R.string.premium_start_upgrade)
//            titleMonth?.text = getString(R.string.premium_start_renew)
//        }else if(showUpgrade()){
//            mOtherLlYear?.visibility = View.GONE
//            val layoutParams = mOtherLlMonth?.layoutParams as? ViewGroup.MarginLayoutParams
//            layoutParams?.bottomMargin = (ResourcesUtils.getDimension(R.dimen.premium_btn_height) / DeviceUtils.SCREEN_DENSITY).toInt()
//            titleMonth?.text =  getString(R.string.premium_start_upgrade)
//        }
//        checkBottomBtnProductId()
//        initOtherAnimal()
//    }
//
//    private fun checkBottomBtnProductId() {
////        if(currentMonthProductId == ""){
////            currentMonthProductId = Billing.SUB_MONTH
////        }
////        if(currentYearProductId == ""){
////            currentYearProductId = Billing.SUB_YEAR
////        }
////        currentDynamicProductId = if (showRenew()) {
////            val tempId = mBillingProxy?.getRenewSkuId()
////            if (ConfigManager.purchaseSubYear) {
////                tempId ?: Billing.getReplaceSku(currentYearProductId)
////            } else {
////                tempId ?:  Billing.getReplaceSku(currentMonthProductId)
////            }
////        } else if (showUpgrade()) {
////            Billing.getReplaceSku(currentYearProductId)
////        }else{
////            Billing.getReplaceSku(currentDynamicProductId)
////        }
//    }
//    private fun showRenew(): Boolean{
//        return ConfigManager.purchaseSubYear || ConfigManager.purchaseSubMonth && !ConfigManager.subMonthAutoRenew
//    }
//
//    private fun showUpgrade(): Boolean{
//        return !ConfigManager.subYearAutoRenew && ConfigManager.purchaseSubMonth && ConfigManager.subMonthAutoRenew
//    }
//
//    private var animalHandler: Handler? = null
//
//    private var scaleStart = 0f;
//    private var scaleEnd = 0f;
//    private fun initAnimal() {
//        animalHandler = object : Handler(mainLooper) {
//            override fun handleMessage(msg: Message?) {
//                when (msg?.what) {
//                    MESSAGE_ANIMAL_START -> mValueAnimator?.start()
//                }
//            }
//        }
//        scaleStart = scaleMax;
//        scaleEnd = scaleMin;
//        val valueAnimator = ValueAnimator.ofFloat(scaleMax, scaleMin)
//        mValueAnimator = valueAnimator
//        valueAnimator.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationRepeat(animation: Animator?) {
//            }
//
//            override fun onAnimationCancel(animation: Animator?) {
//                animation?.removeAllListeners()
//            }
//
//            override fun onAnimationEnd(animation: Animator?) {
//                mValueAnimator?.startDelay = 0
//                turn = if(turn){
//                    mValueAnimator?.setFloatValues(scaleMin,scaleMax)
//                    temp1 = if (temp1 == premium_card_1) {
//                        premium_card_3
//                    } else {
//                        premium_card_1
//                    }
//                    ivBanner1?.setDrawable(temp1)
//                    temp2 = if (temp2 == premium_card_2) {
//                        premium_card_4
//                    } else {
//                        premium_card_2
//                    }
//                    ivBanner2?.setDrawable(temp2)
//                    animalHandler?.sendEmptyMessageDelayed(MESSAGE_ANIMAL_START, 0)
//                    false
//                }else{
//                    mValueAnimator?.setFloatValues(scaleMax,scaleMin)
//                    ivBanner1?.setDrawable(temp1)
//                    ivBanner2?.setDrawable(temp2)
//                    animalHandler?.sendEmptyMessageDelayed(MESSAGE_ANIMAL_START, delayTime)
//                    true
//                }
//
//            }
//
//            override fun onAnimationStart(animation: Animator?) {
//            }
//        })
//        valueAnimator.addUpdateListener { animation ->
//            val  scale = animation.animatedValue as Float
//            val round = Math.round((scale - scaleMin) * (animalTime / (scaleMax - scaleMin)) / (animalTime / (radiusMax - radiusMin)))
//            radius = radiusMax - radiusMin + 2 - round + 1
//            val alpha = ( 255 - round * 10)
//            ivBanner1?.setBlurAlpha(alpha)
//            ivBanner2?.setBlurAlpha(alpha)
//            ivBanner1?.scaleX = scale
//            ivBanner1?.scaleY = scale
//            ivBanner2?.scaleX = scale
//            ivBanner2?.scaleY = scale
//            llBanner1?.alpha = scale
//        }
//        valueAnimator.duration = animalTime
//        valueAnimator.startDelay = delayTime
//        valueAnimator.start()
//    }
//    private fun initOtherAnimal() {
//        val objectAnimator = ObjectAnimator.ofFloat(mArrow, "translationX", 0f, 15f, 0f, -15f, 0f)
//        objectAnimator.duration = 800
//        objectAnimator.interpolator = LinearInterpolator()
//        objectAnimator.start()
//        objectAnimator.repeatCount = ValueAnimator.INFINITE
//    }
//    private fun initListener() {
//        llMonth?.setOnClickListener(this)
//        llYear?.setOnClickListener(this)
//        cbMonth?.setOnClickListener(this)
//        cbYear?.setOnClickListener(this)
//        (findViewById<View>(R.id.iv_close) as ImageView).setOnClickListener(this)
//        btnContinueBuy?.setOnClickListener(this)
//    }
//    private fun initOtherListener() {
//        mOtherLlMonth?.setOnClickListener(this)
//        mOtherLlYear?.setOnClickListener(this)
//        (findViewById<View>(R.id.iv_close) as ImageView).setOnClickListener(this)
//    }
//
//    override fun onClick(v: View) {
//        when (v.id) {
//            R.id.iv_close -> {
//                //StatisticsUtils.statisics("inapp_billing_ui", "close", "${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
//                if (!checkNeedShowRetainDialog()) {
//                    finish()
//                }
//            }
//            R.id.ll_other_month ->{
////                mOtherLlMonth?.background = ContextCompat.getDrawable(this,R.drawable.shape_round_premium_other_select_bg)
////                mOtherLlYear?.background = ContextCompat.getDrawable(this,R.drawable.shape_round_premium_other_normal_bg)
////                val arrowDrawable = ContextCompat.getDrawable(this, R.drawable.premium_icon_03)
////                arrowDrawable.setColorFilter(ContextCompat.getColor(this, R.color.premium_other_normal_arrow_color), PorterDuff.Mode.SRC_IN)
////                mArrow?.setImageDrawable(arrowDrawable)
////                titleMonth?.setTextColor(ContextCompat.getColor(this, R.color.white))
////                titleMonth?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
////                despMonth?.setTextColor(ContextCompat.getColor(this, R.color.white))
//
////                titleYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_other_normal_title_gray))
////                titleYear?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
////                despYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_other_normal_desp_gray))
//                checkBottomBtnProductId()
//                mBillingProxy?.purchase(this,currentDynamicProductId,"ui-${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
//                //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", currentDynamicProductId)
//                //StatisticsUtils.statisics("inapp_billing", "click", "month")
//            }
//            R.id.ll_other_year ->{
////                mOtherLlYear?.background = ContextCompat.getDrawable(this,R.drawable.shape_round_premium_other_select_bg)
////                mOtherLlMonth?.background = ContextCompat.getDrawable(this,R.drawable.shape_round_premium_other_normal_bg)
////                val arrowDrawable = ContextCompat.getDrawable(this, R.drawable.premium_icon_03)
////                arrowDrawable.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
////                mArrow?.setImageDrawable(arrowDrawable)
////                titleYear?.setTextColor(ContextCompat.getColor(this, R.color.white))
////                titleYear?.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
////                despYear?.setTextColor(ContextCompat.getColor(this, R.color.white))
//
////                titleMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_other_normal_title_gray))
////                titleMonth?.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
////                despMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_other_normal_desp_gray))
//                if(currentYearProductId == ""){
//                    currentYearProductId = Billing.SUB_YEAR
//                }
////                currentYearProductId = Billing.getReplaceSku(currentYearProductId)
////                mBillingProxy?.purchase(this,currentYearProductId,"ui-${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
////                //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", currentYearProductId)
////                //StatisticsUtils.statisics("inapp_billing", "click", "year")
//            }
//            R.id.ll_month -> {
//                //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", "select-"+currentMonthProductId)
//                //StatisticsUtils.statisics("inapp_billing", "click", "month")
//                selectYearSubs = false
//                cbMonth?.isChecked = true
//                cbYear?.isChecked = false
//                titleMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_check_title_gray))
//                titleMonth?.textSize = 20f
//                despMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_check_desp_gray))
//                despMonth?.textSize = 10F
//                titleYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_normal_title_gray))
//                titleYear?.textSize = 14F
//                despYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_normal_desp_gray))
//                despYear?.textSize = 10F
//                llMonth?.background = ContextCompat.getDrawable(this, R.drawable.premium_check_box_selected)
//                llYear?.background = ContextCompat.getDrawable(this, R.drawable.premium_check_box_un_selected)
//            }
//            R.id.ll_year -> {
//                //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}",  "select-"+currentYearProductId)
//                //StatisticsUtils.statisics("inapp_billing", "click", "year")
//                selectYearSubs = true
//                cbYear?.isChecked = true
//                cbMonth?.isChecked = false
//                titleYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_check_title_gray))
//                titleYear?.textSize = 20F
//                despYear?.setTextColor(ContextCompat.getColor(this, R.color.premium_check_desp_gray))
//                despYear?.textSize = 10F
//                titleMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_normal_title_gray))
//                titleMonth?.textSize = 14F
//                despMonth?.setTextColor(ContextCompat.getColor(this, R.color.premium_normal_desp_gray))
//                despMonth?.textSize = 10F
//                llMonth?.background = ContextCompat.getDrawable(this, R.drawable.premium_check_box_un_selected)
//                llYear?.background = ContextCompat.getDrawable(this, R.drawable.premium_check_box_selected)
//            }
//            R.id.cb_month -> {
//                llMonth?.performClick()
//            }
//            R.id.cb_year -> {
//                llYear?.performClick()
//            }
//            R.id.btn_continue_buy -> {
////                if(currentYearProductId == ""){
////                    currentYearProductId = Billing.SUB_YEAR
////                }
////                currentYearProductId = Billing.getReplaceSku(currentYearProductId)
////                if(currentMonthProductId == ""){
////                    currentMonthProductId = Billing.SUB_MONTH
////                }
////                currentMonthProductId = Billing.getReplaceSku(currentMonthProductId)
////                if (selectYearSubs) {
////                    //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", "btn-$currentYearProductId")
////                    mBillingProxy?.purchase(this, currentYearProductId,"ui-${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
////                } else {
////                    //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", "btn-$currentMonthProductId")
////                    mBillingProxy?.purchase(this, currentMonthProductId,"ui-${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
////                }
//            }
//        }
//    }
//
//    /**
//     * 检查是否需要弹挽留窗口
//     * 非购买情况下，每隔24小时，会再次展示挽留弹窗
//     */
//    private fun checkNeedShowRetainDialog(): Boolean {
////        if(Billing.alreadySubscribe()){
////            return false
////        }
////        val currentTimeMillis = System.currentTimeMillis()
////        if((currentTimeMillis - ConfigManager.firstEnterGpRetainTime) > TimeUtils.MILLIS_IN_DAY){
////            ConfigManager.firstEnterGpRetainTime = currentTimeMillis
////            val retainDialog = RetainDialog2(this)
////            retainDialog.onRetainListener = object : RetainDialog2.OnRetainListener{
////                override fun onSureClick() {
////                    //StatisticsUtils.statisics("inapp_billing_ui", "a${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}", "rd-btn-$currentYearProductId")
////                    ConfigManager.retainDialogFinishSubsOrNot = true
////                    currentYearProductId = Billing.getReplaceSku(currentYearProductId)
////                    mBillingProxy?.purchase(this@BillingActivity, currentYearProductId,"ui-${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
////                }
////
////                override fun onQuitClick() {
////                    finish()
////                }
////            }
////            retainDialog.show()
////            return true
////        }
//        return false
//    }
//
//    override fun onDestroy() {
//        mediaView?.release()
//        mBillingProxy?.endConnect()
//        mCallback?.onBillingPageClosed()
//        clearAnimal()
//        super.onDestroy()
//    }
//
//    override fun finish() {
//        super.finish()
//
//        mResultActivityClass?.also {
//            val intent = Intent(this, it)
//            this.startActivity(intent)
//        }
//
//    }
//
//    override fun onBackPressed() {
//        //StatisticsUtils.statisics("inapp_billing_ui", "close_back", "${ConfigManager.firstEnterGpStyle}-${mCurrentBillingBannerUseMedia}")
//        if (!checkNeedShowRetainDialog()) {
//            super.onBackPressed()
//        }
//    }
//
//    private fun clearAnimal() {
//        mValueAnimator?.cancel()
//        mValueAnimator = null
//        banner1Set?.cancel()
//        banner2Set?.cancel()
//        llBanner1?.clearAnimation()
//        animalHandler?.removeCallbacksAndMessages(null);
//    }
//
//    public interface Callback{
//        fun onBillingPageClosed()
//    }
//}
