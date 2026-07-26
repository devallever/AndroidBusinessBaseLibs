package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.BuildConfig;
import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.BillCodeManager;
import com.hd.calculator.app.business.BuffetManager;
import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.business.TakeoutTableManager;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.ActivityMainBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.function.network.response.UseTableResponseData;
import com.hd.calculator.app.function.printer.PrinterManager;
import com.hd.calculator.app.function.sync.DataSyncManager;
import com.hd.calculator.app.ui.adapter.MainTableAdapter;
import com.hd.calculator.app.ui.adapter.MainTableUnpaidAdapter;
import com.hd.calculator.app.ui.dialog.CommonTipsDialog;
import com.hd.calculator.app.ui.dialog.DeleteDBDataDialog;
import com.hd.calculator.app.ui.dialog.DeleteTableOrderTipsDialog;
import com.hd.calculator.app.ui.dialog.TransformTableTipsDialog;
import com.hd.calculator.app.ui.item.MainTableItem;
import com.hd.calculator.app.ui.item.MainUnpaidTableItem;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.IntervalTimer;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * 主界面
 */
public class MainActivity extends BaseActivity<ActivityMainBinding> {
    private final List<MainTableItem> mTable1List = new ArrayList<>();
    private final List<MainTableItem> mTable2List = new ArrayList<>();
    private final List<MainUnpaidTableItem> mLokalUnPaidList = new ArrayList<>();
    private AccountEntity mAccount = AccountManager.getIns().getAccount();
    private final Set<Integer> mUsedTableCodeSet = new HashSet<>();
    private final Map<Integer, OrderWithDishesRef> mTableOrderMap = new HashMap<>();
    private MainTableAdapter mTable1Adapter;
    private MainTableAdapter mTable2Adapter;
    private MainTableUnpaidAdapter mTableUnPaidAdapter;
    private boolean mShowUnPaid = false;
    private List<OrderWithDishesRef> mAllOrderRecord = new ArrayList<>();

    private IntervalTimer mTimerTask;

    private IntervalTimer mCheckLocalUnloadTableDataTimerTask;

    private IntervalTimer mUpdateAccountTimerTask;

    private MainTableItem mSelectTableItem;
    private boolean mSelectTableItemFromInputBossPwd = false;

    private boolean mIsInitAllData = false;
    private boolean mIsInitTable1Data = false;
    private boolean mIsInitTable2Data = false;
    private boolean mIsInitUnpaidTableData = false;


