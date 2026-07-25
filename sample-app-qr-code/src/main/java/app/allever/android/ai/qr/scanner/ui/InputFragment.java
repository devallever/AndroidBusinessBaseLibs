package app.allever.android.ai.qr.scanner.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.ListPopupWindow;

import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.allever.app.qr.code.scaner.databinding.FragmentShareQrcodeBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.android.Contents;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.clipboard.ClipboardInterface;
import com.google.zxing.client.result.EmailAddressParsedResult;
import com.google.zxing.client.result.FacebookParsedResult;
import com.google.zxing.client.result.GeoParsedResult;
import com.google.zxing.client.result.InstagramParsedResult;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.SMSParsedResult;
import com.google.zxing.client.result.SpotifyParsedResult;
import com.google.zxing.client.result.TelParsedResult;
import com.google.zxing.client.result.TwitterParsedResult;
import com.google.zxing.client.result.ViberParsedResult;
import com.google.zxing.client.result.WhatsappParsedResult;
import com.google.zxing.client.result.WifiParsedResult;
import com.google.zxing.client.result.YoutubeParsedResult;
import app.allever.android.ai.qr.scanner.Config;
import com.allever.app.qr.code.scaner.R;

import app.allever.android.ai.qr.scanner.ui.adapter.SsidEncryptionAdapter;
import app.allever.android.ai.qr.scanner.bean.ShareItem;
import app.allever.android.ai.qr.scanner.bean.WifiInfo;
import app.allever.android.ai.qr.scanner.core.encode.Constant;
import app.allever.android.ai.qr.scanner.core.encode.EncodeActivity;
import app.allever.android.ai.qr.scanner.core.preview.ResultUIModel;
import app.allever.android.ai.qr.scanner.ui.widget.ShareItemInput;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/***
 * 输入分享内容界面
 */

public class InputFragment extends AppCompatDialogFragment {


    private static final String TAG = "ShareQRCodeFragment";

    private ShareItem mShareItem = null;

    private EditText mEtWifiSSID;
    private EditText mEtLatitude;
    private EditText mEtLongitude;
    private EditText mEtAltitude;

    private ShareItemInput mEtEmailAddress;
    private ShareItemInput mEtEmailContent;
    private ShareItemInput mEtClipboard;
    private ShareItemInput mEtUrl;
    private ShareItemInput mEtText;
    private ShareItemInput mEtContactName;
    private ShareItemInput mEtContactNumber;
    private ShareItemInput mEtPhone;
    private ShareItemInput mEtSmsTo;
    private ShareItemInput mEtSmsContent;
    private ShareItemInput mEtWifiPWD;

    private ShareItemInput mInput1;
    private ShareItemInput mInput2;

    private Spinner mSpinnerWifiEncryption;
    private Switch mSwitchWifiHidden;

    private String mWifiEncryption;
    List<String> mWifiEncryptionList;

    private ArrayAdapter<String> mWifiListAdapter;
    private List<String> mConnectedWifiList;
    private List<WifiInfo> mWifiInfoList;

    int mSelectWifiPosition = 0;

    private View mView;

    //wifi ssid列表 弹窗;
    private ListPopupWindow mListPopupWindowSSID;

    private FragmentShareQrcodeBinding mBinding;

    public InputFragment() {
    }

