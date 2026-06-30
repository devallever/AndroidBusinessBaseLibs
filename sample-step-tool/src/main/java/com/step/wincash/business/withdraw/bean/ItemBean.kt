package com.step.wincash.business.withdraw.bean

class ItemBean {
    var start: String

    //可以是字符串 还可以是 int
    var end: Any
    var isFailed: Boolean = false

    constructor(start: String, end: Any, isFailed: Boolean) {
        this.start = start
        this.end = end
        this.isFailed = isFailed
    }

    constructor(start: String, end: Any) {
        this.start = start
        this.end = end
    }
}