package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.databinding.HdcActivitySplitPaymentBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.ui.adapter.SplitPaymentDishesAdapter;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拆分付款
 */
public class SplitPaymentActivity extends BaseActivity<HdcActivitySplitPaymentBinding> {
    //curret list
    private final List<DishesItem> mFromList = new ArrayList<>();
    //target list
    private final List<DishesItem> mTargetList = new ArrayList<>();
    //current split adapter
    private SplitPaymentDishesAdapter mFromSplitDishesAdapter;
    //target split adapter
    private SplitPaymentDishesAdapter mTargetSplitDishesAdapter;
    private long mOrderId;
    private long mOriginOrderId;
    private int mOrderType;
    private int mTableCode;

    public static void start(Activity context, long originOrderId,  long orderId, int orderType, int tableCode) {
        Intent intent = new Intent(context, SplitPaymentActivity.class);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORIGIN_ORDER_ID, originOrderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_SPLIT_PAY);
    }

    @Override
    protected HdcActivitySplitPaymentBinding getViewBinding() {
        return HdcActivitySplitPaymentBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        initExtraData();
        initDishes();
        mBinding.ivBack.setOnClickListener(v -> finish());
        if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            mBinding.tvPaymentCurrent.setText("Tisch " + mTableCode);
        } else {
            mBinding.tvPaymentCurrent.setText(TableManager.getIns().getDisplayTableName(mTableCode, OrderType.ORDER_TYPE_IN_HOUSE));
        }
        mBinding.btnCurrentAll.setOnClickListener(v -> {
            if (mTargetList.isEmpty()) {
                //全部移动不需要考虑合并
                mTargetList.addAll(mFromList);
                mFromList.clear();
            } else {
                handleTransformAll(mFromList, mTargetList);
            }

            mFromSplitDishesAdapter.notifyDataSetChanged();
            mTargetSplitDishesAdapter.notifyDataSetChanged();
            updateBottomMenuState();
        });
        mBinding.btnTargetAll.setOnClickListener(v -> {
            if (mFromList.isEmpty()) {
                mFromList.addAll(mTargetList);
                mTargetList.clear();
            } else {
                handleTransformAll(mTargetList, mFromList);
            }

            mFromSplitDishesAdapter.notifyDataSetChanged();
            mTargetSplitDishesAdapter.notifyDataSetChanged();
            updateBottomMenuState();
        });
        mBinding.ivConfirm.setOnClickListener(v -> {
            if (mTargetList.isEmpty()) {
            } else {
                handleTransformData();
            }
        });
    }

    @Override
    protected void initData() {
        getOrderDishesData();
    }

    private void initExtraData() {
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mOriginOrderId = getIntent().getLongExtra(ExtraKey.ORIGIN_ORDER_ID, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
    }

    private void initDishes() {
        mBinding.rvCurrentDishes.setLayoutManager(new LinearLayoutManager(this));
        mFromSplitDishesAdapter = new SplitPaymentDishesAdapter(mFromList);
        mBinding.rvCurrentDishes.setAdapter(mFromSplitDishesAdapter);
        mFromSplitDishesAdapter.setItemClickListener(item -> {
            handleRemoveDishes(item, mFromList, mTargetList);
        });

        //target
        mBinding.rvTargetDishes.setLayoutManager(new LinearLayoutManager(this));
        mTargetSplitDishesAdapter = new SplitPaymentDishesAdapter(mTargetList);
        mBinding.rvTargetDishes.setAdapter(mTargetSplitDishesAdapter);
        mTargetSplitDishesAdapter.setItemClickListener(item -> {
            handleRemoveDishes(item, mTargetList, mFromList);
        });
    }

    private void handleTransformData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //处理原订单
            Map<Long, DishesItem> fromId2DishesMap = new HashMap<>();
            for (DishesItem item : mFromList) {
                fromId2DishesMap.put(item.getId(), item);
            }
            OrderWithDishesRef fromOrderList = DataBaseRepository.getInstance().getOrderById(mOrderId);
            for (OrderDishesRecordEntity dishesRecord : fromOrderList.getDishesList()) {
                //update Count
                if (fromId2DishesMap.containsKey(dishesRecord.getId())) {
                    dishesRecord.setCount(fromId2DishesMap.get(dishesRecord.getId()).getCount());
                    //更新数量
                    DataBaseRepository.getInstance().updateOrderDishes(dishesRecord);
                } else {
                    //删除记录
                    DataBaseRepository.getInstance().deleteOrderDishesById(dishesRecord.getId());
                }
            }
            TableManager.getIns().postOrderRecord(mTableCode, ActionType.SPLIT_BILL, false, () -> {
                if (mFromList.isEmpty()) {
                    //删除订单
                    DataBaseRepository.getInstance().deleteOrderByOrderId(mOrderId);
                }

                //创建新订单

                //处理新桌订单
                OrderRecordEntity targetOrder = new OrderRecordEntity();
                targetOrder.setTableCode(mTableCode);
                targetOrder.setOrderUserId(AccountManager.getIns().getAccount().getUserId());
                targetOrder.setOrderType(mOrderType);
                targetOrder.setCreateTime(System.currentTimeMillis());
                DataBaseRepository.getInstance().addOrderRecord(targetOrder);
                targetOrder = DataBaseRepository.getInstance().getLastOrderRecord();
                float targetAmount = 0;
                for (DishesItem item : mTargetList) {
                    OrderDishesRecordEntity orderDishesRecordEntity = new OrderDishesRecordEntity();
                    DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(item.getCode());
                    orderDishesRecordEntity.setOrderId(targetOrder.getId());
                    orderDishesRecordEntity.setDishesCode(item.getCode());
                    orderDishesRecordEntity.setRemark(item.getRemark());
                    orderDishesRecordEntity.setCount(item.getCount());
                    targetAmount += dishesEntity.getPrice() * item.getCount();
                    DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
                }

                long finalOriginId = mOriginOrderId;
                if (mFromList.isEmpty()) {
                    finalOriginId = targetOrder.getId();
                }
                PaymentActivity.startActivity(SplitPaymentActivity.this, finalOriginId, targetOrder.getId(), targetOrder.getOrderType(), targetOrder.getTableCode(), targetAmount);
                setResult(RESULT_OK);
                finish();
            });


        });
    }

    private void handleTransformAll(List<DishesItem> from, List<DishesItem> target) {
        for (DishesItem item : from) {
            int currentCount = item.getCount();
            //判断是否已经添加
            DishesItem targetItem = null;
            for (DishesItem dishesItem : target) {
                if (dishesItem.getId() == item.getId()) {
                    targetItem = dishesItem;
                    break;
                }
            }
            if (targetItem == null) {
                //不存在，添加
                targetItem = item.copy();
                targetItem.setCount(currentCount);
                target.add(targetItem);
            } else {
                //已存在，添加数量
                targetItem.setCount(targetItem.getCount() + currentCount);
            }
        }

        from.clear();
        mTargetSplitDishesAdapter.notifyDataSetChanged();
        mFromSplitDishesAdapter.notifyDataSetChanged();
        updateBottomMenuState();
    }

    private void handleRemoveDishes(DishesItem item, List<DishesItem> from, List<DishesItem> target) {
        //点击一次菜牌数量
        //移动1个数量到对面
        //处理点击Item
        item.setCount(item.getCount() - 1);
        if (item.getCount() == 0) {
            //删除这个菜牌
            from.remove(item);
        }
        //处理对面Item
        //判断是否已经添加
        DishesItem targetItem = null;
        for (DishesItem dishesItem : target) {
            if (dishesItem.getId() == item.getId()) {
                targetItem = dishesItem;
                break;
            }
        }
        if (targetItem == null) {
            //不存在，添加
            targetItem = item.copy();
            target.add(targetItem);
        } else {
            //已存在，添加数量
            targetItem.setCount(targetItem.getCount() + 1);
        }

        mTargetSplitDishesAdapter.notifyDataSetChanged();
        mFromSplitDishesAdapter.notifyDataSetChanged();
        updateBottomMenuState();
    }

    private void getOrderDishesData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            mFromList.clear();
            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
            orderWithDishesRef.getDishesList().forEach(orderDishesRecordEntity -> {
                DishesItem dishesItem = new DishesItem();
                dishesItem.setId(orderDishesRecordEntity.getId());
                dishesItem.setCode(orderDishesRecordEntity.getDishesCode());
                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
                dishesItem.setName(dishesEntity.getName());
                dishesItem.setPrice(dishesEntity.getPrice());
                dishesItem.setFirstSortType( dishesEntity.getFirstSortType());
                dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                dishesItem.setCount(orderDishesRecordEntity.getCount());
                dishesItem.setOrdered(true);
                dishesItem.setRemark(orderDishesRecordEntity.getRemark());
                mFromList.add(dishesItem);
            });

            runOnUiThread(() -> {
                mFromSplitDishesAdapter.notifyDataSetChanged();
                updateBottomMenuState();
            });
        });
    }

    private void updateBottomMenuState() {
        if (mFromList.isEmpty()) {
            mBinding.btnCurrentAll.setVisibility(View.GONE);
        } else {
            mBinding.btnCurrentAll.setVisibility(View.VISIBLE);
        }

        if (mTargetList.isEmpty()) {
            mBinding.btnTargetAll.setVisibility(View.GONE);
        } else {
            mBinding.btnTargetAll.setVisibility(View.VISIBLE);
        }

        float fromTotal = 0;
        for (DishesItem item : mFromList) {
            fromTotal += item.getPrice() * item.getCount();
        }
        mBinding.tvPaymentCurrentCost.setText(MoneyUtils.formatMoney(fromTotal));
        float targetTotal = 0;
        for (DishesItem item : mTargetList) {
            targetTotal += item.getPrice() * item.getCount();
        }
        mBinding.tvPaymentTargetCost.setText(MoneyUtils.formatMoney(targetTotal));
    }

    private void initTestData() {
        //current data for
        //for
        for (int i = 0; i < 2; i++) {
            DishesItem item = new DishesItem();
            item.setCode(i + "");
            item.setName("Dishes" + i);
            item.setPrice(i * 10);
            item.setFirstSortType(DishesFirstSortType.FOOD);
            item.setEnablePrint(true);
            item.setCount(i);
            mFromList.add(item);
        }
        runOnUiThread(() -> {
            mFromSplitDishesAdapter.notifyDataSetChanged();
        });

        //target
        for (int i = 0; i < 2; i++) {
            DishesItem item = new DishesItem();
            item.setCode(i + "");
            item.setName("Dishes" + i);
            item.setPrice(i * 10);
            item.setFirstSortType(DishesFirstSortType.FOOD);
            item.setEnablePrint(true);
            item.setCount(i);
            mTargetList.add(item);
        }
        runOnUiThread(() -> {
            mTargetSplitDishesAdapter.notifyDataSetChanged();
        });
    }
}
