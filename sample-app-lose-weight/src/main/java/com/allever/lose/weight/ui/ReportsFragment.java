package com.allever.lose.weight.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.allever.lose.weight.MyApplication;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.allever.lose.weight.R;
import com.allever.lose.weight.util.Constant;
import com.allever.lose.weight.base.BaseDialog;
import com.allever.lose.weight.ui.dialog.HeightWeightDialog;
import com.allever.lose.weight.ui.mvp.presenter.ReportPresenter;
import com.allever.lose.weight.ui.mvp.view.IReportView;
import com.allever.lose.weight.util.DateUtil;
import com.allever.lose.weight.util.DensityUtil;
import com.allever.lose.weight.ui.view.widget.BMIView;
import com.allever.lose.weight.ui.adapter.HistoryItemAdapter;
import com.allever.lose.weight.base.BaseMainFragment;
import com.allever.lose.weight.ui.dialog.WeightFragment;
import com.allever.lose.weight.util.ScreenUtils;
import com.allever.lose.weight.ui.view.widget.HorizontalDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReportsFragment extends BaseMainFragment<IReportView, ReportPresenter> implements IReportView, HeightWeightDialog.IWHDataListener,
        WeightFragment.IWeightRecordListener {
    private static final String TAG = "ReportsFragment";

    private static final int REQUEST_OAUTH_REQUEST_CODE = 0x01;
    private static final int RC_SIGN_IN = 0x02;

    RecyclerView recyclerView;
    LineChart chart;
    ImageView addWeight;
    TextView mTvEditBmi;
    TextView tvEditHeight;
    TextView mTvWorkout;
    TextView mTvKcal;
    TextView mTvDuration;
    NestedScrollView scrollView;
    TextView mTvHeaviestWeight;
    TextView mTvLightestWeight;
    TextView mTvCurrentWeight;
    TextView mTvCurrentHeight;
    BMIView mBmiView;
    LinearLayout mLlSyncContainer;
    TextView mTvAccount;
    TextView mTvSyncTime;
    private HistoryItemAdapter mAdapter;

    private HeightWeightDialog mHeightWeightDialog;

    public static Fragment newInstance() {
        return new ReportsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        EventBus.getDefault().register(this);
        View view = inflater.inflate(R.layout.lw_fragment_reports, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        chart = view.findViewById(R.id.chart);
        addWeight = view.findViewById(R.id.add_weight);
        mTvEditBmi = view.findViewById(R.id.tv_edit_bmi);
        tvEditHeight = view.findViewById(R.id.tv_edit_height);
        mTvWorkout = view.findViewById(R.id.tv_workout);
        mTvKcal = view.findViewById(R.id.tv_kcal);
        mTvDuration = view.findViewById(R.id.tv_duration);
        scrollView = view.findViewById(R.id.scroll_view);
        mTvHeaviestWeight = view.findViewById(R.id.heaviest);
        mTvLightestWeight = view.findViewById(R.id.lightest);
        mTvCurrentWeight = view.findViewById(R.id.tv_current);
        mTvCurrentHeight = view.findViewById(R.id.tv_current_height);
        mBmiView = view.findViewById(R.id.id_fg_report_bmi);
        mLlSyncContainer = view.findViewById(R.id.id_sync_ll_sync_container);
        mTvAccount = view.findViewById(R.id.id_sync_tv_account);
        mTvSyncTime = view.findViewById(R.id.id_sync_tv_sync_time);
        mBmiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        addWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("ReportsFragment", "click");
                WeightFragment.setRecordListener(ReportsFragment.this);
                MyApplication.startFragment(WeightFragment.class, null);
            }
        });
        mTvEditBmi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeightWeightDialog.show(true);
            }
        });
        tvEditHeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHeightWeightDialog.show(true);
            }
        });


        //设置图表控件样式
        setChartStyle();

        initDialog();

        setRecyclerView();
        refreshView();

        return view;
    }

    private void initDialog() {
        mHeightWeightDialog = new HeightWeightDialog.Builder(requireActivity())
                .setOkBtn(getResources().getString(R.string.lw_save), new BaseDialog.ClickListener() {
                    @Override
                    public void onClick(BaseDialog dialog) {
                        //dialog.hide();
                    }
                })
                .setDataListener(ReportsFragment.this)
                .build();

    }

    private void setRecyclerView() {
        LayoutInflater layoutInflater = LayoutInflater.from(requireActivity());
        mAdapter = new HistoryItemAdapter(R.layout.lw_item_weekly_calendar_item, getItemData());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 7) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        };
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter.addHeaderView(layoutInflater.inflate(R.layout.lw_item_history_header, recyclerView, false));
        mAdapter.addFooterView(getFooter(layoutInflater));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new HorizontalDecoration(ScreenUtils.dp2px(10)));
    }

    //数据无实际意义 只获取数量
    private List<Integer> getItemData() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            list.add(i);
        }
        return list;
    }

    private View getFooter(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.lw_item_history_footer, recyclerView, false);
        TextView records = view.findViewById(R.id.records);
        records.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getParentFragment() instanceof HomeFragment) {
                    MyApplication.startFragment(HistoryFragment.class, null);
                }
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected ReportPresenter createPresenter() {
        return new ReportPresenter();
    }

    @Override
    public void setWorkout(int workout) {
        mTvWorkout.setText(String.valueOf(workout));
    }

    @Override
    public void setKcal(int kcal) {
        mTvKcal.setText(String.valueOf(kcal));
    }

    @Override
    public void setDuration(String durationStr) {
        mTvDuration.setText(String.valueOf(durationStr));
    }

    @Override
    public void setCurrentWeight(double currentWeight, String unit) {
        mTvCurrentWeight.setText(currentWeight + unit);
    }

    @Override
    public void setHeaviestWeight(double heaviestWeight, String unit) {
        mTvHeaviestWeight.setText(heaviestWeight + unit);
    }

    @Override
    public void setLightestWeight(double lightestWeight, String unit) {
        mTvLightestWeight.setText(lightestWeight + unit);
    }

    @Override
    public void setBMI(int gender, float currentWeight, float height) {
        mBmiView.setGender(gender)
                .setWeight(currentWeight)
                .setHeight(height);
    }


    @Override
    public void setHeight(double height, String unit) {
        mTvCurrentHeight.setText(height + unit);
    }

    @Override
    public void onWHDataOptain(float weight, float height) {
        mPresenter.updateWeightHeight(weight, height);
        mHeightWeightDialog.hide();
        mPresenter.getBMI();
        mPresenter.getWeight();
        mPresenter.getHeight();
    }

    @Override
    public void refreshView() {
        mPresenter.setWorkout();
        mPresenter.getKcal();
        mPresenter.getDuration();
        mPresenter.getWeight();
        mPresenter.getHeight();
        mPresenter.getBMI();
        mPresenter.getChartData();
        mPresenter.getSyncData();
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRefreshView(String event) {
        if (Constant.EVENT_REFRESH_VIEW.equals(event)) {
            refreshView();
        }
        if (Constant.EVENT_UPDATE_REPORT_SYNC.equalsIgnoreCase(event)){
            mPresenter.getSyncData();
        }
    }


    private void setChartStyle() {
        // 显示数据描述
        chart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        chart.setNoDataText("暂无数据");
        //显示y轴右边的值
        chart.getAxisRight().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        // 设置x轴数据的位置
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // 设置x轴数据偏移量
        xAxis.setYOffset(DensityUtil.dip2px(requireActivity(), 3f));
        xAxis.setXOffset(DensityUtil.dip2px(requireActivity(), 1f));

        YAxis yAxis = chart.getAxisLeft();
        // 设置y轴数据的位置
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        // 设置y轴数据偏移量
        yAxis.setXOffset(DensityUtil.dip2px(requireActivity(), 3f));
        yAxis.setYOffset(DensityUtil.dip2px(requireActivity(), -1f));

        chart.invalidate();
    }

    @Override
    public void onSaveClick(double weight, int year, int month, int day) {
        Log.d(TAG, "onSaveClick: ");
        //保存体重记录
        mPresenter.saveWeightRecord(weight, year, month, day);
        //刷新图表
        mPresenter.getChartData();
        mPresenter.getWeight();
    }

    @Override
    public void setChartData(List<Entry> entryList, Date startDate, Date endDate) {
        if (entryList == null || startDate == null || endDate == null){
            return;
        }
        final List<String> dayList = DateUtil.getIntevalDayStrList(startDate, endDate);
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dayList.get((int) value);
            }
        });

        if (entryList.size() == 0) {
            LineData data = new LineData();
            chart.setData(data);
            chart.invalidate();
            return;
        }
        LineDataSet lineDataSet;

        lineDataSet = new LineDataSet(entryList, getString(R.string.lw_weight));
        // 设置曲线颜色
        lineDataSet.setColor(getResources().getColor(R.color.lw_orange_500));
        // 设置平滑曲线
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        // 坐标点的小圆点
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleColor(getResources().getColor(R.color.lw_orange_500));
        lineDataSet.setCircleSize(DensityUtil.dip2px(requireActivity(), 1.2f));
        // 不显示坐标点的数据
        lineDataSet.setDrawValues(false);
        // 不显示定位线
        lineDataSet.setHighlightEnabled(false);
        lineDataSet.setLineWidth(DensityUtil.dip2px(requireActivity(), 0.5f));

        LineData data = new LineData(lineDataSet);
        chart.setData(data);
        chart.invalidate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: REQUEST_OAUTH_REQUEST_CODE");
            //获取上一次的同步时间
            mPresenter.saveSyncState(true);
            //刷新界面
            EventBus.getDefault().post(Constant.EVENT_UPDATE_REPORT_SYNC);
        }
    }
    @Override
    public void setSync(String account, String time) {
        mTvAccount.setText(account);
        mTvSyncTime.setText(getString(R.string.last_sync) + ": " + time);
        mTvSyncTime.setTextColor(getResources().getColor(R.color.lw_green_16));
    }
}
