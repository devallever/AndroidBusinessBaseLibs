//package com.videoeditor.function.billing
//
//import android.app.Activity
//import android.content.Context
//import com.android.absbase.App
//import com.android.absbase.utils.ToastUtils
//import com.android.billing.compat.BillingManager
//import com.android.billing.compat.bean.PurchaseItem
//import com.android.billing.compat.bean.ResultItem
//import com.android.billing.compat.bean.SkuDetailsItem
//import com.android.billing.compat.notice.mode.DiscountProduct
//import com.statistics.StatisticsUtils
//import com.allever.video.editor.ConfigManager
//import com.allever.video.editor.R
//
//class BillingProxy{
//
//    private var mBillingManager = BillingManager()
//    //当前商品id
//    private var mCurrentSkuId: String? = null
//    //当前购买场景
//    private var mCurrentPurchaseScene:String? = null
//
//    //当前已经购买的商品
//    private var purchaseList: MutableList<PurchaseItem>? = null
//
//    private var needShowError: Boolean = true
//
//
//    private val internalPurchaseFinishedListener = object : BillingManager.OnPurchaseFinishedListener {
//        override fun onPurchaseSuccess(list: List<PurchaseItem>?) {
//            val purchaseItem = list?.get(0)
//            val skuId = purchaseItem?.productId ?: mCurrentSkuId ?: "null"
////            //StatisticsUtils.statisics("inapp_billing_proxy", "finish", "success")
////            //StatisticsUtils.statisics("inapp_billing_ui",  "finish-success","$skuId-${ConfigManager.retainDialogFinishSubsOrNot}-${mCurrentPurchaseScene ?: "null"}")
//            if (list != null) {
//                ToastUtils.show(App.getContext().getString(R.string.purchase_state))
//            }
//            ConfigManager.purchaseSubSize = mBillingManager.purchasesSizeSubs
//            onPurchaseFinishedListener?.onPurchaseSuccess(list)
//            ConfigManager.retainDialogFinishSubsOrNot = false
//            purchaseItem?.let {
//                val discountParentId = mBillingManager.getDiscountParentId(purchaseItem.productId) ?: purchaseItem.productId
//                when (discountParentId) {
//                    Billing.SUB_MONTH /*, Billing.SUB_PRE_MONTH*/ -> {
//                        ConfigManager.purchaseSubMonth = true
//                        ConfigManager.subMonthAutoRenew = it.autoRenewing
//                    }
//                    Billing.SUB_YEAR /*, Billing.SUB_PRE_YEAR */-> {
//                        ConfigManager.purchaseSubYear = true
//                        ConfigManager.subYearAutoRenew = it.autoRenewing
//                    }
//                }
//            }
//        }
//
//        override fun onPurchaseFail(resultItem: ResultItem) {
////            //StatisticsUtils.statisics("inapp_billing_proxy", "finish", "fail", "finisherror", "${resultItem.msg}(${resultItem.code})")
////            //StatisticsUtils.statisics("inapp_billing_ui",  "finish-fail","${mCurrentSkuId ?: "null"}-${ConfigManager.retainDialogFinishSubsOrNot}-${mCurrentPurchaseScene ?: "null"}")
//            if (resultItem.code != ResultItem.Code.USER_CANCELED && needShowError) {
//                ToastUtils.show(resultItem.msg)
//            }
//            onPurchaseFinishedListener?.onPurchaseFail(resultItem)
//            ConfigManager.retainDialogFinishSubsOrNot = false
//        }
//
//        override fun onPurchaseError() {
////            //StatisticsUtils.statisics("inapp_billing_proxy", "finish", "error")
////            //StatisticsUtils.statisics("inapp_billing_ui",  "finish-error","${mCurrentSkuId ?: "null"}-${ConfigManager.retainDialogFinishSubsOrNot}-${mCurrentPurchaseScene ?: "null"}")
//
//            onPurchaseFinishedListener?.onPurchaseError()
//            ConfigManager.retainDialogFinishSubsOrNot = false
//        }
//    }
//
//
//
//    var onPurchaseFinishedListener: BillingManager.OnPurchaseFinishedListener? = null
//    var onStartSetupFinishedListener: BillingManager.OnStartSetupFinishedListener? = null
//    var onQueryIsPurchaseSubsListener: BillingManager.OnQueryIsPurchaseSubsListener? = null
//    var mRenewStateListener: BillingManager.RenewStateListener? = null
//
//    init {
//        //Gp
//        mBillingManager.setOnStartSetupFinishedListener(object : BillingManager.OnStartSetupFinishedListener {
//            override fun onSetupError(resultCode: Int) {
////                //StatisticsUtils.statisics("inapp_billing_proxy", "setup", "$resultCode")
//                onStartSetupFinishedListener?.onSetupError(resultCode)
//            }
//
//            override fun onSetupFail(resultItem: ResultItem?) {
//                val info = resultItem?.msg ?: "unknown"
////                //StatisticsUtils.statisics("inapp_billing_proxy", "setupfail", info)
//                if (resultItem?.code != ResultItem.Code.USER_CANCELED && needShowError) {
//                    ToastUtils.show(info)
//                }
//                onStartSetupFinishedListener?.onSetupFail(resultItem)
//            }
//
//            override fun onSetupSuccess() {
//                //StatisticsUtils.statisics("inapp_billing_proxy", "setup", "success")
//                mBillingManager.checkPurchaseSub()
//                onStartSetupFinishedListener?.onSetupSuccess()
//            }
//        })
//
//        mBillingManager.setOnQueryIsPurchaseSubsListener(object : BillingManager.OnQueryIsPurchaseSubsListener {
//            override fun purchaseCallBack(size: Int, p1: MutableList<PurchaseItem>?) {
//                purchaseList = p1
//                ConfigManager.purchaseSubSize = size
//                val pIds = mutableListOf<String>()
//                p1?.map {
//                    val discountParentId = mBillingManager.getDiscountParentId(it.productId) ?: it.productId
//                    when (discountParentId) {
//                        Billing.SUB_MONTH /*, Billing.SUB_PRE_MONTH*/ -> {
//                            ConfigManager.purchaseSubMonth = true
//                            ConfigManager.subMonthAutoRenew = it.autoRenewing
//                        }
//                        Billing.SUB_YEAR /*, Billing.SUB_PRE_YEAR*/ -> {
//                            ConfigManager.purchaseSubYear = true
//                            ConfigManager.subYearAutoRenew = it.autoRenewing
//                        }
//                    }
//                    pIds.add(it.productId)
//                }
//                if (size <= 0) {
//                    ConfigManager.purchaseSubYear = false
//                    ConfigManager.purchaseSubMonth = false
//                    ConfigManager.subMonthAutoRenew = false
//                    ConfigManager.subYearAutoRenew = false
//                }
//                onQueryIsPurchaseSubsListener?.purchaseCallBack(size,p1)
//
//                val stasInfo = mutableListOf("query", "callback")
//                for (pId in pIds) {
//                    stasInfo.add("queryPid")
//                    stasInfo.add(pId)
//                }
////                //StatisticsUtils.statisics("inapp_billing_proxy", *stasInfo.toTypedArray())
//            }
//
//            override fun error(resultItem: ResultItem?) {
//                val info = resultItem?.msg ?: "unknown"
////                //StatisticsUtils.statisics("inapp_billing_proxy", "query", info)
//                if(needShowError){
//                    ToastUtils.show(info)
//                }
//                onQueryIsPurchaseSubsListener?.error(resultItem)
//            }
//        })
//
//        mBillingManager.setOnQueryPurchaseHistoryListener(object :BillingManager.OnQueryPurchaseHistoryListener{
//            override fun onPurchaseHistorySuccess(p0: String?, p1: MutableList<PurchaseItem>?, p2: MutableList<PurchaseItem>?) {
//            }
//
//            override fun onPurchaseHistoryFailed(p0: ResultItem?) {
//
//            }
//        })
//        //折扣商品统计
//        mBillingManager.setDiscountProductStatisticsListener { object :BillingManager.DiscountProductStatisticsListener{
//            override fun callbackDiscount(p0: DiscountProduct?) {
//                if(p0 != null){
//                    //StatisticsUtils.statisics("inapp_billing_proxy_discountProduct", "discountProduct","productId:${p0.productId},parentId:${p0.parentId}")
//                }
//            }
//        } }
//    }
//
//    /**
//     * 获取购买的商品id
//     * 年优先级高
//     * 如果先购买了月，月改了周，那么续订还是月
//     * 如果没有购买，月改了周，再购买周，那么续订是周
//     */
//    fun getRenewSkuId(): String?{
//        var purchaseId: String? = null
//        val size = purchaseList?.size ?: 0
//        for(i in 0 until size){
//            val item = purchaseList?.get(i) ?: continue
//            val discountParentId = getSkuId(item.productId) ?: item.productId
//            if(discountParentId == Billing.SUB_YEAR){
//                purchaseId = item.productId
//                break
//            }else if(discountParentId == Billing.SUB_MONTH){
//                purchaseId = item.productId
//            }
//        }
//        return purchaseId
//    }
//
//
//    fun setPurchaseFinishedListener(listener:BillingManager.OnPurchaseFinishedListener){
//        onPurchaseFinishedListener = listener
//    }
//
//    fun setOnStartSetupListener(listener:BillingManager.OnStartSetupFinishedListener){
//        onStartSetupFinishedListener = listener
//    }
//    fun setRenewStateListener(listener:BillingManager.RenewStateListener){
//        mRenewStateListener = listener
//    }
//    fun checkPurchaseSub(){
//        mCurrentPurchaseScene = "check_dialog"
//        mBillingManager.setOnPurchaseFinishedListener(internalPurchaseFinishedListener)
//        mBillingManager.checkPurchaseSub()
//    }
//
//    fun purchase(activity: Activity, sku: String , scene:String){
//        mCurrentSkuId = sku
//        mCurrentPurchaseScene = scene
//
//        //购买接口
//        mBillingManager.purchaseSubs(activity, sku)
//    }
//    fun querySubsSkuDetails( onQueryFinishedListener:BillingManager.OnQueryFinishedListener){
//        mBillingManager.setOnQueryFinishedListener(onQueryFinishedListener)
//        mBillingManager.queryInventorySubs()
//    }
//    fun startConnect(context: Context) {
//        needShowError = true
//        mBillingManager.initial(context)
//    }
//
//    fun startConnect(context: Context, needShowErrorTip:Boolean) {
//        needShowError = needShowErrorTip
//        mBillingManager.initial(context,needShowErrorTip)
//    }
//    fun endConnect() {
//        mBillingManager.cleanListener()
//        mBillingManager.endConnection()
//    }
//
//    /**
//     * 根据折扣商品id 获取父id
//     */
//    fun getSkuId(childId:String):String?{
//        return mBillingManager.getDiscountParentId(childId)
//    }
//
//    /**
//     * 根据SkuId获取对应的商品详情
//     */
//    fun getSkuDetails(sku: String?): SkuDetailsItem?{
//        return null
////        val skuDetailsFromStorage = mBillingManager.getInAppPositionBySku(sku)
////        return skuDetailsFromStorage.find { it.productId == sku }
//    }
//
//    fun getDiscountSkuDetails():List<SkuDetailsItem>{
//        return mBillingManager.currentDiscountProductDetails
//    }
//    fun getHitstorySkus():List<String>{
//        return mBillingManager.cachedSubscriptionSku
//    }
//
//    fun getSkuDetailsItem(productId: String): SkuDetailsItem? {
//        return mBillingManager.hashMapSku[productId]
//    }
//    companion object {
//
//        fun getProxy( ): BillingProxy {
//            return BillingProxy()
//        }
//    }
//}
