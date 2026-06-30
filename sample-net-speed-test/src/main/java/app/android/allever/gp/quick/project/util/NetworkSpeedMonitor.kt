package app.android.allever.gp.quick.project.util

import android.net.TrafficStats
import android.os.Handler
import android.os.SystemClock

class NetworkSpeedMonitor(private val intervalInMillis: Long = 1000, private val cb:(download:Long, upload: Long) -> Unit) {
    private var lastTotalRxBytes = 0L
    private var lastTotalTxBytes = 0L
    private var handler = Handler()
    private var runnable: Runnable? = null

    fun startMonitoring() {
        lastTotalRxBytes = TrafficStats.getTotalRxBytes()
        lastTotalTxBytes = TrafficStats.getTotalTxBytes()

        runnable = Runnable {
            val totalRxBytes = TrafficStats.getTotalRxBytes()
            val totalTxBytes = TrafficStats.getTotalTxBytes()

            var timeDelta = SystemClock.elapsedRealtime() - SystemClock.elapsedRealtime()
            if (timeDelta == 0L) {
                timeDelta = 1L
            }
            val rxSpeed = (totalRxBytes - lastTotalRxBytes) * 8 / timeDelta // convert to bits per second
            val txSpeed = (totalTxBytes - lastTotalTxBytes) * 8 / timeDelta

            // 更新上一次的字节数
            lastTotalRxBytes = totalRxBytes
            lastTotalTxBytes = totalTxBytes

            // 处理速度数据
            cb.invoke(rxSpeed / 1024, txSpeed / 1024)


            // 继续监听
            handler.postDelayed(runnable!!, intervalInMillis)
        }

        handler.post(runnable!!)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(runnable!!)
    }
}
