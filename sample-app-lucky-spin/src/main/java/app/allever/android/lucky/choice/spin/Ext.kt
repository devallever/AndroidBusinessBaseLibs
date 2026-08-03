package app.allever.android.lucky.choice.spin

import android.util.Log
import android.widget.Toast

fun log(msg: String) {
    if (BuildConfig.DEBUG) {
        Log.d("SpinLog",  msg)
    }
}

fun toast(msg: String) {
    Toast.makeText(LuckSpinApplication.context, msg, Toast.LENGTH_SHORT).show()
}