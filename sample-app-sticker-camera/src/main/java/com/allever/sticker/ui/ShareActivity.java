package com.allever.sticker.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.allever.sticker.ControllerEnum;
import org.xm.sticker.camera.R;
import com.allever.sticker.util.Constant;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import app.allever.android.lib.core.base.AbstractActivity;

/**
 *
 * @author Allever
 * @date 18/1/3
 */

public class ShareActivity extends AbstractActivity implements View.OnClickListener{

    private static final int REQUEST_CODE_PICK_IMAGE = 0x01;

    public static final String EXTRA_IMAGE_PATH = "image_path";


    private String mImagePath;

    private ImageView mIvDisplay;

    private LinearLayout mLlBack;

    private TextView mTvHome;

    private ImageView mBtnShareMore;
    private ImageView mBtnFacebook;
    private ImageView mBtnTwitter;
    private ImageView mBtnLine;
    private ImageView mBtnWhatsapp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_share);

        mImagePath = getIntent().getStringExtra(EXTRA_IMAGE_PATH);

        //初始化控件及设置监听器
        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        MobService.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        MobService.onResume(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initView(){
        mIvDisplay = (ImageView)findViewById(R.id.id_share_iv);
        Glide.with(this).load(new File(mImagePath)).into(mIvDisplay);

        mTvHome = findViewById(R.id.id_share_tv_home);
        mTvHome.setOnClickListener(this);
        mLlBack = findViewById(R.id.id_share_ll_back);
        mLlBack.setOnClickListener(this);

        mBtnShareMore = (ImageView)findViewById(R.id.id_share_btn_more);
        mBtnShareMore.setOnClickListener(this);
        mBtnFacebook = (ImageView)findViewById(R.id.id_share_btn_facebook);
        mBtnFacebook.setOnClickListener(this);
        mBtnTwitter = (ImageView)findViewById(R.id.id_share_btn_twtter);
        mBtnTwitter.setOnClickListener(this);
        mBtnLine = (ImageView)findViewById(R.id.id_share_btn_line);
        mBtnLine.setOnClickListener(this);
        mBtnWhatsapp = (ImageView)findViewById(R.id.id_share_btn_whatsapp);
        mBtnWhatsapp.setOnClickListener(this);

        RelativeLayout rlEditNext = findViewById(R.id.id_share_rl_edit_next);
        rlEditNext.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.id_share_ll_back) {
            finish();
        } else if (id == R.id.id_share_tv_home) {
            backHome();
        } else if (id == R.id.id_share_btn_facebook) {
            shareSingle(Constant.PKG_FACEBOOK);
        } else if (id == R.id.id_share_btn_twtter) {
            shareSingle(Constant.PKG_TWTTER);
        } else if (id == R.id.id_share_btn_line) {
            shareSingle(Constant.PKG_LINE);
        } else if (id == R.id.id_share_btn_whatsapp) {
            shareSingle(Constant.PKG_WHATSAPP);
        } else if (id == R.id.id_share_btn_more) {
            shareMore();
        } else if (id == R.id.id_share_rl_edit_next) {//如果具有读取存储权限，则打开相册选择图片
            ControllerEnum.chooseImageFromGallery(this, REQUEST_CODE_PICK_IMAGE);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //先进行resultCode判断，避免每个requestCode都有进行判断
        if (resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_CODE_PICK_IMAGE:
                    /**
                     * 如何用户选择了图片，则打开剪裁界面
                     * 注意：打开剪裁界面后，该Activity不能销毁，否则从谷歌照片选择图片后的Uri是没有权限
                     * SecurityException: Permission Denial +
                     * * com.google.android.apps.photos.contentprovider.impl.MediaContentProvider* requires the provider be exported, or grantUriPermission()
                    */
                    CropActivity.startSelf(this, data.getData());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        backHome();
        super.onBackPressed();
    }

    private void backHome(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void shareSingle(String containAppPackage){
        Uri imageUri = Uri.fromFile(new File(mImagePath));
        Intent fbIntent = new Intent(Intent.ACTION_SEND);
        fbIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        fbIntent.setType("image/*");

        //判断手机是否安装该应用
        boolean existApp = false;
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(fbIntent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().contains(containAppPackage)) {
                fbIntent.setPackage(info.activityInfo.packageName);
                existApp = true;
            }
        }

        //如果已安装，则调用分享，否则提示未安装
        if (existApp){
            startActivity(fbIntent);
        }else {
            Toast.makeText(this,R.string.not_install_app,Toast.LENGTH_SHORT).show();
        }
    }

    private void shareMore(){
        Uri imageUri = Uri.fromFile(new File(mImagePath));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_to)));
    }

    public static void startSelf(Context context, String path){
        Intent intent = new Intent(context, ShareActivity.class);
        intent.putExtra(ShareActivity.EXTRA_IMAGE_PATH,path);
        context.startActivity(intent);
    }
}
