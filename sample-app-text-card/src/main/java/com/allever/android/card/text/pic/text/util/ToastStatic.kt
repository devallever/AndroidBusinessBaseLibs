package com.allever.android.card.text.pic.text.util

import android.widget.Toast
import com.allever.android.card.text.pic.text.App

fun toast(msg: String) {
    Toast.makeText(App.context, msg, Toast.LENGTH_SHORT).show()
}