package com.example.charge.withdraw

import android.text.TextUtils
import com.example.charge.R

// 钱包管理器
object WalletManager {

    // 通过支付方式获取图标、跳转方式等
    private val paramsMap = mutableMapOf<String, PaymentParams>()

    fun getPaymentParamsList(countryCode: String): List<PaymentParams> {
        val paymentParams = mutableListOf<PaymentParams>()

        return when {
            //巴西 BR
            TextUtils.equals(CountryUtil.BR, countryCode) -> {
                paymentParams.add(findPaymentParams(PaymentName.PIX, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PAGBANK, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PAYPAL, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.VISA, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.MASTERCARD, countryCode))
                paymentParams
            }
//            //菲律宾 PH
//            TextUtils.equals(CountryUtil.PH, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.LAZADA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.GCASH, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.GRABPAY, countryCode))
//                paymentParams
//            }
//            //印尼 ID
//            TextUtils.equals(CountryUtil.ID, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.DANA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.SHOPEEPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.OVO, countryCode))
//                paymentParams
//            }
//            //泰国 TH
//            TextUtils.equals(CountryUtil.TH, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.TRUEMONEY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.RABBITLINEPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.MPAY, countryCode))
//                paymentParams
//            }
//            //土耳其 TR
//            TextUtils.equals(CountryUtil.TR, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.PAPARA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.FASTPAY, countryCode))
//                paymentParams
//            }
            //美国 US
            TextUtils.equals(CountryUtil.US, countryCode) -> {
                paymentParams.add(findPaymentParams(PaymentName.PAYPAL, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.VISA, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.MASTERCARD, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PIX, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PAGBANK, countryCode))
                paymentParams
            }
//            //越南 VN
//            TextUtils.equals(CountryUtil.VN, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.ZALOPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.MOMO, countryCode))
//                paymentParams
//            }
//            //韩国 KR
//            TextUtils.equals(CountryUtil.KR, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.KAKAOPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PAYCO, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.NAVERPAY, countryCode))
//                paymentParams
//            }
//            //孟加拉 BD
//            TextUtils.equals(CountryUtil.BD, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.BKASH, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.SURECASH, countryCode))
//                paymentParams
//            }
//            //巴基斯坦  PK
//            TextUtils.equals(CountryUtil.PK, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.EASYPAISA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.JAZZCASH, countryCode))
//                paymentParams
//            }
//            TextUtils.equals(CountryUtil.NG, countryCode) ||
//                    TextUtils.equals(CountryUtil.UZ, countryCode) ||
//                    TextUtils.equals(CountryUtil.ZA, countryCode) -> {
//                paymentParams.add(findPaymentParams(PaymentName.PhoneFee, countryCode))
//                paymentParams
//            }
            else -> {
                paymentParams.add(findPaymentParams(PaymentName.PAYPAL, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.VISA, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.MASTERCARD, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PIX, countryCode))
                paymentParams.add(findPaymentParams(PaymentName.PAGBANK, countryCode))
                paymentParams
                //all payment
//                paymentParams.add(findPaymentParams(PaymentName.PAYPAL, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.VISA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.MASTERCARD, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.CASHAPP, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.MOMO, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.BankCard, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.Clipspay, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.KAKAOPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PAYCO, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.NAVERPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.BKASH, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.SURECASH, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.EASYPAISA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.JAZZCASH, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PhoneFee, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.ZALOPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PAGBANK, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PICPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.LAZADA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.TRUEMONEY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.DANA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.SHOPEEPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.PAPARA, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.FASTPAY, countryCode))
//                paymentParams.add(findPaymentParams(PaymentName.OVO, countryCode))
//                paymentParams
            }
        }
    }

    /**
     * 获取支付对象
     */
    fun findPaymentParams(payment: String, countryCode: String): PaymentParams {
        return paramsMap[payment] ?: createPaymentParams(payment, countryCode)?.also {
            paramsMap[payment] = it
        } ?: throw IllegalArgumentException("Unknown payment method: $payment")
    }
    fun findPaymentParams2(payment: String, countryCode: String): PaymentParams? {
        return paramsMap[payment] ?: createPaymentParams(payment, countryCode)?.also {
            paramsMap[payment] = it
        }
    }


