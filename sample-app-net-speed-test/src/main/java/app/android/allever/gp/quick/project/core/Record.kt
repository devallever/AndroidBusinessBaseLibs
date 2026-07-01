package app.android.allever.gp.quick.project.core

import org.litepal.crud.LitePalSupport

/***
 * 【ok】相当于 xxx 兆带宽：下载速度
 * 【x】下载使用数据量：
 * 【x】上传使用数据量：
 * 动画：飞机，汽车，摩托车，自行车
 * 【ok】下载速度：
 * 【ok】上传速度：
 * 【ok】ping耗时：
 * 【ok】丢包：默认0
 * 【ok】网络：WIFI, 5g，4g，3g
 * 【ok】运营商：电信，移动，联通
 * 【ok】服务：默认互联网服务器
 * 内部IP：
 * 外部IP：
 * 机型：
 * 【ok】时间：
 * 【x】位置：
 */
class Record : LitePalSupport(){
    var id: Long? = null
    var downloadSpeed: Double = 0.0
    var uploadSpeed: Double = 0.0
    var downloadBytes = 0L
    var uploadBytes = 0L
    var pingSpeed = 0
    val missPkg = 0
    var networkType = ""
    var operator = ""
    var serverName = "互联网服务器"
    var internalIp = "192.168.0.1"
    var ip = "0.0.0.0"
    var model = "未知"
    var time = System.currentTimeMillis()
    var location = "未知"

}