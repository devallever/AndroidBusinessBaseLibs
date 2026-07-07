package com.allever.lose.weight.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;

import com.allever.lose.weight.MyApplication;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.allever.lose.weight.R;
import com.allever.lose.weight.data.GlobalData;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.base.BaseDialog;
import com.allever.lose.weight.base.BaseFragment;
import com.allever.lose.weight.bean.ActionItem;
import com.allever.lose.weight.ui.dialog.ExitActionDialog;
import com.allever.lose.weight.ui.dialog.SoundDialog;
import com.allever.lose.weight.ui.mvp.presenter.ActionPresenter;
import com.allever.lose.weight.ui.mvp.view.IActionView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
/**
 * Created by Mac on 18/2/28.
 */

public class ActionFragment extends BaseFragment<IActionView, ActionPresenter> implements IActionView, SoundDialog.ISoundListener {
    private static final String TAG = "ActionFragment";

    private static final int DIALOG_EXIT = 0x01;
    private static final int DIALOG_DELAY = 0x02;
    private static final int DIALOG_CLOSE = 0x03;

    ImageView mIvGuide;
    Toolbar mToolbar;
    FloatingActionButton mBtnPause;
    ProgressBar mProgressBarTime;
    TextView mTvDesc;
    TextView mTvProgress;
    TextView mTvTime;
    TextView mTvTotal;
    TextView mTvActionName;
    ImageView mIvVideo;
    ImageView mIvVoice;
    ViewGroup bannerContainer;

    private ExitActionDialog mExitDialog;

    private int mDialogOption = DIALOG_CLOSE;

    private SoundDialog mSoundDialog;

    //第几天id
    private int mDayId = 1;
    private int mActionId = 1;


    public static ActionFragment newInstance(int dayId, int actionId) {
        ActionFragment fragment = new ActionFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_DAY_ID, dayId);
        bundle.putInt(Constant.EXTRA_ACTION_ID, actionId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.lw_fragment_action, container, false);

        mIvGuide = view.findViewById(R.id.id_fg_action_iv_guide);
        mToolbar = view.findViewById(R.id.id_toolbar);
        mBtnPause = view.findViewById(R.id.id_fg_action_btn_pause);
        mProgressBarTime = view.findViewById(R.id.id_fg_action_progress_time);
        mTvDesc = view.findViewById(R.id.id_fg_action_tv_action_desc);
        mTvProgress = view.findViewById(R.id.id_fg_action_tv_process);
        mTvTime = view.findViewById(R.id.id_fg_action_tv_current_time);
        mTvTotal = view.findViewById(R.id.id_fg_action_tv_total);
        mTvActionName = view.findViewById(R.id.id_fg_action_tv_action_name);
        mIvVideo = view.findViewById(R.id.id_fg_action_iv_video);
        mIvVoice = view.findViewById(R.id.id_fg_action_iv_sound);
        bannerContainer = view.findViewById(R.id.bannerContainer);

        adaptStatusBar(mToolbar);


        Bundle args = getArguments();
        if (args != null) {
            mDayId = args.getInt(Constant.EXTRA_DAY_ID);
            mActionId = args.getInt(Constant.EXTRA_ACTION_ID);
        }

        initDialog();
        initView();

        mPresenter.getCurrentActionId(mDayId);
        mPresenter.getActionData(mDayId, mActionId);
        mPresenter.getCurrentLevel(mDayId, mActionId);
        mPresenter.getActionImgUrlList(mDayId, mActionId);

        mPresenter.setStartTime();

