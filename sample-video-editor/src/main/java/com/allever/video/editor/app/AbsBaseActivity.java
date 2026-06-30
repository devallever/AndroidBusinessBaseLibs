package com.allever.video.editor.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Handler.Callback;
import androidx.appcompat.app.AppCompatActivity;

import com.android.absbase.ui.BaseApplication;

import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//@Metadata(
//        mv = {1, 1, 11},
//        bv = {1, 0, 2},
//        k = 1,
//        d1 = {"\u0000l\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0005\b\u0016\u0018\u00002\u00020\u0001B\u0005¢\u0006\u0002\u0010\u0002J\u001a\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010 \u001a\u0004\u0018\u00010!H\u0002J\u0010\u0010\"\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010#\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J*\u0010$\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020&2\b\u0010(\u001a\u0004\u0018\u00010)H\u0002J\u0010\u0010*\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u001a\u0010+\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\b\u0010,\u001a\u0004\u0018\u00010!H\u0002J\u0010\u0010-\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010.\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0010\u0010/\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\u0012\u00100\u001a\u00020\b2\b\u00101\u001a\u0004\u0018\u000102H\u0004J\"\u00103\u001a\u00020\u001d2\u0006\u0010%\u001a\u00020&2\u0006\u0010'\u001a\u00020&2\b\u0010(\u001a\u0004\u0018\u00010)H\u0014J\u0012\u00104\u001a\u00020\u001d2\b\u0010 \u001a\u0004\u0018\u00010!H\u0014J\b\u00105\u001a\u00020\u001dH\u0014J\b\u00106\u001a\u00020\u001dH\u0014J\b\u00107\u001a\u00020\u001dH\u0014J\u0012\u00108\u001a\u00020\u001d2\b\u0010,\u001a\u0004\u0018\u00010!H\u0014J\b\u00109\u001a\u00020\u001dH\u0014J\b\u0010:\u001a\u00020\u001dH\u0014J\b\u0010;\u001a\u00020\u001dH\u0014J\u000e\u0010<\u001a\u00020\u001d2\u0006\u0010=\u001a\u00020>J\u0016\u0010?\u001a\u00020\u001d2\u0006\u0010=\u001a\u00020>2\u0006\u0010@\u001a\u00020AJ\u000e\u0010B\u001a\u00020\u001d2\u0006\u0010=\u001a\u00020>J\u0012\u0010C\u001a\u00020\u001d2\b\u0010D\u001a\u0004\u0018\u00010)H\u0016J\u001a\u0010E\u001a\u00020\u001d2\b\u0010D\u001a\u0004\u0018\u00010)2\u0006\u0010%\u001a\u00020&H\u0016R\u0013\u0010\u0003\u001a\u0004\u0018\u00010\u00048F¢\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R$\u0010\t\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b@BX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\t\u0010\n\"\u0004\b\u000b\u0010\fR$\u0010\r\u001a\u00020\b2\u0006\u0010\u0007\u001a\u00020\b@BX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\r\u0010\n\"\u0004\b\u000e\u0010\fR\u0014\u0010\u000f\u001a\u00020\b8BX\u0082\u0004¢\u0006\u0006\u001a\u0004\b\u000f\u0010\nR\u0011\u0010\u0010\u001a\u00020\b8F¢\u0006\u0006\u001a\u0004\b\u0010\u0010\nR\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004¢\u0006\u0002\n\u0000R\u0016\u0010\u0013\u001a\n \u0015*\u0004\u0018\u00010\u00140\u0014X\u0082\u0004¢\u0006\u0002\n\u0000R$\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0007\u001a\u00020\u0016@DX\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0018\u0010\u0019\"\u0004\b\u001a\u0010\u001b¨\u0006F"},
//        d2 = {"Lcom/android/absbase/ui/BaseActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "baseApplication", "Lcom/android/absbase/ui/BaseApplication;", "getBaseApplication", "()Lcom/android/absbase/ui/BaseApplication;", "<set-?>", "", "isActivityResumed", "()Z", "setActivityResumed", "(Z)V", "isActivityStarted", "setActivityStarted", "isBaseApplication", "isMainThread", "mHandlerCallback", "Landroid/os/Handler$Callback;", "mMainThread", "Ljava/lang/Thread;", "kotlin.jvm.PlatformType", "Landroid/os/Handler;", "mainHandler", "getMainHandler", "()Landroid/os/Handler;", "setMainHandler", "(Landroid/os/Handler;)V", "dispatchActivityCreatedInner", "", "activity", "Landroid/app/Activity;", "savedInstanceState", "Landroid/os/Bundle;", "dispatchActivityDestroyedInner", "dispatchActivityPausedInner", "dispatchActivityResultInner", "requestCode", "", "resultCode", "data", "Landroid/content/Intent;", "dispatchActivityResumedInner", "dispatchActivitySaveInstanceStateInner", "outState", "dispatchActivityStartedInner", "dispatchActivityStoppedInner", "dispatchActivityUserLeaveHintInner", "handleMessageLogic", "message", "Landroid/os/Message;", "onActivityResult", "onCreate", "onDestroy", "onPause", "onResume", "onSaveInstanceState", "onStart", "onStop", "onUserLeaveHint", "post", "r", "Ljava/lang/Runnable;", "postDelayed", "delayMillis", "", "removeCallbacks", "startActivity", "intent", "startActivityForResult", "Toolkit_release"}
//)
public class AbsBaseActivity extends AppCompatActivity {
    private final Callback mHandlerCallback = (Callback)(new Callback() {
        public final boolean handleMessage(Message message) {
            if (message == null) {
                return false;
            } else {
                return AbsBaseActivity.this.isFinishing() ? false : AbsBaseActivity.this.handleMessageLogic(message);
            }
        }
    });
    private final Thread mMainThread;
    @NotNull
    private Handler mainHandler;
    private boolean isActivityResumed;
    private boolean isActivityStarted;

