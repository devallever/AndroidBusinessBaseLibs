package com.allever.daymatter.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.allever.daymatter.data.Event;
import com.allever.daymatter.data.Repository;
import com.allever.daymatter.ui.dialog.DialogHelper;
import com.allever.daymatter.event.SortEvent;
import com.allever.daymatter.mvp.presenter.EditDayMatterPresenter;
import com.allever.daymatter.utils.ToastUtil;
import com.allever.daymatter.R;
import com.allever.daymatter.ui.dialog.RepeatTypeDialog;
import com.allever.daymatter.mvp.BaseActivity;
import com.allever.daymatter.mvp.view.IAddDayMatterView;
import com.allever.daymatter.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;


/**
 *
 * @author Allever
 * @date 18/5/21
 */

public class EditDayMatterActivity extends BaseActivity<IAddDayMatterView, EditDayMatterPresenter> implements IAddDayMatterView, View.OnClickListener {

    private static final String TAG = "EditDayMatterActivity";
    private static final String EXTRA_EDIT_MODE = "EXTRA_EDIT_MODE";
    private static final String EXTRA_EVENT_ID = "EXTRA_EVENT_ID";
    private static final int DEFAULT_EVENT_ID = -1;

    private boolean mIsEditMode = false;
    private int mEventId = DEFAULT_EVENT_ID;

    Toolbar mToolbar;
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
    Button mBtnSave;
    LinearLayout mLlRoot;
    RelativeLayout mRlEndDateSwitchContainer;
    RelativeLayout mRlEndDateContainer;
    Button mBtnDelete;
    Button mBtnUpdate;
    ViewGroup mDeleteUpdateContainer;

    private String mEventTitle;

    private ProgressDialog mProgressDialog;

    private RepeatTypeDialog mRepeatDialog;


    private AlertDialog mAddSortDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dm_activity_add_day_matter);

        mToolbar = findViewById(R.id.id_toolbar);
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
        mBtnSave = findViewById(R.id.id_add_day_matter_btn_save);
        mLlRoot = findViewById(R.id.id_add_day_matter_ll_root);
        mRlEndDateSwitchContainer = findViewById(R.id.id_input_rl_end_date_switch_container);
        mRlEndDateContainer = findViewById(R.id.id_input_rl_end_date_container);
        mBtnDelete = findViewById(R.id.id_edit_day_matter_btn_delete);
        mBtnUpdate = findViewById(R.id.id_edit_day_matter_btn_update);
        mDeleteUpdateContainer = findViewById(R.id.id_add_day_matter_delete_update_container);

        adaptStatusBar(mToolbar);

        // 设置点击监听
        mTvDate.setOnClickListener(this);
        mTvSort.setOnClickListener(this);
        mTvIsTop.setOnClickListener(this);
        mTvRepeat.setOnClickListener(this);
        mTvIsEndDate.setOnClickListener(this);
        mTvEndDate.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);
        mBtnSave.setOnClickListener(this);

        mIsEditMode = getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false);
        mEventId = getIntent().getIntExtra(EXTRA_EVENT_ID, -1);

        if (mIsEditMode) {
            initToolbar(mToolbar, R.string.dm_modify_day_matter);
            mBtnSave.setVisibility(View.GONE);
            mDeleteUpdateContainer.setVisibility(View.VISIBLE);
            mPresenter.getEventData(this, mEventId);
        } else {
            mPresenter.getDefaultData(this);
            initToolbar(mToolbar, R.string.dm_add_event);
            mBtnSave.setVisibility(View.VISIBLE);
            mDeleteUpdateContainer.setVisibility(View.GONE);
        }

        initDialog();

        //设置控件监听器
        setListener();

        //设置事件默认值->
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



        DialogHelper.Builder addSortDialogBuilder = new DialogHelper.Builder()
                .isShowEditText(true)
                .isShowMessage(false)
                .setTitleContent(getString(R.string.dm_add_sort));
        mAddSortDialog = DialogHelper.INSTANCE.createEditTextDialog(this, addSortDialogBuilder, new DialogHelper.EditDialogCallback() {
            @Override
            public void onOkClick(@NotNull AlertDialog dialog, @NotNull String etContent) {
                if (etContent.isEmpty()) {
                    ToastUtil.INSTANCE.show("请输入内容");
                } else {
                    Event.Sort sort = Repository.getIns().saveSort(etContent);
                    SortEvent sortEvent = new SortEvent();
                    EventBus.getDefault().post(sortEvent);
                    dialog.dismiss();
                    mPresenter.setmSortId(sort.getId());
                    setSort(sort.getName());
                }
            }

            @Override
            public void onCancelClick(@NotNull AlertDialog dialog) {

            }
        });

    }

    @Override
    protected EditDayMatterPresenter createPresenter() {
        return new EditDayMatterPresenter();
    }

    public static void startSelf(Context context, boolean editMode, int eventId) {
        Intent intent = new Intent(context, EditDayMatterActivity.class);
        intent.putExtra(EXTRA_EDIT_MODE, editMode);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        context.startActivity(intent);
    }

    private void setListener() {
        mSwitchIsTop.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.setmTop(isChecked)
        );

        mSwitchEndDate.setOnCheckedChangeListener((buttonView, isChecked) ->
                mPresenter.setmEndDateSwitch(isChecked)
        );
    }

    @Override
    public void showProgressDialog() {
        mProgressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        mProgressDialog.dismiss();
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
            DialogHelper.INSTANCE.createSelectSortDialog(this, new DialogHelper.SelectSortCallback() {
                        @Override
                        public void onItemClick(int position, @NotNull String sortName, int id, @NotNull AlertDialog dialog) {
                            mPresenter.setmSortId(id);
                            setSort(sortName);
                            dialog.dismiss();
                        }

                        @Override
                        public void onAddSortClick(@NotNull AlertDialog dialog) {
                            dialog.dismiss();
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mAddSortDialog.show();
                                }
                            }, 100);
                        }
                    })
                    .show();

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

            //保存
        } else if (id == R.id.id_add_day_matter_btn_save) {
            mEventTitle = mEtEventTitle.getText().toString();
            //如果标题不为空，则保存事件
            if (!TextUtils.isEmpty(mEventTitle)) {
                //打开progressDialog
                mPresenter.saveEvent(mEventTitle);

            } else {
                showToast(getResources().getString(R.string.dm_please_input_event_title));
            }
        } else if (id == R.id.id_edit_day_matter_btn_update) {
            String content = mEtEventTitle.getText().toString();
            if (mEventId != DEFAULT_EVENT_ID && !content.isEmpty()) {
                mPresenter.updateEvent(mEventId, content);
            }
        } else if (id == R.id.id_edit_day_matter_btn_delete) {
            if (mEventId != DEFAULT_EVENT_ID) {
                mPresenter.deleteDayMatter(mEventId);
            }
        }
    }
}
