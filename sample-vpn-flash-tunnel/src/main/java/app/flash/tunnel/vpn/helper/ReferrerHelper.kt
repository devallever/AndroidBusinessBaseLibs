package app.flash.tunnel.vpn.helper

import android.content.Context
import app.flash.tunnel.vpn.lib.common.util.StoreManager
import app.flash.tunnel.vpn.lib.common.util.log
//import com.android.installreferrer.api.InstallReferrerClient
//import com.android.installreferrer.api.InstallReferrerStateListener
import com.google.gson.Gson
import java.net.URLDecoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object ReferrerHelper {
//    private lateinit var installReferrerClient: InstallReferrerClient
    var facebookReferrerDecryption: FacebookReferrerDecryption? = null
        private set
    private const val REFERRER_KEY =
        "aa4716dfad4595af45640a42ac085a9bebf746c0d5e1112fd80b1545b6f0f39a"
    private const val HEX_STR = "0123456789abcdef"
    private const val FB_REFERRER_DATA_KEY = "FLASH_TUNNEL_FB_REFERRER_DATA_KEY"

    fun init(context: Context) {
        val referrerDecryptionData = getFbReferrerData()
        if (referrerDecryptionData.isNotEmpty()) {
            log("referrerDecryptionData has cache: $referrerDecryptionData")
            createReferrerDecryption(referrerDecryptionData)
            parseReferrerType()
            return
        }
//        installReferrerClient = InstallReferrerClient.newBuilder(context).build()
//        installReferrerClient.startConnection(object : InstallReferrerStateListener {
//            override fun onInstallReferrerSetupFinished(responseCode: Int) {
//                log("onInstallReferrerSetupFinished: $responseCode")
//                if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
//                    try {
//                        val referrerUrl: String =
//                            installReferrerClient.installReferrer.installReferrer
//                        log("referrerUrl = $referrerUrl")
//                        parseAndSaveReferrer(referrerUrl)
//                        parseReferrerType()
//
//                        if (EventHelper.referrerSValue == EventHelper.ReferSValue.FB) {
//                            EventHelper.logRefIds("")
//                        } else {
//                            if (Random.nextInt(10) == 5) {
//                                val subCount = 100
//                                val newUrl = if (referrerUrl.length > subCount) {
//                                    referrerUrl.substring(0, subCount)
//                                } else {
//                                    referrerUrl
//                                }
//                                EventHelper.logRefIds(newUrl)
//                            }
//                        }
//                    } catch (e: Exception) {
//                        log("referrer exception: ${e.message}")
//                    }
//                }
//
//            }
//
//            override fun onInstallReferrerServiceDisconnected() {
//            }
//        })
    }

    private fun parseAndSaveReferrer(url: String) {
        url.split("&").forEach {
            if (it.startsWith("utm_content=")) {
                val referrerContent =
                    URLDecoder.decode(it.substring("utm_content=".length), "UTF-8")
                val fbReferrer = Gson().fromJson(referrerContent, FacebookReferrer::class.java)
                val decryData =
                    decryptGCM(
                        fbReferrer.source.data,
                        REFERRER_KEY,
                        fbReferrer.source.nonce
                    )
                createReferrerDecryption(decryData)
                saveFbReferrerData(decryData)
            }
        }

    }

    private fun createReferrerDecryption(decryData: String?) {
        try {
            facebookReferrerDecryption =
                Gson().fromJson(decryData, FacebookReferrerDecryption::class.java)
        } catch (_: Exception) {
        }
    }

    private fun saveFbReferrerData(data: String?) {
        StoreManager.putString(FB_REFERRER_DATA_KEY, data ?: "")
    }

    private fun getFbReferrerData(): String {
        return StoreManager.getString(FB_REFERRER_DATA_KEY)
    }


    private fun parseReferrerType() {
        if (facebookReferrerDecryption == null || facebookReferrerDecryption?.adId == null || facebookReferrerDecryption?.adId == 0L) {
            EventHelper.referrerSValue = EventHelper.ReferSValue.OTHER
        } else {
            EventHelper.referrerSValue = EventHelper.ReferSValue.FB
        }
    }

    private fun decryptGCM(content: String, password: String, iv: String): String? {
        try {
            val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec =
                SecretKeySpec(hexToByteArr(password), "AES")
            val ivParameterSpec = IvParameterSpec(hexToByteArr(iv))
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
            val result: ByteArray = cipher.doFinal(hexToByteArr(content))
            return String(result, charset("utf-8"))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }


    private fun hexToByteArr(hexStr: String): ByteArray {
        val charArr = hexStr.toCharArray()
        val btArr = ByteArray(charArr.size / 2)
        var index = 0
        var i = 0
        while (i < charArr.size) {
            val highBit: Int = HEX_STR.indexOf(charArr[i])
            val lowBit: Int = HEX_STR.indexOf(charArr[++i])
            btArr[index] = (highBit shl 4 or lowBit).toByte()
            index++
            i++
        }
        return btArr
    }


}