package app.flash.tunnel.vpn.helper.ad

import app.flash.tunnel.vpn.lib.common.util.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AdTimeOutJob(
    private var mKey: String,
    private val mTimeOut: (key: String) -> Unit
) {

    private var mStartTime = 0L
    private val DELAY_TIME = 20 * 1000L
    private var mJob: Job? = null

    init {
        mStartTime = System.currentTimeMillis()
        startJob()
    }

    fun cancel() {
        if (mJob != null) {
            mJob?.cancel()
        }
        log("cancel ad time out job")
    }

    private fun startJob() {
        mJob = CoroutineScope(Dispatchers.Default).launch {
            delay(DELAY_TIME)
            if (!isActive) {
                return@launch
            }
            log("ad load time out")
            //for remove cache outside
            mTimeOut.invoke(mKey)
        }
    }

}