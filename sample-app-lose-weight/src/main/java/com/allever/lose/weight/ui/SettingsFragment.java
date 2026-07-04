package com.allever.lose.weight.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allever.lose.weight.MainActivity;
import com.allever.lose.weight.MyApplication;
import com.allever.lose.weight.R;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.base.BaseFragment;
import com.allever.lose.weight.ui.dialog.LanguageDialog;
import com.allever.lose.weight.ui.mvp.presenter.SettingPresenter;
import com.allever.lose.weight.ui.mvp.view.ISettingView;
import com.allever.lose.weight.util.Util;
import com.allever.lose.weight.ui.dialog.SingleChoiceDialogFragment;
import com.allever.lose.weight.ui.dialog.SoundOptionsFragment;
import com.allever.lose.weight.ui.dialog.TTSFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mac on 2018/3/1.
 */

public class SettingsFragment extends BaseFragment<ISettingView, SettingPresenter> implements ISettingView, LanguageDialog.ILanguageListener, SoundOptionsFragment.ISoundListener {

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x01;
    private static final int RC_SIGN_IN = 0x02;


    Toolbar mToolbar;
    TextView testVoice;
    TextView mSetting;
    TextView selectEngine;
    TextView soundOptions;
    TextView mTvHealthData;
    TextView mTvUnitSetting;
    TextView mTvLanguageSetting;
    TextView mTvShare;
    TextView mTvDeleteAllData;
    TextView mTvDownloadTts;
    TextView mTvRateUs;

    LinearLayout mLlSyncContainer;
    TextView mTvAccount;
    TextView mTvSyncTime;
    TextView mTvReminder;
    TextView mTvFeedback;
    TextView mTvVersion;

    private TextToSpeech mSpeech;
    private int index;
    private List<TextToSpeech.EngineInfo> engineInfoList = new ArrayList<>();
    public static final String TAG = "SettingsFragment";

    private LanguageDialog mLanguageDialog;
    private SoundOptionsFragment mSoundDialog;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mToolbar = view.findViewById(R.id.toolbar);
        testVoice = view.findViewById(R.id.test_voice);
        mSetting = view.findViewById(R.id.setting);
        selectEngine = view.findViewById(R.id.select_engine);
        soundOptions = view.findViewById(R.id.sound_options);
        mTvHealthData = view.findViewById(R.id.id_fg_setting_tv_health_data);
        mTvUnitSetting = view.findViewById(R.id.id_fg_setting_tv_unit_setting);
        mTvLanguageSetting = view.findViewById(R.id.id_fg_setting_tv_language_setting_setting);
        mTvShare = view.findViewById(R.id.id_fg_setting_tv_share);
        mTvDeleteAllData = view.findViewById(R.id.id_fg_setting_tv_delete_all_data);
        mTvDownloadTts = view.findViewById(R.id.id_fg_setting_tv_download_tts);
        mTvRateUs = view.findViewById(R.id.id_fg_setting_tv_rate_us);
        mTvReminder = view.findViewById(R.id.id_fg_setting_tv_reminder);
        mTvFeedback = view.findViewById(R.id.id_fg_setting_tv_feedback);
        mTvVersion = view.findViewById(R.id.tvVersion);
        mLlSyncContainer = view.findViewById(R.id.id_sync_ll_sync_container);
        mTvAccount = view.findViewById(R.id.id_sync_tv_account);
        mTvSyncTime = view.findViewById(R.id.id_sync_tv_sync_time);
        mTvLanguageSetting = view.findViewById(R.id.id_fg_setting_tv_language_setting_setting);

        testVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeech.speak(getString(R.string.tts_tset), TextToSpeech.QUEUE_FLUSH, null);
                showTTSTestDialog();
            }
        });
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent("com.android.settings.TTS_SETTINGS"));
            }
        });
        soundOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSoundDialog.show(getFragmentManager());
            }
        });
        mTvHealthData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(new HealthDataFragment());
            }
        });
        mTvUnitSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(new UnitSettingFragment());
            }
        });
        mTvLanguageSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLanguageDialog.show(true);
            }
        });
        mTvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = Util.getShareIntent(_mActivity);
                startActivity(shareIntent);
            }
        });
        mTvDeleteAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(_mActivity)
                        .setMessage(R.string.delete_all_data)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.deleteAllData();
                                Util.restartApp(_mActivity, MainActivity.class);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
        mTvDownloadTts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.searchFromMarket(_mActivity, "text to speech");
            }
        });
        mTvRateUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.openAppInPlay(_mActivity, _mActivity.getPackageName());
            }
        });
        selectEngine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSingleChoiceDialogFragment();
            }
        });
        mTvReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(new ReminderFragment());
            }
        });
        mTvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.sendFeedBack(_mActivity);
            }
        });

        EventBus.getDefault().register(this);

        initToolbar(mToolbar, R.string.settings);
        mSpeech = MyApplication.speechInstant();
        initDialog();

        mPresenter.getLanguage(_mActivity);
        mPresenter.getSyncData();

        mTvVersion.setText("Ver:1.0");

        return view;
    }

    private void initDialog() {
        mLanguageDialog = new LanguageDialog(_mActivity, this);
        mSoundDialog = new SoundOptionsFragment(_mActivity, this);
    }


    private void showTTSTestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_mActivity);
        builder.setMessage(R.string.voice_dialog_title);
        builder.setPositiveButton(R.string.voice_dialog_can, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(R.string.voice_dialog_cant, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                extraTransaction().startDontHideSelf(TTSFragment.newInstance());
            }
        });
        builder.show();
    }

    public void showSingleChoiceDialogFragment() {
        engineInfoList = mSpeech.getEngines();
        SingleChoiceDialogFragment singleChoiceDialogFragment = new SingleChoiceDialogFragment();
        singleChoiceDialogFragment.show("请选择TTS语音", mPresenter.getTTSEngins(mSpeech), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                index = which;
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextToSpeech.EngineInfo engineInfo = engineInfoList.get(index);
                Log.i(TAG, engineInfo.name);
                mSpeech.setEngineByPackageName(engineInfo.name);
                //保存引擎配置
                mPresenter.saveTTSConfig(engineInfo.name);
                dialog.dismiss();
            }
        }, getFragmentManager());
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateSyncView(String event) {
        if (Constant.EVENT_UPDATE_REPORT_SYNC.equalsIgnoreCase(event)) {
            mPresenter.getSyncData();
        }
    }

    @Override
    protected SettingPresenter createPresenter() {
        return new SettingPresenter();
    }

    @Override
    public void onObtainLanguage(int id, String language) {
        Log.d(TAG, "onObtainLanguage: id = " + id);
        Log.d(TAG, "onObtainLanguage: language = " + language);
        //更新数据
        mTvLanguageSetting.setText(getString(R.string.language_select) + " - " + language);
        mPresenter.saveLanguage(id);

        //更新语言
        Util.setLanguage(_mActivity);
        Util.restartApp(_mActivity, MainActivity.class);
    }


    @Override
    public void onObtainSoundOption(boolean isMute, boolean voice, boolean trainVoice) {
        //更新数据库
        mPresenter.saveVoiceOption(isMute, voice, trainVoice);
    }

    @Override
    public void setLanguage(int flag, String language) {
        mTvLanguageSetting.setText(getString(R.string.language_select) + " - " + language);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: REQUEST_OAUTH_REQUEST_CODE");
            //获取上一次的同步时间
            mPresenter.saveSyncState(true);
            EventBus.getDefault().post(Constant.EVENT_UPDATE_REPORT_SYNC);
        }
    }
}
