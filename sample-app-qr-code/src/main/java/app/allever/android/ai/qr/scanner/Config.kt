package app.allever.android.ai.qr.scanner

import com.android.absbase.utils.SpUtils
import com.android.absbase.utils.TimeUtils

object Config {

    const val KEY_LAST_OPEN_APP_TIME = "cm_loat_dkekf"
    var lastOpenAppTime: Long
        get() {
            var time = SpUtils.obtain().get(KEY_LAST_OPEN_APP_TIME, 0L)
            if (time == 0L) {
                time = System.currentTimeMillis()
                SpUtils.obtain().save(KEY_LAST_OPEN_APP_TIME, time)
            }
            return time
        }
        set(value) = SpUtils.obtain().save(KEY_LAST_OPEN_APP_TIME, value)

    /**
     * 当天进入程序的次数
     */
    const val KEY_OPEN_APP_COUNT_OF_DAY = "cm_oacod_diksal"
    var openAppCountOfDay: Int
        get() {
            var count = 0
            val currentTime = System.currentTimeMillis()
            if (!TimeUtils.isSameDayOfMillis(currentTime,
                    lastOpenAppTime
                )) {
                SpUtils.obtain().save(KEY_OPEN_APP_COUNT_OF_DAY, 0)
            } else {
                count = SpUtils.obtain().get(KEY_OPEN_APP_COUNT_OF_DAY, 0)
            }
            return count
        }
        set(value) = SpUtils.obtain().save(KEY_OPEN_APP_COUNT_OF_DAY, value)

    /**
     * 当天首次进入App的时间
     */

    const val KEY_SETTING_COMPLETE_VERSINO = "cm_scv_dkdkkfkf"
    var settingCompleteVersion: Boolean
        get() = SpUtils.obtain().get(KEY_SETTING_COMPLETE_VERSINO, false)
        set(value) = SpUtils.obtain().save(KEY_SETTING_COMPLETE_VERSINO, value)

    /**
     * 记录购买的状态，0无效订阅,-1查询失败，>0有效订阅
     */
    const val KEY_PURCHASE_SUB_SIZE = "cm_pss_awasd"
    var purchaseSubSize: Int
        //        get() = SpUtils.obtain().get(KEY_PURCHASE_SUB_SIZE, -1)
        get() = 1
        set(value) {
            SpUtils.obtain().save(KEY_PURCHASE_SUB_SIZE, value)
        }
}