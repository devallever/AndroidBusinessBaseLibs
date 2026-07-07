package com.allever.lose.weight.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.allever.lose.weight.MyApplication;
import com.allever.lose.weight.R;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.base.BaseDialog;
import com.allever.lose.weight.base.BaseFragment;
import com.allever.lose.weight.ui.dialog.HeightWeightDialog;
import com.allever.lose.weight.ui.dialog.RemindDialog;
import com.allever.lose.weight.ui.dialog.YearSelectDialog;
import com.allever.lose.weight.ui.mvp.presenter.ActionFinishPresenter;
import com.allever.lose.weight.ui.mvp.view.IActionFinishView;
import com.allever.lose.weight.util.CalculationUtil;
import com.allever.lose.weight.util.Util;
import com.allever.lose.weight.ui.view.widget.BMIView;

import org.greenrobot.eventbus.EventBus;
/**
 * Created by Mac on 18/3/2.
 */
@SuppressLint("NonConstantResourceId")
public class ActionFinishFragment extends BaseFragment<IActionFinishView, ActionFinishPresenter> implements IActionFinishView,
        RemindDialog.IRemindListener,
        HeightWeightDialog.IWHDataListener,
        YearSelectDialog.IYearDataListener {
    private static final String TAG = "ActionFinishFragment";

    Toolbar mToolbar;
    BMIView mBmiView;
    TextView mTvCalName;
    TextView mTvCalValue;
    TextView mTvRemind;
    TextView mTvSave;
    TextView mTvShare;
    TextView mTvHide;
    TextView mTvEditWeight;
    TextView mTvExerciseDuration;
    TextView mTvTrainCount;
    TextView mTvDayFinish;
    RadioButton mRbKg;
    RadioButton mRbLb;

    EditText mEtWeight;
    SwitchCompat mSwitchSyncGoogle;
    RadioGroup mRgFeel;
    RadioGroup mRgUnit;
    NestedScrollView idFgActionFinishNestedScrollView;

    private float mHeight;

    private RemindDialog mReminderDialog;
    private HeightWeightDialog mHeightWeightDialog;
    private YearSelectDialog mYearDialog;

    private int mDayId = 1;


    public static ActionFinishFragment newInstance(int dayId, int actionId) {
        ActionFinishFragment fragment = new ActionFinishFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constant.EXTRA_DAY_ID, dayId);
        bundle.putInt(Constant.EXTRA_ACTION_ID, actionId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        super.onCreateView(inflater, container, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mDayId = bundle.getInt(Constant.EXTRA_DAY_ID);
        }

        View view = inflater.inflate(R.layout.lw_fragment_action_finish, container, false);

        mToolbar = view.findViewById(R.id.id_fg_action_finish_toolbar);
        mBmiView = view.findViewById(R.id.id_fg_action_finish_bmi_view);
        mTvCalName = view.findViewById(R.id.id_fg_action_finish_tv_cal_name);
        mTvCalValue = view.findViewById(R.id.id_fg_action_finish_tv_cal_value);
        mTvRemind = view.findViewById(R.id.id_fg_action_finish_tv_remind);
        mTvSave = view.findViewById(R.id.id_fg_action_finish_tv_save);
        mTvShare = view.findViewById(R.id.id_fg_action_finish_tv_share);
        mTvEditWeight = view.findViewById(R.id.id_fg_action_finish_tv_edit_weight);
        mTvExerciseDuration = view.findViewById(R.id.id_fg_action_finish_tv_train_duration);
        mTvTrainCount = view.findViewById(R.id.id_fg_action_finish_tv_train_count);
        mTvDayFinish = view.findViewById(R.id.id_fg_action_finish_tv_finish);
        mRbKg = view.findViewById(R.id.id_fg_action_finish_rb_unit_kg);
        mRbLb = view.findViewById(R.id.id_fg_action_finish_rb_unit_lb);
        mEtWeight = view.findViewById(R.id.id_fg_action_finish_et_weight);
        mSwitchSyncGoogle = view.findViewById(R.id.id_fg_action_finish_switch_sync_google);
        mRgFeel = view.findViewById(R.id.id_fg_action_finish_rg_feel);
        mRgUnit = view.findViewById(R.id.id_fg_action_finish_rg_unit_container);


        initView();
        initToolbar(mToolbar);

        mReminderDialog = new RemindDialog(requireActivity(), this);
        mReminderDialog.show();

        mPresenter.getTrainCount();
        mPresenter.getDuration();
        mPresenter.getCalorie(mDayId);
        mPresenter.getBmi();
        mPresenter.getCurrentDay();
        mPresenter.getWeight();

        return view;
    }

    protected void initView() {
        mHeightWeightDialog = new HeightWeightDialog.Builder(requireActivity())
                .setOkBtn(getResources().getString(R.string.lw_next), new BaseDialog.ClickListener() {
                    @Override
                    public void onClick(BaseDialog dialog) {
                        //dialog.hide();
                    }
                })
                .setDataListener(ActionFinishFragment.this)
                .build();

        mYearDialog = new YearSelectDialog(requireActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mYearDialog.hide();
                mHeightWeightDialog.show(true);
            }
        }, ActionFinishFragment.this);

        mTvCalName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeightWeightDialog.show(true);
            }
        });

        mTvCalValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeightWeightDialog.show(true);
            }
        });

        mTvRemind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.startFragment(ReminderFragment.class, null);
            }
        });

        mTvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().onBackPressed();
            }
        });

        mTvShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = Util.getShareIntent(requireActivity());
                startActivity(shareIntent);
            }
        });

        mRgUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                String value = mEtWeight.getText().toString();
                if (value.isEmpty()) {
                    value = String.valueOf(0);
                }
                if (id == R.id.id_fg_action_finish_rb_unit_kg) {
                    mPresenter.calLb2Kg(Float.valueOf(value));
                } else if (id == R.id.id_fg_action_finish_rb_unit_lb) {
                    mPresenter.calKg2Lb(Float.valueOf(value));
                }
            }
        });


        //todo 输入完成后刷新BMI
        mEtWeight.setText("0");
        mEtWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String value = mEtWeight.getText().toString();
                if (value.isEmpty()) {
                    value = String.valueOf(0);
                }
                if (mRbLb.isChecked()) {
                    value = String.valueOf(CalculationUtil.lb2Kg(Float.valueOf(value)));
                }

                //value 单位是 kg
                mPresenter.saveWeight(Double.valueOf(value));