    private fun createPaymentParams(payment: String, countryCode: String): PaymentParams? {
        if (TextUtils.isEmpty(payment)) {
            return null
        }

        return when {
            //巴西 BR
            payment == PaymentName.PAGBANK -> PaymentParams(
                payment,
                R.drawable.ic_pay_pagbank,
                R.drawable.ic_pay_pagbank_long,
                PaymentUrl.Companion.PAGBANK_URL,
                1,
                1,
                "R$"
            )
            payment == PaymentName.PIX -> PaymentParams(
                payment,
                R.drawable.ic_pay_pix,
                R.drawable.ic_pay_pix_long,
                PaymentUrl.Companion.NO_URL,
                2,
                21,
                "R$"
            )
            //PICPAY
            payment == PaymentName.PICPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_picpay,
                R.drawable.ic_pay_picpay_long,
                PaymentUrl.Companion.NO_URL,
                2,
                19,
                "R$"
            )
            //NUBANK
            payment == PaymentName.NUBANK -> PaymentParams(
                payment,
                R.drawable.ic_pay_nubank,
                R.drawable.ic_pay_nubank_long,
                PaymentUrl.Companion.NO_URL,
                2,
                18,
                "R$"
            )

            //美国 US
            payment == PaymentName.PAYPAL -> PaymentParams(
                payment,
                R.drawable.ic_pay_paypal,
                R.drawable.ic_pay_paypal_long,
                PaymentUrl.Companion.PAYPAL_URL,
                2,
                20,
                "$"
            )
            payment == PaymentName.CASHAPP -> PaymentParams(
                payment,
                R.drawable.ic_pay_cashapp,
                R.drawable.ic_pay_cashapp_long,
                PaymentUrl.Companion.NO_URL,
                2,
                22,
                "$"
            )
            //visa
            payment == PaymentName.VISA -> PaymentParams(
                payment,
                R.drawable.ic_pay_visa,
                R.drawable.ic_pay_visa_long,
                PaymentUrl.Companion.NO_URL,
                2,
                23,
                "$"
            )
            //mastercard
            payment == PaymentName.MASTERCARD -> PaymentParams(
                payment,
                R.drawable.ic_pay_mastercard,
                R.drawable.ic_pay_mastercard_long,
                PaymentUrl.Companion.NO_URL,
                2,
                24,
                "$"
            )

            //菲律宾 PH
            payment == PaymentName.LAZADA -> PaymentParams(
                payment,
                R.drawable.ic_pay_lazada,
                R.drawable.ic_pay_lazada_long,
                PaymentUrl.Companion.LAZADA_URL,
                1,
                3,
                "₱"
            )
            //gcash
            payment == PaymentName.GCASH -> PaymentParams(
                payment,
                R.drawable.ic_pay_gcash,
                R.drawable.ic_pay_gcash_long,
                PaymentUrl.Companion.NO_URL,
                1,
                1,
                "₱"
            )
            //grabpay
            payment == PaymentName.GRABPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_grabpay,
                R.drawable.ic_pay_grabpay_long,
                PaymentUrl.Companion.NO_URL,
                1,
                6,
                "₱"
            )

