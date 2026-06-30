package com.allever.video.editor.function.online

import androidx.annotation.Keep

@Keep
open class OnlineDataBean {

    /**
     * id: 节点id
     */
    var id = ""

    /**
     * 内容类型(分类、item)
     */
    var type = 0

    /***
     * 域名
     */
    var hostname = ""

    /***
     * url 路径
     */
    var path = ""

    /***
     *
     */
    var name = ""

    /***
     * 包名
     */
    var pkgName = ""

    /***
     * 下载地址
     */
    var downloadUrl = ""

    /**
     * 是否需要购买
     */
    var isNeedBuy = false

    /**
     * 小尺寸图片，逗号分隔
     */
    var smallImg = ""

    /**
     * 中尺寸图片，逗号分隔
     */
    var mediumImg = ""

    /**
     * 大尺寸图片，逗号分隔
     */
    var bigImg = ""
    var tutorialUrl = ""
    var tutorialDescription = ""

    /**
     * 推荐，逗号分隔, 内容为id
     */
    var recommends = ""

    /**
     * 信息流显示列表，逗号分隔，内容为id
     * */
    var flow = ""

    /***
     * 子节点
     */
    var child: MutableList<OnlineDataBean>? = null


}