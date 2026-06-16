package app.flash.tunnel.vpn.helper

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


data class FacebookReferrer(
    @SerializedName("source")
    val source: FacebookReferrerSource
)

data class FacebookReferrerSource(
    @SerializedName("data")
    val data: String,
    @SerializedName("nonce")
    val nonce: String
)

@Keep
data class FacebookReferrerDecryption(
    @SerializedName("ad_id")
    val adId: Long,
    @SerializedName("campaign_id")
    val campaignId: Long,
    @SerializedName("campaign_name")
    val campaignName: String,
    @SerializedName("campaign_group_id")
    val campaignGroupId: Long,
    @SerializedName("campaign_group_name")
    val campaignGroupName: String
)
