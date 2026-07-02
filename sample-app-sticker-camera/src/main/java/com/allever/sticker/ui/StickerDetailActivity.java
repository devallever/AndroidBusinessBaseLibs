package com.allever.sticker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.isseiaoki.simplecropview.util.Utils;
import com.allever.sticker.ControllerEnum;
import org.xm.sticker.camera.R;
import com.allever.sticker.ui.adapter.StickerDetailRecyclerAdapter;
import com.allever.sticker.bean.StickerDetailItem;
import com.allever.sticker.ui.dialog.ApplyDialog;
import com.allever.sticker.event.DownloadEvent;
import com.allever.sticker.util.Constant;
import com.allever.sticker.util.DialogUtil;
import com.allever.sticker.util.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.allever.android.lib.core.base.AbstractActivity;

/**
 *
 * @author Allever
 * @date 18/2/10
 */

public class StickerDetailActivity extends AbstractActivity implements View.OnClickListener{
    private static final String TAG = "StickerDetailActivity";

    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_SAF_PICK_IMAGE = 1002;
    private static final int RESULD_CODE_TAKE_PHOTO = 1004;
    private static final int REQUEST_PERMISSION = 1005;
    private static final int REQUEST_CODE_PERMISSION_SETTING = 1006;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 1007;
    private static final int REQUEST_CODE_PERMISSION_STORAGE = 1008;

    private String mStoreDir;
    private String mStickerName;
    private TextView tvDownload;
    private StickerDetailItem mStickerDetailItem;

    private StickerDetailRecyclerAdapter mStickerDetailRecyclerAdapter;
    private List<String> mPathList = new ArrayList<>();

    private ImageView mIvHead;
    private ImageView mIvStickerType;
    private TextView mTvName;
    private TextView mTvSize;
    private CardView mCardContainer;

    private LinearLayout mNetErrorContainer;

    private Uri mImageUri;
    private boolean mEditing;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_sticker_detail);

        //获取下载贴纸的路径
        mStoreDir = FileUtil.getStoreDir(this);

        EventBus.getDefault().register(this);

        //获取当前编辑状态，从编辑界面进入商店时为true，从首页进入商店时为false
        mEditing = getIntent().getBooleanExtra(Constant.EXTRA_EDITOR_ING, false);

        mStickerName = getIntent().getStringExtra(Constant.EXTRA_STICKER_NAME);

        if (mStickerName == null){
            Toast.makeText(this,R.string.not_exist_sticker,Toast.LENGTH_SHORT).show();
            return;
        }

        initView();

        initDialog();

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void initView(){
        tvDownload = findViewById(R.id.id_sticker_detail_tv_download);
        tvDownload.setOnClickListener(this);
        if (isDownloaded()){
            tvDownload.setText(R.string.apply);
        }

        mIvHead = findViewById(R.id.id_sticker_detail_iv_type_head);
        mIvStickerType = findViewById(R.id.id_sticker_detail_iv_type);
        mTvName = findViewById(R.id.id_sticker_detail_tv_name);
        mTvSize = findViewById(R.id.id_sticker_detail_tv_file_size);

        RecyclerView mRecyclerView = findViewById(R.id.id_sticker_detail_rv_sticker);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6));
        mStickerDetailRecyclerAdapter = new StickerDetailRecyclerAdapter(this, mPathList);
        mRecyclerView.setAdapter(mStickerDetailRecyclerAdapter);

        findViewById(R.id.id_sticker_detail_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mCardContainer = findViewById(R.id.id_card_container);

        mNetErrorContainer = findViewById(R.id.id_net_error);
        Button mBtnRetry = findViewById(R.id.id_net_error_btn_retry);
        mBtnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNetErrorContainer.setVisibility(View.INVISIBLE);
                mProgressDialog.show();
            }
        });
    }

    private AlertDialog mProgressDialog;
    private ApplyDialog mApplyDialog;
    private void initDialog(){
        mProgressDialog = DialogUtil.createProgressAlertDialog(this, R.string.loading,true);

        mApplyDialog = new ApplyDialog(this,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mApplyDialog.hide();
                        //choose pic
                        ControllerEnum.chooseImageFromGallery(StickerDetailActivity.this, REQUEST_PICK_IMAGE);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mApplyDialog.hide();
                        //open camera
                        mImageUri = ControllerEnum.openCamera(StickerDetailActivity.this, RESULD_CODE_TAKE_PHOTO);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    CropActivity.startSelf(this, result.getData());
                    break;
                case REQUEST_SAF_PICK_IMAGE:
                    CropActivity.startSelf(this, Utils.ensureUriPermission(StickerDetailActivity.this, result));
                    break;
                case RESULD_CODE_TAKE_PHOTO:
                    CropActivity.startSelf(this, mImageUri);
                    break;
                default:
                    break;
            }
        }
    }


    private void setImageIntoIv(){
        if (mStickerDetailItem != null){
            Glide.with(this).load(mStickerDetailItem.getTypeurl()).into(mIvHead);
            Glide.with(this).load(mStickerDetailItem.getFitststickurl()).into(mIvStickerType);
            mTvSize.setText(getResources().getString(R.string.size) + mStickerDetailItem.getSize());
            mTvName.setText(mStickerDetailItem.getName());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.id_sticker_detail_tv_download) {//判断是否已经下载
            if (!isDownloaded()) {
                //没有下载，则开启服务下载贴纸

            } else {
                //已经下载，
                //判断当前是否正在存在编辑的状态
                if (mEditing) {
                    //正在处于编辑状态，则发送通知，刷新编辑界面，主要是刷新贴纸数， 并退出当前Activity
                    EventBus.getDefault().post(Constant.EVENT_FINISH_STORE_ACTIVITY);
                    finish();
                } else {
                    //如果不是编辑状态，则弹出对话框，选择图片或打开相机
                    mApplyDialog.show();
                }
            }
        }
    }
    private boolean isDownloaded(){
        File stickerDir = new File(mStoreDir + "/" + mStickerName);
        if (stickerDir.exists()){
            return stickerDir.list().length > 0;
        }else {
            return false;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloading(DownloadEvent  event){
        //下载时会发送该事件刷新显示进度
        tvDownload.setText(event.getProgress() + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadListener(String event){
        if (Constant.EVENT_DOWNLOAD_FINISH.equals(event) || Constant.EVENT_DOWNLOAD_ERROR.equals(event )){
            tvDownload.setText(R.string.apply);
        }
        if (Constant.EVENT_DOWNLOAD_ERROR.equals(event)){
            tvDownload.setText(R.string.retry);
            Toast.makeText(this, R.string.download_fail, Toast.LENGTH_SHORT).show();
        }
        tvDownload.setBackgroundColor(getResources().getColor(R.color.sc_colorAccent));
        tvDownload.setClickable(true);

    }

    public static void startSelf(Context context, String stickerName, boolean editing){
        Intent intent = new Intent(context, StickerDetailActivity.class);
        intent.putExtra(Constant.EXTRA_STICKER_NAME, stickerName);
        intent.putExtra(Constant.EXTRA_EDITOR_ING, editing);
        context.startActivity(intent);
    }

}
