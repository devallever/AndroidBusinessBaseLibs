package com.allever.video.editor.utils;

import android.graphics.Typeface;
import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;

/**
 *
 */

public class FontUtil {

    public final static Typeface CUSTOM_FONT = Typeface.DEFAULT/*Typeface.createFromAsset(App.getContext().getAssets(),
            "fonts/josefinsans-regular.ttf")*/;
    public final static Typeface CUSTOM_FONT_BOLD = Typeface.DEFAULT_BOLD/*Typeface.createFromAsset(App.getContext().getAssets(),
            "fonts/JosefinSans-Bold.ttf")*/;

    public static void setCustomFont(TextView view) {
        /*if (view != null) {
            view.setTypeface(CUSTOM_FONT);
        }*/
    }

    public static void setCustomFontBold(TextView view) {
        /*if (view != null) {
            view.setTypeface(CUSTOM_FONT_BOLD);
        }*/
    }

    public static void setCustomFontBold(TextView... view) {
        for (TextView tmp : view) {
            setCustomFontBold(tmp);
        }
    }

    public static void setCustomFont(TextView... view) {
        for (TextView tmp : view) {
            setCustomFont(tmp);
        }
    }

    public static void setAlertDialogCustomFont(AlertDialog dialog) {
        /*if (dialog == null) {
            return;
        }
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object alertController = mAlert.get(dialog);

            Field mTitleView = alertController.getClass().getDeclaredField("mTitleView");
            mTitleView.setAccessible(true);

            TextView title = (TextView) mTitleView.get(alertController);
            FontUtil.setCustomFont(title);

            Field mMessageView = alertController.getClass().getDeclaredField("mMessageView");
            mMessageView.setAccessible(true);

            TextView messageView = (TextView) mMessageView.get(alertController);
            FontUtil.setCustomFont(messageView);

            Field mButtonPositive = alertController.getClass().getDeclaredField("mButtonPositive");
            mButtonPositive.setAccessible(true);

            TextView buttonPositive = (TextView) mButtonPositive.get(alertController);
            FontUtil.setCustomFont(buttonPositive);

            Field mButtonNeutral = alertController.getClass().getDeclaredField("mButtonNeutral");
            mButtonNeutral.setAccessible(true);

            TextView buttonNeutral = (TextView) mButtonNeutral.get(alertController);
            FontUtil.setCustomFont(buttonNeutral);

            Field mButtonNegative = alertController.getClass().getDeclaredField("mButtonNegative");
            mButtonNegative.setAccessible(true);

            TextView buttonNegative = (TextView) mButtonNegative.get(alertController);
            FontUtil.setCustomFont(buttonNegative);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }*/
    }
}