            //泰国 TH
            payment == PaymentName.TRUEMONEY -> PaymentParams(
                payment,
                R.drawable.ic_pay_truemoney,
                R.drawable.ic_pay_truemoney_long,
                PaymentUrl.Companion.TRUEMONEY_URL,
                1,
                2,
                "฿"
            )
            //rabbitlinepay
            payment == PaymentName.RABBITLINEPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_rabbitlinepay,
                R.drawable.ic_pay_rabbitlinepay_long,
                PaymentUrl.Companion.NO_URL,
                1,
                7,
                "฿"
            )
            //mpay
            payment == PaymentName.MPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_mpay,
                R.drawable.ic_pay_mpay_long,
                PaymentUrl.Companion.NO_URL,
                1,
                8,
                "฿"
            )

            //印尼 ID
            payment == PaymentName.DANA -> PaymentParams(
                payment,
                R.drawable.ic_pay_dana,
                R.drawable.ic_pay_dana_long,
                PaymentUrl.Companion.DANA_URL,
                1,
                4,
                "Rp"
            )
            payment == PaymentName.SHOPEEPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_shopeepay,
                R.drawable.ic_pay_shopeepay_long,
                PaymentUrl.Companion.SHOPEEPAY_URL,
                1,
                5,
                "Rp"
            )
            //ovo
            payment == PaymentName.OVO -> PaymentParams(
                payment,
                R.drawable.ic_pay_ovo,
                R.drawable.ic_pay_ovo_long,
                PaymentUrl.Companion.NO_URL,
                1,
                9,
                "Rp"
            )

            //土耳其 TR
            payment == PaymentName.PAPARA -> PaymentParams(
                payment,
                R.drawable.ic_pay_papara,
                R.drawable.ic_pay_papara_long,
                PaymentUrl.Companion.PAPARA_URL,
                1,
                13,
                "₺"
            )
            //fastpay
            payment == PaymentName.FASTPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_fastpay,
                R.drawable.ic_pay_fastpay_long,
                PaymentUrl.Companion.NO_URL,
                1,
                12,
                "₺"
            )

            //越南 VN
            payment == PaymentName.ZALOPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_zalopay,
                R.drawable.ic_pay_zalopay_long,
                PaymentUrl.Companion.ZALOPAY_URL,
                1,
                14,
                "₫"
            )
            //momo
            payment == PaymentName.MOMO -> PaymentParams(
                payment,
                R.drawable.ic_pay_momo,
                R.drawable.ic_pay_momo_long,
                PaymentUrl.Companion.NO_URL,
                1,
                11,
                "₫"
            )

            payment == PaymentName.BankCard -> PaymentParams(
                payment,
                R.drawable.ic_pay_bank_card,
                R.drawable.ic_pay_bank_card_long,
                PaymentUrl.Companion.NO_URL,
                5,
                3113,
                "₩"
            )

            //孟加拉 BD
            payment == PaymentName.BKASH -> PaymentParams(
                payment,
                R.drawable.ic_pay_bkash,
                R.drawable.ic_pay_bkash_long,
                PaymentUrl.Companion.BKASH_URL,
                1,
                9,
                "৳"
            )
            //Surecash
            payment == PaymentName.SURECASH -> PaymentParams(
                payment,
                R.drawable.ic_pay_surecash,
                R.drawable.ic_pay_surecash_long,
                PaymentUrl.Companion.NO_URL,
                1,
                10,
                "৳"
            )

            //巴基斯坦 PK
            payment == PaymentName.EASYPAISA -> PaymentParams(
                payment,
                R.drawable.ic_pay_easypaisa,
                R.drawable.ic_pay_easypaisa_long,
                PaymentUrl.Companion.EASYPAISA_URL,
                1,
                8,
                "Rs"
            )
            //JazzCash
            payment == PaymentName.JAZZCASH -> PaymentParams(
                payment,
                R.drawable.ic_pay_jazzcash,
                R.drawable.ic_pay_jazzcash_long,
                PaymentUrl.Companion.NO_URL,
                1,
                11,
                "Rs"
            )

            //韩国
            payment == PaymentName.Clipspay -> PaymentParams(
                payment,
                R.drawable.ic_pay_clipspay,
                R.drawable.ic_pay_clipspay_long,
                PaymentUrl.Companion.NO_URL,
                12,
                1,
                "₩"
            )
            //kakaopay
            payment == PaymentName.KAKAOPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_kakaopay,
                R.drawable.ic_pay_kakaopay_long,
                PaymentUrl.Companion.NO_URL,
                12,
                1,
                "₩"
            )
            //payco
            payment == PaymentName.PAYCO -> PaymentParams(
                payment,
                R.drawable.ic_pay_payco,
                R.drawable.ic_pay_payco_long,
                PaymentUrl.Companion.NO_URL,
                12,
                1,
                "₩"
            )
            //neverpay
            payment == PaymentName.NAVERPAY -> PaymentParams(
                payment,
                R.drawable.ic_pay_naverpay,
                R.drawable.ic_pay_naverpay_long,
                PaymentUrl.Companion.NO_URL,
                12,
                1,
                "₩"
            )

            payment == PaymentName.PhoneFee -> {
                val (accountType, accountSubType) = when (countryCode) {
                    CountryUtil.BD -> 7 to 1
                    CountryUtil.ZA -> 7 to 2
                    CountryUtil.NG -> 7 to 3
                    CountryUtil.UZ -> 7 to 4
                    else -> -1 to -1
                }
                PaymentParams(
                    payment,
                    R.drawable.ic_phone_fee,
                    R.drawable.ic_phone_fee_long,
                    PaymentUrl.Companion.NO_URL,
                    accountType,
                    accountSubType,
                    CountryUtil.getSymbolByCode(countryCode)
                )
            }
            else -> null
        }
    }
}