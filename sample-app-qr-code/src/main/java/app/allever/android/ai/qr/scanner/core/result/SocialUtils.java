package app.allever.android.ai.qr.scanner.core.result;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.android.absbase.App;
import com.android.absbase.utils.AppUtils;

public class SocialUtils {

    public static final boolean isAppExist(String... packgenames) {
        Context context = App.getContext();
        for (String packagename: packgenames) {
            if (AppUtils.isAppExist(context, packagename)) {
                return true;
            }
        }
        return false;
    }

    public static final void showDialog(Activity activity, String appName, final String packageName) {
        /**
         * 弹窗提示文案：The Viber App is not installed and needs to be installed before it can be used. Do you need to install it?
         * 确定按钮：Install
         * 取消按钮：Not Now
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(String.format("The %s App is not installed and needs to be installed before it can be used. " +
                "Do you need to install it?", appName));
        builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AppUtils.gotoGPStoreOrBrowserByPackageName(packageName);
            }
        });
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        builder.show();
    }
}
