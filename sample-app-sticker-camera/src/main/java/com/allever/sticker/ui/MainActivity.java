package com.allever.sticker.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.isseiaoki.simplecropview.util.Utils;
import com.allever.sticker.ControllerEnum;
import org.xm.sticker.camera.R;

import app.allever.android.lib.core.base.AbstractActivity;


/**
 *
 * @author Allever
 * @date 2018/1/1
 */

public class MainActivity extends AbstractActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private static final int REQUEST_PICK_IMAGE = 1001;
    private static final int REQUEST_SAF_PICK_IMAGE = 1002;
    private static final int RESULD_CODE_TAKE_PHOTO = 1004;

    private ImageView btn_pick_img;
    private ImageView btn_camera;
    private ImageView btn_store;

    private Uri mImageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_main);

        initView();
    }

    private void initView(){
        btn_pick_img = (ImageView)findViewById(R.id.id_main_btn_pick_img);
        btn_pick_img.setOnClickListener(this);
        btn_camera = (ImageView)findViewById(R.id.id_main_btn_camera);
        btn_camera.setOnClickListener(this);
        btn_store = (ImageView)findViewById(R.id.id_main_btn_store);
        btn_store.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.id_main_btn_pick_img) {//如果有存储权限，则打开相册选择图片，否则申请存储权限
            ControllerEnum.chooseImageFromGallery(this, REQUEST_PICK_IMAGE);
        } else if (id == R.id.id_main_btn_camera) {//如果有相机权限，则打开相机，否则申请相机权限
            mImageUri = ControllerEnum.openCamera(this, RESULD_CODE_TAKE_PHOTO);
        } else if (id == R.id.id_main_btn_store) {
            StoreActivity.startSelf(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    CropActivity.startSelf(this,result.getData());
                    break;
                case REQUEST_SAF_PICK_IMAGE:
                    CropActivity.startSelf(this, Utils.ensureUriPermission(MainActivity.this, result));
                    break;
                case RESULD_CODE_TAKE_PHOTO:
                    CropActivity.startSelf(this,mImageUri);
                    break;
                default:
                    break;
            }
        }
    }

    private long mPrevClickBackTime = -1;
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (mPrevClickBackTime == -1 || currentTime - mPrevClickBackTime > 3000) {
            mPrevClickBackTime = currentTime;
            Toast.makeText(this, "Press again to exit",
                    Toast.LENGTH_LONG).show();
            return;
        }
        Process.killProcess(Process.myPid());
    }
}
