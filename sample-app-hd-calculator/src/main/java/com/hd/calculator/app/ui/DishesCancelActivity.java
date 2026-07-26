package com.hd.calculator.app.ui;

import android.app.DatePickerDialog;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.databinding.ActivityCancelRecordBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.ReduceDishesRecordEntity;
import com.hd.calculator.app.ui.adapter.DishesCancelAdapter;
import com.hd.calculator.app.ui.item.DishesCancelItem;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/***
 * 取消记录
 * 出单后，减少数量
 */
public class DishesCancelActivity extends BaseActivity<ActivityCancelRecordBinding> {
    private final List<DishesCancelItem> mDishesCancelItemList = new ArrayList<>();
    //orderCancelAdapter
    private DishesCancelAdapter mDishesCancelAdapter;
    private DatePickerDialog mDateSelectDialog;

    private long mSelectedDate;


    @Override
    protected ActivityCancelRecordBinding getViewBinding() {
        return ActivityCancelRecordBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.already_cancel_dishes));
        mSelectedDate = TimeUtils.getCurrentDay7amTimestamps()[0];
        initOrderCancelList();
        initDatePickerDialog();
        mBinding.dateContainer.setOnClickListener(v -> {
            mDateSelectDialog.show();
        });
    }

    @Override
    protected void initData() {
        getCancelRecordData();
    }


    private void initDatePickerDialog() {
        mDateSelectDialog = new DatePickerDialog(this);
        mDateSelectDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            mBinding.tvDateDesc.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            mSelectedDate = TimeUtils.get7amTimestamps(year, month + 1, dayOfMonth)[0];
            getCancelRecordData();
        });
    }

    private void initOrderCancelList() {
        //init rv
        mDishesCancelAdapter = new DishesCancelAdapter(mDishesCancelItemList);
        mBinding.rvCancelOrder.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvCancelOrder.setAdapter(mDishesCancelAdapter);
    }

    private void getCancelRecordData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            mDishesCancelItemList.clear();
            long[] times = TimeUtils.get7amTimestamps(mSelectedDate);
            List<ReduceDishesRecordEntity> reduceDishesRecordEntityList = DataBaseRepository.getInstance().getReduceDishesRecordByCreateTime(times[0], times[1]);
            for (ReduceDishesRecordEntity reduceDishesRecordEntity : reduceDishesRecordEntityList) {
                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(reduceDishesRecordEntity.getDishesCode());
                DishesCancelItem item = new DishesCancelItem();
                item.setDishesName(dishesEntity.getName());
                item.setCost(dishesEntity.getPrice() * reduceDishesRecordEntity.getCount());
                item.setTime(System.currentTimeMillis());
                item.setTableCode(reduceDishesRecordEntity.getTableCode());
                item.setWaiterId(reduceDishesRecordEntity.getUserId());
                AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(reduceDishesRecordEntity.getUserId());
                item.setWaiterName(accountEntity.getUserName());
                item.setOrderId(reduceDishesRecordEntity.getOrderId());
                item.setOrderType(reduceDishesRecordEntity.getOrderType());

                mDishesCancelItemList.add(item);
            }

            runOnUiThread(() -> {
                mDishesCancelAdapter.notifyDataSetChanged();
                mBinding.tvEmpty.setVisibility(mDishesCancelItemList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void initTestData() {
        //for
        for (int i = 1; i <= 2; i++) {
            DishesCancelItem item = new DishesCancelItem();
            item.setDishesName("Dishes" + i);
            item.setTableName("Table" + i);
            item.setCost(i * 10);
            item.setTime(System.currentTimeMillis());
            item.setTableCode(i);
            item.setWaiterId(i);
            item.setWaiterName("Waiter" + i);
            mDishesCancelItemList.add(item);
        }
        mDishesCancelAdapter.notifyDataSetChanged();
        mBinding.tvEmpty.setVisibility(mDishesCancelItemList.isEmpty() ? View.VISIBLE : View.GONE);

    }
}
