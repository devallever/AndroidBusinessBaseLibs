package com.plinkopro.wincash.business.withdraw.account;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({BrAccountType.EMAIL,
        BrAccountType.PHONE,
        BrAccountType.CPF,
})
@Retention(RetentionPolicy.SOURCE)
public @interface BrAccountType {
    String EMAIL = "EMAIL";
    String PHONE = "PHONE";
    String CPF = "CPF";
}
