package com.hd.calculator.app.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.base.BasePopWindow;
import com.hd.calculator.app.constant.OrderSortType;
import com.hd.calculator.app.databinding.HdcPopOrderSortBinding;
import com.hd.calculator.app.function.db.OrderBy;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.ui.adapter.OrderSortAdapter;
import com.hd.calculator.app.ui.item.BillSortItem;

import java.util.ArrayList;
import java.util.List;

public class BillSortPopWindow extends BasePopWindow<HdcPopOrderSortBinding> {

    //adapter
    private OrderSortAdapter mOrderSortAdapter;
    //list
    private List<BillSortItem> mBillSortItemList;

    private ItemChangeListener<BillSortItem> mItemChangeListener;

    private String mSelectedOrderCondition;

    public BillSortPopWindow(Context context) {
        super(context);
    }


    public void setItemChangeListener(ItemChangeListener<BillSortItem> itemChangeListener) {
        mItemChangeListener = itemChangeListener;
    }

    @Override
    public HdcPopOrderSortBinding intBinding() {
        return HdcPopOrderSortBinding.inflate(LayoutInflater.from(context));
    }

    @Override
    public void initView() {
        initOrderSortList();
        initListData();
    }

    @Override
    public int getWidth() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    @Override
    public int getHeight() {
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private void initOrderSortList() {
        mBillSortItemList = new ArrayList<>();
        //init rv
        mOrderSortAdapter = new OrderSortAdapter(mBillSortItemList);
        mBinding.rvSort.setLayoutManager(new LinearLayoutManager(MyApp.context));
        mBinding.rvSort.setAdapter(mOrderSortAdapter);
        mOrderSortAdapter.setItemClickListener(data -> {
            //single select
            for (BillSortItem billSortItem : mBillSortItemList) {
                billSortItem.setSelect(billSortItem.getSortType() == data.getSortType());
                if (billSortItem.isSelect()) {
                    if (mItemChangeListener != null) {
                        mItemChangeListener.itemChange(billSortItem);
                    }
                    mSelectedOrderCondition = billSortItem.getOrderCondition();
                }
            }
            mOrderSortAdapter.notifyDataSetChanged();
            dismiss();
        });
    }

    private void initListData() {
        //init list data
        mBillSortItemList.add(new BillSortItem(OrderSortType.ORDER_SORT_TYPE_TIME, "Zeit der RN-Stellung", true, BillRecordEntity.COLUMN_CREATE_TIME + OrderBy.DESC));
        mBillSortItemList.add(new BillSortItem(OrderSortType.ORDER_SORT_TYPE_TABLE_CODE, "Tisch Nummer", false, BillRecordEntity.COLUMN_TABLE_CODE + OrderBy.ASC));
        mBillSortItemList.add(new BillSortItem(OrderSortType.ORDER_SORT_TYPE_BILL_CODE, "Rechnungsnummer", false, BillRecordEntity.COLUMN_BILL_CODE + OrderBy.ASC));
        mBillSortItemList.add(new BillSortItem(OrderSortType.ORDER_SORT_TYPE_AMOUNT, "Rechnungsbetrag", false, BillRecordEntity.COLUMN_AMOUNT + OrderBy.ASC));
        mOrderSortAdapter.notifyDataSetChanged();
        mSelectedOrderCondition = mBillSortItemList.get(0).getOrderCondition();
    }

    public String getCurrentSortCondition() {
        return mSelectedOrderCondition;
    }

    public interface ItemChangeListener<T> {
        void itemChange(T item);
    }
}
