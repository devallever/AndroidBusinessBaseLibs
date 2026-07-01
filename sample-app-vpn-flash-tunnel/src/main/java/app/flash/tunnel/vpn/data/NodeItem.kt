package app.flash.tunnel.vpn.data

import androidx.annotation.Keep
import com.github.shadowsocks.database.Profile


@Keep
class NodeItem {
    var entity: Profile? = null //null <=> smart
    var cc = ""//countryCode
    var cn = ""//countryName
    var nn = ""//nodeName&cityName
    var weight = 0
    var adIdUnit = ""//adUnitId

    //    var type = NodeAdapter.TYPE_NODE
    var type = 0

    fun isSmartMode() = entity == null
}