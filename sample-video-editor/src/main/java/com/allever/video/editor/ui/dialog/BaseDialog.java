package com.allever.video.editor.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.allever.video.editor.R;


public class BaseDialog extends Dialog {

    protected View mRootView;

    protected TextView mTitle;

    protected TextView mContent;

    protected Button mPositiveButton;

    protected Button mNegativeButton;

    private boolean mIsCallDismiss = false;


    public BaseDialog(Context context) {
        this(context, R.style.dialog_style);
    }

    public BaseDialog(Context context, int theme) {
        super(context, theme);
        initDefaultView(context);
    }

    protected BaseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initDefaultView(context);
    }

    public void show(Context context) {
        if (!(context instanceof Activity)) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        try {
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        Context context = getContext();
        if (context instanceof ContextThemeWrapper) {
            context = ((ContextThemeWrapper) context).getBaseContext();
        }
        if (!(context instanceof Activity)) {
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        try {
            super.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initDefaultView(Context context) {
        // 这里暂时不设置为全屏，设置为全屏后点击外围不能关闭窗口
//        setParams(context);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mRootView = view;
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        mRootView = view;
    }

    public View getContentView() {
        return mRootView;
    }

    public void setTitle(int text) {
        if (null != mTitle && text > 0) {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(text);
        }
    }

    public void setTitle(String title) {
        if (null != mTitle && !TextUtils.isEmpty(title)) {
            mTitle.setVisibility(View.VISIBLE);
            mTitle.setText(title);
        }
    }

    public void setMessage(int text) {
        if (null != mContent && text > 0) {
            String[] contents = getContext().getResources().getStringArray(text);
            if (null != contents && contents.length > 0) {
                StringBuffer stringBuffer = new StringBuffer();
                for (String rs : contents) {
                    stringBuffer.append(rs).append("\n");
                }
                mContent.setText(stringBuffer.toString());
            }
        }
    }

    public void setMessage(String msg) {
        if (null != mContent && !TextUtils.isEmpty(msg)) {
            mContent.setText(msg);
        }
    }

    public void setPositiveButton(int textId, View.OnClickListener listener) {
        if (null != mPositiveButton && textId > 0) {
            mPositiveButton.setVisibility(View.VISIBLE);
            mPositiveButton.setText(textId);
            mPositiveButton.setOnClickListener(listener);
        }
    }

    public void setPositiveButtonEnable(boolean enable) {
        if (null != mPositiveButton) {
            mPositiveButton.setEnabled(enable);
        }
    }

    public void setNegativeButton(int textId, View.OnClickListener listener) {
        if (null != mNegativeButton && textId > 0) {
            mNegativeButton.setVisibility(View.VISIBLE);
            mNegativeButton.setText(textId);
            mNegativeButton.setOnClickListener(listener);
        }
    }

    protected void setParams(Context context) {
        WindowManager.LayoutParams lay = this.getWindow().getAttributes();
        DisplayMetrics dm = new DisplayMetrics();
        if (context instanceof Activity) {
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        } else {
            WindowManager wmManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wmManager.getDefaultDisplay().getMetrics(dm);
        }
        Rect rect = new Rect();
        View view = getWindow().getDecorView();
        view.getWindowVisibleDisplayFrame(rect);
        lay.height = dm.heightPixels - rect.top;
        lay.width = dm.widthPixels;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mIsCallDismiss) {
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public void dismissWithAnimation() {
        if (mIsCallDismiss) {
            return;
        }
        mIsCallDismiss = true;
        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                BaseDialog.super.dismiss();
                mIsCallDismiss = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        set.addAnimation(alphaAnimation);

        RotateAnimation rotateAnimation = new RotateAnimation(0, 10, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(100);
        set.addAnimation(rotateAnimation);

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 2);
        translateAnimation.setStartOffset(100);
        translateAnimation.setDuration(400);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        set.addAnimation(translateAnimation);

        if (null != mRootView) {
            mRootView.startAnimation(set);
        } else {
            super.dismiss();
        }
    }

}
