package com.example.charge.utils

import android.widget.Toast
import com.example.charge.ChargeApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun toast(message: String) {
    CoroutineScope(Dispatchers.Main).launch {
        Toast.makeText(ChargeApp.instance, message, Toast.LENGTH_SHORT).show()
    }
}