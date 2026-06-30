package com.step.wincash.business.withdraw

import androidx.annotation.StringDef

@StringDef(
    PaymentUrl.PAGBANK_URL,
    PaymentUrl.PAYPAL_URL,
    PaymentUrl.LAZADA_URL,
    PaymentUrl.TRUEMONEY_URL,
    PaymentUrl.DANA_URL,
    PaymentUrl.SHOPEEPAY_URL,
    PaymentUrl.PAPARA_URL,
    PaymentUrl.NO_URL,
    PaymentUrl.ZALOPAY_URL,
    PaymentUrl.BKASH_URL,
    PaymentUrl.EASYPAISA_URL
)
@Retention(AnnotationRetention.SOURCE)
annotation class PaymentUrl {
    companion object {
        //巴西
        const val PAGBANK_URL: String = "https://investors.pagbank.com/"

        //美国
        const val PAYPAL_URL: String = "https://www.paypal.com"

        //菲律宾
        const val LAZADA_URL: String = "https://www.lazada.com.ph/"

        //泰国
        const val TRUEMONEY_URL: String = "https://www.truemoney.com"

        //印尼
        const val DANA_URL: String = "https://www.dana.id/"
        const val SHOPEEPAY_URL: String = "https://shopee.co.id/"

        //土耳其
        const val PAPARA_URL: String = "https://www.papara.com"

        //越南
        const val ZALOPAY_URL: String = "https://zalopay.vn/"

        const val BKASH_URL: String = "https://www.bkash.com"
        const val EASYPAISA_URL: String = "https://easypaisa.com.pk/"

        const val NO_URL: String = ""
    }
}
