//package com.videoeditor.function.billing
//
//import android.util.Log
//import com.android.absbase.App
//import com.android.absbase.utils.ArrayUtils
//import com.android.absbase.utils.ResourcesUtils
//import com.android.absbase.utils.TimeUtils
//import com.android.billing.compat.BillingManager
//import com.android.billing.compat.notice.mode.DiscountProduct
//import com.rice.balls.strategy.StrategyConstant
//import com.rice.balls.strategy.StrategyManager
//import com.allever.video.editor.ConfigManager
//import com.allever.video.editor.R
//import com.allever.video.editor.function.online.OnlineConstant
//import com.allever.video.editor.ui.BillingActivity
//import java.util.HashMap
//
//object Billing : StrategyManager.UpdateStrategyListener {
//    const val STYLE_TYLE = 1
//    const val STYLE_OTHER_TYLE = 2
//
//    const val BASE_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlKqFzsRZr4AAcAVTfvibyulFKhRzDKdSxSemTHDbCxKjk98T6L1Wh3K0UiNtgBwEMApZxVB1B2AwRbe4p6OVB/IatdSTSkxWTRLoLKhxeoWvZlX0esUpFQWCRSZtHNuku6LoQI48WXhcTl4/H+oR08iNq9qBnDFPwIw6hPLi+51v8eymg9vkOCmGCFeKDkHvyuMAlTLYVD5WVkTMnUBDpE06LreWogWLbLwt78tlmntPMyL5ywWBHv1IU2Tx7ct5BpAMsK2fCuO9FCBvCLMNYzNrEmJvZkh73xspeRzIzHuSeCjjxRqYOKcc4ehFm0qgPZ6TFC5LasPHSXWr24qH9wIDAQAB"
//    const val SUB_YEAR = "com.year.videoeditor"
//    const val SUB_YEAR_SEVEN_PERCENT = "com.year.videoeditor30off"
//    const val SUB_YEAR_EIGHT_PERCENT = "com.year.videoeditor20off"
//    const val SUB_MONTH = "com.month.videoeditor"
//    const val SUB_MONTH_SEVEN_PERCENT = "com.month.videoeditor30off"
//    const val SUB_MONTH_EIGHT_PERCENT = "com.month.videoeditor20off"
////    const val SUB_PRE_MONTH = "com.month.scanner9.9"
////    const val SUB_PRE_YEAR = "com.year.scanner4.9"
//    /**
//     *
//    7折
//    月费：com.month.scanner30off
//    年费：com.year.scanner30off
//    8折
//    月费：com.month.scanner20off
//    年费：com.year.scanner20off
//     */
//
//    //默认显示的金额
//    val SUB_MONTH_PRICE = "$25.99"
//    val SUB_YEAR_PRICE = "$6.99"
//    val SUB_YEAR_PRICE_SAVE = "$79.99"
//    val SUB_YEAR_OTHER = "$95.99"
//    val SUB_YEAR_PRE_MONTH_OTHER = "$7.99"
//
//
//    var switch: Boolean = true
//        private set
//    var style = STYLE_OTHER_TYLE
//        private set
//    var bannerImageUrl = ""
//        private set
//    var bannerVideoUrl = OnlineConstant.getUrl("ev/qr-video1.mp4")
//        private set
////    var skusConfig = hashMapOf<String, BillingStrategyBean.SkuConfig>()
////        private set
//    var bottomDesc = ""
//        private set
//
//    private var subsSkus = arrayOf(
//            SUB_YEAR,
//            SUB_MONTH,
//            SUB_YEAR_EIGHT_PERCENT,
//            SUB_YEAR_SEVEN_PERCENT,
//            SUB_MONTH_EIGHT_PERCENT,
//            SUB_MONTH_SEVEN_PERCENT
////            SUB_PRE_MONTH,
////            SUB_PRE_YEAR
//    )
//
//    private val sky2DiscountProductByYear = arrayOf(
//            DiscountProduct(SUB_YEAR, SUB_YEAR,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip) + String(Character.toChars(0x1F62D)),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_day_tip),
//                    100,
//                    TimeUtils.MILLIS_IN_DAY * 5),
//            DiscountProduct(SUB_YEAR_EIGHT_PERCENT, SUB_YEAR,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip) + String(Character.toChars(0x1F62D)),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_discount_eighty_percent_tip),
//                    80,
//                    TimeUtils.MILLIS_IN_DAY * 2),
//            DiscountProduct(SUB_YEAR_SEVEN_PERCENT, SUB_YEAR,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip2),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_discount_seventy_percent_tip) + "${String(Character.toChars(0x1F4A9))}!",
//                    70,
//                    TimeUtils.MILLIS_IN_DAY * 1)
//    )
//
//    private val sky2DiscountProductBMonth = arrayOf(
//            DiscountProduct(SUB_MONTH, SUB_MONTH,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip) + String(Character.toChars(0x1F62D)),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_day_tip),
//                    100,
//                    TimeUtils.MILLIS_IN_DAY * 5),
//            DiscountProduct(SUB_MONTH_EIGHT_PERCENT, SUB_MONTH,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip) + String(Character.toChars(0x1F62D)),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_discount_eighty_percent_tip),
//                    80,
//                    TimeUtils.MILLIS_IN_DAY * 2),
//            DiscountProduct(SUB_MONTH_SEVEN_PERCENT, SUB_MONTH,
//                    ResourcesUtils.getString(R.string.grace_period_expiring_tip2),
//                    ResourcesUtils.getString(R.string.grace_period_expiring_discount_seventy_percent_tip) + "${String(Character.toChars(0x1F4A9))}!",
//                    70,
//                    TimeUtils.MILLIS_IN_DAY * 1)
//    )
//
//    init {
////        StrategyManager.instance.registerStrategy(StrategyConstant.FUN_ID_BILLING, this)
//    }
//
//    fun init() {
////        BillingManager.setBase64EncodedPublicKey(BASE_PUBLIC_KEY)
////        BillingManager.setSubsSKUS(subsSkus.asList())
////
////        //设置折扣商品
////        val map = HashMap<String, List<DiscountProduct>>()
////        map[SUB_YEAR] = sky2DiscountProductByYear.asList()
////        map[SUB_MONTH] = sky2DiscountProductBMonth.asList()
////
////        BillingManager.setDiscountProductMap(map)
////
////        ConfigManager.firstEnterGpStyle = STYLE_OTHER_TYLE
////
////        updateStrategy()
////
////        if (App.isMainProcess()) {
////            BillingActivity.reloadBannerVideo(bannerVideoUrl)
////            BillingActivity.reloadBannerImage(bannerImageUrl)
////        }
//    }
//
//    override fun updateStrategy() {
////        val strategyConfig = StrategyManager.instance.getStrategyFirstConfig(StrategyConstant.FUN_ID_BILLING, BillingStrategyBean::class.java)
////                ?: return
////        if (strategyConfig.bannerImageUrl.isNotEmpty()) {
////            bannerImageUrl = strategyConfig.bannerImageUrl
////        }
////        if (strategyConfig.bannerVideoUrl.isNotEmpty()) {
////            bannerVideoUrl = strategyConfig.bannerVideoUrl
////        }
////        if (strategyConfig.bottomDesc.isNotEmpty()) {
////            bottomDesc = strategyConfig.bottomDesc
////        }
////        if (strategyConfig.style != -1) {
////            style = strategyConfig.style
////        }
////        switch = strategyConfig.isSwitch()
////
////        ConfigManager.firstEnterGpStyle = style
////
////        val skusConfig = strategyConfig.skus
////        this.skusConfig = skusConfig
////
////        val replaceFun = { sku: String,
////                           skuConfig: BillingStrategyBean.SkuConfig,
////                           pds: Array<DiscountProduct> ->
////            for (dp in pds) {
////                if (dp.productId.equals(sku, ignoreCase = true)) {
////                    if (skuConfig.replaceSku.isNotEmpty()) {
////                        dp.productId = skuConfig.replaceSku
////                    }
////                    if (skuConfig.notifyTitle.isNotEmpty()) {
////                        dp.discountTitle = skuConfig.notifyTitle
////                    }
////                    if (skuConfig.notifyDesc.isNotEmpty()) {
////                        dp.discountDescript = skuConfig.notifyDesc
////                    }
////                }
////            }
////        }
////        val newSubsSkus = mutableListOf<String>()
////        for ((sku, config) in skusConfig) {
////            replaceFun(sku, config, sky2DiscountProductByYear)
////            replaceFun(sku, config, sky2DiscountProductBMonth)
////
////            val replaceSku = config.replaceSku
////            if (replaceSku.isNotEmpty()) {
////                newSubsSkus.add(replaceSku)
////            }
////        }
////
////        if (newSubsSkus.isNotEmpty()) {
////            val subsSkus = hashSetOf<String>()
////            subsSkus.addAll(this.subsSkus)
////            subsSkus.addAll(newSubsSkus)
////            this.subsSkus = subsSkus.toTypedArray()
////            BillingManager.setSubsSKUS(this.subsSkus.asList())
////        }
//    }
//
////    fun getSkuConfig(sku: String): BillingStrategyBean.SkuConfig? {
////        val skuConfig = this.skusConfig[sku]
////        return skuConfig
////    }
////
////    fun getReplaceSku(sku: String): String {
////        val skuConfig = getSkuConfig(sku)
////        return if (skuConfig != null && skuConfig.replaceSku.isNotEmpty()) {
////            skuConfig.replaceSku
////        } else sku
////    }
//
//    fun alreadySubscribe(): Boolean {
//        return ConfigManager.purchaseSubSize > 0
//    }
//
//    fun getString(str: String?): String? {
//        return null
////        val str = str ?: return str
////        return if (str.startsWith(OnlineMultiLangManger.KEY_PREFIX)) {
////            OnlineMultiLangManger.getString(str.substring(OnlineMultiLangManger.KEY_PREFIX.length))
////        } else str
//    }
//
//}