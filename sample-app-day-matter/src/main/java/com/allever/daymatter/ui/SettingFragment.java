package com.allever.daymatter.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.allever.daymatter.utils.TimeUtils;
import com.allever.daymatter.R;
import com.allever.daymatter.data.Config;
import com.allever.daymatter.mvp.BaseFragment;
import com.allever.daymatter.mvp.presenter.SettingPresenter;
import com.allever.daymatter.mvp.view.ISettingView;

/**
 * Created by Allever on 18/6/1.
 */

public class SettingFragment extends BaseFragment<ISettingView, SettingPresenter> implements ISettingView, View.OnClickListener {

    private static final String TAG = "SettingFragment";


    //当天提醒开关
    SwitchCompat mSwitchCurrentDay;

    //当天提醒项-主要用于设置监听
    RelativeLayout mRlCurrentDaySwitchContainer;

    //当天提醒时间
    TextView mTvCurrentDayRemindTime;

    //当天提醒时间项-主要用于设置监听
    RelativeLayout mRlCurrentDayRemindTimeContainer;

    //前天提醒开关
    SwitchCompat mSwitchBeforeDay;

    //前天提醒开关选项-主要用于设置监听
    RelativeLayout mRlBeforeDaySwitchContainer;

    //前天提醒时间
    TextView mTvBeforeDayRemindTime;

    //前天提醒时间选项-主要用于设置监听
    RelativeLayout mRlBeforeDayRemindTimeContainer;

    ViewGroup mAboutContainer;
    ViewGroup mFeedbackContainer;


    //当天提醒时间选择器
    private TimePickerDialog mCurrentTimePicker;

    //前天提醒时间选择器
    private TimePickerDialog mBeforeTimePicker;

    private Config mConfig;

    private ViewGroup mBannerContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dm_fragment_remind, container, false);


        mSwitchCurrentDay = view.findViewById(R.id.id_fg_remind_switch_current_day);
        mRlCurrentDaySwitchContainer = view.findViewById(R.id.id_fg_remind_rl_current_day_switch_container);
        mTvCurrentDayRemindTime = view.findViewById(R.id.id_fg_remind_tv_current_day_remind_time);
        mRlCurrentDayRemindTimeContainer = view.findViewById(R.id.id_fg_remind_rl_current_day_remind_time_container);
        mSwitchBeforeDay = view.findViewById(R.id.id_fg_remind_switch_before_day);
        mRlBeforeDaySwitchContainer = view.findViewById(R.id.id_fg_remind_rl_before_day_switch_container);
        mTvBeforeDayRemindTime = view.findViewById(R.id.id_fg_remind_tv_before_day_remind_time);
        mRlBeforeDayRemindTimeContainer = view.findViewById(R.id.id_fg_remind_rl_before_day_remind_time_container);
        mAboutContainer = view.findViewById(R.id.id_fg_remind_rl_before_day_remind_about_container);
        mFeedbackContainer = view.findViewById(R.id.id_fg_remind_rl_before_day_remind_feedback_container);

        // 设置点击监听
        mRlCurrentDaySwitchContainer.setOnClickListener(this);
        mRlCurrentDayRemindTimeContainer.setOnClickListener(this);
        mRlBeforeDaySwitchContainer.setOnClickListener(this);
        mRlBeforeDayRemindTimeContainer.setOnClickListener(this);
        mAboutContainer.setOnClickListener(this);
        mFeedbackContainer.setOnClickListener(this);
        view.findViewById(R.id.id_fg_remind_rl_before_day_remind_support_container).setOnClickListener(this);
//        mSupportContainer.setOnClickListener(this); // 别忘了这个
        

        //获取提醒配置
        mPresenter.getRemindConfig();

        //设置当前提醒
        mPresenter.setCurrentDayRemind(getActivity());

        //设置前天提醒
        mPresenter.setBeforeDayRemind(getActivity());

        setListener();

        initDialog();

        mBannerContainer = view.findViewById(R.id.bannerContainer);

        return view;
    }

    private void initDialog() {
        if (mConfig == null) {
            return;
        }
        mCurrentTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //保存到数据库
                mPresenter.updateCurrentRemindTime(hourOfDay, minute);
                //
                setCurrentRemindTime(TimeUtils.INSTANCE.formatTime(hourOfDay, minute));

                //更新提醒
                mPresenter.setCurrentDayRemind(getActivity());

            }
        }, mConfig.getCurrentRemindHour(), mConfig.getCurrentRemindMin(), true);

        mBeforeTimePicker = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                //保存到数据库
                mPresenter.updateBeforeRemindTime(hourOfDay, minute);
                //
                setBeforeRemindTime(TimeUtils.INSTANCE.formatTime(hourOfDay, minute));

                //更新提醒
                mPresenter.setBeforeDayRemind(getActivity());
            }
        }, mConfig.getBeforeRemindHour(), mConfig.getBeforeRemindMin(), true);
    }

    private void setListener() {
        mSwitchCurrentDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: current switch");
                //保存到数据库
                mPresenter.updateCurrentDaySwitch(isChecked);

                //更新提醒
                mPresenter.setCurrentDayRemind(getActivity());
            }
        });

        mSwitchBeforeDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: before");
                //保存到数据库
                mPresenter.updateBeforeDaySwitch(isChecked);

                //更新提醒
                mPresenter.setBeforeDayRemind(getActivity());
            }
        });
    }

    @Override
    protected SettingPresenter createPresenter() {
        return new SettingPresenter();
    }
    @Override
    public void setCurrentRemindSwitch(boolean value) {
        mSwitchCurrentDay.setChecked(value);
    }

    @Override
    public void setCurrentRemindTime(String time) {
        mTvCurrentDayRemindTime.setText(time);
    }

    @Override
    public void setBeforeRemindSwitch(boolean value) {
        mSwitchBeforeDay.setChecked(value);
    }

    @Override
    public void setBeforeRemindTime(String time) {
        mTvBeforeDayRemindTime.setText(time);
    }

    @Override
    public void returnRemindConfig(Config config) {
        if (config != null){
            mConfig = config;
        }
    }

    private void supportUS() {
        new AlertDialog.Builder(getActivity())
                .setMessage("需要消耗流量，是否继续？")
                .setPositiveButton("确定", (dialog, which) -> {
                })
                .setNegativeButton("残忍拒绝", (dialog, which) -> {
                    showToast("点击下面小广告支持我们");
                })
                .show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();//当天提醒
        if (id == R.id.id_fg_remind_rl_current_day_switch_container) {
            mSwitchCurrentDay.setChecked(!mSwitchCurrentDay.isChecked());
            //当天提醒时间
        } else if (id == R.id.id_fg_remind_rl_current_day_remind_time_container) {
            if (mCurrentTimePicker != null) {
                mCurrentTimePicker.show();
            }
            //前天提醒
        } else if (id == R.id.id_fg_remind_rl_before_day_switch_container) {
            mSwitchBeforeDay.setChecked(!mSwitchBeforeDay.isChecked());
            //前天提醒时间
        } else if (id == R.id.id_fg_remind_rl_before_day_remind_time_container) {
            if (mBeforeTimePicker != null) {
                mBeforeTimePicker.show();
            }
        } else if (id == R.id.id_fg_remind_rl_before_day_remind_about_container) {
            AboutActivity.Companion.actionStart(getActivity());
        } else if (id == R.id.id_fg_remind_rl_before_day_remind_feedback_container) {
            mPresenter.feedback(getActivity());
        } else if (id == R.id.id_fg_remind_rl_before_day_remind_support_container) {
            supportUS();
        }
    }
}
