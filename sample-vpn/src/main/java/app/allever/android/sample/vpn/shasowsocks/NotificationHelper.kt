package app.allever.android.sample.vpn.shasowsocks

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.allever.android.lib.core.app.App

object NotificationHelper {

    init {
        createNotificationChannel()
    }

    // 创建一个通知通道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "ShasowsocksVpn"
            val channelName = "ShasowsocksVpn"
            val important = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, important)
            val notificationManager: NotificationManager =
                App.context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(message: String, notificationId: Int = 999) {
        val context = App.context
        val builder =
            NotificationCompat.Builder(context, "ShasowsocksVpn").apply {
//                setSmallIcon(R.mipmap.ic_launcher_foreground)
                setContentText(message)
                setTicker(message)
                setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, ShasowsocksVpnActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
                setAutoCancel(true)
                setCategory(NotificationCompat.CATEGORY_ERROR)
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }



        with(NotificationManagerCompat.from(context)) {
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (hasPermission) {
                notify(notificationId, builder.build())
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun cancelNotification(notificationId: Int = 999) {
        val notificationManager: NotificationManager =
            App.context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationId)
    }
}