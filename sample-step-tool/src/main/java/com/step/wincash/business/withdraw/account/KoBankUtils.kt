package com.step.wincash.business.withdraw.account

import com.step.wincash.business.withdraw.PaymentName


object KoBankUtils {
    //Haipay
    private val reversedBankMap: MutableMap<String, String> = HashMap()
    //ClipsPay
    private val clipsPayBankMap: MutableMap<String, String> = HashMap()

    init {
        initBankMap()
    }


    fun getBankNameList(@PaymentName paymentName: String): List<String>{
        return if (paymentName.equals(PaymentName.Clipspay, true)){
            clipsPayBankMap.keys.toList()
        }else{
            reversedBankMap.keys.toList()
        }
    }

    fun getBankCode(bankName: String, @PaymentName paymentName: String): String? {
        return if (paymentName.equals(PaymentName.Clipspay, true)){
            clipsPayBankMap[bankName]
        }else{
            reversedBankMap[bankName]
        }
    }

    private fun initBankMap() {
        reversedBankMap["Samsung Securities"] = "SAMCKRSL"
        reversedBankMap["Korea Post(Epost)"] = "KOPOKRS1"
        reversedBankMap["Kwangju Bank(KJ)"] = "KWABKRSE"
        reversedBankMap["Kyobo Securities"] = "KYBOKRSE"
        reversedBankMap["Kyongnam Bank"] = "KYNAKR22"
        reversedBankMap["Mirae Asset Daewoo"] = "MHCBKRSE"
        reversedBankMap["Nonghyup Bank(NH)"] = "NACFKRSE"
        reversedBankMap["Suhyup Local Cooperatives"] = "NFFCKRSE"
        reversedBankMap["Busan Bank(BNK)"] = "PUSBKR2P"
        reversedBankMap["Korea Development Bank(KDB)"] = "KODBKRSE"
        reversedBankMap["Standard Chartered Korea(SC First)"] = "SCBLKRSE"
        reversedBankMap["Daishin Securities"] = "SECDKRS1"
        reversedBankMap["Eugene Investment and  Securities"] = "SEOSKRS1"
        reversedBankMap["Shinhan Bank"] = "SHBKKRSE"
        reversedBankMap["Hyundai Motor Securities"] = "SHHEKRS1"
        reversedBankMap["SK Securities"] = "SKSEKRS1"
        reversedBankMap["Shinhan Investment"] = "SSISKRSS"
        reversedBankMap["NH Investment and Securities"] = "WISHKRSE"
        reversedBankMap["Woori Bank"] = "HVBKKRSE"
        reversedBankMap["K Bank"] = "CITIKRSXKAK"
        reversedBankMap["Kookmin-Bank(KB)"] = "CZNBKRSE"
        reversedBankMap["Daegu Bank(DGB)"] = "DAEBKR22"
        reversedBankMap["Deutsche Bank"] = "DEUTKRSE"
        reversedBankMap["eBEST Investment and Securities"] = "ETSIKRS1"
        reversedBankMap["KEB Hana Bank"] = "HNBNKRSE"
        reversedBankMap["Hanhwa Investment and Securities"] = "HNWSKRSS"
        reversedBankMap["HSBC"] = "HSBCKRSE"
        reversedBankMap["Citibank Korea"] = "CITIKRSX"
        reversedBankMap["KB Securities"] = "HYSEKRSE"
        reversedBankMap["Industrial Bank of Korea(IBK)"] = "IBKOKRSE"
        reversedBankMap["Jeonbuk Bank(JB)"] = "JEONKRSE"
        reversedBankMap["Jeju Bank"] = "JJBKKR22"
        reversedBankMap["KAKAO Bank"] = "KAKOKR22"
        reversedBankMap["Meritz Securities"] = "KFBCKRSE"
        reversedBankMap["Korea Investment and Securities"] = "KISEKRSE"
        reversedBankMap["Kiwoom Securities"] = "KIWCKRSE"


        clipsPayBankMap["JPMorgan Chase Bank"] = "JPMORGAN"
        clipsPayBankMap["Deutsche Bank"] = "DEUTSCHE"
        clipsPayBankMap["HSBC Bank"] = "HSBC"
        clipsPayBankMap["Saving Bank"] = "SAVING_BANK"
        clipsPayBankMap["Kyongnam Bank"] = "KYONGNAM"
        clipsPayBankMap["Jeonbuk Bank"] = "JEONBUK"
        clipsPayBankMap["Jeju Bank"] = "JEJU"
        clipsPayBankMap["Gwangju Bank"] = "GWANGJU"
        clipsPayBankMap["BNK Busan Bank"] = "BUSAN"
        clipsPayBankMap["Daegu Bank"] = "DAEGU"
        clipsPayBankMap["Standard Chartered First Bank Korea"] = "SC"
        clipsPayBankMap["Toss Bank"] = "TOSS_BANK"
        clipsPayBankMap["K Bank"] = "K_BANK"
        clipsPayBankMap["Kakao Bank"] = "KAKAO_BANK"
        clipsPayBankMap["Korea Development Bank"] = "KDB"
        clipsPayBankMap["Industrial Bank of Korea"] = "IBK"
        clipsPayBankMap["Korea Post Office"] = "POST_OFFICE"
        clipsPayBankMap["National Credit Union Federation of Korea"] = "SINHYUP"
        clipsPayBankMap["Citibank Korea"] = "CITI"
        clipsPayBankMap["KEB Hana Bank"] = "HANA"
        clipsPayBankMap["National Agricultural Cooperative Federation"] = "NONGHYUP"
        clipsPayBankMap["Kookmin Bank"] = "KOOKMIN"
        clipsPayBankMap["Woori Bank"] = "WOORI"
        clipsPayBankMap["Shinhan Bank"] = "SHINHAN"
    }
}