    @Override
    protected ActivityMainBinding getViewBinding() {
        return ActivityMainBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mUpdateAccountTimerTask != null) {
            mUpdateAccountTimerTask.start(3, () -> {
                DataSyncManager.getInstance().fetchAccountData( () -> {
                    if (AccountManager.getIns().getAccount() != null) {
                        long userId = AccountManager.getIns().getAccount().getUserId();
                        AccountEntity accountEntity = DataBaseRepository.getInstance().getAccountByUserId(userId);
                        if (accountEntity != null) {
                            AccountManager.getIns().updateAccount(accountEntity);
                        }
                    }
                });
            });
        }
    }

    @Override
    protected void initView() {
        if (mAccount == null) {
            ToastUtils.show("Login first");
            finish();
            return;
        }
        mTimerTask = new IntervalTimer();
        mCheckLocalUnloadTableDataTimerTask = new IntervalTimer();
        mUpdateAccountTimerTask = new IntervalTimer();
        PrinterManager.getInstance().init(this);

        initMainAllTable();
        initUnpaidTable();
        initListener();

        updateCurrentTableZoom(1);
        updateTableState(mShowUnPaid);
        updateLocalMode(Config.isLocalMode());
        updateLoginUserUi();
    }

    @Override
    protected void initData() {
//        initAllData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initAllData();
        //unlockTable
        if (mSelectTableItem != null) {
            LogUtils.log("unlockTable" + mSelectTableItem.getTableCode() + ", mSelectTableItemFromInputBossPwd = " + mSelectTableItemFromInputBossPwd);
        } else {
            LogUtils.log("unlockTable " + "mSelectTableItem == null");
        }
        if (mSelectTableItem != null && !mSelectTableItemFromInputBossPwd) {
            TableManager.getIns().unLockTableUse(mSelectTableItem.getTableCode(), null);
            mSelectTableItem = null;
        }
        mTimerTask.start(2, () -> {
//            LogUtils.log("timer task ");
            TableManager.getIns().getUseTableList(data -> {
//                LogUtils.log("getUseTableList = " + GsonUtils.toJson(data));
                if (data.isEmpty()) {
                    return;
                }

                ThreadUtils.runOnIoThreadDelayed(() -> {
                    insertTableUseData(data);
//                    syncTableData(data);
                    LogUtils.log("needUpdateMainTableList = " + needUpdateMainTableList);
                    if (needUpdateMainTableList) {
                        initAllData();
                        needUpdateMainTableList = false;
                    }
                    Map<Integer, OrderWithDishesRef> usedTableOrderMap = new HashMap<>();
                    data.forEach((key, value) -> {
//                        if (value.getTableData().getOrder() != null) {
                            usedTableOrderMap.put(key, value.getTableData().getOrder());
//                        }
                    });
//                    replaceTableUseData(usedTableOrderMap, mTable1List);
//                    runOnUiThread(() -> mTable1Adapter.notifyDataSetChanged());
//                    replaceTableUseData(usedTableOrderMap, mTable2List);
//                    runOnUiThread(() -> mTable2Adapter.notifyDataSetChanged());
                });
            });
        });

        mCheckLocalUnloadTableDataTimerTask.start(10, () -> {
            if (TableManager.getIns().checkHasLocalUnloadTableData()) {
                runOnUiThread(() -> {
                    if (!mIgnoreUnloadLocalTableData) {
                        initUnLoadLocalTableDataDialog();
                        mUnLoadLocalTableDataDialog.setMessage("有未上传数据, 是否上传或舍弃\n" + TableManager.getIns().getLocalUnloadTableDataTableCode());
                        mUnLoadLocalTableDataDialog.show();
                    }
                });
            } else {
                LogUtils.log("没有未上传数据");
            }
        });
    }

    private CommonTipsDialog mUnLoadLocalTableDataDialog;
    private boolean mIgnoreUnloadLocalTableData = false;
    private void initUnLoadLocalTableDataDialog() {
        mIgnoreUnloadLocalTableData = false;
        if (mUnLoadLocalTableDataDialog != null) {
            return;
        }
        mUnLoadLocalTableDataDialog = new CommonTipsDialog(this, "有未上传数据, 是否上传或舍弃", new CommonTipsDialog.ClickListener() {
            @Override
            public void onClickOk(DialogInterface dialog) {
                //上传
                TableManager.getIns().uploadLocalUnloadTableData();
                dialog.dismiss();
            }

            @Override
            public void onClickCancel(DialogInterface dialog) {
                //舍弃
                TableManager.getIns().removeAllLocalUnloadTableData();
                dialog.dismiss();
            }

            @Override
            public void onClickClose(DialogInterface dialog) {
//                mUnLoadLocalTableDataDialog = null;
//                mIgnoreUnloadLocalTableData = true;
            }
        });
        mUnLoadLocalTableDataDialog.setCancelable(false);
        mUnLoadLocalTableDataDialog.setBtnText("上传", "舍弃");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTimerTask.stop();
        mCheckLocalUnloadTableDataTimerTask.stop();
    }

    private void initAllData() {
        if (mIsInitAllData) {
            return;
        }
        ThreadUtils.runSingleThread(() -> {
            mIsInitAllData = true;
            //AllRecordEntity
            mUsedTableCodeSet.clear();
            mTableOrderMap.clear();
            mAllOrderRecord = DataBaseRepository.getInstance().getAllInHouseOrderRecord();
            mAllOrderRecord.forEach(orderWithDishesRef -> {
//                LogUtils.log("main db order = " + GsonUtils.toJson(orderWithDishesRef));
                mUsedTableCodeSet.add(orderWithDishesRef.getOrder().getTableCode());
                mTableOrderMap.put(orderWithDishesRef.getOrder().getTableCode(), orderWithDishesRef);
            });

            runOnUiThread(() -> {
                initTable1Data();
                initTable2Data();
                initUnpaidTableData();
                mIsInitAllData = false;
            });

        });
    }

    private void initUnpaidTableData() {
        if (mIsInitUnpaidTableData) {
            return;
        }
        mIsInitUnpaidTableData = true;
        mLokalUnPaidList.clear();
        mTableUnPaidAdapter.notifyDataSetChanged();
        ThreadUtils.runOnIoThreadDelayed(() -> {
            DataBaseRepository.getInstance().getUnPaidOrder().forEach(orderWithDishesRef -> {
                MainUnpaidTableItem item = new MainUnpaidTableItem();
                item.setOrderId(orderWithDishesRef.getOrder().getId());
                item.setOrderType(orderWithDishesRef.getOrder().getOrderType());
                item.setTableCode(orderWithDishesRef.getOrder().getTableCode());
                item.setTime(orderWithDishesRef.getOrder().getCreateTime());
                AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(orderWithDishesRef.getOrder().getOrderUserId());
                item.setWaiter(accountEntity.getUserName());
                item.setCount(orderWithDishesRef.getDishesList().size());
                float cost = 0f;
                int adultCount = 0;
                int childCount = 0;
                int babyCount = 0;
                StringBuilder builder = new StringBuilder();
                for (OrderDishesRecordEntity orderDishesRecordEntity : orderWithDishesRef.getDishesList()) {
                    DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
                    cost += dishesEntity.getPrice() * orderDishesRecordEntity.getCount();
                    item.setCost(cost);
//                    builder.append(orderDishesRecordEntity.getCount()).append(dishesEntity.getBuffetType()).append("/");
                    item.setBuffetCountDisplay(dishesEntity.getBuffetType());
                    if (BuffetManager.getIns().isAdultDishes(orderDishesRecordEntity.getDishesCode())) {
                        adultCount += 1;
                    }
                    if (BuffetManager.getIns().isChildDishes(orderDishesRecordEntity.getDishesCode())) {
                        childCount += 1;
                    }
                    if (BuffetManager.getIns().isBabyDishes(orderDishesRecordEntity.getDishesCode())) {
                        babyCount += 1;
                    }
                }
                item.setAdultCount(adultCount);
                item.setChildCount(childCount);
                item.setBabyCount(babyCount);
//                builder.deleteCharAt(builder.length() - 1);

                mLokalUnPaidList.add(item);
            });

            runOnUiThread(() -> {
                mTableUnPaidAdapter.notifyDataSetChanged();
                mIsInitUnpaidTableData = false;
            });
        });
    }

    private void initTable2Data() {
        if (mIsInitTable2Data) {
            return;
        }
        mIsInitTable2Data = true;
        mTable2List.clear();
        mTable2Adapter.notifyDataSetChanged();
        ThreadUtils.runOnIoThreadDelayed(() -> {
            DataBaseRepository.getInstance().getTableByZone(2).forEach(tableEntity -> {
                MainTableItem item = new MainTableItem();
                item.setTableCode(tableEntity.getCode());
                if (mUsedTableCodeSet.contains(item.getTableCode())) {
                    //used
                    item.setUsed(true);
                    OrderRecordEntity order = mTableOrderMap.get(item.getTableCode()).getOrder();
                    item.setOrderId(order.getId());
                    item.setOrderType(order.getOrderType());
                    item.setUserId(order.getOrderUserId());
                    item.setTime(order.getCreateTime());
                    List<OrderDishesRecordEntity> orderDishesRecordEntities = mTableOrderMap.get(item.getTableCode()).getDishesList();
                    item.setDishesCount(orderDishesRecordEntities.size());
                    float cost = 0f;
                    int adultCount = 0;
                    int childCount = 0;
                    int babyCount = 0;
                    for (OrderDishesRecordEntity orderDishesRecordEntity : orderDishesRecordEntities) {
                        int count = orderDishesRecordEntity.getCount();
                        float price = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode()).getPrice();
                        cost += price * count;
                        if (BuffetManager.getIns().isAdultDishes(orderDishesRecordEntity.getDishesCode())) {
                            adultCount += 1;
                        }
                        if (BuffetManager.getIns().isChildDishes(orderDishesRecordEntity.getDishesCode())) {
                            childCount += 1;
                        }
                        if (BuffetManager.getIns().isBabyDishes(orderDishesRecordEntity.getDishesCode())) {
                            babyCount += 1;
                        }
                    }
                    item.setAdultCount(adultCount);
                    item.setChildCount(childCount);
                    item.setBabyCount(babyCount);
                    item.setCost(cost);
                } else {
                    item.setUsed(false);
                    item.setTime(System.currentTimeMillis());
                    item.setDishesCount(0);
                    item.setUserId(0);
                    item.setBuffetCount("0E/0K/0B");
                    item.setCost(0f);
                }
                mTable2List.add(item);
            });

            runOnUiThread(() -> {
                mTable2Adapter.notifyDataSetChanged();
                mIsInitTable2Data = false;
            });
        });
    }

    private void initTable1Data() {
        if (mIsInitTable1Data) {
            return;
        }
        mIsInitTable1Data = true;
        mTable1List.clear();
        mTable1Adapter.notifyDataSetChanged();
        ThreadUtils.runOnIoThreadDelayed(() -> {

            DataBaseRepository.getInstance().getTableByZone(1).forEach(tableEntity -> {
                MainTableItem item = new MainTableItem();
                item.setTableCode(tableEntity.getCode());
                if (mUsedTableCodeSet.contains(item.getTableCode())) {
                    //used
                    item.setUsed(true);
                    OrderRecordEntity order = mTableOrderMap.get(item.getTableCode()).getOrder();
                    item.setOrderId(order.getId());
                    item.setOrderType(order.getOrderType());
                    item.setUserId(order.getOrderUserId());
                    item.setTime(order.getCreateTime());
                    List<OrderDishesRecordEntity> orderDishesRecordEntities = mTableOrderMap.get(item.getTableCode()).getDishesList();
                    item.setDishesCount(orderDishesRecordEntities.size());
                    item.setBuffetCount("0E/0K/0B");
                    float cost = 0f;
                    int adultCount = 0;
                    int childCount = 0;
                    int babyCount = 0;
                    for (OrderDishesRecordEntity orderDishesRecordEntity : orderDishesRecordEntities) {
                        int count = orderDishesRecordEntity.getCount();
                        DishesEntity dishes = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
                        float price = 0;
                        if (dishes != null) {
                            price = dishes.getPrice();
                        }
                        cost += price * count;
                        if (BuffetManager.getIns().isAdultDishes(orderDishesRecordEntity.getDishesCode())) {
                            adultCount += 1;
                        }
                        if (BuffetManager.getIns().isChildDishes(orderDishesRecordEntity.getDishesCode())) {
                            childCount += 1;
                        }
                        if (BuffetManager.getIns().isBabyDishes(orderDishesRecordEntity.getDishesCode())) {
                            babyCount += 1;
                        }
                    }
                    item.setAdultCount(adultCount);
                    item.setChildCount(childCount);
                    item.setBabyCount(babyCount);
                    item.setCost(cost);
                } else {
                    item.setUsed(false);
                    item.setTime(System.currentTimeMillis());
                    item.setDishesCount(0);
                    item.setUserId(0);
                    item.setBuffetCount("0E/0K/0B");
                    item.setCost(0f);
                }
                mTable1List.add(item);
            });

            runOnUiThread(() -> {
                mTable1Adapter.notifyDataSetChanged();
                mIsInitTable1Data = false;
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().release();
        if (mUpdateAccountTimerTask != null) {
            mUpdateAccountTimerTask.stop();
        }
    }

    private void initListener() {
        mBinding.tvZoom1.setOnClickListener(v -> {
            updateCurrentTableZoom(1);
        });
        mBinding.tvZoom2.setOnClickListener(v -> {
            updateCurrentTableZoom(2);
        });
        mBinding.ivState.setOnClickListener(v -> {
            updateTableState(!mShowUnPaid);
        });
        mBinding.btnTakeout.setOnClickListener(v -> {
            //外带点餐
            int newTableCode = TakeoutTableManager.getIns().getAndIncrement();
            ChooseDishesActivity.start(MainActivity.this,
                    newTableCode,
                    0,
                    OrderType.ORDER_TYPE_TAKE_OUT,
                    ChooseDishesActivity.FROM_CREATE, null);
        });
        mBinding.btnDining.setOnClickListener(v -> {
            ChooseTableActivity.start(this, 0, 0, OrderType.ORDER_TYPE_IN_HOUSE, ChooseTableActivity.FROM_CREATE);
        });
        mBinding.ivMenu.setOnClickListener(v -> {
            mBinding.drawer.openDrawer(GravityCompat.START);
        });
        mTable1Adapter.setItemClickListener(item -> {
            handleTableItemClick(item);
        });
        mTable1Adapter.setItemLongClickListener(item -> {
            if (item.isUsed()) {
                showTransFormDialog(item);
                return true;
            }
            return false;
        });
        mTable2Adapter.setItemClickListener(item -> {
            handleTableItemClick(item);
        });
        mTable2Adapter.setItemLongClickListener(item -> {
            if (item.isUsed()) {
                showTransFormDialog(item);
                return true;
            }
            return false;
        });
        mBinding.ivPower.setOnClickListener(v -> {
            logout();
        });
        mBinding.menuLocalModeSwitch.setOnClickListener(v -> {
            if (!AccountManager.getIns().canLocalMode()) {
                ToastUtils.show("no permission");
                return;
            }
            updateLocalMode(!Config.isLocalMode());
        });
        mBinding.menuLogout.setOnClickListener(v -> {
            startSelectUserLoginActivity();
        });
        mBinding.menuMenuManage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, TableManageActivity.class));
        });
        mBinding.menuOrderManage.setOnClickListener(v -> {
            //start order manage activity
            startActivity(new Intent(this, BillManageActivity.class));
        });
        mBinding.menuCancelRecord.setOnClickListener(v -> {
            startActivity(new Intent(this, DishesCancelActivity.class));
        });
        mBinding.menuPrinter.setOnClickListener(v -> {
            ThreadUtils.runOnIoThreadDelayed(new Runnable() {
                @Override
                public void run() {
//                    PrinterManager.getInstance().printOrder();
                }
            });
        });
        mBinding.menuReport.setOnClickListener(v -> {
            if (AccountManager.getIns().canViewDailyBill()) {
                startActivity(new Intent(this, ReportBossActivity.class));
            } else {
                startActivity(new Intent(this, ReportWaiterActivity.class));
            }
        });
        mBinding.menuService.setOnClickListener(v -> {
            startActivity(new Intent(this, SystemInfoActivity.class));
        });
        mBinding.menuSetting.setOnClickListener(v -> {
            ModifyBossPwdActivity.start(this);
        });
        mBinding.menuDeleteData.setOnClickListener(v -> {
            //
            DeleteDBDataDialog deleteDBDataDialog = new DeleteDBDataDialog(this);
            deleteDBDataDialog.setOptionClickListener(new DeleteDBDataDialog.OptionClickListener() {
                @Override
                public void onClickDeleteTodayBefore() {
                    CommonTipsDialog dialog = new CommonTipsDialog(MainActivity.this, "确定要删除今天之前的所有数据吗？", new CommonTipsDialog.ClickListener() {
                        @Override
                        public void onClickOk(DialogInterface dialog) {
                            deleteTodayBeforeData(() -> {
                                ToastUtils.show("删除成功");
                                initAllData();
                                dialog.dismiss();
                            });
                        }

                        @Override
                        public void onClickCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }

                @Override
                public void onClickToday() {

                    CommonTipsDialog dialog = new CommonTipsDialog(MainActivity.this, "确定要删除今天所有数据吗？", new CommonTipsDialog.ClickListener() {
                        @Override
                        public void onClickOk(DialogInterface dialog) {
                            deleteTodayData(() -> {
                                ToastUtils.show("删除成功");
                                initAllData();
                                dialog.dismiss();
                            });
                        }

                        @Override
                        public void onClickCancel(DialogInterface dialog) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
            deleteDBDataDialog.show();

        });
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                if (mBinding.drawer.isDrawerOpen(GravityCompat.START)) {
//                    mBinding.drawer.closeDrawers();
//                } else {
//                    finish();
//                }
                startSelectUserLoginActivity();
            }
        });
        mBinding.menuBleMac.setOnClickListener(v -> {
            startActivity(new Intent(this, BleMacAddressSettingActivity.class));
        });
    }

    private void initMainAllTable() {
        mBinding.rvTable1.setLayoutManager(new GridLayoutManager(this, 3));
        mTable1Adapter = new MainTableAdapter(mTable1List);
        mBinding.rvTable1.setAdapter(mTable1Adapter);
        mBinding.rvTable2.setLayoutManager(new GridLayoutManager(this, 3));
        mTable2Adapter = new MainTableAdapter(mTable2List);
        mBinding.rvTable2.setAdapter(mTable2Adapter);
    }

    private void initUnpaidTable() {
        mBinding.rvTableUnpaid.setLayoutManager(new LinearLayoutManager(this));
        mTableUnPaidAdapter = new MainTableUnpaidAdapter(mLokalUnPaidList);
        mTableUnPaidAdapter.setItemClickListener(item -> {
            handleUnPaidItemClick(item);
        });
        mBinding.rvTableUnpaid.setAdapter(mTableUnPaidAdapter);
    }

    private boolean checkSameOrder(OrderRecordEntity dbOrder, UseTableResponseData useTableResponseData) {
        OrderWithDishesRef onlineOrderWithDishesRef = useTableResponseData.getTableData().getOrder();

        if (dbOrder == null && onlineOrderWithDishesRef == null) {
            return true;
        }

        if (dbOrder != null && onlineOrderWithDishesRef == null) {
            return false;
        }

        if (dbOrder == null && onlineOrderWithDishesRef != null) {
            return false;
        }


        OrderWithDishesRef dbOrderWithDishesRef = DataBaseRepository.getInstance().getOrderById(dbOrder.getId());
        int dbOrderDishesCount = dbOrderWithDishesRef.getDishesList().size();
        int onlineOrderDishesCount = onlineOrderWithDishesRef.getDishesList().size();
        if (dbOrderDishesCount != onlineOrderDishesCount) {
            return false;
        }

        Map<String, OrderDishesRecordEntity> onlineDishesMap = new HashMap<>();
        for (OrderDishesRecordEntity dishesRecord : onlineOrderWithDishesRef.getDishesList()) {
            onlineDishesMap.put(dishesRecord.getDishesCode(), dishesRecord);
        }

        for (OrderDishesRecordEntity dbDishes : dbOrderWithDishesRef.getDishesList()) {
            OrderDishesRecordEntity onlineDishes = onlineDishesMap.get(dbDishes.getDishesCode());
            if (onlineDishes == null) {
                return false;
            }
            if (dbDishes.getCount() != onlineDishes.getCount()) {
                return false;
            }

            if (!TextUtils.equals(dbDishes.getRemark(), onlineDishes.getRemark())) {
                return false;
            }
        }

        return true;

    }
    private boolean needUpdateMainTableList = false;
    private void insertTableUseData(Map<Integer, UseTableResponseData> data) {
        needUpdateMainTableList = false;
        data.forEach((tableCode, useTableResponseData) -> {
            OrderWithDishesRef onlineOrderEntity = useTableResponseData.getTableData().getOrder();
            OrderRecordEntity dbOrder = DataBaseRepository.getInstance().getInHouseOrderByTableCode(tableCode);

            //checkSame
            boolean same = checkSameOrder(dbOrder, useTableResponseData);
//            LogUtils.log( "tableCode: " + tableCode + " is same order = " + same);

            if (!same) {
                LogUtils.log( "tableCode: " + tableCode + " is same order = " + same);
                if (onlineOrderEntity == null) {
                    //占用，但是没数据
                    if (dbOrder != null) {
                        DataBaseRepository.getInstance().deleteOrderByOrderId(dbOrder.getId());
                        needUpdateMainTableList = true;
                    }
                } else {
                    //check can insert
                    if (dbOrder == null) {
                        //不存在，插入
                        dbOrder = onlineOrderEntity.getOrder();
                        dbOrder.setId(0);
                        dbOrder.setOrderUserId(useTableResponseData.getUserId());
                        DataBaseRepository.getInstance().addOrderRecord(dbOrder);
                        dbOrder = DataBaseRepository.getInstance().getLastOrderRecord();
                        long orderId = dbOrder.getId();
                        LogUtils.log("orderId = " + orderId);
                        onlineOrderEntity.getDishesList().forEach(orderDishesRecordEntity -> {
                            orderDishesRecordEntity.setOrderId(orderId);
                            orderDishesRecordEntity.setId(0);
                            DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
                        });
                        needUpdateMainTableList = true;
                    } else {
//                    if (dbOrder.getOrderUserId() == onlineOrderEntity.getOrder().getOrderUserId()) {
                        //同用户操作的 已存在，更新
                        List<OrderDishesRecordEntity> dbOrderDishesRecordEntities = DataBaseRepository.getInstance().getOrderDishesListByOrderId(dbOrder.getId());
                        for (OrderDishesRecordEntity dbOrderDishesRecordEntity : dbOrderDishesRecordEntities) {
                            // 添加删除订单菜品日志
                            DataBaseRepository.getInstance().deleteOrderDishesById(dbOrderDishesRecordEntity.getId());
                            needUpdateMainTableList = true;
                        }

                        for (OrderDishesRecordEntity dishesRecord : onlineOrderEntity.getDishesList()) {
                            dishesRecord.setOrderId(dbOrder.getId());
                            // 修复主键冲突问题：清除从网络获取的对象的id值，让数据库自动生成新的id
                            dishesRecord.setId(0);
                            DataBaseRepository.getInstance().addOrderDishesRecord(dishesRecord);
                            needUpdateMainTableList = true;
                        }
//                    }
                    }
                }
            }

            if (needUpdateMainTableList) {
//                runOnUiThread(this::initAllData);
            }
        });
    }

    private void replaceTableUseData(Map<Integer, OrderWithDishesRef> data, List<MainTableItem> mTableList) {
        for (MainTableItem item : mTableList) {
            if (data.containsKey(item.getTableCode())) {
                //总是显示线上
                //used
                if (data.get(item.getTableCode()) == null) {
                    item.setOnLine(false);
                    OrderRecordEntity  dbOrder = DataBaseRepository.getInstance().getInHouseOrderByTableCode(item.getTableCode());
                    if (dbOrder == null) {
                        item.setUsed(false);
                        item.setTime(System.currentTimeMillis());
                        item.setDishesCount(0);
                        item.setUserId(0);
                        item.setBuffetCount("0E/0K/0B");
                        item.setCost(0f);
                    } else {
                        item.setUsed(true);
                        item.setOrderId(dbOrder.getId());
                        item.setOrderType(dbOrder.getOrderType());
                        item.setUserId(dbOrder.getOrderUserId());
                        item.setTime(dbOrder.getCreateTime());
                        List<OrderDishesRecordEntity> orderDishesRecordEntities = DataBaseRepository.getInstance().getOrderDishesListByOrderId(dbOrder.getId());
                        item.setDishesCount(orderDishesRecordEntities.size());
                        item.setBuffetCount("0E/0K/0B");
                        float cost = 0f;
                        int adultCount = 0;
                        int childCount = 0;
                        int babyCount = 0;
                        for (OrderDishesRecordEntity orderDishesRecordEntity : orderDishesRecordEntities) {
                            int count = orderDishesRecordEntity.getCount();
                            float price = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode()).getPrice();
                            cost += price * count;
                            if (BuffetManager.getIns().isAdultDishes(orderDishesRecordEntity.getDishesCode())) {
                                adultCount += 1;
                            }
                            if (BuffetManager.getIns().isChildDishes(orderDishesRecordEntity.getDishesCode())) {
                                childCount += 1;
                            }
                            if (BuffetManager.getIns().isBabyDishes(orderDishesRecordEntity.getDishesCode())) {
                                babyCount += 1;
                            }
                        }
                        item.setAdultCount(adultCount);
                        item.setChildCount(childCount);
                        item.setBabyCount(babyCount);
                        item.setCost(cost);
                    }
                } else {
                    OrderRecordEntity order = data.get(item.getTableCode()).getOrder();
                    item.setUsed(true);
                    item.setOnLine(true);
                    item.setOrderId(order.getId());
                    item.setOrderType(order.getOrderType());
                    item.setUserId(order.getOrderUserId());
                    item.setTime(order.getCreateTime());
                    List<OrderDishesRecordEntity> orderDishesRecordEntities = data.get(item.getTableCode()).getDishesList();
                    item.setDishesCount(orderDishesRecordEntities.size());
                    float cost = 0f;
                    int adultCount = 0;
                    int childCount = 0;
                    int babyCount = 0;
                    for (OrderDishesRecordEntity orderDishesRecordEntity : orderDishesRecordEntities) {
                        int count = orderDishesRecordEntity.getCount();
                        float price = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode()).getPrice();
                        cost += price * count;
                        if (BuffetManager.getIns().isAdultDishes(orderDishesRecordEntity.getDishesCode())) {
                            adultCount += 1;
                        }
                        if (BuffetManager.getIns().isChildDishes(orderDishesRecordEntity.getDishesCode())) {
                            childCount += 1;
                        }
                        if (BuffetManager.getIns().isBabyDishes(orderDishesRecordEntity.getDishesCode())) {
                            babyCount += 1;
                        }
                    }
                    item.setAdultCount(adultCount);
                    item.setChildCount(childCount);
                    item.setBabyCount(babyCount);
                    item.setCost(cost);
                }

            }
        }
    }

    private void handleUnPaidItemClick(MainUnpaidTableItem item) {
        mSelectTableItem = new MainTableItem();
        mSelectTableItem.setOrderId(item.getOrderId());
        mSelectTableItem.setOrderType(item.getOrderType());
        mSelectTableItem.setTableCode(item.getTableCode());
        TableManager.getIns().checkTableCanUsed(item.getTableCode(), canUse -> {
            if (canUse) {
                TableManager.getIns().lockTableUse(item.getTableCode(), null);
                OrderDetailActivity.startActivity(MainActivity.this, item.getOrderId(), item.getOrderType(), item.getTableCode());
            } else {
                new CommonTipsDialog(MainActivity.this, "所选桌正在使用中, 是否强制使用？", new CommonTipsDialog.ClickListener() {
                    @Override
                    public void onClickOk(DialogInterface dialog) {
                        dialog.dismiss();
                        //输入boss密码
                        Bundle bundle = new Bundle();
                        bundle.putInt(ExtraKey.BUNDLE_PWD_ACTION, ExtraKey.BOSS_PWD_ACTION_HANDLE_CLICK_UNPAID_TABLE);
                        BossPwdActivity.start(MainActivity.this, bundle);
                        mSelectTableItemFromInputBossPwd = true;
                    }

                    @Override
                    public void onClickCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
    }

    private void handleTableItemClick(MainTableItem item) {
        mSelectTableItem = item;
        TableManager.getIns().checkTableCanUsed(item.getTableCode(), canUse -> {
            if (canUse) {
                TableManager.getIns().lockTableUse(item.getTableCode(), null);
                if (item.isUsed()) {
                    ThreadUtils.runOnIoThreadDelayed(() -> {
                        List<OrderDishesRecordEntity> list = DataBaseRepository.getInstance().getOrderDishesListByOrderId(item.getOrderId());
                    });
                    //detail
                    OrderDetailActivity.startActivity(MainActivity.this, item.getOrderId(), OrderType.ORDER_TYPE_IN_HOUSE, item.getTableCode());
                } else {
                    //点餐
//                    TableManager.getIns().postOrderRecord(mSelectTableItem.getTableCode(), false, null);
                    ChooseDishesActivity.start(MainActivity.this,
                            item.getTableCode(),
                            0,
                            OrderType.ORDER_TYPE_IN_HOUSE,
                            ChooseDishesActivity.FROM_CREATE, null);
                }
            } else {
                new CommonTipsDialog(MainActivity.this, "所选桌正在使用中, 是否强制使用？", new CommonTipsDialog.ClickListener() {
                    @Override
                    public void onClickOk(DialogInterface dialog) {
                        dialog.dismiss();
                        //输入boss密码
                        Bundle bundle = new Bundle();
                        bundle.putInt(ExtraKey.BUNDLE_PWD_ACTION, ExtraKey.BOSS_PWD_ACTION_HANDLE_CLICK_MAIN_TABLE);
                        BossPwdActivity.start(MainActivity.this, bundle);
                        mSelectTableItemFromInputBossPwd = true;
                    }

                    @Override
                    public void onClickCancel(DialogInterface dialog) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

    }

    private void updateCurrentTableZoom(int index) {
        switch (index) {
            case 1:
                mBinding.rvTable1.setVisibility(android.view.View.VISIBLE);
                mBinding.rvTable2.setVisibility(android.view.View.GONE);
                mBinding.tvZoom1.setBackgroundColor(getResources().getColor(R.color.color_576BD5));
                mBinding.tvZoom2.setBackgroundColor(getResources().getColor(R.color.color_DCDFE0));
                //设置颜色
                mBinding.tvZoom1.setTextColor(getResources().getColor(R.color.white));
                mBinding.tvZoom2.setTextColor(getResources().getColor(R.color.color_646566));

                break;
            case 2:
                mBinding.rvTable1.setVisibility(android.view.View.GONE);
                mBinding.rvTable2.setVisibility(android.view.View.VISIBLE);
                mBinding.tvZoom1.setBackgroundColor(getResources().getColor(R.color.color_DCDFE0));
                mBinding.tvZoom2.setBackgroundColor(getResources().getColor(R.color.color_576BD5));
                //设置颜色
                mBinding.tvZoom1.setTextColor(getResources().getColor(R.color.color_646566));
                mBinding.tvZoom2.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void updateTableState(boolean showUnPaid) {
        mShowUnPaid = showUnPaid;
        if (showUnPaid) {
            mBinding.stateUnpaid.setVisibility(View.VISIBLE);
            mBinding.stateAll.setVisibility(View.GONE);
            mBinding.ivState.setImageResource(R.drawable.ic_main_table_state_unpaid);
        } else {
            mBinding.stateUnpaid.setVisibility(View.GONE);
            mBinding.stateAll.setVisibility(View.VISIBLE);
            mBinding.ivState.setImageResource(R.drawable.ic_main_table_state_all);
        }
    }

    private void updateLoginUserUi() {
        mAccount = AccountManager.getIns().getAccount();
        mBinding.tvTitle.setText(mAccount.getUserName());
        mBinding.tvUser.setText(mAccount.getUserName());
        if (mAccount.isBoss()) {
            //set gone
            mBinding.tvUserType.setVisibility(View.GONE);
            mBinding.menuSetting.setVisibility(View.VISIBLE);
            mBinding.menuDeleteData.setVisibility(View.VISIBLE);
        } else {
            mBinding.tvUserType.setVisibility(View.VISIBLE);
            mBinding.menuSetting.setVisibility(View.GONE);
            mBinding.menuDeleteData.setVisibility(View.GONE);
        }
    }

    private void updateLocalMode(boolean localMode) {
        Config.setLocalMode(localMode);
        if (localMode) {
            //setImageRes
            mBinding.switchSync.setImageResource(R.drawable.ic_menu_switch_open);
        } else {
            mBinding.switchSync.setImageResource(R.drawable.ic_menu_switch_close);
        }
    }

    private void showTransFormDialog(MainTableItem item) {
        new TransformTableTipsDialog(this, new TransformTableTipsDialog.ClickListener() {
            @Override
            public void onClickTransform() {
                TableManager.getIns().checkTableCanUsed(item.getTableCode(), canUse -> {
                    if (canUse) {
                        if (!AccountManager.getIns().canTransformTable()) {
                            ToastUtils.show("no permission");
                            return;
                        }
                        ChooseTableActivity.start(MainActivity.this, item.getTableCode(), item.getOrderId(), OrderType.ORDER_TYPE_IN_HOUSE, ChooseTableActivity.FROM_TRANSFER);
                        mSelectTableItem = item;
                    } else {
                        ToastUtils.show("使用中");
                    }
                });
            }

            @Override
            public void onClickDelete() {
                TableManager.getIns().checkTableCanUsed(item.getTableCode(), canUse -> {
                    if (canUse) {
                        if (!AccountManager.getIns().canDeleteTable()) {
                            ToastUtils.show("no permission");
                            return;
                        }
                        showDeleteTipsDialog(item);
                    } else {
                        ToastUtils.show("使用中");
                    }
                });
            }
        }).show();
    }

    private void showDeleteTipsDialog(MainTableItem item) {
        new DeleteTableOrderTipsDialog(this, new DeleteTableOrderTipsDialog.ClickListener() {
            @Override
            public void onClickOk() {
                deleteOrder(item);
            }

            @Override
            public void onClickCancel() {
                //解除占用
                TableManager.getIns().updateTableState(item.getTableCode(),  false, null);
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PrinterManager.getInstance().handlePermissionResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ExtraKey.REQUEST_CODE_CHOOSE_TABLE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    int tableId = data.getIntExtra(ExtraKey.RESULT_TABLE_CODE, 0);
                    ToastUtils.show("TableId = " + tableId);
                    //chooseDishes
                    startActivity(new Intent(MainActivity.this, ChooseDishesActivity.class));
                }
            }
        } else if (requestCode == ExtraKey.REQUEST_CODE_MODIFY_PWD) {
            if (resultCode == RESULT_OK) {
                finish();
                startActivity(new Intent(this, CalculatorActivity.class));
            }
        } else if (requestCode == ExtraKey.REQUEST_CODE_SELECT_USER_LOGIN) {
            updateLoginUserUi();
        } else if (requestCode == ExtraKey.REQUEST_CODE_INPUT_BOSS_PWD) {

            if (data != null) {
                Bundle bundle = data.getBundleExtra(ExtraKey.BOSS_PWD_BUNDLE);
                if (bundle != null) {
                    if (bundle.getInt(ExtraKey.BUNDLE_PWD_ACTION) == ExtraKey.BOSS_PWD_ACTION_HANDLE_CLICK_MAIN_TABLE) {
                        if (mSelectTableItem == null) {
                            return;
                        }
                        //force use table
                        TableManager.getIns().forceLockTableUse(mSelectTableItem.getTableCode(), () -> {
                            TableManager.getIns().postOrderRecord(mSelectTableItem.getTableCode(), ActionType.FORCE_USE_TABLE_BOSS_PASSWORD, true, () -> {
                                if (mSelectTableItem.isUsed()) {
                                    //detail
                                    OrderDetailActivity.startActivity(MainActivity.this, mSelectTableItem.getOrderId(), OrderType.ORDER_TYPE_IN_HOUSE, mSelectTableItem.getTableCode());
                                } else {
                                    ChooseDishesActivity.start(MainActivity.this,
                                            mSelectTableItem.getTableCode(),
                                            0,
                                            OrderType.ORDER_TYPE_IN_HOUSE,
                                            ChooseDishesActivity.FROM_CREATE, null);
                                }
//                                mSelectTableItem = null;
                                mSelectTableItemFromInputBossPwd = false;
                            });
                        });
                    } else if (bundle.getInt(ExtraKey.BUNDLE_PWD_ACTION) == ExtraKey.BOSS_PWD_ACTION_HANDLE_CLICK_UNPAID_TABLE) {
                        TableManager.getIns().forceLockTableUse(mSelectTableItem.getTableCode(), () -> {
                            OrderDetailActivity.startActivity(MainActivity.this, mSelectTableItem.getOrderId(), mSelectTableItem.getOrderType(), mSelectTableItem.getTableCode());
//                            mSelectTableItem = null;
                            mSelectTableItemFromInputBossPwd = false;
                        });

                    }
                }
            }
        }
    }

    private void deleteOrder(MainTableItem item) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<OrderDishesRecordEntity> dishesList = DataBaseRepository.getInstance().getOrderDishesListByOrderId(item.getOrderId());
            LogUtils.log("orderId = " + item.getOrderId() + " dishes size = " + dishesList.size());
            //todo 这里应该产生一条账单记录，做上标记，做上标记是删除桌面订单方式
            BillRecordEntity billRecordEntity = new BillRecordEntity();
            billRecordEntity.setBillCode(BillCodeManager.getIns().getAndIncrement());
            billRecordEntity.setBillUserId(mAccount.getUserId());
            billRecordEntity.setTableCode(item.getTableCode());
            billRecordEntity.setFromDeleteTable(true);
            billRecordEntity.setCreateTime(System.currentTimeMillis());
            billRecordEntity.setOrderType(OrderType.ORDER_TYPE_IN_HOUSE);
            billRecordEntity.setPayType(0);
            //支付金额 = 付款金额+消费
            billRecordEntity.setPayTotal(0);
            //付款金额 = 价格 + 数量
//            dishesList.forEach(orderDishesRecordEntity -> {
//                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
//                billRecordEntity.setAmount(billRecordEntity.getAmount() + dishesEntity.getPrice() * item.getDishesCount());
//            });
            billRecordEntity.setAmount(item.getCost());
            billRecordEntity.setTipsTotal(0);
            billRecordEntity.setServerId(0);
            DataBaseRepository.getInstance().addBillRecord(billRecordEntity);
            BillRecordEntity lastBill = DataBaseRepository.getInstance().getLastBillRecord();
            dishesList.forEach(dishesRecord -> {
                BillDishesRecordEntity billDishesRecordEntity = new BillDishesRecordEntity();
                billDishesRecordEntity.setBillId(lastBill.getId());
                billDishesRecordEntity.setDishesCode(dishesRecord.getDishesCode());
                billDishesRecordEntity.setCount(dishesRecord.getCount());
                billDishesRecordEntity.setRemark(dishesRecord.getRemark());
                DataBaseRepository.getInstance().addBillDishesRecord(billDishesRecordEntity);
            });

            if (BuildConfig.DEBUG) {
                printAllBillRecord();
            }

            DataBaseRepository.getInstance().deleteOrderByOrderId(item.getOrderId());

            TableManager.getIns().postOrderRecord(item.getTableCode(), ActionType.DELETE_ORDER, false, () -> {
                TableManager.getIns().unLockTableUse(item.getTableCode(), null);
            });

            initAllData();
        });
    }

    private void deleteTodayBeforeData(Runnable finish) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //deleteOrder
            DataBaseRepository.getInstance().deleteTodayBeforeOrder();
            //deleteBill
            DataBaseRepository.getInstance().deleteTodayBeforeBill();
            //deleteReduceDishesRecord
            DataBaseRepository.getInstance().deleteTodayBeforeReduceDishesRecord();
            //deleteBillDailyReport
            DataBaseRepository.getInstance().deleteTodayBeforeBillDailyReport();
            runOnUiThread(finish);
        });
    }

    private void deleteTodayData(Runnable finish) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //deleteOrder
            DataBaseRepository.getInstance().deleteTodayOrder();
            //deleteBill
            DataBaseRepository.getInstance().deleteTodayBill();
            //deleteReduceDishesRecord
            DataBaseRepository.getInstance().deleteTodayReduceDishesRecord();
            //deleteBillDailyReport
            DataBaseRepository.getInstance().deleteTodayBillDailyReport();
            runOnUiThread(finish);
        });
    }

    private void logout() {
        AccountManager.getIns().updateAccount(null);
        startActivity(new Intent(this, CalculatorActivity.class));
        finish();
    }

    private void startSelectUserLoginActivity() {
        SelectUserLoginActivity.start(MainActivity.this, true);
        finish();
    }

    private void printAllBillRecord() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            DataBaseRepository.getInstance().getAllBillWithDishesList().forEach(item -> {
                LogUtils.log("bill = " + GsonUtils.toJson(item));
                item.getDishesList().forEach(dishes -> {
                    LogUtils.log("bill dishes = " + GsonUtils.toJson(dishes));
                });
            });
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initTestData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            for (int i = 1; i <= 20; i++) {
                MainTableItem item = new MainTableItem();
                item.setCost(3900f);
                item.setTableCode(i);
                item.setTime(System.currentTimeMillis());
                item.setDishesCount(i * 10);
                item.setBuffetCount("0E");
                item.setUsed(i < 3);
                mTable1List.add(item);
            }

            for (int i = 1; i <= 9; i++) {
                MainTableItem item = new MainTableItem();
                item.setCost(3900f);
                item.setTableCode(i);
                item.setTime(System.currentTimeMillis());
                item.setDishesCount(i * 10);
                item.setBuffetCount("0E");
                item.setUsed(i < 4);
                mTable2List.add(item);
            }

            mTable2List.get(2).setUsed(true);

            //un paid
            for (int i = 1; i <= 5; i++) {
                MainUnpaidTableItem item = new MainUnpaidTableItem();
                item.setOrderId(i);
                item.setOrderType(OrderType.ORDER_TYPE_IN_HOUSE);
                item.setTime(System.currentTimeMillis());
                item.setCount(i);
                item.setCost(3900f);
                item.setWaiter("Kellner-" + i);
                mLokalUnPaidList.add(item);
            }

            runOnUiThread(() -> {
                mTable1Adapter.notifyDataSetChanged();
                mTable2Adapter.notifyDataSetChanged();
                mTableUnPaidAdapter.notifyDataSetChanged();
            });
        });
    }
}
