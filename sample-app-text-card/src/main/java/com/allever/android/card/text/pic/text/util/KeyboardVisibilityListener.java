package com.allever.android.card.text.pic.text.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.view.inputmethod.InputMethodManager;

public class KeyboardVisibilityListener {

    private Context mContext;
    private KeyboardVisibilityListener.OnKeyboardVisibilityListener onKeyboardVisibilityListener;

    public KeyboardVisibilityListener(Context context, KeyboardVisibilityListener.OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        mContext = context;
        this.onKeyboardVisibilityListener = onKeyboardVisibilityListener;

        // 注册广播接收器来监听键盘状态的改变
        mContext.registerReceiver(receiver, new IntentFilter(
                Intent.ACTION_INPUT_METHOD_CHANGED));
        mContext.registerReceiver(receiver, new IntentFilter(
                Intent.ACTION_CONFIGURATION_CHANGED));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_INPUT_METHOD_CHANGED) ||
                    intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                boolean isKeyboardOpen = isKeyboardUp(context);
                if (onKeyboardVisibilityListener != null) {
                    if (isKeyboardOpen) {
                        onKeyboardVisibilityListener.onKeyboardVisible();
                    } else {
                        onKeyboardVisibilityListener.onKeyboardHidden();
                    }
                }
            }
        }
    };

    private boolean isKeyboardUp(Context context) {
        // 检查是否有物理键盘
        boolean isHardKeyboardAvailable = false;
        try {
            Configuration config = context.getResources().getConfiguration();
            isHardKeyboardAvailable = (config.keyboard != Configuration.KEYBOARD_NOKEYS)
                    && ((config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
                    || (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_UNDEFINED));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 如果没有物理键盘，假设键盘已经弹出
        if (!isHardKeyboardAvailable) {
            return true;
        }

        // 检查是否有软键盘输入法正在运行
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isAcceptingText();

        return isOpen;
    }

    public interface OnKeyboardVisibilityListener {
        void onKeyboardVisible();

        void onKeyboardHidden();
    }

    // 注销广播接收器
    public void unregisterReceiver() {
        mContext.unregisterReceiver(receiver);
    }
}