    @SuppressLint("ValidFragment")
    public InputFragment(ShareItem shareItem) {
        mShareItem = shareItem;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            // 必须设置，否则无法全屏
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //设置dialog在屏幕底部
//            window.setGravity(Gravity.BOTTOM);
            //设置dialog弹出时的动画效果，从屏幕底部向上弹出
            window.setWindowAnimations(R.style.ActivityStyle);
            //获得window窗口的属性
            WindowManager.LayoutParams lp = window.getAttributes();
            //设置窗口宽度为充满全屏
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            //设置窗口高度为充满全屏
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            //将设置好的属性set回去
            window.setAttributes(lp);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mBinding = FragmentShareQrcodeBinding.inflate(getLayoutInflater());
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(mBinding.getRoot())
                .create();

        mView = mBinding.getRoot();
        inflateView();
        initListener();

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void inflateView() {
        // 处理广告
        if (mShareItem == null || !mShareItem.isLock() || Config.INSTANCE.getPurchaseSubSize() > 0) {
            //设置空图标
            mBinding.btnNext.setCompoundDrawables(null, null, null, null);
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.icon_crown);
            int width = drawable.getIntrinsicWidth() >> 1;
            int height = drawable.getIntrinsicHeight() >> 1;
            drawable.setBounds(0, 0, width, height);
            mBinding.btnNext.setCompoundDrawables(null, null, drawable, null);
        }
        ShareItem shareItem = mShareItem;
        if (shareItem == null) {
            return;
        }

        ViewStub viewStub = null;
        int shareType = shareItem.getType();
        switch (shareType) {
            case ShareItem.TYPE_CLIPBOARD:
//                viewStub = mView.findViewById(R.id.viewstub_share_clipboard);
                mBinding.viewstubShareClipboard.inflate();
                initShareClipboardView();
                break;
            case ShareItem.TYPE_URL:
//                viewStub = mView.findViewById(R.id.viewstub_share_url);
                mBinding.viewstubShareUrl.inflate();
                initShareUrlView();
                break;
            case ShareItem.TYPE_WIFI:
//                viewStub = mView.findViewById(R.id.viewstub_share_wifi);
                mBinding.viewstubShareWifi.inflate();
                initShareWifiView();
                getConnectedWifiList();
                break;
            case ShareItem.TYPE_TEXT:
//                viewStub = mView.findViewById(R.id.viewstub_share_text);
                mBinding.viewstubShareText.inflate();
                initShareTextView();
                break;
            case ShareItem.TYPE_CONTACT:
//                viewStub = mView.findViewById(R.id.viewstub_share_contact);
                mBinding.viewstubShareContact.inflate();
                initShareContactView();
                break;
            case ShareItem.TYPE_PHONE:
//                viewStub = mView.findViewById(R.id.viewstub_share_phone);
                mBinding.viewstubSharePhone.inflate();
                initSharePhoneView();
                break;
            case ShareItem.TYPE_EMAIL:
//                mView.findViewById(R.id.viewstub_share_email).setVisibility(View.VISIBLE);
                mBinding.viewstubShareEmail.inflate();
                initShareEmailView();
                break;
            case ShareItem.TYPE_SMS:
//                viewStub = mView.findViewById(R.id.viewstub_share_sms);
                mBinding.viewstubShareSms.inflate();
                initShareSMSView();
                break;
            case ShareItem.TYPE_LOCATION:
//                viewStub = mView.findViewById(R.id.viewstub_share_location);
                mBinding.viewstubShareLocation.inflate();
                initShareLocationView();
                break;
            case ShareItem.TYPE_BARCODE:
//                viewStub = mView.findViewById(R.id.viewstub_share_barcode);
                mBinding.viewstubShareBarcode.inflate();
                initShareBarcodeView();
                break;


            case ShareItem.TYPE_INSTAGRAM:
//                viewStub = mView.findViewById(R.id.viewstub_share_instagram);
                mBinding.viewstubShareInstagram.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            case ShareItem.TYPE_FACEBOOK:
//                viewStub = mView.findViewById(R.id.viewstub_share_facebook);
                mBinding.viewstubShareFacebook.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            case ShareItem.TYPE_WHATSAPP:
//                viewStub = mView.findViewById(R.id.viewstub_share_whatsapp);
                mBinding.viewstubShareWhatsapp.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            case ShareItem.TYPE_YOUTUBE:
//                viewStub = mView.findViewById(R.id.viewstub_share_youtube);
                mBinding.viewstubShareYoutube.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            case ShareItem.TYPE_TWITTER:
//                viewStub = mView.findViewById(R.id.viewstub_share_twitter);
                mBinding.viewstubShareTwitter.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            case ShareItem.TYPE_SPOTIFY:
//                viewStub = mView.findViewById(R.id.viewstub_share_spotify);
                mBinding.viewstubShareSpotify.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input1);
                mInput2 = mView.findViewById(R.id.id_share_item_input2);
                break;
            case ShareItem.TYPE_VIBER:
//                viewStub = mView.findViewById(R.id.viewstub_share_viber);
                mBinding.viewstubShareViber.inflate();
                mInput1 = mView.findViewById(R.id.id_share_item_input);
                break;
            default:
//                viewStub = mView.findViewById(R.id.viewstub_share_clipboard);
                mBinding.viewstubShareClipboard.inflate();
                break;
        }

