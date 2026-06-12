package app.allever.android.sample.cleaner.memory

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import app.allever.android.lib.core.app.App

/**
 * 进程管理
 *
 * 对应文档"进程管理"章节。
 * 获取运行中的进程列表，按优先级分类，
 * 支持关闭后台进程释放内存。
 *
 * 进程优先级（从高到低）：
 * 前台进程 → 可见进程 → 服务进程 → 后台进程 → 空进程
 */
object ProcessManager {

    /**
     * 进程信息数据类
     *
     * @param pid 进程 ID
     * @param processName 进程名（包名）
     * @param importance 重要性级别（值越小优先级越高）
     * @param importanceDesc 重要性描述
     * @param memorySize 占用内存大小（字节，仅部分系统可获取）
     */
    data class ProcessInfo(
        val pid: Int,
        val processName: String,
        val importance: Int,
        val importanceDesc: String,
        val memorySize: Long = 0L
    )

    /**
     * 获取运行中的进程列表
     *
     * @return 进程信息列表（按重要性升序排列，低优先级的在前）
     */
    fun getRunningProcesses(): List<ProcessInfo> {
        val context = App.context
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = mutableListOf<ProcessInfo>()

        try {
            val runningProcesses = activityManager.runningAppProcesses ?: return emptyList()

            for (process in runningProcesses) {
                val info = ProcessInfo(
                    pid = process.pid,
                    processName = process.processName,
                    importance = process.importance,
                    importanceDesc = describeImportance(process.importance),
                    memorySize = 0L // Android 5.0+ 已无法获取其他进程内存
                )
                processes.add(info)
            }
        } catch (_: SecurityException) {
            // 无权限时返回空列表
        }

        // 按重要性排序（空进程和后台进程排在前面）
        return processes.sortedBy { it.importance }.reversed()
    }

    /**
     * 杀死指定包名的后台进程
     *
     * 对应文档：killBackgroundProcesses
     *
     * @param packageName 包名
     * @return 是否成功发送了终止请求（注意：系统不保证一定杀死）
     */
    fun killBackgroundProcesses(packageName: String): Boolean {
        return try {
            val context = App.context
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 描述进程重要性级别
     */
    private fun describeImportance(importance: Int): String = when {
        importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "前台进程"

        importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "可见进程"

        importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "服务进程"

        importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "后台进程"

        else -> "缓存进程"
    }

    /**
     * 判断是否为可清理的后台进程
     *
     * 只清理非前台、非自身应用的进程
     */
    fun isKillable(info: ProcessInfo): Boolean {
        val selfPackage = App.context.packageName

        // 不杀自身
        if (info.processName.contains(selfPackage)) return false

        // 不杀前台进程
        if (info.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            return false
        }

        // 不杀可见进程
        if (info.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
            return false
        }

        return true
    }
}
