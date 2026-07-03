package com.allever.daymatter.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allever.daymatter.R;
import com.allever.daymatter.ui.dialog.RepeatTypeDialog;
import com.allever.daymatter.ui.dialog.SortDialog;
import com.allever.daymatter.mvp.BaseActivity;
import com.allever.daymatter.mvp.presenter.ModifyDayMatterPresenter;
import com.allever.daymatter.mvp.view.IModifyDayMatterView;
import com.allever.daymatter.utils.Constants;


/**
 * Created by Allever on 18/5/22.
 */

public class ModifyDayMatterActivity extends BaseActivity<IModifyDayMatterView, ModifyDayMatterPresenter> implements IModifyDayMatterView, View.OnClickListener {

    private static final String TAG = "ModifyDayMatterActivity";

    public static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";

    Toolbar mToolbar;
    Button mBtnDelete;
    Button mBtnSave;
    EditText mEtEventTitle;
    TextView mTvDate;
    TextView mTvSort;
    TextView mTvSortSelector;
    TextView mTvIsTop;
    SwitchCompat mSwitchIsTop;
    TextView mTvRepeat;
    TextView mTvRepeatSelector;
    TextView mTvIsEndDate;
    SwitchCompat mSwitchEndDate;
    TextView mTvEndDate;
    LinearLayout mLlRoot;
    RelativeLayout mRlEndDateSwitchContainer;
    RelativeLayout mRlEndDateContainer;

    private RepeatTypeDialog mRepeatDialog;
    private SortDialog mSortDialog;
    private ProgressDialog mProgressDialog;