        mBinding.tvShareTitle.setText(shareItem.getTitle());
        mBinding.ivShareTypeLogo.setImageResource(shareItem.getResId());
        if (shareItem.isIconHasBackground()) {
            mBinding.ivShareTypeLogo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (shareItem.getType() == ShareItem.TYPE_CLIPBOARD) {
                ResultUIModel.INSTANCE.changeBackground(ResultUIModel.INSTANCE.getClipboardUIData(), mBinding.ivShareTypeLogo, false);
            } else {
                ResultUIModel.INSTANCE.changeBackground(shareItem.getParsedResultType(), mBinding.ivShareTypeLogo, false);
            }
        } else {
            mBinding.ivShareTypeLogo.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    /***
     * 获取连接过的Wifi列表数据
     */
    private void getConnectedWifiList() {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        //判断wifi是否可用
        if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(getActivity(), R.string.wifi_not_enable, Toast.LENGTH_SHORT).show();
            return;
        }

        scanWifi();
    }

    @SuppressLint("MissingPermission")
    private void scanWifi() {
        Log.d(TAG, "scanWifi: start");
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         List<WifiConfiguration> wifiConList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConList) {
            //格式 "ssid"
            String ssid = wifiConfiguration.SSID;
            String parseSSID = "";
            if (!TextUtils.isEmpty(ssid)) {
                parseSSID = ssid.substring(1, ssid.length() - 1);
            }
            //用于显示WifiSSID Spinner的数据源
            mConnectedWifiList.add(parseSSID);

            //获取内部加密方式的索引
            int internalIndex = getAuthType(wifiConfiguration);
            //根据内部索引获取 SpinnerWifiEncryption 对应数据源的索引
            int typeIndex = getTypeIndexByInternalIndex(internalIndex);

            WifiInfo wifiInfo = new WifiInfo();
            wifiInfo.setSsid(ssid);

            wifiInfo.setTypeIndex(typeIndex);
            mWifiInfoList.add(wifiInfo);
        }
        mWifiListAdapter.notifyDataSetChanged();
    }

    /***
     * 根据wifi内部加密方式索引，获取应用内wifi加密类型数据列表的索引
     * 0: WAP
     * 1: EAP
     * 2: None
     * @param internalIndex
     * @return
     */
    private int getTypeIndexByInternalIndex(int internalIndex) {
        //该索引对应 R.attrs.wifi_encryption
        int index;
        switch (internalIndex) {
            case WifiConfiguration.KeyMgmt.WPA_PSK:
                index = 0;
                break;
            case WifiConfiguration.KeyMgmt.WPA_EAP:
                index = 1;
                break;
            case WifiConfiguration.KeyMgmt.NONE:
                index = 2;
                break;
            default:
                index = 2;
                break;
        }
        return index;
    }


    private void initShareBarcodeView() {
    }

    private void initShareLocationView() {
        mEtLatitude = mView.findViewById(R.id.et_latitude);
        mEtLongitude = mView.findViewById(R.id.et_longitude);
        mEtAltitude = mView.findViewById(R.id.et_altitude);
    }

    private void initShareSMSView() {
        mEtSmsTo = mView.findViewById(R.id.id_share_item_input_sms_to);
        mEtSmsContent = mView.findViewById(R.id.id_share_item_input_sms_content);
    }

    private void initShareEmailView() {
        mEtEmailAddress = mView.findViewById(R.id.id_share_item_input_email_address);
        mEtEmailContent = mView.findViewById(R.id.id_share_item_input_email_content);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initShareWifiView() {
        mEtWifiSSID = mView.findViewById(R.id.et_wifi_ssid);
        mEtWifiPWD = mView.findViewById(R.id.id_share_item_input_wifi_pwd);
        mSwitchWifiHidden = mView.findViewById(R.id.switch_wifi_hidden);
        mSpinnerWifiEncryption = mView.findViewById(R.id.spinner_wifi_encryption);

        //wifi加密类型列表Spinner
        mSpinnerWifiEncryption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mWifiEncryption = mWifiEncryptionList.get(position);

                //使用了自带的布局，文本颜色为黑色,需要修改为指定颜色
                TextView textView = (TextView) view;
                textView.setTextColor(getResources().getColor(R.color.share_item_tv_color));

                //如果选择最后一个，None ,密码输入框设置为不可见状态
                if (position == mWifiEncryptionList.size() - 1) {
                    mEtWifiPWD.setVisibility(View.INVISIBLE);
                    mEtWifiPWD.setText("");
                    mWifiEncryption = "";
                } else {
                    mEtWifiPWD.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //wifi加密类型列表数据
        mWifiEncryptionList = Arrays.asList(getResources().getStringArray(R.array.wifi_encryption));
        //设置默认加密方式WPA，第一项,
        mWifiEncryption = mWifiEncryptionList.get(0);
        SsidEncryptionAdapter adapter = new SsidEncryptionAdapter(getActivity(), mWifiEncryptionList);
        mSpinnerWifiEncryption.setAdapter(adapter);

        mConnectedWifiList = new ArrayList<>();
        mWifiListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mConnectedWifiList);
        mListPopupWindowSSID = new ListPopupWindow(getActivity());
        mListPopupWindowSSID.setAdapter(mWifiListAdapter);
        mListPopupWindowSSID.setAnchorView(mEtWifiSSID);
        mListPopupWindowSSID.setModal(true);
        mListPopupWindowSSID.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListPopupWindowSSID.dismiss();
                mSelectWifiPosition = position;

                String wifiSSID = mConnectedWifiList.get(mSelectWifiPosition);
                mEtWifiSSID.setText(wifiSSID);
                mEtWifiSSID.setSelection(wifiSSID.length());

                //更新该Wifi的配置信息
                WifiInfo wifiInfo = mWifiInfoList.get(mSelectWifiPosition);
                mSpinnerWifiEncryption.setSelection(wifiInfo.getTypeIndex());
            }
        });

        mEtWifiSSID.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                // Check if touch point is in the area of the right button
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() >= (mEtWifiSSID.getWidth() - mEtWifiSSID
                            .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action here
                        //展开布局
                        mListPopupWindowSSID.show();
                        if (mConnectedWifiList.size() == 0) {
                            Toast.makeText(getActivity(), R.string.not_found_connnected_wifi, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        mWifiInfoList = new ArrayList<>();
    }

    private void initShareContactView() {
        mEtContactName = mView.findViewById(R.id.id_share_item_input_contact_name);
        mEtContactNumber = mView.findViewById(R.id.id_share_item_input_contact_number);
    }

    private void initSharePhoneView() {
        mEtPhone = mView.findViewById(R.id.id_share_item_input_phone_number);
    }

    private void initShareUrlView() {
        mEtUrl = mView.findViewById(R.id.id_share_item_input_url);
    }

    private void initShareTextView() {
        mEtText = mView.findViewById(R.id.id_share_item_input_text);
    }

    private void initShareClipboardView() {
        mEtClipboard = mView.findViewById(R.id.id_share_item_input_clipboard);

        CharSequence text = ClipboardInterface.getText(getActivity());
        mEtClipboard.setText(text);
        mEtClipboard.setSelection(mEtClipboard.getText().length());

        mEtClipboard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mEtClipboard.setSelection(mEtClipboard.getText().length());
                }
            }
        });
    }

    public void initListener() {
        mBinding.topBack.setOnClickListener(v -> dismiss());
        mBinding.btnNext.setOnClickListener(v -> {
            doNext();
        });
        mBinding.topNext.setOnClickListener(v -> {
            doNext();
        });
    }

    private void doNext() {
        ShareItem shareItem = mShareItem;
        if (shareItem == null) {
            return;
        }

        switch (shareItem.getType()) {
            case ShareItem.TYPE_CLIPBOARD:
                shareCommonText(mEtClipboard.getText());
                break;
            case ShareItem.TYPE_URL:
                shareCommonText(mEtUrl.getText());
                break;
            case ShareItem.TYPE_WIFI:
                String wifiData = getWifiData();
                shareCommonText(wifiData);
                break;
            case ShareItem.TYPE_TEXT:
                shareCommonText(mEtText.getText());
                break;
            case ShareItem.TYPE_CONTACT:
//                String contactData = getContactData();
                shareContact();
                break;
            case ShareItem.TYPE_PHONE:
                String phoneData = getTelData();
                shareCommonText(phoneData);
                break;
            case ShareItem.TYPE_EMAIL:
                String emailData = getEmailData();
                shareCommonText(emailData);
                break;
            case ShareItem.TYPE_SMS:
                String smsData = getSmsData();
                shareCommonText(smsData);
                break;
            case ShareItem.TYPE_LOCATION:
                String locationData = getLocationData();
                shareCommonText(locationData);
                break;
            case ShareItem.TYPE_BARCODE:
                String barcodeData = getBarcodeData();
                break;
            case ShareItem.TYPE_INSTAGRAM: {
                ParsedResult parsedResult = InstagramParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_FACEBOOK: {
                ParsedResult parsedResult = FacebookParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_WHATSAPP: {
                ParsedResult parsedResult = WhatsappParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_YOUTUBE: {
                ParsedResult parsedResult = YoutubeParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_TWITTER: {
                ParsedResult parsedResult = TwitterParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_SPOTIFY: {
                ParsedResult parsedResult = SpotifyParsedResult.build(mInput1.getText(), mInput2.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            case ShareItem.TYPE_VIBER: {
                ParsedResult parsedResult = ViberParsedResult.build(mInput1.getText());
                shareCommonText(parsedResult.revertRawData(), parsedResult.getDisplayResult());
                break;
            }
            default:
                break;
        }
    }

//    private String getContactData() {
//        String name = mEtContactName.getText();
//        String number = mEtContactNumber.getText();
//
//        //Contact format    MECARD:N:alllllll;TEL:18933130520;;
//        String contactData = String.format("MECARD:N:%s;TEL:%s;;", name, number);
//        return contactData
//    }

    private String getBarcodeData() {
        return "";
    }

    private String getLocationData() {
        String strLatitude = mEtLatitude.getText().toString();
        String strLongitude = mEtLongitude.getText().toString();
        String strAltitude = mEtAltitude.getText().toString();
        try {
            double latitude = Double.valueOf(strLatitude);
            double longitude = Double.valueOf(strLongitude);
            double altitude = Double.valueOf(strAltitude);

            GeoParsedResult geoParsedResult = new GeoParsedResult(latitude, longitude, altitude, null);
            return geoParsedResult.revertRawData();
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            return "";
        }
    }

    private String getTelData() {
        String phone = mEtPhone.getText();
        if (TextUtils.isEmpty(phone)) {
            return "";
        }

        TelParsedResult telParsedResult = new TelParsedResult(phone, null, null);
        return telParsedResult.revertRawData();
    }

    private String getSmsData() {
        String smsTo = mEtSmsTo.getText();
        String smsContent = mEtSmsContent.getText();

        if (TextUtils.isEmpty(smsTo) || TextUtils.isEmpty(smsContent)) {
            return "";
        }

        SMSParsedResult smsParsedResult = new SMSParsedResult(smsTo, null, null, smsContent);

        String smsData = smsParsedResult.revertRawData();
        return smsData;
    }

    private String getEmailData() {
        String emailAddress = mEtEmailAddress.getText();
        String emailContent = mEtEmailContent.getText();

        if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(emailContent)) {
            return "";
        }

        EmailAddressParsedResult emailAddressParsedResult = new EmailAddressParsedResult(null, null, null, emailAddress, emailContent);

        String emailData = emailAddressParsedResult.revertRawData();
        return emailData;
    }

    private String getWifiData() {
        String ssid;
        if (mSelectWifiPosition == 0) {
            ssid = mEtWifiSSID.getText().toString();
        } else {
            ssid = mConnectedWifiList.get(mSelectWifiPosition);
        }

        String pwd = mEtWifiPWD.getText().toString();
        String encryption = mWifiEncryption;
        boolean hidden = mSwitchWifiHidden.isChecked();

        WifiParsedResult wifiParsedResult = new WifiParsedResult(encryption, ssid, pwd, hidden, "", "", "PWD", "PAP");
        String wifiData = wifiParsedResult.revertRawData();

        if (TextUtils.isEmpty(ssid)) {
            return "";
        }

        return wifiData;
    }

    /***
     * 分享联系人单独一个方法
     */
    private void shareContact() {
        ShareItem shareItem = mShareItem;
        if (shareItem == null) {
            return;
        }

        String name = mEtContactName.getText().toString();
        String number = mEtContactNumber.getText().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
            Bundle bundle = new Bundle();
            bundle.putString(ContactsContract.Intents.Insert.NAME, name);
            bundle.putString(Contents.PHONE_KEYS[0], number);

            Intent intent = new Intent(getActivity(), EncodeActivity.class);
            intent.setAction(Intents.Encode.ACTION);
            intent.addFlags(Intents.FLAG_NEW_DOC);
            intent.putExtra(Intents.Encode.TYPE, Contents.Type.CONTACT);
            intent.putExtra(Intents.Encode.DATA, bundle);
            intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
            intent.putExtra(Constant.EXTRA_SHARE_TYPE, shareItem.getType());
            startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.input_content, Toast.LENGTH_SHORT).show();
        }
    }

    /***
     * 通用分享
     * @param content 格式化后的文本数据
     */
    private void shareCommonText(String content) {
        shareCommonText(content, content);
    }

    private void shareCommonText(String content, String showContent) {
        ShareItem shareItem = mShareItem;
        if (shareItem == null) {
            return;
        }
        if (TextUtils.isEmpty(content) || TextUtils.isEmpty(showContent)) {
            Toast.makeText(getActivity(), R.string.input_content, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getActivity(), EncodeActivity.class);
        intent.setAction(Intents.Encode.ACTION);
        intent.addFlags(Intents.FLAG_NEW_DOC);
        intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
        intent.putExtra(Intents.Encode.DATA, content);
        intent.putExtra(Intents.Encode.SHOW_CONTENTS, showContent);
        intent.putExtra(Intents.Encode.FORMAT, BarcodeFormat.QR_CODE.toString());
        intent.putExtra(Constant.EXTRA_SHARE_TYPE, shareItem.getType());
        intent.putExtra(EncodeActivity.INTENT_KEY_LOCK, shareItem.isLock());
        startActivity(intent);
    }

    /**
     * 获取wifi加密类型
     *
     * @param wifiConfiguration
     * @return
     */
    public int getAuthType(WifiConfiguration wifiConfiguration) {
        if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return WifiConfiguration.KeyMgmt.WPA_PSK;
        } else if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)) {
            return WifiConfiguration.KeyMgmt.WPA_EAP;
        }
//        else if (wifiConfiguration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
//            return WifiConfiguration.KeyMgmt.IEEE8021X;
//        }
        return WifiConfiguration.KeyMgmt.NONE;
    }
}
