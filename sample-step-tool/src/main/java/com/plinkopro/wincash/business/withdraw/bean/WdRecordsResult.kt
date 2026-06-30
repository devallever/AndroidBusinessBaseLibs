package com.plinkopro.wincash.business.withdraw.bean

data class WdRecordsResult(
    val list: List<WdRecord>
) {
    /**
     * @param id 提现id
     * @param createTime
     * @param failCode
     * @param money 提现金额
     * @param status 提现状态： 0=打款中,1=成功,2=失败
     * @param transferAccountsAccount 提现账号
     * @param title 提现说明
     * @param returnGold 是否已退回金币
     * @param isNow 是否已显示失败弹窗
     */
    data class WdRecord(
        val id: Long,
        val createTime: Long,
        val failCode: String,
        val money: Double,
        var status: Int,
        val transferAccountsAccount: String,
        val title: String,
        var returnGold : Boolean = false,
        var isCurrentUser : Boolean = true
    )
}