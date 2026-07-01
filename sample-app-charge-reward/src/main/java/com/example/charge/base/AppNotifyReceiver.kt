package com.example.charge.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlin.let

class AppNotifyReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent ?: return
        if (intent.action == "action_notify_bar_close") {
            context?.let {
                NotificationManagerCompat.from(context).cancel(20002)
            }
        }
    }
}


