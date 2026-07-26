package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.ActivityOrderDetailBinding;
import com.hd.calculator.app.function.UserLog;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.function.db.entity.operation.ReduceDishesRecordEntity;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.function.printer.PrintOrderDishesRequireData;
import com.hd.calculator.app.function.printer.PrinterManager;
import com.hd.calculator.app.ui.adapter.OrderDetailDishesAdapter;
import com.hd.calculator.app.ui.dialog.CommonTipsDialog;
import com.hd.calculator.app.ui.dialog.UnOrderTipsDialog;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.EventUtils;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单详情-待结账
 */
public class OrderDetailActivity extends BaseActivity<ActivityOrderDetailBinding> {

    //list
    private final List<DishesItem> mDishesList = new ArrayList<>();
    //新增菜牌缓存到这里，ordered = false，且在列表之上
    private final List<DishesItem> mNewDishesList = new ArrayList<>();
    //adapter
    private OrderDetailDishesAdapter mAdapter;
    private long mOrderId;
    private int mOrderType;
    private int mTableCode;

    private float mOrderTotal;

    private int mSelectedModifyCountItemPosition = 0;

    public static void startActivity(Activity context, long orderId, int orderType, int tableCode) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        context.startActivity(intent);
    }

    @Override
    protected ActivityOrderDetailBinding getViewBinding() {
        return ActivityOrderDetailBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initExtraData();
        mBinding.tvTitle.setText(TableManager.getIns().getDisplayTableName(mTableCode, mOrderType));
        initDishes();
        initListener();
    }

    @Override
    protected void initData() {
        getOrderData(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePrintSwitchUI();
    }

    private void initExtraData() {
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        //orderType
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
    }

    private void initListener() {
        mBinding.ivBack.setOnClickListener(v -> {
            handleClickBack();
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleClickBack();
            }
        });
        mBinding.ivSplit.setOnClickListener(v -> {
            if (!mNewDishesList.isEmpty()) {
                ToastUtils.show("There are still dishes that have not been ordered");
                return;
            }
            ChooseTableActivity.start(OrderDetailActivity.this, mTableCode, mOrderId, mOrderType, ChooseTableActivity.FROM_SPLIT_ORDER);
        });
        mBinding.ivPrintTicket.setOnClickListener(v -> {
            boolean currentPrintSwitch = Config.getPrintSwitch();
            Config.setPrintSwitch(!currentPrintSwitch);
            updatePrintSwitchUI();
        });
        mBinding.cartContainer.setOnClickListener(v -> {
            PaymentActivity.startActivity(OrderDetailActivity.this, mOrderId, mOrderId, mOrderType, mTableCode, mOrderTotal);
        });
        mBinding.btnOrdering.setOnClickListener(v -> {
            ChooseDishesActivity.start(OrderDetailActivity.this, mTableCode, mOrderId, mOrderType, ChooseDishesActivity.FROM_APPEND, (ArrayList<DishesItem>) mNewDishesList);
        });
        mBinding.btnMakeOrder.setOnClickListener(v -> {
            handleMakeOrder(null);
        });
    }

    private void initDishes() {
        mBinding.rvDishes.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new OrderDetailDishesAdapter(mDishesList);
        mBinding.rvDishes.setAdapter(mAdapter);
        mAdapter.setItemClickListener(data -> {
            ToastUtils.show("Click：" + data.getName());
        });
        mAdapter.setOptionClickListener(new OrderDetailDishesAdapter.OptionClickListener() {
            @Override
            public void onClickCount(DishesItem data) {
                if (data.isCanceled()) {
                    return;
                }
                //start modify dishes count
                mSelectedModifyCountItemPosition = mDishesList.indexOf(data);
                ModifyDishesCountActivity.startActivity(OrderDetailActivity.this, data.getName(), data.getCount(), data.isOrdered());
            }

            @Override
            public void onClickAdd(DishesItem data) {
                if (data.isOrdered()) {
                    //已出单的菜品创建新的item
                    DishesItem newDishesItem = null;
                    boolean addItem = true;
                    //遍历：是否已经添加过新菜牌
                    for (DishesItem dishesItem : mNewDishesList) {
                        if (dishesItem.getCode().equals(data.getCode())) {
                            newDishesItem = dishesItem;
                            newDishesItem.setCount(newDishesItem.getCount() + 1);
                            addItem = false;
                            break;
                        }
                    }

                    //没有，copy
                    if (newDishesItem == null) {
                        newDishesItem = data.copy();
                    }

                    //判断备注是否一致
                    if (data.getRemark().isEmpty()) {
                        if (addItem) {
                            mDishesList.add(0, newDishesItem);
                        }
                        mAdapter.notifyDataSetChanged();
                    } else {
                        newDishesItem.setRemark(data.getRemark());
                        DishesItem finalNewDishesItem = newDishesItem;
                        boolean finalAddItem = addItem;
                        new CommonTipsDialog(OrderDetailActivity.this, "Neuen Artikelmit MEMO hinzufuegen?", new CommonTipsDialog.ClickListener() {
                            @Override
                            public void onClickOk(DialogInterface dialog) {
                                //因为copy时候已经设置了备注，所以这里不需要设置备注，相反，选择取消是删除备注
                                if (finalAddItem) {
                                    mDishesList.add(0, finalNewDishesItem);
                                }
                                mAdapter.notifyDataSetChanged();
                                updateBottomMenuState();
                                dialog.dismiss();
                            }

                            @Override
                            public void onClickCancel(DialogInterface dialog) {
                                finalNewDishesItem.setRemark("");
                                if (finalAddItem) {
                                    mDishesList.add(0, finalNewDishesItem);
                                }
                                mAdapter.notifyDataSetChanged();
                                updateBottomMenuState();
                                dialog.dismiss();
                            }
                        }).show();
                    }
                } else {
                    //直接修改数量
                    int count = data.getCount() + 1;
                    data.setCount(count);
                    int position = mDishesList.indexOf(data);
                    mAdapter.notifyItemChanged(position, position);
                }
                updateBottomMenuState();
            }

            @Override
            public void onClickDel(DishesItem data) {
                //只有新增的菜品才能点击减少数量哦
                int count = data.getCount() - 1;
                int position = mNewDishesList.indexOf(data);
                if (count <= 0) {
                    //删除这个菜牌
                    mDishesList.remove(data);
                    mAdapter.notifyDataSetChanged();
                } else {
                    data.setCount(count);
                    mAdapter.notifyItemChanged(position, position);
                }

                updateBottomMenuState();
            }
        });
    }

    private void handleClickBack() {
        if (mNewDishesList.isEmpty()) {
            finish();
        } else {
            UnOrderTipsDialog dialog = new UnOrderTipsDialog(this);
            dialog.setOptionClickListener(new UnOrderTipsDialog.OptionClickListener() {

                @Override
                public void onClickMakerOrderAndPrint() {
                    handleMakeOrder(true);
//                    finish();
                }

                @Override
                public void onClickMakerOrderAndUnPrint() {
                    handleMakeOrder(false);
//                    finish();
                }

                @Override
                public void onClickDrop() {
                    finish();
                }
            });
            dialog.show();
        }
    }

    private void handleMakeOrder(Boolean forcePrint) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<PrintOrderDishesRequireData> printItemList = new ArrayList<>();
            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
            List<OrderDishesRecordEntity> dishesList = orderWithDishesRef.getDishesList();
            Map<String, OrderDishesRecordEntity> dishesMap = new HashMap<>();
            for (OrderDishesRecordEntity dishesRecord : dishesList) {
                dishesMap.put(dishesRecord.getDishesCode(), dishesRecord);
            }
            mNewDishesList.forEach(dishesItem -> {
                //判断添加还是合并
                OrderDishesRecordEntity orderDishesRecordEntity = dishesMap.get(dishesItem.getCode());
                if (orderDishesRecordEntity == null) {
                    orderDishesRecordEntity = new OrderDishesRecordEntity();
                    orderDishesRecordEntity.setOrderId(mOrderId);
                    orderDishesRecordEntity.setDishesCode(dishesItem.getCode());
                    orderDishesRecordEntity.setCount(dishesItem.getCount());
                    orderDishesRecordEntity.setRemark(dishesItem.getRemark());
                    DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
                } else {
                    int newCount = orderDishesRecordEntity.getCount() + dishesItem.getCount();
                    orderDishesRecordEntity.setCount(newCount);
                    DataBaseRepository.getInstance().updateOrderDishes(orderDishesRecordEntity);
                }

                if (dishesItem.isEnablePrint()) {
                    PrintOrderDishesRequireData printItem = new PrintOrderDishesRequireData();
                    printItem.setCount(dishesItem.getCount());
                    printItem.setDishedCode(dishesItem.getCode());
                    printItem.setName(dishesItem.getName());
                    printItem.setRemark(dishesItem.getRemark());
                    printItemList.add(printItem);
                } else if (dishesItem.isCanceled()) {
                    LogUtils.log("不打印：" + dishesItem.getName() + " " + dishesItem.getCode());
                }

            });

            TableManager.getIns().postOrderRecord(mTableCode, ActionType.MAKE_ORDER, false, null);

            mNewDishesList.clear();

            finish();

            if (forcePrint == null) {
                if (Config.getPrintSwitch()) {
                    PrinterManager.getInstance().printOrder(mOrderId, printItemList);
                }
            } else {
                if (forcePrint) {
                    PrinterManager.getInstance().printOrder(mOrderId, printItemList);
                }
            }


