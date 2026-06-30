package com.plinkopro.wincash.business.withdraw.account


class AccountBean {
    var account: String? = null
    var accountName: String? = null //pageBank的名称 、TrueMoney/DANA/Papara/Lazada/ShopeePay/Zalopay的姓名、 BankCard/Clipspay的银行名称
    var firstName: String? = null //Clipspay的名
    var lastName: String? = null //Clipspay的姓
    var email: String? = null //Bkash才有

    //巴西独有的
    @BrAccountType
    var accountType: String? = null //身份证类型
    var cpfId: String? = null //身份证号

    constructor(account: String?, accountName: String?){
        this.account = account
        this.accountName = accountName
    }

    //pix&pagbank
    constructor(account: String?, @BrAccountType accountType: String?, accountName: String?, cpfId: String?) {
        this.account = account
        this.accountType = accountType
        this.accountName = accountName
        this.cpfId = cpfId
    }

}