    @NotNull
    public final Handler getMainHandler() {
        return this.mainHandler;
    }

    protected final void setMainHandler(@NotNull Handler var1) {
        Intrinsics.checkParameterIsNotNull(var1, "<set-?>");
        this.mainHandler = var1;
    }

    public final boolean isActivityResumed() {
        return this.isActivityResumed;
    }

    private final void setActivityResumed(boolean var1) {
        this.isActivityResumed = var1;
    }

    public final boolean isActivityStarted() {
        return this.isActivityStarted;
    }

    private final void setActivityStarted(boolean var1) {
        this.isActivityStarted = var1;
    }

    private final boolean isBaseApplication() {
        return this.getApplication() instanceof BaseApplication;
    }

    @Nullable
    public final BaseApplication getBaseApplication() {
        Application application = this.getApplication();
        Application var10000 = application;
        if (!(application instanceof BaseApplication)) {
            var10000 = null;
        }

        return (BaseApplication)var10000;
    }

    public final boolean isMainThread() {
        return this.mMainThread == Thread.currentThread();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dispatchActivityCreatedInner((Activity)this, savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.isActivityResumed = true;
        this.dispatchActivityResumedInner((Activity)this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.isActivityStarted = true;
        this.dispatchActivityStartedInner((Activity)this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.isActivityResumed = false;
        this.dispatchActivityPausedInner((Activity)this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.isActivityStarted = false;
        this.dispatchActivityStoppedInner((Activity)this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.dispatchActivityDestroyedInner((Activity)this);
    }

    @Override
    public void startActivity(@Nullable Intent intent) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(@Nullable Intent intent, int requestCode) {
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        this.dispatchActivitySaveInstanceStateInner((Activity)this, outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.dispatchActivityResultInner((Activity)this, requestCode, resultCode, data);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        this.dispatchActivityUserLeaveHintInner((Activity)this);
    }

    protected final boolean handleMessageLogic(@Nullable Message message) {
        return false;
    }

    public final void post(@NotNull Runnable r) {
        Intrinsics.checkParameterIsNotNull(r, "r");
        this.mainHandler.post(r);
    }

    public final void postDelayed(@NotNull Runnable r, long delayMillis) {
        Intrinsics.checkParameterIsNotNull(r, "r");
        this.mainHandler.postDelayed(r, delayMillis);
    }

    public final void removeCallbacks(@NotNull Runnable r) {
        Intrinsics.checkParameterIsNotNull(r, "r");
        this.mainHandler.removeCallbacks(r);
    }

    private final void dispatchActivityCreatedInner(Activity activity, Bundle savedInstanceState) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityCreatedInner(activity, savedInstanceState);
        }

    }

    private final void dispatchActivityStartedInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityStartedInner(activity);
        }

    }

    private final void dispatchActivityResumedInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityResumedInner(activity);
        }

    }

    private final void dispatchActivityPausedInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityPausedInner(activity);
        }

    }

    private final void dispatchActivityStoppedInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityStoppedInner(activity);
        }

    }

    private final void dispatchActivitySaveInstanceStateInner(Activity activity, Bundle outState) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivitySaveInstanceStateInner(activity, outState);
        }

    }

    private final void dispatchActivityDestroyedInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityDestroyedInner(activity);
        }

    }

    private final void dispatchActivityUserLeaveHintInner(Activity activity) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityUserLeaveHintInner(activity);
        }

    }

    private final void dispatchActivityResultInner(Activity activity, int requestCode, int resultCode, Intent data) {
        BaseApplication baseApplication = this.getBaseApplication();
        if (baseApplication != null) {
            baseApplication.dispatchActivityResultInner(activity, requestCode, resultCode, data);
        }

    }

    public AbsBaseActivity() {
        Looper var10001 = Looper.getMainLooper();
        Intrinsics.checkExpressionValueIsNotNull(var10001, "Looper.getMainLooper()");
        this.mMainThread = var10001.getThread();
        this.mainHandler = new Handler(Looper.getMainLooper(), this.mHandlerCallback);
    }
}