//                //TODO 保存当前体重值 然后刷新BMI
//                mPresenter.getBmi();
//
                //DEBUG
                //单位m 从model中获取
                mBmiView.setGender(0)
                        .setWeight(Float.valueOf(value))
                        .setHeight(mHeight / 100);
            }
        });



        mTvEditWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeightWeightDialog.show(true);
            }
        });

        mRgFeel.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.id_fg_action_finish_rb_level_1) {
                    showToast("level 1");
                } else if (id == R.id.id_fg_action_finish_rb_level_2) {
                    showToast("level 2");
                } else if (id == R.id.id_fg_action_finish_rb_level_3) {
                    showToast("level 3");
                } else if (id == R.id.id_fg_action_finish_rb_level_4) {
                    showToast("level 4");
                } else if (id == R.id.id_fg_action_finish_rb_level_5) {
                    showToast("level 5");
                }
            }
        });

    }

    @Override
    protected ActionFinishPresenter createPresenter() {
        return new ActionFinishPresenter();
    }

    @Override
    public void updataWeightEditText(float value) {
        mEtWeight.setText(String.valueOf(value));
    }

    @Override
    public void setExerciseCount(int count) {
        mTvTrainCount.setText(String.valueOf(count));
    }


    /**
     * 获取卡路里
     */
    @Override
    public void setCalorie(int calorie) {
        mTvCalName.setText(getString(R.string.lw_calorie));
        mTvCalName.setClickable(false);
        mTvCalValue.setText(String.valueOf(calorie));
        mTvCalValue.setClickable(false);
    }

    @Override
    public void setCurrentExerciseDuration(String duration) {
        mTvExerciseDuration.setText(duration);
    }

    /**
     * 获取BMI回调
     * 0-1都一样
     * kg
     * m
     */
    @Override
    public void setbBmi(float weight, float height) {
        Log.d(TAG, "setbBmi: weight = " + weight);
        mHeight = height;
        mBmiView.setVisibility(View.VISIBLE);
        mBmiView.setGender(0)
                .setWeight(weight)
                .setHeight(height / 100);
        mTvEditWeight.setVisibility(View.VISIBLE);
//        mTvHide.setVisibility(View.VISIBLE);
//        mTvInputWH.setVisibility(View.INVISIBLE);
        if (mRbKg.isChecked()) {
            mEtWeight.setText(String.valueOf(weight));
        } else {
            mEtWeight.setText(String.valueOf(CalculationUtil.kg2Lb(height)));
        }
    }

    @Override
    public void setDayFinish(int day) {
        mTvDayFinish.setText("Day " + day + getResources().getString(R.string.lw_finish));
    }

    @Override
    public void setDayFinishTitle(String title) {
        mTvDayFinish.setText(title);
    }

    @Override
    public void setWeight(double weight) {
        mEtWeight.setText(String.valueOf(weight));
        mRbKg.setChecked(true);
        mRbLb.setChecked(false);
    }

    /**
     * 提醒框回调
     */
    @Override
    public void onAddReminder(int hour) {
        //
        Log.d(TAG, "onAddReminder: hour = " + hour);
        //添加一条提醒记录
        mPresenter.addReminder(hour);
    }

    /**
     * 身高体重输入框回调
     */
    @Override
    public void onWHDataOptain(float weight, float height) {
        Log.d(TAG, "onWHDataOptain: weight = " + weight);
        //更新数据 向数据库或全局类中刷新数据
        if (weight == 0 || height == 0) {
            showToast(R.string.lw_please_input_weight_height);
            mHeightWeightDialog.show(true);
        } else {
            mHeightWeightDialog.hide();
            mYearDialog.show(true);
            //更新bmi
            setbBmi(weight, height);
        }
        mPresenter.setWHData(weight, height);

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().post(Constant.EVENT_START_HISTORY);
        EventBus.getDefault().post(Constant.EVENT_REFRESH_VIEW);
    }

    /**
     * 性别年份输入框回调
     *
     * @param gender 男：0  女：1
     */
    @Override
    public void onOptainYearData(int year, int gender) {
        //更新数据 向数据库或全局类中刷新数据
        mPresenter.getCalorie(mDayId);
        mPresenter.updateYear(year);
        mPresenter.updateGender(gender);
    }

}
