package app.allever.android.sample.vpn.shasowsocks

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class FTResponse(
    @SerializedName("ftau")
    var aConfig: AdId,
    @SerializedName("ftsn")
    var servers: List<Server>?
)

@Keep
data class AdId(
    @SerializedName("b")
    var bannerId: String,
    @SerializedName("b_i")
    var beforeInterId: String,
    @SerializedName("i")
    var interId: String,
    @SerializedName("n")
    var nativeId: String,
    @SerializedName("r")
    var rewardId: String
)

@Keep
data class Server(
    @SerializedName("a_u")
    var au: String,
    @SerializedName("cn")
    var cName: String,
    @SerializedName("co")
    var cCode: String,
    @SerializedName("em")
    var method: String,
    @SerializedName("ip")
    var address: String,
    @SerializedName("n")//nodeName
    var nName: String,
    @SerializedName("pw")
    var pwd: String,
    @SerializedName("rp")
    var port: Int,
    @SerializedName("w")
    var weight: Int
)