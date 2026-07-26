package com.hd.calculator.app.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.base.BasePopWindow;
import com.hd.calculator.app.databinding.PopOrderFilterBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.ui.adapter.OrderFilterAdapter;
import com.hd.calculator.app.ui.item.BillFilterItem;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

public class BillFilterPopWindow extends BasePopWindow<PopOrderFilterBinding> {

    //adapter
    private OrderFilterAdapter mBillFilterAdapter;
    //list
    private List<BillFilterItem> mBillFilterItemList;

    private ItemChangeListener<BillFilterItem> mItemChangeListener;

    private long mSelectUserId;

    public BillFilterPopWindow(Context context) {
        super(context);
    }


    public void setItemChangeListener(ItemChangeListener<BillFilterItem> itemChangeListener) {
        mItemChangeListener = itemChangeListener;
    }

    @Override
    public PopOrderFilterBinding intBinding() {
        return PopOrderFilterBinding.inflate(LayoutInflater.from(context));
    }

    @Override
    public void initView() {
        initUserList();
        getUserListData();
    }

    @Override
    public int getWidth() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public int getHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void initUserList() {
        mBillFilterItemList = new ArrayList<>();
        //init rv
        mBillFilterAdapter = new OrderFilterAdapter(mBillFilterItemList);
        mBinding.rvSort.setLayoutManager(new LinearLayoutManager(MyApp.context));
        mBinding.rvSort.setAdapter(mBillFilterAdapter);
        mBillFilterAdapter.setItemClickListener(data -> {
            //single select
            for (BillFilterItem orderSortItem : mBillFilterItemList) {
                orderSortItem.setSelect(orderSortItem.getUserId() == data.getUserId());
                if (orderSortItem.isSelect()) {
                    if (mItemChangeListener != null) {
                        mItemChangeListener.itemChange(orderSortItem);
                    }
                }
            }
            mBillFilterAdapter.notifyDataSetChanged();
            dismiss();
        });
    }

    private void getUserListData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<AccountEntity> accountList = DataBaseRepository.getInstance().getAccountList();
            for (AccountEntity accountEntity : accountList) {
                BillFilterItem billFilterItem = new BillFilterItem(accountEntity.getUserId(), accountEntity.getUserName(), false);
                mBillFilterItemList.add(billFilterItem);
            }
            mBillFilterItemList.add(0, new BillFilterItem(0, "Alle", true));
            mSelectUserId = accountList.get(0).getUserId();
            ThreadUtils.runOnUiThread(() -> {
                mBillFilterAdapter.notifyDataSetChanged();
            });
        });
    }

    private void initTestData() {
        //init list data

        mBillFilterItemList.add(new BillFilterItem(1, "Boss-1", false));
        //for
        for (int i = 2; i < 6; i++) {
            mBillFilterItemList.add(new BillFilterItem(i, "Kellner-" + i, false));
        }
        //notify
        mBillFilterAdapter.notifyDataSetChanged();
    }

    public long getCurrentUserId() {
        return mSelectUserId;
    }

    public interface ItemChangeListener<T> {
        void itemChange(T item);
    }
}