        return view;
    }


    private void initDialog() {
        mSoundDialog = new SoundDialog(requireActivity(), this);
        mExitDialog = new ExitActionDialog.Builder(requireActivity())
                .setExitListener(new ExitActionDialog.ClickListener() {
                    @Override
                    public void onClick(BaseDialog dialog) {
                        mDialogOption = DIALOG_EXIT;
                        hideExitDialog();
                        mPresenter.stopAction();
                        requireActivity().onBackPressed();
                        mPresenter.saveExerciseRecord(mDayId);
                        EventBus.getDefault().post(Constant.EVENT_REFRESH_VIEW);
                        mPresenter.pauseTTS();
                    }
                })
                .setCloseListener(new ExitActionDialog.ClickListener() {
                    @Override
                    public void onClick(BaseDialog dialog) {
                        mDialogOption = DIALOG_CLOSE;
                        hideExitDialog();
                        mPresenter.restartAction(mDayId, mActionId);
                    }
                })
                .setDelayListener(new ExitActionDialog.ClickListener() {
                    @Override
                    public void onClick(BaseDialog dialog) {
                        //todo 没处理延时
                        mDialogOption = DIALOG_DELAY;
                        hideExitDialog();
                        mPresenter.stopAction();
                        requireActivity().onBackPressed();
                        mPresenter.saveExerciseRecord(mDayId);
                        EventBus.getDefault().post(Constant.EVENT_REFRESH_VIEW);
                        mPresenter.pauseTTS();
                    }
                }).build();
    }

    private void initView() {
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stop action
                mPresenter.pauseAction();
                int duration = mPresenter.getCurrentDurationTime();
                Bundle bundle = new Bundle();
                bundle.putInt(Constant.EXTRA_DAY_ID, mDayId);
                bundle.putInt(Constant.EXTRA_ACTION_ID, mActionId);
                bundle.putInt(Constant.EXTRA_DURATION, duration);
                MyApplication.startFragment(ActionPauseFragment.class, bundle);
                mPresenter.pauseTTS();
            }
        });

        mIvVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //暂停运动
                showSoundDialog();
                mPresenter.pauseAction();
            }
        });

    }


    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected ActionPresenter createPresenter() {
        return new ActionPresenter(requireActivity());
    }

    @Override
    public void showExitDialog() {
        mExitDialog.show();
        mPresenter.pauseAction();
    }

    @Override
    public void hideExitDialog() {
        mExitDialog.hide();
    }

    @Override
    public void showSoundDialog() {
        mSoundDialog.show(false);
    }

    @Override
    public void hideSoundDialog() {
        mSoundDialog.hide();
    }

    @Override
    public void setData(final ActionItem actionItem) {
        Log.d(TAG, "setData: ");
        if (actionItem == null) {
            return;
        }
        initToolbar(mToolbar, actionItem.getName());
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExitDialog.show();
            }
        });

        mTvTotal.setText("/" + actionItem.getTime());
        mTvActionName.setText(actionItem.getName());
        if (GlobalData.config.isTrainVoiceOption()) {
            mPresenter.speak(actionItem.getName(), TextToSpeech.QUEUE_ADD);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String desc : actionItem.getDescList()) {
            stringBuilder.append("\n" + desc + "\n");
            if (GlobalData.config.isTrainVoiceOption()) {
                mPresenter.speak(desc, TextToSpeech.QUEUE_ADD);
            }
        }
        mTvDesc.setText(stringBuilder.toString());

        mProgressBarTime.setMax(actionItem.getTime());

//        mIvVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //url->youtube
//                showToast(actionItem.getVideoUrl());
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionItem.getVideoUrl()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setPackage("com.google.android.youtube");
//                startActivity(intent);
//            }
//        });

    }

    @Override
    public void setCurrentActionId(int actionId) {
        mActionId = actionId;
    }

    @Override
    public void setCurrent(int currentLevel, int levelCount) {
        mTvProgress.setText(currentLevel + "/" + levelCount);
    }

    @Override
    public void setActionImgList(List<String> urlList) {
        Log.d(TAG, "setActionImgList: ");
        //使用Handler发送消息更换图片实现动画效果.
        mPresenter.sendStartMessage();
    }

    @Override
    public void updateProgress(int progress) {
        mTvTime.setText(String.valueOf(progress));
        mProgressBarTime.setProgress(progress);
    }

    @Override
    public void updateActionImg(Bitmap bitmap) {
        mIvGuide.setImageBitmap(bitmap);
    }

    @Override
    public void startNextFragment() {
        //添加训练进度
        mPresenter.saveActionScheduleRecord(mDayId, mActionId);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_DAY_ID, mDayId);
        bundle.putInt(Constant.EXTRA_ACTION_ID, mActionId);
        MyApplication.startFragment(ActionNextFragment.class, bundle);
        finish();
    }

    @Override
    public void startFinishFragment() {
        Log.d(TAG, "startFinishFragment: ");
        mPresenter.stopAction();
        //添加训练进度
        mPresenter.saveActionScheduleRecord(mDayId, mActionId);
        //添加训练记录
        mPresenter.saveExerciseRecord(mDayId);
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_DAY_ID, mDayId);
        bundle.putInt(Constant.EXTRA_ACTION_ID, mActionId);
        MyApplication.startFragment(ActionFinishFragment.class, bundle);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRestartAnim(String event) {
        if (Constant.EVENT_ON_RESTART_ACTION.equals(event)) {
            mPresenter.restartAction(mDayId, mActionId);
        }
    }


    @Override
    public void onObtainSound(boolean muteOption, boolean voiceOption, boolean trainVoiceOption) {
        //更新数据库
        Log.d(TAG, "onObtainSoundOption: muteOption = " + muteOption);
        Log.d(TAG, "onObtainSoundOption: voiceOption = " + voiceOption);
        Log.d(TAG, "onObtainSoundOption: trainVoiceOption = " + trainVoiceOption);
        mPresenter.updateSoundOption(muteOption, voiceOption, trainVoiceOption);
        //恢复运动
        mPresenter.restartAction(mDayId, mActionId);
    }

    @Override
    public void onSoundDialogCancel() {
        mPresenter.restartAction(mDayId, mActionId);
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }
}
