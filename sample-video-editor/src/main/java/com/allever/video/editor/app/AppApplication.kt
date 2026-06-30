package com.allever.video.editor.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.SystemClock
import androidx.multidex.MultiDex
import butterknife.ButterKnife
import com.allever.video.editor.function.save.VideoMaker
import com.allever.video.editor.utils.AsyncTaskDefaultSerialExecutor
import com.allever.video.editor.utils.ImageLoader
import com.android.absbase.App
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


class AppApplication : BaseApplication() {

    companion object {
        private val TAG = AppApplication::class.java.name
        var application: AppApplication? = null
            private set

        /**
         * 首次启动，数据延时初始化时间
         */
        private val DATA_INIT_DELAY_TIME = 2000
    }

    override fun onCreate() {
        super.onCreate()
        com.allever.lib.common.app.App.context = this
        App.setContext(this)

        try {
            AppUtils.getVersionCode(App.getContext())
        } catch (e: RuntimeException) {
            return
        }

        if (DebugUtil.isDebuggable()) {
            LogUtil.setFileLogEnable(true)
            initUncaughtExceptionHandler()
        }
        ButterKnife.setDebug(DebugUtil.isDebuggable())

        // 替换系统AsyncTask的Executor,editText会造成内存泄漏 使用完之后清空任务
        try {
            val asyncTaskManager = Class.forName("android.os.AsyncTask")
            val setDefaultExecutor = asyncTaskManager.getMethod("setDefaultExecutor", Executor::class.java)
            setDefaultExecutor.invoke(android.os.AsyncTask::class.java, AsyncTaskDefaultSerialExecutor.getInstance())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setSketchMemoryCacheSize()

        if (App.isMainProcess()) {
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
                VideoMaker.init(App.getContext())
            } catch (e: Exception) {
            }
        }, DATA_INIT_DELAY_TIME.toLong())
    }

    override fun attachBaseContext(base: Context) {
        application = this
        super.attachBaseContext(base)
        MultiDex.install(this)
        fixFinalizeBug()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ImageLoader.onLowMemory()
    }

    private fun initUncaughtExceptionHandler() {
        ExceptionManager.getInstance().initiate(this)
        UncaughtExceptionHandler.getInstance(this).register()
        UncaughtExceptionHandler.getInstance(this).setInterceptor(object : UncaughtExceptionHandler.UncaughtExceptionInterceptor {

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

    override fun startActivity(intent: Intent?) {
        if (null != intent) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //处理admob广告多进程打开异常
            val component = intent.component
            if (null != component && "com.google.android.gms.ads.AdActivity" == component.className) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
        try {
            super.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun dispatchActivityStartedInner(activity: Activity) {
        super.dispatchActivityStartedInner(activity)
    }

    override fun dispatchActivityStoppedInner(activity: Activity) {
        super.dispatchActivityStoppedInner(activity)
        if (App.isBackground()) {
            ImageLoader.clearMemoryCache()
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
            Sketch.with(App.getContext()).configuration.memoryCache = LruMemoryCache(App.getContext(), memoryCacheMaxSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ImageLoader.onTrimMemroy(level)
    }

}
