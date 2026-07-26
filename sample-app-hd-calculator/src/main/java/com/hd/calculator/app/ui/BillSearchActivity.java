package com.hd.calculator.app.ui;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.databinding.HdcActivityBillSearchBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.ui.adapter.BillManageAdapter;
import com.hd.calculator.app.ui.item.BillManageItem;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.StringUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/***
 * 搜索账单
 */
public class BillSearchActivity extends BaseActivity<HdcActivityBillSearchBinding> {
    //adapter
    private BillManageAdapter mBillManageAdapter;

    private final List<BillManageItem> mBillManageList = new ArrayList<>();

    @Override
    protected HdcActivityBillSearchBinding getViewBinding() {
        return HdcActivityBillSearchBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        mBinding.includeTopBar.tvTitle.setText(getString(R.string.hdc_search_order));
        initOrderList();
        mBinding.etTableCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String tableCode = s.toString();
                searchBill(tableCode);
            }
        });
    }

    @Override
    protected void initData() {
    }

    private void initOrderList() {
        //initRV
        mBinding.rvOrder.setLayoutManager(new LinearLayoutManager(this));
        mBillManageAdapter = new BillManageAdapter(mBillManageList);
        mBinding.rvOrder.setAdapter(mBillManageAdapter);
        mBillManageAdapter.setItemClickListener(item -> {
            item.setExpend(!item.isExpend());
            int position = mBillManageList.indexOf(item);
            mBillManageAdapter.notifyItemChanged(position, position);
        });
        mBillManageAdapter.setItemLongClickListener(item -> {
            ToastUtils.show(item.getTableCode() + "");
            return true;
        });
    }

    private void searchBill(String tableCode) {
        mBillManageList.clear();
        mBillManageAdapter.notifyDataSetChanged();

        if (tableCode.isEmpty()) {
            return;
        }
        if (!StringUtils.canConvertToInt(tableCode)) {
            return;
        }

        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<BillWithDishesRef> billList = DataBaseRepository.getInstance().getBillByTableCodeOrderByTimeDesc(Integer.parseInt(tableCode));
            for (BillWithDishesRef bill : billList) {
                BillRecordEntity billRecordEntity = bill.getBill();
                BillManageItem item = new BillManageItem();
                item.setTableCode(billRecordEntity.getTableCode());
                List<BillDishesRecordEntity> dishesList = bill.getDishesList();
                float total = 0;
                for (BillDishesRecordEntity billDishesRecordEntity : dishesList) {
                    DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(billDishesRecordEntity.getDishesCode());
                    total = total + dishesEntity.getPrice() * billDishesRecordEntity.getCount();
                    //dishesItem
                    DishesItem dishesItem = new DishesItem();
                    dishesItem.setCode(dishesEntity.getCode());
                    dishesItem.setName(dishesEntity.getName());
                    dishesItem.setPrice(dishesEntity.getPrice());
                    dishesItem.setFirstSortType( dishesEntity.getFirstSortType());
                    dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                    dishesItem.setCount(billDishesRecordEntity.getCount());
                    dishesItem.setRemark(billDishesRecordEntity.getRemark());
                    item.getDishesList().add(dishesItem);
                }
                item.setCost(billRecordEntity.getAmount());
                item.setPayTotal(billRecordEntity.getPayTotal());
                item.setFromDeleteTable(billRecordEntity.isFromDeleteTable());
                item.setCanceled(billRecordEntity.isCanceled());
                item.setPayType(billRecordEntity.getPayType());
                item.setBillCode(billRecordEntity.getBillCode());
                item.setOrderType(billRecordEntity.getOrderType());
                item.setWaiterId(billRecordEntity.getBillUserId());
                AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(billRecordEntity.getBillUserId());
                item.setWaiterName(accountEntity.getUserName());
                item.setOrderTime(billRecordEntity.getCreateTime());

                mBillManageList.add(item);
            }

            runOnUiThread(() -> {
                mBillManageAdapter.notifyDataSetChanged();
            });
        });
    }

    private void initTestData() {
        for (int i = 1; i <= 3; i++) {
            BillManageItem item = new BillManageItem();
            item.setTableCode(i);
            item.setCost(i * 10);
            item.setWaiterId(i);
            item.setWaiterName("Waiter" + i);
            item.setOrderTime(System.currentTimeMillis());
            item.setDishesList(new ArrayList<>());
            if (i == 1 || i == 2) {
                //deshItem
                DishesItem dishesItem = new DishesItem();
                dishesItem.setCode(i + "");
                dishesItem.setName("Dishes" + i);
                dishesItem.setPrice(i);
                dishesItem.setFirstSortType( DishesFirstSortType.FOOD);
                dishesItem.setEnablePrint(true);
                dishesItem.setCount(i);
                dishesItem.setRemark("Remark" + i);
                item.getDishesList().add(dishesItem);
                //add deshItem
                DishesItem dishesItem2 = new DishesItem();
                dishesItem2.setCode(i + "");
                dishesItem2.setName("Dishes" + i);
                dishesItem2.setPrice(i);
                dishesItem.setFirstSortType( DishesFirstSortType.FOOD);
                dishesItem.setEnablePrint(true);
                dishesItem2.setCount(i);
                item.getDishesList().add(dishesItem2);
            }
            mBillManageList.add(item);
        }

        //orderTYPE DATA
    }
}
