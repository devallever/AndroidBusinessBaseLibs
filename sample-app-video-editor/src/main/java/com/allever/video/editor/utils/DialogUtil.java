package com.allever.video.editor.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.allever.video.editor.R;


public class DialogUtil {

    /**
     * 展示一个普通的ProgressDialog 编辑界面保存的那种
     * @param context
     * @param cancelable
     * @param canceledOnTouchOutside
     * @return
     */
    public static ProgressDialog showConmonProgressDialog(Context context, boolean cancelable, boolean canceledOnTouchOutside){
        ProgressDialog progressDialog;
        final View v = LayoutInflater.from(context).inflate(R.layout.progress_bar, null, false);
        progressDialog = new ProgressDialog(context, ProgressDialog.THEME_TRADITIONAL);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        progressDialog.show();
        progressDialog.setContentView(v, lp);
        return progressDialog;
    }

    /**
     * 展示一个遮罩的ProgressDialog
     * 不能取消
     * @param context
     * @return
     */
    public static ProgressDialog showCoverProgressDialog(Context context){
        ProgressDialog progressDialog;
        final View v = LayoutInflater.from(context).inflate(R.layout.progress_bar, null, false);
        progressDialog = new ProgressDialog(context, R.style.Dialog_Fullscreen);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.CENTER;
        progressDialog.show();
        v.setVisibility(View.GONE);//只是增加一层遮罩防止用户操作
        progressDialog.setContentView(v, lp);
        return progressDialog;
    }

}
