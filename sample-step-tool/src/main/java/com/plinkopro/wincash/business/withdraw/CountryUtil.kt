package com.plinkopro.wincash.business.withdraw

object CountryUtil {
    const val BR = "BR" //巴西
    const val US = "US" //美国
    const val ID = "ID" //印尼
    const val PH = "PH" //菲律宾
    const val TH = "TH" //泰国
    const val TR = "TR" //土耳其
    const val VN = "VN" //越南
    const val BD = "BD" //孟加拉
    const val KR = "KR" //韩国
    const val PK = "PK" //巴基斯坦
    const val NG = "NG" //尼日利亚
    const val UZ = "UZ" //乌兹别克斯坦
    const val ZA = "ZA" //南非

    /**
     * 根据国家获取货币符合
     * */
    fun getSymbolByCode(countryCode: String): String{
       return when(countryCode.uppercase()){
            BR -> "R$"
            US -> "$"
            ID -> "Rp"
            PH -> "₱"
            TH -> "฿"
            TR -> "₺"
            VN -> "₫"
            BD -> "৳"
            KR -> "₩"
            PK -> "₨"
            NG -> "₦"
            UZ -> "лв"
            ZA -> "R"
            else -> "$"
        }
    }
}