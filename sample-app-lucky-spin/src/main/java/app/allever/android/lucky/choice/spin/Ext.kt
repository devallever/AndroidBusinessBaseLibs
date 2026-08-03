package app.allever.android.lucky.choice.spin

import android.util.Log
import android.widget.Toast
import app.allever.android.lib.core.app.App

fun log(msg: String) {
    if (App.DEBUG) {
        Log.d("SpinLog",  msg)
    }
}

fun toast(msg: String) {
    Toast.makeText(LuckSpinApplication.context, msg, Toast.LENGTH_SHORT).show()
}