    private int mEventId;
    private String mEventTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dm_activity_modify_day_matter);

        mToolbar = findViewById(R.id.id_toolbar);
        mBtnDelete = findViewById(R.id.id_modify_day_matter_btn_delete);
        mBtnSave = findViewById(R.id.id_modify_day_matter_btn_save);
        mEtEventTitle = findViewById(R.id.id_input_et_title);
        mTvDate = findViewById(R.id.id_input_tv_date);
        mTvSort = findViewById(R.id.id_input_tv_sort);
        mTvSortSelector = findViewById(R.id.id_input_tv_sort_selector);
        mTvIsTop = findViewById(R.id.id_input_tv_is_top);
        mSwitchIsTop = findViewById(R.id.id_input_switch_is_top);
        mTvRepeat = findViewById(R.id.id_input_tv_repeat);
        mTvRepeatSelector = findViewById(R.id.id_input_tv_repeat_selector);
        mTvIsEndDate = findViewById(R.id.id_input_tv_is_end_date);
        mSwitchEndDate = findViewById(R.id.id_input_switch_end_date);
        mTvEndDate = findViewById(R.id.id_input_tv_end_date);
        mLlRoot = findViewById(R.id.id_modify_day_matter_ll_root);
        mRlEndDateSwitchContainer = findViewById(R.id.id_input_rl_end_date_switch_container);
        mRlEndDateContainer = findViewById(R.id.id_input_rl_end_date_container);

        adaptStatusBar(mToolbar);

        mBtnDelete.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);
        mTvDate.setOnClickListener(this);
        mTvSort.setOnClickListener(this);
        mTvIsTop.setOnClickListener(this);
        mTvRepeat.setOnClickListener(this);
        mTvIsEndDate.setOnClickListener(this);
        mTvEndDate.setOnClickListener(this);

        mEventId = getIntent().getIntExtra(EXTRA_EVENT_ID,0);
        if (mEventId == 0){
            return;
        }

        initToolbar(mToolbar, R.string.dm_modify_day_matter);

        initDialog();

        setListener();

        mPresenter.getEventData(this, mEventId);
    }

    private void setListener() {
        mSwitchIsTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPresenter.setmTop(isChecked);
            }
        });

        mSwitchEndDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPresenter.setmEndDateSwitch(isChecked);
            }
        });
    }

    private void initDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.dm_saving));

        /**
         * 重复类型对话框
         * 当选择不重复，结束时间开关项可见
         * 当选择其他重复类型，结束时间开关可见
         */
        mRepeatDialog = RepeatTypeDialog.newInsance(new RepeatTypeDialog.OptionListener() {
            @Override
            public void onItemClick(DialogFragment dialog, int repeatType) {
                //当修改了重复类型后，保存修改的值
                mPresenter.setmRepeatType(repeatType);

                dialog.dismiss();
                Log.d(TAG, "onItemClick: repeat" + repeatType);
                switch (repeatType) {
                    case Constants.REPEAT_TYPE_NO_REPEAT:
                        setTvRepeatType(getString(R.string.dm_no_repeat));

                        //结束日期可见
                        setEndDateSwitchVisible();
                        mPresenter.setmEndDateSwitch(mSwitchEndDate.isChecked());
                        break;
                    case Constants.REPEAT_TYPE_PER_WEEK:
                        setTvRepeatType(getString(R.string.dm_per_week_repeat));

                        //结束日期可见
                        setEndDateSwitchGone();
                        setEndDateItemGone();
                        break;
                    case Constants.REPEAT_TYPE_PER_MONTH:
                        setTvRepeatType(getString(R.string.dm_per_month_repeat));

                        //结束日期可见
                        setEndDateSwitchGone();
                        setEndDateItemGone();
                        break;
                    case Constants.REPEAT_TYPE_PER_YEAR:
                        setTvRepeatType(getString(R.string.dm_per_year_repeat));

                        //结束日期可见
                        setEndDateSwitchGone();
                        setEndDateItemGone();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onCancel(DialogFragment dialog) {
                dialog.dismiss();
            }
        });

        mSortDialog = SortDialog.newInsance(new SortDialog.OptionListener() {
            @Override
            public void onItemClick(DialogFragment dialog, String sortName, int sortId) {
                //当修改了事件类型后，保存修改的值
                mPresenter.setmSortId(sortId);
                setSort(sortName);
                dialog.dismiss();
            }

            @Override
            public void onCancel(DialogFragment dialog) {
                dialog.dismiss();
            }
        });


    }

    @Override
    protected ModifyDayMatterPresenter createPresenter() {
        return new ModifyDayMatterPresenter();
    }


    public static void startSelf(Context context, int eventId){
        Intent intent = new Intent(context, ModifyDayMatterActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        context.startActivity(intent);
    }

    @Override
    public void showProgressDialog() {
        //mProgressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        //mProgressDialog.dismiss();
    }

    @Override
    public void finishSelf() {
        finish();
    }

    @Override
    public void setTvDate(String date) {
        mTvDate.setText(date);
    }

    @Override
    public void setSort(String sort) {
        mTvSortSelector.setText(sort);
    }

    @Override
    public void setTopSwitch(boolean value) {
        mSwitchIsTop.setChecked(value);
    }

    @Override
    public void setTvRepeatType(String repeatType) {
        mTvRepeatSelector.setText(repeatType);
    }

    @Override
    public void setEndDateSwitch(boolean value) {
        mSwitchEndDate.setChecked(value);
    }

    @Override
    public void setTvEndDate(String endDate) {
        mTvEndDate.setText(endDate);
    }

    @Override
    public void showDatePickDialog(DatePickerDialog datePickerDialog) {
        datePickerDialog.show();
    }

    @Override
    public void showEndDatePickDialog(DatePickerDialog datePickerDialog) {
        datePickerDialog.show();
    }

    @Override
    public void showRepeatTypeDialog() {
        mRepeatDialog.show(getSupportFragmentManager(), TAG);
    }

    @Override
    public void setEndDateItemVisible() {
        mRlEndDateContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void setEndDateItemGone() {
        mRlEndDateContainer.setVisibility(View.GONE);
    }

    @Override
    public void setEndDateSwitchVisible() {
        mRlEndDateSwitchContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void setEndDateSwitchGone() {
        mRlEndDateSwitchContainer.setVisibility(View.GONE);
    }

    @Override
    public void setEtTetle(String title) {
        mEtEventTitle.setText(title);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();//日期
        if (id == R.id.id_input_tv_date) {//弹出日历选择器
            mPresenter.openDatePicker(this);

            //分类
        } else if (id == R.id.id_input_tv_sort) {//打开选择分类界面
            mSortDialog.show(getSupportFragmentManager(), TAG);

            //置顶项
        } else if (id == R.id.id_input_tv_is_top) {
            mSwitchIsTop.setChecked(!mSwitchIsTop.isChecked());

            //重复
        } else if (id == R.id.id_input_tv_repeat) {//打开重复类型对话框
            showRepeatTypeDialog();

            //结束时间项
        } else if (id == R.id.id_input_tv_is_end_date) {
            mSwitchEndDate.setChecked(!mSwitchEndDate.isChecked());

            //选择结束时间
        } else if (id == R.id.id_input_tv_end_date) {//如果结束开关为打开状态,则打开日历选择器
            if (mSwitchEndDate.isChecked()) {
                mPresenter.openEndDatePicker(this);
            } else {
                showToast(getResources().getString(R.string.dm_please_open_end_date_switch));
            }
        } else if (id == R.id.id_modify_day_matter_btn_delete) {
            mPresenter.deleteDayMatter(mEventId);
        } else if (id == R.id.id_modify_day_matter_btn_save) {
            mEventTitle = mEtEventTitle.getText().toString();
            //如果标题不为空，则保存事件
            if (!TextUtils.isEmpty(mEventTitle)) {
                //打开progressDialog
                mPresenter.updateEvent(mEventId, mEventTitle);
            } else {
                showToast(getResources().getString(R.string.dm_please_input_event_title));
            }
        }
    }
}
