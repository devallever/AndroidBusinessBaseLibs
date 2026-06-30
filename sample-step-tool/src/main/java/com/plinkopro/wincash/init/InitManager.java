package com.plinkopro.wincash.init;



import android.app.Application;
import java.util.Locale;


public class InitManager {

    public static String getCountryCode() {
        return Locale.getDefault().getCountry();
    }

    public static void init(Application application) {
    }

}
