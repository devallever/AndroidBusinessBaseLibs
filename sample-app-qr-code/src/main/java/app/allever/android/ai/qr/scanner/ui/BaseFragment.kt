package app.allever.android.ai.qr.scanner.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.absbase.ui.BaseApplication
import com.android.absbase.ui.BaseFragment
import kotlin.jvm.internal.Intrinsics

open class BaseFragment : Fragment,Handler.Callback {

    protected var mApplication: Application? = null
    protected var mMainThread: Thread
    protected var mMainHandler: Handler
    protected var mNotifyToast: Toast? = null


    constructor() {
        val mainLooper = Looper.getMainLooper()
        Intrinsics.checkExpressionValueIsNotNull(mainLooper, "Looper.getMainLooper()")
        this.mMainThread = mainLooper.thread
        this.mMainHandler = Handler(Looper.getMainLooper(), this as Handler.Callback)
    }

    init {
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity != null) {
            mApplication = activity.application
            if (this.isBaseApplication()) {
                val var10000 = this.mApplication
                if (this.mApplication == null) {
                    throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
                }

                (var10000 as BaseApplication).dispatchFragmentAttachedInner(this as androidx.fragment.app.Fragment, activity)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (this.isBaseApplication()) {
            val application = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (application as BaseApplication).dispatchFragmentCreatedInner(this as androidx.fragment.app.Fragment, savedInstanceState)
        }

    }

    override fun onStart() {
        super.onStart()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentStartedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onResume() {
        super.onResume()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentResumedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentHiddenChangedInner(this as androidx.fragment.app.Fragment, hidden)
        }

    }

    override fun onPause() {
        super.onPause()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentPausedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onStop() {
        super.onStop()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentStoppedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState!!)
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentSaveInstanceStateInner(this as androidx.fragment.app.Fragment, outState)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentDestroyedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onDetach() {
        super.onDetach()
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentDetachedInner(this as androidx.fragment.app.Fragment)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentOnActivityResultInner(
                this as androidx.fragment.app.Fragment,
                requestCode,
                resultCode,
                data
            )
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (this.isBaseApplication()) {
            val var10000 = this.mApplication
            if (this.mApplication == null) {
                throw TypeCastException("null cannot be cast to non-null type com.android.absbase.ui.BaseApplication")
            }

            (var10000 as BaseApplication).dispatchFragmentActivityCreatedInner(this as androidx.fragment.app.Fragment, savedInstanceState)
        }

    }




    /////////////////

//    @JvmOverloads
//    fun showNotifyMessage(resId: Int, gravity: Int) {
//        this.showNotifyMessage(if (resId == 0) null else this.getString(resId), gravity)
//    }

    // $FF: synthetic method
    // $FF: bridge method
    @JvmOverloads
    fun `showNotifyMessage$default`(var0: BaseFragment, var1: Int, var2: Int, var3: Int, var4: Any?) {
        var var2 = var2
        if (var4 != null) {
            throw UnsupportedOperationException("Super calls with default arguments not supported in this target, function: showNotifyMessage")
        } else {
            if (var3 and 2 != 0) {
                var2 = 81
            }

            var0.showNotifyMessage(var1, var2)
        }
    }

//    @JvmOverloads
//    fun showNotifyMessage(resId: Int) {
//        `showNotifyMessage$default`(this, resId, 0, 2, null as Any?)
//    }
//
//    @JvmOverloads
//    fun showNotifyMessage(msg: String?, gravity: Int) {
//        if (msg != null && msg.length != 0 && !this.isDetached && this.activity != null) {
//            if (this.isMainThread()) {
//                val toast = this.obtainNotifyToast()
//                if (toast != null) {
//                    toast.setText(msg as CharSequence?)
//                    toast.setGravity(gravity, toast.xOffset, toast.yOffset)
//                    toast.show()
//                }
//            } else {
//                this.runOnUiThread(Runnable {
//                    val toast = this@BaseFragment.obtainNotifyToast()
//                    if (toast != null) {
//                        toast.setText(msg as CharSequence?)
//                        toast.setGravity(gravity, toast.xOffset, toast.yOffset)
//                        toast.show()
//                    }
//                })
//            }
//
//        }
//    }
//
//    // $FF: synthetic method
//    // $FF: bridge method
//    @JvmOverloads
//    fun `showNotifyMessage$default`(var0: BaseFragment, var1: String?, var2: Int, var3: Int, var4: Any?) {
//        var var2 = var2
//        if (var4 != null) {
//            throw UnsupportedOperationException("Super calls with default arguments not supported in this target, function: showNotifyMessage")
//        } else {
//            if (var3 and 2 != 0) {
//                var2 = 81
//            }
//
//            var0.showNotifyMessage(var1, var2)
//        }
//    }
//
//    @JvmOverloads
//    fun showNotifyMessage(msg: String?) {
//        showNotifyMessage$default(this, msg, 0, 2, null as Any?)
//    }
//
//    private fun obtainNotifyToast(): Toast? {
//        // $FF: Couldn't be decompiled
//    }

    open fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }

    override fun handleMessage(message: Message): Boolean {
        return if (message == null) {
            false
        } else {
            val activity = this.activity
            if (activity != null && !activity.isFinishing) {
                !this.isRemoving && !this.isDetached && this.handleMessageLogic(message)
            } else {
                false
            }
        }
    }


    open fun handleMessageLogic(message: Message?): Boolean {
        return false
    }

    fun post(r: Runnable) {
        Intrinsics.checkParameterIsNotNull(r, "r")
        this.mMainHandler.post(r)
    }

    fun postDelayed(r: Runnable, delayMillis: Long) {
        Intrinsics.checkParameterIsNotNull(r, "r")
        this.mMainHandler.postDelayed(r, delayMillis)
    }

    private fun isBaseApplication(): Boolean {
        return this.mApplication is BaseApplication
    }

    fun isMainThread(): Boolean {
        return this.mMainThread === Thread.currentThread()
    }

    fun runOnUiThread(action: Runnable) {
        Intrinsics.checkParameterIsNotNull(action, "action")
        if (!this.isMainThread()) {
            this.mMainHandler.post(action)
        } else {
            action.run()
        }

    }
}