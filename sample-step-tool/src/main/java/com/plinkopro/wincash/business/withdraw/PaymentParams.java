package com.plinkopro.wincash.business.withdraw;

import com.plinkopro.wincash.R;

public class PaymentParams {
    //支付名称
    private @PaymentName String paymentName;
    //icon
    private int paymentIcon;
    private int paymentIconLong;
    //注册链接
    private @PaymentUrl String registerUrl;

    private int accountType;

    private int accountSubType;
    private boolean defaultValue;
    //货币单位
    private String symbol;

    public static PaymentParams DEFAULT = new PaymentParams(
            PaymentName.PAYPAL,
            R.drawable.ic_pay_paypal,
            R.drawable.ic_pay_paypal_long,
            PaymentUrl.Companion.PAYPAL_URL,
            2,
            20,
            "$"
    );

    public PaymentParams(@PaymentName String paymentName,
                         int paymentIcon, int paymentIconLong,
                         @PaymentUrl String registerUrl
            , int accountType
            , int accountSubType
    , String symbol) {
        this.paymentName = paymentName;
        this.paymentIcon = paymentIcon;
        this.paymentIconLong =  paymentIconLong;
        this.registerUrl = registerUrl;
        this.accountType = accountType;
        this.accountSubType = accountSubType;
        this.symbol = symbol;
    }

    public void setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public int getAccountSubType() {
        return accountSubType;
    }

    public int getAccountType() {
        return accountType;
    }

    public @PaymentName String getPaymentName() {
        return paymentName;
    }

    public int getPaymentIcon() {
        return paymentIcon;
    }

    public int getPaymentIconLong(){
        return paymentIconLong;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public String getSymbol() {
        return symbol;
    }
}