//            runOnUiThread(() -> getOrderData(null));
        });
    }

    private void getOrderData(Runnable finish) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
            if (orderWithDishesRef == null) {
                //找不到就是全部付款，后删除订单找不到了
                finish();
                return;
            }
            mDishesList.clear();
            mOrderTotal = 0;
            for (OrderDishesRecordEntity orderDishesRecordEntity : orderWithDishesRef.getDishesList()) {
                DishesItem dishesItem = new DishesItem();
                dishesItem.setCode(orderDishesRecordEntity.getDishesCode());
                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
                dishesItem.setId(orderDishesRecordEntity.getId());
                dishesItem.setName(dishesEntity.getName());
                dishesItem.setPrice(dishesEntity.getPrice());
                dishesItem.setFirstSortType( dishesEntity.getFirstSortType());
                dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                dishesItem.setCount(orderDishesRecordEntity.getCount());
                dishesItem.setOrdered(true);//已出单
                dishesItem.setRemark(orderDishesRecordEntity.getRemark());
                mOrderTotal += dishesEntity.getPrice() * orderDishesRecordEntity.getCount();
                mDishesList.add(dishesItem);
                LogUtils.log("orderDishesRecordEntity = " + GsonUtils.toJson(orderDishesRecordEntity));
            }
            runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                updateBottomMenuState();
                if (finish != null) {
                    finish.run();
                }
            });
        });
    }

    private void updateBottomMenuState() {
        mNewDishesList.clear();
        mOrderTotal = 0;
        for (DishesItem dishesItem : mDishesList) {
            if (!dishesItem.isOrdered()) {
                mNewDishesList.add(dishesItem);
            }
            if (dishesItem.isCanceled()) {
                continue;
            }
            mOrderTotal += dishesItem.getPrice() * dishesItem.getCount();
        }

        mBinding.tvCost.setText(MoneyUtils.formatMoney(mOrderTotal));

        if (mNewDishesList.isEmpty()) {
            mBinding.btnMakeOrder.setVisibility(View.GONE);
        } else {
            mBinding.btnMakeOrder.setVisibility(View.VISIBLE);
        }
    }

    private void updatePrintSwitchUI() {
        if (Config.getPrintSwitch()) {
            mBinding.ivPrintTicket.setImageResource(R.drawable.ic_choose_dishes_menu_print_ticket_enable);
        } else {
            mBinding.ivPrintTicket.setImageResource(R.drawable.ic_choose_dishes_menu_print_ticket_disable);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.log("requestCode = " + requestCode);
        if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_PAYMENT) {
            getOrderData(null);
        } else if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_SPLIT_TABLE) {
            getOrderData(null);
        } else if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_CHOOSE_TABLE) {
            //选桌号后回调
            if (data != null) {
                int targetTableCode = data.getIntExtra(ExtraKey.RESULT_TABLE_CODE, 0);
                SplitTableActivity.start(OrderDetailActivity.this, mTableCode, targetTableCode, mOrderId, mOrderType);
            }
        } else if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_ADD_DISHES) {
            finish();

//            getOrderData(new Runnable() {
//                @Override
//                public void run() {
//                    if (data != null) {
//                        List<DishesItem> dishesItemList = data.getParcelableArrayListExtra(ExtraKey.DISHES_ITEM_LIST);
//                        if (dishesItemList != null && !dishesItemList.isEmpty()) {
//                            int count = dishesItemList.size();
//                            for (int i = count - 1; i >= 0; i--) {
//                                mDishesList.add(0, dishesItemList.get(i));
//                            }
//                        }
//                        updateBottomMenuState();
//                    }
//                }
//            });
        } else if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_MODIFY_DISHES_COUNT && data != null) {
            int newCount = data.getIntExtra(ExtraKey.RESULT_DISHES_COUNT, 1);
            DishesItem dishesItem = mDishesList.get(mSelectedModifyCountItemPosition);
            int oldCount = dishesItem.getCount();
            if (newCount == oldCount) {
                return;
            }

            if (newCount > oldCount) {
                //增加
                if (dishesItem.isOrdered()) {
                    //已出单的 增加 -> 相当于新增菜牌
                    DishesItem targetItem = null;
                    for (DishesItem newDishesItem : mNewDishesList) {
                        if (newDishesItem.getCode().equals(dishesItem.getCode())) {
                            targetItem = newDishesItem;
                            break;
                        }
                    }
                    if (targetItem == null) {
                        targetItem = dishesItem.copy();
                        targetItem.setCount(newCount - oldCount);
                        mDishesList.add(0, targetItem);
                    } else {
                        targetItem.setCount(newCount - oldCount);
                    }
                } else {
                    //未出单 增加 -> 直接修改count值
                    dishesItem.setCount(newCount);
                }
            } else {
                //减少
                if (dishesItem.isOrdered()) {
                    //已出单减少 ->
                    if (newCount == 0) {
                        //将原item标记为删除, 红色标记
                        dishesItem.setCanceled(true);
                        //新增一条取消记录，删除订单菜牌这条记录，根据id
                        ThreadUtils.runOnIoThreadDelayed(() -> {
                            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
                            ReduceDishesRecordEntity reduceDishesRecordEntity = new ReduceDishesRecordEntity();
                            reduceDishesRecordEntity.setDishesCode(dishesItem.getCode());
                            reduceDishesRecordEntity.setCount(dishesItem.getCount());
                            reduceDishesRecordEntity.setCreateTime(System.currentTimeMillis());
                            reduceDishesRecordEntity.setTableCode(mTableCode);
                            reduceDishesRecordEntity.setOrderId(mOrderId);
                            reduceDishesRecordEntity.setOrderType(mOrderType);
                            reduceDishesRecordEntity.setUserId(AccountManager.getIns().getAccount().getUserId());

                            //新增一条取消记录，
                            DataBaseRepository.getInstance().addReduceDishesRecord(reduceDishesRecordEntity);
                            // 添加删除订单菜品日志
                            EventUtils.logDeleteOrderEvent(mOrderId, "菜品数量减少至0删除菜品");
                            //删除订单菜牌这条记录，根据id
                            DataBaseRepository.getInstance().deleteOrderDishesById(dishesItem.getId());

                            orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
                            if (orderWithDishesRef.getDishesList().isEmpty()) {
                                // 添加删除订单日志
                                EventUtils.logDeleteOrderEvent(mOrderId, "所有菜品清空后删除订单");
                                //删除订单
                                DataBaseRepository.getInstance().deleteOrderByOrderId(mOrderId);
                                finish();
                            }
                            TableManager.getIns().postOrderRecord(mTableCode, ActionType.REDUCE_DISHES, false, null);
                        });
                    } else {
                        //将原item的count修改成新值
                        dishesItem.setCount(newCount);
                        //新增一条 item ，count为减少的数量 newCount - oldCount, 并标记为删除，插入到position + 1位置
                        DishesItem newItem = dishesItem.copy();
                        newItem.setCount(oldCount - newCount);
                        newItem.setOrdered(true);
                        newItem.setCanceled(true);
                        mDishesList.add(mSelectedModifyCountItemPosition + 1, newItem);

                        //新增一条取消记录，修改订单菜牌这条记录的数量newCount，根据id
                        ThreadUtils.runOnIoThreadDelayed(() -> {
                            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
                            ReduceDishesRecordEntity reduceDishesRecordEntity = new ReduceDishesRecordEntity();
                            reduceDishesRecordEntity.setDishesCode(dishesItem.getCode());
                            reduceDishesRecordEntity.setCount(oldCount - newCount);
                            reduceDishesRecordEntity.setCreateTime(System.currentTimeMillis());
                            reduceDishesRecordEntity.setTableCode(mTableCode);
                            reduceDishesRecordEntity.setOrderId(mOrderId);
                            reduceDishesRecordEntity.setOrderType(mOrderType);
                            reduceDishesRecordEntity.setUserId(AccountManager.getIns().getAccount().getUserId());

                            //新增一条取消记录
                            DataBaseRepository.getInstance().addReduceDishesRecord(reduceDishesRecordEntity);
                            //修改订单菜牌这条记录的数量newCount，根据id
                            OrderDishesRecordEntity orderDishesRecordEntity = DataBaseRepository.getInstance().getOrderDishesById(dishesItem.getId());
                            orderDishesRecordEntity.setCount(newCount);
                            DataBaseRepository.getInstance().updateOrderDishes(orderDishesRecordEntity);

                            TableManager.getIns().postOrderRecord(mTableCode, ActionType.REDUCE_DISHES, false, null);
                        });
                    }
                } else {
                    //未出单减少 ->
                    if (newCount == 0) {
                        mDishesList.remove(dishesItem);
                    } else {
                        dishesItem.setCount(newCount);
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
            updateBottomMenuState();
        }
    }

    private void initTestData() {
        for (int i = 1; i <= 2; i++) {
            DishesItem item = new DishesItem();
            item.setCode(i + "");
            item.setName("Dishes" + i);
            item.setPrice(i * 10);
            item.setFirstSortType(DishesFirstSortType.FOOD);
            item.setEnablePrint(true);
            item.setCount(i);
            item.setRemark("remark " + i);
            mDishesList.add(item);
        }
        runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
        });
    }
}
