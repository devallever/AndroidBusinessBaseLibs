package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.business.TaxManager;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.TaxType;
import com.hd.calculator.app.databinding.HdcActivityPreivewTicketBinding;
import com.hd.calculator.app.ui.adapter.TicketDishesAdapter;
import com.hd.calculator.app.ui.adapter.TicketTaxAdapter;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.ui.item.TicketDishesItem;
import com.hd.calculator.app.ui.item.TicketTaxItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreviewTicketActivity extends BaseActivity<HdcActivityPreivewTicketBinding> {

    //adapter
    private TicketDishesAdapter mTicketDishesAdapter;
    private TicketTaxAdapter mTicketTaxAdapter;

    //listData
    private final List<TicketDishesItem> mTicketDishesList = new ArrayList<>();
    private final List<TicketTaxItem> mTicketTaxList = new ArrayList<>();

    private TicketTaxItem mTaxItemLabel = new TicketTaxItem();

    private long mOrderId;
    private int mTableCode;
    private int mOrderType;
    private List<DishesItem> mExtraDishesItemList;

    public static void startActivity(Activity activity, long orderId, int orderType, int tableCode, ArrayList<DishesItem> dishesItemArrayList) {
        Intent intent = new Intent(activity, PreviewTicketActivity.class);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.DISHES_ITEM_LIST, dishesItemArrayList);
        activity.startActivity(intent);
    }

    @Override
    protected HdcActivityPreivewTicketBinding getViewBinding() {
        return HdcActivityPreivewTicketBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        initExtraData();
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            mBinding.includeTopBar.tvTitle.setText("Rechnungansicht (Tisch " + mTableCode + ")");
        } else {
            mBinding.includeTopBar.tvTitle.setText("Rechnungansicht (T-A" + mTableCode + ")");
        }
        initList();
        showTicketData();
    }

    @Override
    protected void initData() {
        mTaxItemLabel.setColumn1("St.Satz");
        mTaxItemLabel.setColumn2("Brutto");
        mTaxItemLabel.setColumn3("Netto");
        mTaxItemLabel.setColumn4("USt.");
    }

    private void initList() {
        //initRv
        mBinding.rvDishes.setLayoutManager(new LinearLayoutManager(this));
        mTicketDishesAdapter = new TicketDishesAdapter(mTicketDishesList);
        mBinding.rvDishes.setAdapter(mTicketDishesAdapter);

        mBinding.rvTax.setLayoutManager(new LinearLayoutManager(this));
        mTicketTaxAdapter = new TicketTaxAdapter(mTicketTaxList);
        mBinding.rvTax.setAdapter(mTicketTaxAdapter);
    }
    private void initExtraData() {
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mExtraDishesItemList = getIntent().getParcelableArrayListExtra(ExtraKey.DISHES_ITEM_LIST);
        if (mExtraDishesItemList == null) {
            mExtraDishesItemList = new ArrayList<>();
        }
    }

    @SuppressLint("SetTextI18n")
    private void showTicketData() {
        if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            mBinding.tvTableName.setText("Tisch: " + mTableCode);
        } else {
            mBinding.tvTableName.setText(TableManager.getIns().getDisplayTableName(mTableCode, mOrderType));
        }

        mBinding.tvUserName.setText(AccountManager.getIns().getAccount().getUserName());

        mBinding.tvTime.setText(TimeUtils.formatTimestampToDDMMYYYYHHmm(System.currentTimeMillis()));

        Map<Integer, Float> taxTypeTotalMap = new LinkedHashMap<>();
        taxTypeTotalMap.put(TaxType.DRINK_IN_HOUSE_RATE, 0f);
        taxTypeTotalMap.put(TaxType.DRINK_TAKEOUT_RATE, 0f);
        taxTypeTotalMap.put(TaxType.FOOD_IN_HOUSE_RATE, 0f);
        taxTypeTotalMap.put(TaxType.FOOD_TAKEOUT_RATE, 0f);

        //DishesList
        mTicketDishesList.clear();
        float totalCost = 0;
        for (DishesItem dishesItem : mExtraDishesItemList) {
            totalCost += dishesItem.getPrice() * dishesItem.getCount();
            if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesItem.getFirstSortType() == DishesFirstSortType.DRINK) {
                taxTypeTotalMap.put(TaxType.DRINK_IN_HOUSE_RATE, taxTypeTotalMap.get(TaxType.DRINK_IN_HOUSE_RATE) + dishesItem.getPrice() * dishesItem.getCount());
            } else if (mOrderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesItem.getFirstSortType() == DishesFirstSortType.DRINK) {
                taxTypeTotalMap.put(TaxType.DRINK_TAKEOUT_RATE, taxTypeTotalMap.get(TaxType.DRINK_TAKEOUT_RATE) + dishesItem.getPrice() * dishesItem.getCount());
            } else if (mOrderType == OrderType.ORDER_TYPE_TAKE_OUT && dishesItem.getFirstSortType() == DishesFirstSortType.FOOD) {
                taxTypeTotalMap.put( TaxType.FOOD_TAKEOUT_RATE, taxTypeTotalMap.get(TaxType.FOOD_TAKEOUT_RATE) + dishesItem.getPrice() * dishesItem.getCount());
            } else if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE && dishesItem.getFirstSortType() == DishesFirstSortType.FOOD) {
                taxTypeTotalMap.put( TaxType.FOOD_IN_HOUSE_RATE, taxTypeTotalMap.get(TaxType.FOOD_IN_HOUSE_RATE) + dishesItem.getPrice() * dishesItem.getCount());
            }
            if (!dishesItem.isEnablePrint()) {
                continue;
            }
            TicketDishesItem ticketDishesItem = new TicketDishesItem();
            ticketDishesItem.setName(dishesItem.getName() + "("+ MoneyUtils.formatMoney(dishesItem.getPrice()) +")");
            ticketDishesItem.setCount(dishesItem.getCount());
            ticketDishesItem.setCost(dishesItem.getPrice() * dishesItem.getCount());
            ticketDishesItem.setTaxSign(TaxManager.getIns().getTaxSign(mOrderType, dishesItem.getFirstSortType()));
            mTicketDishesList.add(ticketDishesItem);
        }

        if (mTicketDishesList.isEmpty()) {
            mBinding.dishesBottomLine.setVisibility(View.GONE);
        } else {
            mBinding.dishesBottomLine.setVisibility(View.VISIBLE);
        }
        mTicketDishesAdapter.notifyDataSetChanged();

        //SUMME
        mBinding.tvCost.setText(MoneyUtils.formatMoney(totalCost));

        //
        mTicketTaxList.clear();
        mTicketTaxList.add(mTaxItemLabel);
        taxTypeTotalMap.forEach((taxType, cost) -> {
            TicketTaxItem ticketTaxItem = new TicketTaxItem();
            ticketTaxItem.setColumn1(TaxManager.getIns().getSingRate(taxType));

            cost = (float) (Math.round(cost * 100)) / 100;
            ticketTaxItem.setColumn2(formatMoney(cost));

            float column3Value = cost / (1+TaxManager.getIns().getTaxRate(taxType));
            column3Value = (float) (Math.round(column3Value * 100)) / 100;
            ticketTaxItem.setColumn3(formatMoney(column3Value));

            float column4Value = cost - column3Value;
            //float保留两位小数
            column4Value = (float) (Math.round(column4Value * 100)) / 100;
            ticketTaxItem.setColumn4(formatMoney(column4Value));
            if (cost > 0) {
                mTicketTaxList.add(ticketTaxItem);
            }
        });

        mTicketTaxAdapter.notifyDataSetChanged();

    }

    @SuppressLint("DefaultLocale")
    private String formatMoney(float value) {
        return String.format("%.2f", value).replace(".", ",");
    }

    private void initTestData() {
        //for
        mTicketDishesList.clear();
        for (int i = 1; i <= 2; i++) {
            TicketDishesItem item = new TicketDishesItem();
            item.setName("Dishes" + i);
            item.setCost(i * 10);
            item.setCount(i);
            item.setTaxSign("A");
            mTicketDishesList.add(item);
        }

        mTicketTaxList.clear();
        mTicketTaxList.add(mTaxItemLabel);
        for (int i = 1; i <= 2; i++) {
            TicketTaxItem item = new TicketTaxItem();
            item.setColumn1("A" + i + "%");
            item.setColumn2("10");
            item.setColumn3("10");
            item.setColumn4("10");
            mTicketTaxList.add(item);
        }

        runOnUiThread(() -> {
            mTicketTaxAdapter.notifyDataSetChanged();
            mTicketDishesAdapter.notifyDataSetChanged();
        });
    }


}
