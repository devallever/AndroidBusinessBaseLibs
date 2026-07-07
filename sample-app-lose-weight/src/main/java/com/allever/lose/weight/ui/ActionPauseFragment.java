package com.allever.lose.weight.ui;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.allever.lose.weight.R;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.base.BaseFragment;
import com.allever.lose.weight.bean.ActionItem;
import com.allever.lose.weight.ui.mvp.presenter.ActionPausePresenter;
import com.allever.lose.weight.ui.mvp.view.IActionPauseView;

import org.greenrobot.eventbus.EventBus;


/**
 * Created by Mac on 18/3/1.
 */

public class ActionPauseFragment extends BaseFragment<IActionPauseView, ActionPausePresenter> implements IActionPauseView {
    private static final String TAG = "ActionPauseFragment";

    ImageView mIvGuide;
    ImageView mIvClose;
    TextView mTvVideo;
    TextView mTvName;
    TextView mTvDesc;
    TextView mTvLeftTime;
    ProgressBar mProgressBar;


    private AnimationDrawable mAnimationDrawable;

    private int mDayId = 1;
    private int mActionId = 1;
    private int mDuration = 0;

    public static ActionPauseFragment newInstance(int dayId, int actionId, int duration) {
        ActionPauseFragment fragment = new ActionPauseFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_DAY_ID, dayId);
        bundle.putInt(Constant.EXTRA_ACTION_ID, actionId);
        bundle.putInt(Constant.EXTRA_DURATION, duration);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.lw_fragment_action_pause, container, false);

        mIvGuide = view.findViewById(R.id.id_fg_action_pause_iv_guide);
        mIvClose = view.findViewById(R.id.id_fg_action_pause_iv_close);
        mTvVideo = view.findViewById(R.id.id_fg_action_pause_tv_video);
        mTvName = view.findViewById(R.id.id_fg_action_pause_tv_name);
        mTvDesc = view.findViewById(R.id.id_fg_action_pause_tv_action_desc);
        mTvLeftTime = view.findViewById(R.id.id_action_pause_tv_left_time);
        mProgressBar = view.findViewById(R.id.id_fg_action_pause_progress_bar);


        Bundle bundle = getArguments();
        if (bundle != null) {
            mActionId = bundle.getInt(Constant.EXTRA_ACTION_ID);
            mDayId = bundle.getInt(Constant.EXTRA_DAY_ID);
            mDuration = bundle.getInt(Constant.EXTRA_DURATION);
        }

        initView();
        mPresenter.getActionData(mDayId, mActionId);
        mPresenter.getLeftTime(mDayId, mActionId, mDuration);
        mPresenter.getCurrentLevel(mDayId, mActionId);
        return view;
    }

    private void initView() {
        mIvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().onBackPressed();
            }
        });
    }

    @Override
    public void setData(final ActionItem actionItem) {
        if (actionItem == null) {
            return;
        }
        mAnimationDrawable = actionItem.getAnimationDrawable();
        mIvGuide.setImageDrawable(mAnimationDrawable);
        mAnimationDrawable.setOneShot(false);
        mAnimationDrawable.start();

        mTvName.setText(actionItem.getName());
        StringBuilder stringBuilder = new StringBuilder();
        for (String desc : actionItem.getDescList()) {
            stringBuilder.append("\n" + desc + "\n");
        }
        mTvDesc.setText(stringBuilder.toString());

//        mTvVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showToast(actionItem.getVideoUrl());
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(actionItem.getVideoUrl()));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setPackage("com.google.android.youtube");
//                startActivity(intent);
//            }
//        });


    }

    @Override
    public void setLeftTime(String time) {
        mTvLeftTime.setText(time);
    }

    @Override
    public void setCurrent(int currentLevel, int levelCount) {
        mProgressBar.setMax(levelCount);
        mProgressBar.setProgress(currentLevel);
    }

    @Override
    protected ActionPausePresenter createPresenter() {
        return new ActionPausePresenter();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().post(Constant.EVENT_ON_RESTART_ACTION);
    }
}
