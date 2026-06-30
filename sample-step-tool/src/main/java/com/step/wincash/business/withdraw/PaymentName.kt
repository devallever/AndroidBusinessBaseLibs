package com.step.wincash.business.withdraw

import androidx.annotation.StringDef

@StringDef(
    PaymentName.PAGBANK,
    PaymentName.PIX,
    PaymentName.PICPAY,
    PaymentName.NUBANK,
    PaymentName.PAYPAL,
    PaymentName.VISA,
    PaymentName.MASTERCARD,
    PaymentName.CASHAPP,
    PaymentName.LAZADA,
    PaymentName.GCASH,
    PaymentName.GRABPAY,
    PaymentName.TRUEMONEY,
    PaymentName.RABBITLINEPAY,
    PaymentName.MPAY,
    PaymentName.DANA,
    PaymentName.SHOPEEPAY,
    PaymentName.OVO,
    PaymentName.PAPARA,
    PaymentName.FASTPAY,
    PaymentName.ZALOPAY,
    PaymentName.MOMO,
    PaymentName.BankCard,
    PaymentName.Clipspay,
    PaymentName.KAKAOPAY,
    PaymentName.PAYCO,
    PaymentName.NAVERPAY,
    PaymentName.BKASH,
    PaymentName.SURECASH,
    PaymentName.EASYPAISA,
    PaymentName.JAZZCASH,
    PaymentName.PhoneFee
)
@Retention(AnnotationRetention.SOURCE)
annotation class PaymentName {
    companion object {
        //巴西 BR
        const val PAGBANK: String = "PagBank"
        const val PIX: String = "PIX"
        const val PICPAY: String = "Picpay"
        const val NUBANK: String = "Nubank"

        //美国 US
        const val PAYPAL: String = "Paypal"
        const val VISA: String = "VISA"
        const val MASTERCARD: String = "MASTERCARD"
        const val CASHAPP: String = "CashApp"

        //菲律宾 PH
        const val LAZADA: String = "Lazada"
        const val GCASH: String = "Gcash"
        const val GRABPAY: String = "Grabpay"

        //泰国  TH
        const val TRUEMONEY: String = "TrueMoney"
        const val RABBITLINEPAY: String = "RabibitLinePay"
        const val MPAY: String = "MPay"

        //印尼 ID
        const val DANA: String = "DANA"
        const val SHOPEEPAY: String = "ShopeePay"
        const val OVO: String = "OVO"

        //土耳其 TR
        const val PAPARA: String = "Papara"
        const val FASTPAY: String = "Fastpay"

        //越南 VN
        const val ZALOPAY: String = "Zalopay"
        const val MOMO: String = "Momo"

        //韩国 KR
        const val BankCard: String = "Bank Card"
        const val Clipspay: String = "Clipspay"
        const val KAKAOPAY: String = "KakaoPay"
        const val PAYCO: String = "Payco"
        const val NAVERPAY: String = "NaverPay"

        //孟加拉国 BD
        const val BKASH: String = "BKash"
        //sureCash
        const val SURECASH: String = "sureCash"

        //巴基斯坦 PK
        const val EASYPAISA: String = "Easypaisa"
        const val JAZZCASH: String = "JazzCash"

        //尼日利亚、乌兹别克斯坦、南非、孟加拉
        const val PhoneFee: String = "Phone Fee"

    }
}
