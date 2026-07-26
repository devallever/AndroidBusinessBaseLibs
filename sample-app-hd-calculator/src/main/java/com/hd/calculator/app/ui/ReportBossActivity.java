package com.hd.calculator.app.ui;

import android.app.DatePickerDialog;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.databinding.HdcActivityDailyReportBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.operation.BillDailyReportEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.ui.adapter.DailyReportAdapter;
import com.hd.calculator.app.ui.item.DailyReportItem;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 * Boss日报表或者有权限的员工查看日报表
 */
public class ReportBossActivity extends BaseActivity<HdcActivityDailyReportBinding> {

    //list
    private final List<DailyReportItem> mDailyReportList = new ArrayList<>();
    //adapter
    private DailyReportAdapter mAdapter;
    private DatePickerDialog dialog;

    @Override
    protected HdcActivityDailyReportBinding getViewBinding() {
        return HdcActivityDailyReportBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.hdc_daily_report));
        initReportList();
        initDatePickerDialog();
        mBinding.dateContainer.setOnClickListener(v -> {
            dialog.show();
        });
    }

    @Override
    protected void initData() {
        getDailyReport(0, 0, 0);
    }

    private void getDailyReport(int year, int month, int day){
        ThreadUtils.runOnIoThreadDelayed(() -> {
            mDailyReportList.clear();
            if (year == 0 && month == 0 && day == 0) {
                //getAll
                List<BillDailyReportEntity> billDailyReportEntityList = DataBaseRepository.getInstance().getAllBillDailyReport();
                for (BillDailyReportEntity billDailyReportEntity : billDailyReportEntityList) {
                    DailyReportItem item = new DailyReportItem();
                    item.setTitle("D" + billDailyReportEntity.getCode());
                    item.setCost(billDailyReportEntity.getAmount());
                    item.setTime(billDailyReportEntity.getLastBillTime());
                    mDailyReportList.add(item);
                }
            } else {
                //getDailyReport
                long[] timestamps =TimeUtils.get7amTimestamps(year, month, day);
                String time = TimeUtils.formatTimestampToYYYYDDMM(timestamps[0]);
                BillDailyReportEntity billDailyReportEntityList = DataBaseRepository.getInstance().getBillDailyReportByTime(time);
                if (billDailyReportEntityList != null) {
                    DailyReportItem item = new DailyReportItem();
                    item.setTitle("D" + billDailyReportEntityList.getCode());
                    item.setCost(billDailyReportEntityList.getAmount());
                    item.setTime(billDailyReportEntityList.getLastBillTime());
                    mDailyReportList.add(item);
                }
            }

            ThreadUtils.runSingleThread(() -> {
                mAdapter.notifyDataSetChanged();
            });
        });

    }

    private void initReportList() {
        //init rv
        mAdapter = new DailyReportAdapter(mDailyReportList);
        mBinding.rvReport.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvReport.setAdapter(mAdapter);
    }

    private void initDatePickerDialog() {
        dialog = new DatePickerDialog(this);
        dialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            mBinding.tvDateDesc.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            getDailyReport(year, month + 1, dayOfMonth);
        });
    }

    private void initTestData(int year, int month, int day) {

        ThreadUtils.runOnIoThreadDelayed(() -> {
            if (year == 0 && month == 0 && day == 0) {

            }
            BillWithDishesRef first = DataBaseRepository.getInstance().getFirstBill();
            if (first == null) {
                return;
            }

        });

//        //for
//        for (int i = 1; i <= 2; i++) {
//            DailyReportItem item = new DailyReportItem();
//            item.setId(i);
//            item.setTitle("D" + i);
//            item.setCost(i * 10);
//            item.setTime(System.currentTimeMillis());
//            mDailyReportList.add(item);
//        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 兼容Android低版本的日期遍历函数
     *
     * @param year 起始年份
     * @param month 起始月份 (1-12)
     * @param day 起始日 (1-31)
     */
    public void iterateDaysFromDate(int year, int month, int day) {
        // 获取今天的日期
        Calendar today = Calendar.getInstance();

        // 创建起始日期对象（注意：Calendar月份从0开始）
        Calendar startDate = new GregorianCalendar(year, month - 1, day);

        // 清除时间部分，只比较日期
        clearTime(startDate);
        clearTime(today);

        // 验证日期有效性
        if (isInvalidDate(year, month, day)) {
            System.err.println("错误: 提供的日期无效 - 年:" + year + " 月:" + month + " 日:" + day);
            return;
        }

        // 验证起始日期是否超过今天
        if (startDate.after(today)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            System.out.println("起始日期(" + sdf.format(startDate.getTime()) + ")不能晚于今天(" + sdf.format(today.getTime()) + ")");
            return;
        }

        // 设置日期格式化器
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        System.out.println("日期遍历:");

        // 计数器
        int dayCount = 0;

        // 使用临时变量避免修改原始日期
        Calendar current = (Calendar) startDate.clone();

        // 循环遍历每一天
        while (!current.after(today)) {
            System.out.println(dateFormat.format(current.getTime()));

            // 增加一天
            current.add(Calendar.DAY_OF_MONTH, 1);
            dayCount++;
        }

        System.out.println("== 共遍历了 " + dayCount + " 天 ==");
    }

    /**
     * 验证日期是否有效
     */
    private boolean isInvalidDate(int year, int month, int day) {
        // 基础验证
        if (year < 1900 || year > 2100 || month < 1 || month > 12 || day < 1 || day > 31) {
            return true;
        }

        // 使用GregorianCalendar的验证机制
        try {
            new GregorianCalendar(year, month - 1, day);
        } catch (Exception e) {
            return true;
        }

        return false;
    }

    /**
     * 清除时间部分，确保只比较日期
     */
    private void clearTime(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
