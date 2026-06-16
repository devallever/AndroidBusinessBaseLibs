package app.flash.tunnel.vpn.helper

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
import app.flash.tunnel.vpn.R
import app.flash.tunnel.vpn.TunnelApp
import app.flash.tunnel.vpn.page.SplashActivity

object NotificationHelper {

    init {
        createNotificationChannel()
    }

    // 创建一个通知通道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = TunnelApp.context.getString(R.string.app_name)
            val channelName = TunnelApp.context.getString(R.string.app_name)
            val important = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, important)
            val notificationManager: NotificationManager =
                TunnelApp.context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(message: String, notificationId: Int = 999) {
        val context = TunnelApp.context
        val builder =
            NotificationCompat.Builder(context, context.getString(R.string.app_name)).apply {
                setSmallIcon(R.mipmap.ic_launcher_foreground)
                setContentText(message)
                setTicker(message)
                setContentIntent(
                    PendingIntent.getActivity(
                        context,
                        0,
                        Intent(context, SplashActivity::class.java),
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
            TunnelApp.context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationId)
    }
}