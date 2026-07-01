package com.allever.video.editor.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.SystemClock
import app.allever.android.lib.core.app.App
import app.allever.android.lib.core.helper.ProcessHelper
import com.allever.video.editor.function.save.VideoMaker
import com.allever.video.editor.utils.AsyncTaskDefaultSerialExecutor
import com.allever.video.editor.utils.ImageLoader
import com.android.absbase.helper.ExceptionManager
import com.android.absbase.helper.UncaughtExceptionHandler
import com.android.absbase.helper.log.DLog
import com.android.absbase.helper.log.LogUtil
import com.android.absbase.helper.log.RLog
import com.android.absbase.ui.BaseApplication
import com.android.absbase.utils.AppUtils
import com.android.absbase.utils.DebugUtil
import com.android.absbase.utils.DeviceUtils
import me.xiaopan.sketch.Sketch
import me.xiaopan.sketch.cache.LruMemoryCache
import java.util.concurrent.Executor


object AppApplication {

    private val TAG = AppApplication::class.java.name
    var application: Application = App.app
        private set

    /**
     * 首次启动，数据延时初始化时间
     */
    private val DATA_INIT_DELAY_TIME = 2000
    fun onCreate() {
        try {
            com.android.absbase.App.setContext(App.context)
            AppUtils.getVersionCode(App.context)
        } catch (e: RuntimeException) {
            return
        }

        if (DebugUtil.isDebuggable()) {
            LogUtil.setFileLogEnable(true)
            initUncaughtExceptionHandler()
        }

        // 替换系统AsyncTask的Executor,editText会造成内存泄漏 使用完之后清空任务
        try {
            val asyncTaskManager = Class.forName("android.os.AsyncTask")
            val setDefaultExecutor = asyncTaskManager.getMethod("setDefaultExecutor", Executor::class.java)
            setDefaultExecutor.invoke(android.os.AsyncTask::class.java, AsyncTaskDefaultSerialExecutor.getInstance())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setSketchMemoryCacheSize()

        if (ProcessHelper.isInMainProcess(App.app)) {
            initData()
        }
        fixedBug()
    }

    private fun fixedBug() {
        // 解决出现exposed beyond app through ClipData.Item.getUri()
        try {
            val builder = StrictMode.VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
            builder.detectFileUriExposure()
        } catch (e: Exception) {
            if (DebugUtil.isDebuggable()) {
                DLog.printStackTrace(e)
            }
        }

    }

    private fun initData() {
        com.android.absbase.utils.thread.ThreadPool.runOnNonUIThread({
            try {
                VideoMaker.init(App.context)
            } catch (e: Exception) {
            }
        }, DATA_INIT_DELAY_TIME.toLong())
    }

    fun onLowMemory() {
        ImageLoader.onLowMemory()
    }

    private fun initUncaughtExceptionHandler() {
        ExceptionManager.getInstance().initiate(App.context)
        UncaughtExceptionHandler.getInstance(App.context).register()
        UncaughtExceptionHandler.getInstance(App.context).setInterceptor(object : UncaughtExceptionHandler.UncaughtExceptionInterceptor {

            override fun onInterceptExceptionBefore(t: Thread, ex: Throwable): Boolean {
                RLog.e("Exception", ex.message, ex)
                RLog.flush()
                SystemClock.sleep(500)
                return false
            }

            override fun onInterceptExceptionAfter(t: Thread, ex: Throwable): Boolean {
                return false
            }
        })
    }

    fun startActivity(intent: Intent?) {
        if (null != intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //处理admob广告多进程打开异常
            val component = intent.component
            if (null != component && "com.google.android.gms.ads.AdActivity" == component.className) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
        try {
            App.context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun fixFinalizeBug() {
        //        if (PlatformUtils.version() == PlatformUtils.VERSION_CODES.KITKAT) {
        //            try {
        //                Class clazz = Class.forName("java.lang.Daemons$FinalizerWatchdogDaemon");
        //                Method method = clazz.getSuperclass().getDeclaredMethod("stop");
        //                method.setAccessible(true);
        //
        //                Field field = clazz.getDeclaredField("INSTANCE");
        //                field.setAccessible(true);
        //
        //                method.invoke(field.get(null));
        //            } catch (Throwable e) {
        //                e.printStackTrace();
        //            }
        //        }
    }

    private fun setSketchMemoryCacheSize() {
        // 设置默认最大容量是 2 个屏幕像素数
        try {
            val screenSize = DeviceUtils.getRealScreenHeightPx() * DeviceUtils.getScreenWidthPx() * 4
            val memoryCacheMaxSize = screenSize * 2
            Sketch.with(App.context).configuration.memoryCache = LruMemoryCache(App.context, memoryCacheMaxSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun onTrimMemory(level: Int) {
        ImageLoader.onTrimMemroy(level)
    }
}
