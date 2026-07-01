package com.carefree.steplib.step

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.carefree.steplib.lib.ConstStep
import com.carefree.steplib.utils.Mkv

/**
 * Created by jiahongfei on 2017/9/27.
 */
class StepShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            Mkv.put(ConstStep.SHUTDOWN, true)
        }
    }
}
