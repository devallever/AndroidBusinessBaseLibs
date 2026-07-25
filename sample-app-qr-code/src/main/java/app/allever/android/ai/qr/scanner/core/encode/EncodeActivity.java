/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.allever.android.ai.qr.scanner.core.encode;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.activity.OnBackPressedCallback;

import com.android.absbase.ui.BaseActivity;
import com.android.absbase.utils.ToastUtils;

import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.FinishListener;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.R;
import com.google.zxing.client.android.SystemAlbumHelper;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ParsedResultType;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.client.result.SMSTOMMSTOResultParser;
import com.google.zxing.client.result.SMTPResultParser;
import com.google.zxing.client.result.TelResultParser;
import com.google.zxing.client.result.WifiResultParser;

import app.allever.android.ai.qr.scanner.Config;
import app.allever.android.ai.qr.scanner.bean.ShareItem;
import app.allever.android.ai.qr.scanner.core.history.GenerateHistoryManager;
import app.allever.android.ai.qr.scanner.ui.dialog.SaveQRCodeImgDialog;
import app.allever.android.ai.qr.scanner.core.encode.custom.CommonAdapter;
import app.allever.android.ai.qr.scanner.core.encode.custom.CustomEncodeParamsBean;
import app.allever.android.ai.qr.scanner.core.encode.custom.CustomQrCodeManager;
import app.allever.android.ai.qr.scanner.core.encode.custom.CustomQrCodeUtils;
import app.allever.android.ai.qr.scanner.core.encode.custom.ViewHolder;
import app.allever.android.ai.qr.scanner.core.result.ResultHandlerFactory;
import app.allever.android.ai.qr.scanner.ui.widget.HorizontalListView;
import app.allever.android.lib.core.app.App;
import app.allever.android.lib.core.function.notchcompat.NotchCompat;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 展示二维码界面
 * <p>
 * This class encodes data from an Intent into a QR code, and then displays it full screen so that
 * another person can scan it with their device.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class EncodeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = EncodeActivity.class.getSimpleName();


    public static final String INTENT_KEY_LOCK = "ik_lock";



    private static final int RC_PERMISSION_STORAGE = 0x01;
    private static final int RC_SETTING = 0x02;

    private static final int MAX_BARCODE_FILENAME_LENGTH = 24;
    private static final Pattern NOT_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");
    private static final String USE_VCARD_KEY = "USE_VCARD";

    private QRCodeEncoder qrCodeEncoder;

    private SaveQRCodeImgDialog mSaveQRCodeImgDialog;

    private Bitmap mBitmapQRCode = null;

    private Button mBtnShare;
    private ImageView mIvQRCode;
    private TextView mTvType;
    private TextView mTvContent;
    private ImageView ivLock;
    private Button btnPurchase;
    private int purchaseSize = -1;
    private int shareType;

    private HorizontalListView mHorizontalListView;
    private ImageView mPickPhoto;
    private List<CustomEncodeParamsBean> mCustomStyleQrCodes;
    private CommonAdapter<CustomEncodeParamsBean> mCommonAdapter;
    private CustomEncodeParamsBean mCurrentCustomEncodeParamsBean;
    private CustomEncodeParamsBean mPreCustomEncodeParamsBean;

    /**
     * 整个页面上锁
     */
    private boolean mLock = false;

    private boolean mNeedVideoAd = true;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
        } else {
            String action = intent.getAction();
            mLock = intent.getBooleanExtra(INTENT_KEY_LOCK, false);
            shareType = intent.getIntExtra(Constant.EXTRA_SHARE_TYPE, ShareItem.TYPE_TEXT);
            if (Intents.Encode.ACTION.equals(action) || Intent.ACTION_SEND.equals(action)) {
                setContentView(com.allever.app.qr.code.scaner.R.layout.activity_encode);
                initView();
            } else {
                finish();
            }
        }

        updateLockState();

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }


    private void initView() {
        NotchCompat.INSTANCE.adaptNotchWithFullScreen(getWindow());
        mIvQRCode = (ImageView) findViewById(R.id.image_view);
        mTvType = (TextView) findViewById(com.allever.app.qr.code.scaner.R.id.tv_encode_share_type);
        mTvContent = (TextView) findViewById(com.allever.app.qr.code.scaner.R.id.tv_encode_share_content);
        ivLock = (ImageView) findViewById(com.allever.app.qr.code.scaner.R.id.iv_lock);
        btnPurchase = (Button) findViewById(com.allever.app.qr.code.scaner.R.id.btn_continue_buy);
        mHorizontalListView = findViewById(com.allever.app.qr.code.scaner.R.id.custom_qrcode_listview);
        mPickPhoto = findViewById(com.allever.app.qr.code.scaner.R.id.iv_custom_select_photo);
        if (mPickPhoto != null) {
            mPickPhoto.setOnClickListener(this);
        }
        List<CustomEncodeParamsBean> customStyleQrcodeData = CustomQrCodeManager.getCustomStyleQrcodeData(shareType);
        mCustomStyleQrCodes = customStyleQrcodeData;
        //选择框复位
        for (int i = 0; i<mCustomStyleQrCodes.size();i++){
            if(i == 0){
                mCustomStyleQrCodes.get(i).setSelectOrNot(true);
            }else{
                mCustomStyleQrCodes.get(i).setSelectOrNot(false);
            }
        }
        mCurrentCustomEncodeParamsBean = mCustomStyleQrCodes.get(0);
        mCommonAdapter = new CommonAdapter<CustomEncodeParamsBean>(this, mCustomStyleQrCodes, com.allever.app.qr.code.scaner.R.layout.custom_qrcode_list_item) {

            @Override
            protected void convert(ViewHolder vh, final CustomEncodeParamsBean item) {
                if (item == null) {
                    return;
                }
                ImageView imageView = vh.getConvertView().findViewById(com.allever.app.qr.code.scaner.R.id.iv_custom_qrcode);
                View coverView = vh.getConvertView().findViewById(com.allever.app.qr.code.scaner.R.id.custom_qrcode_cover_view);
                coverView.setVisibility(item.getSelectOrNot()?View.VISIBLE:View.GONE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            for (int i = 0; i<mCustomStyleQrCodes.size();i++){
                                mCustomStyleQrCodes.get(i).setSelectOrNot(false);
                            }
                            item.setSelectOrNot(true);
                            notifyDataSetChanged();
                            mCurrentCustomEncodeParamsBean = item;
                            mPreCustomEncodeParamsBean = mCurrentCustomEncodeParamsBean;
                            mBitmapQRCode = qrCodeEncoder.encodeAsBitmap(item);
                            mIvQRCode.setImageBitmap(mBitmapQRCode);

                            updateLockState();
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                });
                try {
                    Bitmap customBitmap = qrCodeEncoder.encodeAsBitmap(item);
                    if (customBitmap != null) {
                        imageView.setImageBitmap(customBitmap);
                    }
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        };

        ImageView ivBack = (ImageView) findViewById(com.allever.app.qr.code.scaner.R.id.top_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvSave = (TextView) findViewById(com.allever.app.qr.code.scaner.R.id.top_save);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (needLock()) {
                    return;
                }
                showSaveDialog();
            }
        });

        mBtnShare = (Button) findViewById(com.allever.app.qr.code.scaner.R.id.btn_share);
        mBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        purchaseSize = Config.INSTANCE.getPurchaseSubSize();
        updateLockState();
        String rawDataStr = intent.getStringExtra(Intents.Encode.DATA);
        String showContents = intent.getStringExtra(Intents.Encode.SHOW_CONTENTS);
        if (shareType == ShareItem.TYPE_CONTACT) {
            //单独处理
            Bundle bundle = intent.getBundleExtra(Intents.Encode.DATA);
            String contactName = bundle.getString(ContactsContract.Intents.Insert.NAME);
            String contactNumber = bundle.getString(Contents.PHONE_KEYS[0]);
            showContents = contactName + " " + contactNumber;
        }
        updateInfo(shareType, rawDataStr, showContents);
    }

    private boolean needLock() {
        return false;
//        if (purchaseSize > 0 || mCurrentUnlock) {
//            return false;
//        }
//        return mLock || mDefaultParamsBean != mCurrentCustomEncodeParamsBean;
    }

    private void updateLockState() {
        if (needLock()) {
            ivLock.setVisibility(View.VISIBLE);
            btnPurchase.setVisibility(View.VISIBLE);
            if (mNeedVideoAd) {
                btnPurchase.setText(com.allever.app.qr.code.scaner.R.string.premium_watch_video);
            } else {
                btnPurchase.setText(com.allever.app.qr.code.scaner.R.string.premium_start_buy);
            }
        } else {
            ivLock.setVisibility(View.GONE);
            btnPurchase.setVisibility(View.GONE);
        }
    }

    /**
     * 获取场景名称
     * @param suffix 后缀
     * @return
     */
    private String getSceneName(String suffix) {
        return   "f-"+ mTvType.getText() + "-"+ suffix;
    }

    private void updateInfo(int share_type, String rawDataStr, String showContents) {
        switch (share_type) {
            case ShareItem.TYPE_CLIPBOARD:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.share_type_clipboard));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_URL:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_uri));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_WIFI:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_wifi));
                mTvContent.setText(parseRawData(rawDataStr, new WifiResultParser()));
                break;
            case ShareItem.TYPE_TEXT:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_text));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_PHONE:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_tel));
                mTvContent.setText(parseRawData(rawDataStr, new TelResultParser()));
                break;
            case ShareItem.TYPE_CONTACT:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.share_type_contact));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_EMAIL:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_email_address));
                mTvContent.setText(parseRawData(rawDataStr, new SMTPResultParser()));
                break;
            case ShareItem.TYPE_SMS:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_sms));
                mTvContent.setText(parseRawData(rawDataStr, new SMSTOMMSTOResultParser()));
                break;

            case ShareItem.TYPE_INSTAGRAM:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_instagram));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_FACEBOOK:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_facebook));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_WHATSAPP:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_whatsapp));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_YOUTUBE:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_youtube));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_TWITTER:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_twitter));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_SPOTIFY:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_spotify));
                mTvContent.setText(showContents);
                break;
            case ShareItem.TYPE_VIBER:
                mTvType.setText(getResources().getString(com.allever.app.qr.code.scaner.R.string.result_type_name_viber));
                mTvContent.setText(showContents);
                break;
        }
    }

    private int[] foreColors = {0xFF000000,0xFF3F52C2, 0xFF5d2f0b, 0xFF36cfa1, 0xFF2d0e08, 0xFF21263b, 0xFF181616};
    private int[] baseBitmap = {
            com.allever.app.qr.code.scaner.R.drawable.code_white,
            com.allever.app.qr.code.scaner.R.drawable.code_beach,
            com.allever.app.qr.code.scaner.R.drawable.code_bread,
            com.allever.app.qr.code.scaner.R.drawable.code_grass,
            com.allever.app.qr.code.scaner.R.drawable.code_melon,
            com.allever.app.qr.code.scaner.R.drawable.code_prince,
            com.allever.app.qr.code.scaner.R.drawable.code_yellow
    };



    private int parsedResultType2ShareItemType(ParsedResultType type) {
        int shareItemType = 0;
        if (type == ParsedResultType.ADDRESSBOOK) {
            shareItemType = ShareItem.TYPE_CONTACT;
        } else if (type == ParsedResultType.EMAIL_ADDRESS) {
            shareItemType = ShareItem.TYPE_EMAIL;
        } else if (type == ParsedResultType.PRODUCT) {
            shareItemType = ShareItem.TYPE_TEXT;
        } else if (type == ParsedResultType.URI) {
            shareItemType = ShareItem.TYPE_URL;
        } else if (type == ParsedResultType.TEXT) {
            shareItemType = ShareItem.TYPE_TEXT;
        } else if (type == ParsedResultType.GEO) {
            shareItemType = ShareItem.TYPE_TEXT;
        } else if (type == ParsedResultType.TEL) {
            shareItemType = ShareItem.TYPE_PHONE;
        } else if (type == ParsedResultType.SMS) {
            shareItemType = ShareItem.TYPE_SMS;
        } else if (type == ParsedResultType.CALENDAR) {
            shareItemType = ShareItem.TYPE_TEXT;
        } else if (type == ParsedResultType.WIFI) {
            shareItemType = ShareItem.TYPE_WIFI;
        } else if (type == ParsedResultType.ISBN) {
            shareItemType = ShareItem.TYPE_BARCODE;
        } else if (type == ParsedResultType.VIN) {
            shareItemType = ShareItem.TYPE_BARCODE;
        } else if (type == ParsedResultType.INSTAGRAM) {
            shareItemType = ShareItem.TYPE_INSTAGRAM;
        } else if (type == ParsedResultType.FACEBOOK) {
            shareItemType = ShareItem.TYPE_FACEBOOK;
        } else if (type == ParsedResultType.WHATSAPP) {
            shareItemType = ShareItem.TYPE_WHATSAPP;
        } else if (type == ParsedResultType.YOUTUBE) {
            shareItemType = ShareItem.TYPE_YOUTUBE;
        } else if (type == ParsedResultType.TWITTER) {
            shareItemType = ShareItem.TYPE_TWITTER;
        } else if (type == ParsedResultType.SPOTIFY) {
            shareItemType = ShareItem.TYPE_SPOTIFY;
        } else if (type == ParsedResultType.VIBER) {
            shareItemType = ShareItem.TYPE_VIBER;
        } else {
            shareItemType = ShareItem.TYPE_TEXT;
        }
        return shareItemType;
    }

    private void showSaveDialog() {
        try {
            if (mSaveQRCodeImgDialog == null) {
                if (mBitmapQRCode == null) {
                    mBitmapQRCode = qrCodeEncoder.encodeAsBitmap();
                }
                mSaveQRCodeImgDialog = new SaveQRCodeImgDialog(this, mBitmapQRCode, new SaveQRCodeImgDialog.OnSaveQrCodeListener() {

                    @Override
                    public void onCancelClick(DialogFragment dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onConfirmClick(DialogFragment dialog, String fileName) {
                        ToastUtils.INSTANCE.show(fileName);
                        dialog.dismiss();
                    }
                });
            }else{
                mSaveQRCodeImgDialog.setBitmap(mBitmapQRCode);
            }
            mSaveQRCodeImgDialog.show(getFragmentManager(), this.getClass().getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void share() {
        QRCodeEncoder encoder = qrCodeEncoder;
        if (encoder == null) { // Odd
            Log.w(TAG, "No existing barcode to send?");
            return;
        }

        String contents = encoder.getContents();
        if (contents == null) {
            Log.w(TAG, "No existing barcode to send?");
            return;
        }

        if (mBitmapQRCode == null) {
            try {
                mBitmapQRCode = encoder.encodeAsBitmap();
                if (mBitmapQRCode == null) {
                    return;
                }
            } catch (WriterException we) {
                Log.w(TAG, we);
                return;
            }
        }

        File bsRoot = new File(getExternalCacheDir(), "BarcodeScanner");
        File barcodesRoot = new File(bsRoot, "Barcodes");
        if (!barcodesRoot.exists() && !barcodesRoot.mkdirs()) {
            Log.w(TAG, "Couldn't make dir " + barcodesRoot);
            showErrorMessage(R.string.msg_unmount_usb);
            return;
        }
        File barcodeFile = new File(barcodesRoot, makeBarcodeFileName(contents) + ".png");
        if (!barcodeFile.delete()) {
            Log.w(TAG, "Could not delete " + barcodeFile);
            // continue anyway
        }

        boolean compressResult;
        try {
            FileOutputStream fos = new FileOutputStream(barcodeFile);
            compressResult = mBitmapQRCode.compress(Bitmap.CompressFormat.PNG, 0, fos);
        } catch (IOException ioe) {
            Log.w(TAG, "Couldn't access file " + barcodeFile + " due to " + ioe);
            showErrorMessage(R.string.msg_unmount_usb);
            return;
        }

        if (!compressResult) {
            return;
        }

        Uri shareFileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            shareFileUri = FileProvider.getUriForFile(this, App.context.getPackageName() + ".fileprovider", barcodeFile);
        } else {
            shareFileUri = Uri.fromFile(barcodeFile);
        }

        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.z_xing_app_name) + " - " + encoder.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, contents);
        intent.putExtra(Intent.EXTRA_STREAM, shareFileUri);
        intent.setType("image/png");
        intent.addFlags(Intents.FLAG_NEW_DOC);
        startActivity(Intent.createChooser(intent, null));
    }

    private static CharSequence makeBarcodeFileName(CharSequence contents) {
        String fileName = NOT_ALPHANUMERIC.matcher(contents).replaceAll("_");
        if (fileName.length() > MAX_BARCODE_FILENAME_LENGTH) {
            fileName = fileName.substring(0, MAX_BARCODE_FILENAME_LENGTH);
        }
        return fileName;
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        //获取控件宽高之后进行后续操作
        mIvQRCode.post(new Runnable() {
            @Override
            public void run() {
                int height = mIvQRCode.getHeight();
                int width = mIvQRCode.getWidth();
                Log.d(TAG, "onResume: width = " + width + "\n height = " + height);
                int smallerDimension = width < height ? width : height;
                smallerDimension = smallerDimension / 10 * 9;

                try {
                    boolean useVCard = intent.getBooleanExtra(USE_VCARD_KEY, false);
                    qrCodeEncoder = new QRCodeEncoder(EncodeActivity.this, intent, smallerDimension, useVCard);
                    ParsedResult parsedResult = qrCodeEncoder.getParsedResult(EncodeActivity.this);
                    if (parsedResult != null) {
                        updateInfo(parsedResultType2ShareItemType(parsedResult.getType()), parsedResult.revertRawData(), parsedResult.getDisplayResult());
                    }
                    if (mBitmapQRCode == null) {
                        if(mCurrentCustomEncodeParamsBean != null){
                            mBitmapQRCode = qrCodeEncoder.encodeAsBitmap(mCurrentCustomEncodeParamsBean);
                        }else{
                            mBitmapQRCode = qrCodeEncoder.encodeAsBitmap();
                        }
                    }
                    if (mBitmapQRCode == null) {
                        Log.w(TAG, "Could not encode barcode");
                        showErrorMessage(R.string.zxing_msg_encode_contents_failed);
                        qrCodeEncoder = null;
                        return;
                    }

                    mIvQRCode.setImageBitmap(mBitmapQRCode);
                    mHorizontalListView.setAdapter(mCommonAdapter);
                    //创建二维码图片后，将数据存入数据库
                    if (!mLock || purchaseSize > 0) {
                        saveCreateQRData();
                    }
                } catch (WriterException e) {
                    Log.w(TAG, "Could not encode barcode", e);
                    showErrorMessage(R.string.zxing_msg_encode_contents_failed);
                    qrCodeEncoder = null;
                }
            }
        });
    }

    private void saveCreateQRData() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String content;
        //联系人单独处理
        int type = intent.getIntExtra(Constant.EXTRA_SHARE_TYPE, ShareItem.TYPE_TEXT);
        if (type == ShareItem.TYPE_CONTACT) {
            Bundle bundle = intent.getBundleExtra(Intents.Encode.DATA);
            String contactName = bundle.getString(ContactsContract.Intents.Insert.NAME);
            String contactNumber = bundle.getString(Contents.PHONE_KEYS[0]);
            //Contact format    MECARD:N:alllllll;TEL:18933130520;;
            content = String.format("MECARD:N:%s;TEL:%s;;", contactName, contactNumber);
        } else {
            content = intent.getStringExtra(Intents.Encode.DATA);
        }

        try {
            Result result = new Result(content, null, null, BarcodeFormat.QR_CODE);
            ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, result);
            GenerateHistoryManager generalHistoryManager = new GenerateHistoryManager(this);
            generalHistoryManager.addHistoryItem(this, result, resultHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorMessage(int message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.zxing_button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    /***
     * 将原始数据格式化成显示的数据
     * @param rawData
     * @param concreteResultParser
     * @return
     */
    private String parseRawData(String rawData, ResultParser concreteResultParser) {
        if (concreteResultParser == null) {
            return "";
        }
        Result result = new Result(rawData, null, null, BarcodeFormat.QR_CODE);
        ResultParser resultParser = concreteResultParser;
        ParsedResult parsedResult = resultParser.parse(result);
        String strDisplayResult = parsedResult.getDisplayResult();
        return strDisplayResult;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == SystemAlbumHelper.REQUEST_CODE_IMG_SELECTION) {
            String imagePath = SystemAlbumHelper.INSTANCE.handleActivityResult(EncodeActivity.this, requestCode, resultCode, data);
            if (imagePath != null) {
                setQrCodeImage(imagePath);
            }
        }

    }

    private void setQrCodeImage(String imagePath) {
        CustomQrCodeManager.setCustomStyleIcon(mCustomStyleQrCodes, imagePath, null);
        mCommonAdapter.setDatas(mCustomStyleQrCodes);
        if (mCurrentCustomEncodeParamsBean != null) {
            mCurrentCustomEncodeParamsBean.setBitmapPath(imagePath);
            mPreCustomEncodeParamsBean = mCurrentCustomEncodeParamsBean;
            try {
                mBitmapQRCode = qrCodeEncoder.encodeAsBitmap(mCurrentCustomEncodeParamsBean);
                mIvQRCode.setImageBitmap(mBitmapQRCode);
                Bitmap bitmap = CustomQrCodeUtils.getScaleBitmap(imagePath, mPickPhoto.getWidth(), mPickPhoto.getHeight());
                mPickPhoto.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == com.allever.app.qr.code.scaner.R.id.iv_custom_select_photo) {
            if (mPreCustomEncodeParamsBean == null
                    || (mPreCustomEncodeParamsBean.getBitmapPath() == null && mPreCustomEncodeParamsBean.getIconResId() == null)
            ) {
                SystemAlbumHelper.INSTANCE.start(EncodeActivity.this);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(EncodeActivity.this);
                builder.setTitle(null);
                builder.setMessage("Choose from Library or reset ?");
                builder.setNegativeButton("reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CustomQrCodeManager.setCustomStyleIcon(mCustomStyleQrCodes, null, null);
                        mCommonAdapter.setDatas(mCustomStyleQrCodes);
                        mPickPhoto.setImageResource(com.allever.app.qr.code.scaner.R.drawable.customize_icon_photo);
                        try {
                            if (mPreCustomEncodeParamsBean != null) {
                                mPreCustomEncodeParamsBean.setBitmapPath(null);
                                mPreCustomEncodeParamsBean.setIconResId(null);
                                mIvQRCode.setImageBitmap(qrCodeEncoder.encodeAsBitmap(mPreCustomEncodeParamsBean));
                            }
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setPositiveButton("Choose from Library", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SystemAlbumHelper.INSTANCE.start(EncodeActivity.this);
                    }
                });
                builder.show();
            }
        }
    }

}
