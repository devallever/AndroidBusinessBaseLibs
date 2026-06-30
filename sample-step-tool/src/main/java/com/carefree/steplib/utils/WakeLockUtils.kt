package com.carefree.steplib.utils

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import com.carefree.steplib.service.StepTrackingService
import java.util.Calendar

/**
 * @author :  jiahongfei
 * @email : jiahongfeinew@163.com
 * @date : 2018/2/12
 * @desc :
 */
internal object WakeLockUtils {
    private var mWakeLock: WakeLock? = null

    @Synchronized
    fun getLock(context: Context): WakeLock? {
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) mWakeLock!!.release()
            mWakeLock = null
        }

        val mgr = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = mgr.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            StepTrackingService::class.java.name
        )
        mWakeLock?.setReferenceCounted(true)
        val c = Calendar.getInstance()
        c.timeInMillis = System.currentTimeMillis()
        mWakeLock?.acquire(10*60*1000L /*10 minutes*/)

        return mWakeLock
    }
}
