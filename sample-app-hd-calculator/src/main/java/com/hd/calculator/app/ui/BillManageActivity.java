package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.databinding.HdcActivityBillManageBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.ui.adapter.BillManageAdapter;
import com.hd.calculator.app.ui.adapter.BillPayTypeAdapter;
import com.hd.calculator.app.ui.dialog.BillFilterPopWindow;
import com.hd.calculator.app.ui.dialog.BillOptionDialog;
import com.hd.calculator.app.ui.dialog.BillSortPopWindow;
import com.hd.calculator.app.ui.dialog.ModifyPayTypeDialog;
import com.hd.calculator.app.ui.dialog.RestoreTableUsedTipsDialog;
import com.hd.calculator.app.ui.item.BillManageItem;
import com.hd.calculator.app.ui.item.BillPayTypeItem;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.TimeUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 账单管理
 */
public class BillManageActivity extends BaseActivity<HdcActivityBillManageBinding> {

    //list
    private final List<BillManageItem> mBillManageList = new ArrayList<>();
    //orderpaytypelist
    private final List<BillPayTypeItem> mBillPayTypeList = new ArrayList<>();
    private final AccountEntity mAccountEntity = AccountManager.getIns().getAccount();
    //adapter
    private BillManageAdapter mBillManageAdapter;
    //type adapter
    private BillPayTypeAdapter mBillPayTypeAdapter;
    //sortpop
    //orderSortPop
    private BillSortPopWindow mBillSortPop;
    //orderfilterpop
    private BillFilterPopWindow mBillFilterPop;
    private DatePickerDialog mDatePickerDialog;
    private int mSelectedPayType;

    private String mSelectSortCondition;
    //boss
    private long mSelectedDate;
    //boss
    private long mSelectedUserId;

    private boolean mNeedUpdateSummery = true;//当修改user时，是否需要更新，用完后置为false

    private int mBillCount = 0;
    private float mBillTotalAmount = 0f;
    private int mCashBillCount = 0;
    private float mCashBillTotalAmount = 0f;
    private int mCardBillCount = 0;
    private float mCardBillTotalAmount = 0f;

    private BillManageItem mSelectedBillItem;

    @Override
    protected HdcActivityBillManageBinding getViewBinding() {
        return HdcActivityBillManageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initPageByUserType();
        initBillList();
        initBillPayTypeList();
        initListener();
        initPop();
        initDateSelector();
    }

    @Override
    protected void initData() {
        mSelectedUserId = 0;
        mSelectedDate = TimeUtils.getCurrentDay7amTimestamps()[0];
        initBillPayTypeData();
        getBillListData();
    }

    @SuppressLint("SetTextI18n")
    private void initPageByUserType() {
        if (isBoss()) {
            mBinding.ivFilter.setVisibility(View.VISIBLE);
            mBinding.ivSearch.setVisibility(View.VISIBLE);
            mBinding.bossAmountContainer.setVisibility(View.VISIBLE);
            mBinding.waiterAmountContainer.setVisibility(View.GONE);

            long today7amTimestamp = TimeUtils.getToday7amTimestamp();
            mBinding.tvDate.setText("07:00 - " + TimeUtils.formatTimestampToDDMMYYYYHHmm(TimeUtils.get7amTimestamps(today7amTimestamp)[1]));
//            mBinding.tvDateDesc.setText(TimeUtils.formatTimestampToYYYYDDMM(today7amTimestamp));
        } else {
            mBinding.ivFilter.setVisibility(View.GONE);
            mBinding.ivSearch.setVisibility(View.GONE);
            mBinding.bossAmountContainer.setVisibility(View.GONE);
            mBinding.waiterAmountContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
        mBinding.ivBack.setOnClickListener(v -> finish());
        mBinding.ivSortType.setOnClickListener(v -> {
            mBillSortPop.show(mBinding.ivSortType);
        });
        mBinding.ivFilter.setOnClickListener(v -> {
            mBillFilterPop.show(mBinding.ivFilter);
        });
        mBinding.ivSearch.setOnClickListener(v -> {
            startActivity(new Intent(BillManageActivity.this, BillSearchActivity.class));
        });
        mBinding.tvDate.setOnClickListener(v -> {
            mDatePickerDialog.show();
        });
        mBinding.tvDateDesc.setOnClickListener(v -> {
            mDatePickerDialog.show();
        });
        mBinding.ivCalendar.setOnClickListener(v -> {
            mDatePickerDialog.show();
        });

    }

    private void initPop() {
        mBillSortPop = new BillSortPopWindow(this);
        mSelectSortCondition = mBillSortPop.getCurrentSortCondition();
        mBillSortPop.setItemChangeListener(item -> {
            mSelectSortCondition = item.getOrderCondition();
            getBillListData();
        });

        mBillFilterPop = new BillFilterPopWindow(this);
        mSelectedUserId = mBillFilterPop.getCurrentUserId();
        mBillFilterPop.setItemChangeListener(item -> {
            mNeedUpdateSummery = true;
            mSelectedUserId = item.getUserId();
            if (item.getUserId() == 0) {
                //tvWaiter GONE
                mBinding.tvWaiter.setVisibility(View.GONE);
            } else {
                mBinding.tvWaiter.setVisibility(View.VISIBLE);
                mBinding.tvWaiter.setText(item.getName());
            }

            //选中用户后重置付款方式
            for (BillPayTypeItem billPayTypeItem : mBillPayTypeList) {
                billPayTypeItem.setSelect(billPayTypeItem.getPayType() == PayType.PAY_TYPE_ALL);
            }
            mSelectedPayType = PayType.PAY_TYPE_ALL;
            mBillPayTypeAdapter.notifyDataSetChanged();
            getBillListData();
        });
    }

    @SuppressLint("SetTextI18n")
    private void initDateSelector() {
        mDatePickerDialog = new DatePickerDialog(this);
        mDatePickerDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {
            mSelectedDate = TimeUtils.get7amTimestamps(year, month + 1, dayOfMonth)[0];
            mBinding.tvDate.setText("07:00 - " + TimeUtils.formatTimestampToDDMMYYYYHHmm(TimeUtils.get7amTimestamps(year, month + 1, dayOfMonth)[1]));
            mBinding.tvDateDesc.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
            getBillListData();
        });
    }

    private void initBillList() {
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
            if (item.isFromDeleteTable() || item.isCanceled()) {
                return false;
            }
            long billTime = item.getOrderTime();
            long[] theBillDayTimeLimit = TimeUtils.get7amTimestamps(billTime);
            long currentTime = System.currentTimeMillis();
            boolean canOption = currentTime >= theBillDayTimeLimit[0] && currentTime < theBillDayTimeLimit[1];

            new BillOptionDialog(this, new BillOptionDialog.OptionClickListener() {
                @Override
                public void onClickRestoreTable() {
                    if (!AccountManager.getIns().canRestoreTable()) {
                        ToastUtils.show("no permission");
                        return;
                    }
                    handleRestoreTable(item, canOption);
                }

                @Override
                public void onClickModifyPayType() {
                    handleModifyPayType(item, canOption);
                }

                @Override
                public void onClickCancelBill() {
                    if (!AccountManager.getIns().canCancelOrder()) {
                        ToastUtils.show("no permission");
                        return;
                    }
                    handleCancelBill(item, canOption);
                }
            }).show();
            return true;
        });
    }

    private void initBillPayTypeList() {
        //initrv
        mBinding.rvPayType.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mBillPayTypeAdapter = new BillPayTypeAdapter(mBillPayTypeList);
        mBinding.rvPayType.setAdapter(mBillPayTypeAdapter);
        mBillPayTypeAdapter.setItemClickListener(item -> {
            //for update select
            for (BillPayTypeItem payTypeItem : mBillPayTypeList) {
                payTypeItem.setSelect(payTypeItem.getPayType() == item.getPayType());
            }
            mBillPayTypeAdapter.notifyDataSetChanged();
            mSelectedPayType = item.getPayType();
            getBillListData();
        });
    }

    private boolean isBoss() {
        return mAccountEntity.isBoss();
    }

    private void initBillPayTypeData() {
        mBillPayTypeList.add(new BillPayTypeItem(0, 0, PayType.PAY_TYPE_ALL, "Alle", true));
        mBillPayTypeList.add(new BillPayTypeItem(0, 0, PayType.PAY_TYPE_CASH, "Bargeld", false));
        mBillPayTypeList.add(new BillPayTypeItem(0, 0, PayType.PAY_TYPE_CARD, "Karte", false));
        mBillPayTypeAdapter.notifyDataSetChanged();
        mSelectedPayType = mBillPayTypeList.get(0).getPayType();
    }

    private void getBillListData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<BillWithDishesRef> allBillList = DataBaseRepository.getInstance().getAllBillWithDishesList();
            allBillList.forEach(billWithDishesRef -> {
                LogUtils.log("all bill = " + GsonUtils.toJson(billWithDishesRef));
            });

            LogUtils.log("bill selectedUserId = " + mSelectedUserId);
            LogUtils.log("bill selectedDate = " + TimeUtils.formatTimestampToDDMMYYYYHHmm(mSelectedDate));
            LogUtils.log("bill selectSortCondition = " + mSelectSortCondition);
            LogUtils.log("bill selectedPayType = " + mSelectedPayType);

            long userId = mAccountEntity.getUserId();
            if (isBoss()) {
                userId = mSelectedUserId;
            }
            List<BillWithDishesRef> billList = DataBaseRepository.getInstance().getBillList(userId, mSelectedDate, mSelectedPayType, mSelectSortCondition);

            mBillTotalAmount = 0;
            mBillManageList.clear();
            mCashBillCount = 0;
            mCashBillTotalAmount = 0;
            mCardBillCount = 0;
            mCardBillTotalAmount = 0;
            mBillCount = billList.size();
            billList.forEach(billWithDishesRef -> {
                LogUtils.log("bill = " + GsonUtils.toJson(billWithDishesRef));
                BillRecordEntity bill = billWithDishesRef.getBill();
                BillManageItem item = new BillManageItem();
                item.setId(bill.getId());
                item.setBillCode(bill.getBillCode());
                item.setOrderType(bill.getOrderType());
                item.setTableCode(bill.getTableCode());
                item.setCost(bill.getAmount());
                item.setPayTotal(bill.getPayTotal());
                item.setWaiterId(bill.getBillUserId());
                AccountEntity accountEntity = DataBaseRepository.getInstance().getByUserId(bill.getBillUserId());
                item.setWaiterName(accountEntity.getUserName());
                item.setOrderTime(bill.getCreateTime());
                item.setFromDeleteTable(bill.isFromDeleteTable());
                item.setCanceled(bill.isCanceled());
                item.setPayType(bill.getPayType());
                if (!bill.isCanceled() && !bill.isFromDeleteTable()) {
                    mBillTotalAmount += bill.getAmount();
                }
                if (bill.getPayType() == PayType.PAY_TYPE_CASH) {
                    if (!bill.isCanceled() && !bill.isFromDeleteTable()) {
                        mCashBillTotalAmount += bill.getAmount();
                        mCashBillCount += 1;
                    }

                } else if (bill.getPayType() == PayType.PAY_TYPE_CARD) {
                    if (!bill.isCanceled() && !bill.isFromDeleteTable()) {
                        mCardBillTotalAmount += bill.getAmount();
                        mCardBillCount += 1;
                    }
                }

                List<BillDishesRecordEntity> dishesList = billWithDishesRef.getDishesList();
                for (BillDishesRecordEntity billDishesRecordEntity : dishesList) {
                    DishesItem dishesItem = new DishesItem();
                    dishesItem.setCode(billDishesRecordEntity.getDishesCode());
                    DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(billDishesRecordEntity.getDishesCode());
                    dishesItem.setName(dishesEntity.getName());
                    dishesItem.setPrice(dishesEntity.getPrice());
                    dishesItem.setFirstSortType(dishesEntity.getFirstSortType());
                    dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                    dishesItem.setCount(billDishesRecordEntity.getCount());
                    dishesItem.setRemark(billDishesRecordEntity.getRemark());
                    item.getDishesList().add(dishesItem);
                }

                mBillManageList.add(item);

            });

            runOnUiThread(() -> {
                mBillManageAdapter.notifyDataSetChanged();
                mBinding.tvBossAmount.setText(MoneyUtils.formatMoney(mBillTotalAmount));
                mBinding.tvWaiterAmount.setText(MoneyUtils.formatMoney(mBillTotalAmount));

                if (mSelectedPayType == PayType.PAY_TYPE_ALL || mNeedUpdateSummery) {
                    for (BillPayTypeItem billPayTypeItem : mBillPayTypeList) {
                        switch (billPayTypeItem.getPayType()) {
                            case PayType.PAY_TYPE_ALL:
                                billPayTypeItem.setCost(mBillTotalAmount);
                                billPayTypeItem.setCount(mBillCount);
                                break;
                            case PayType.PAY_TYPE_CASH:
                                billPayTypeItem.setCost(mCashBillTotalAmount);
                                billPayTypeItem.setCount(mCashBillCount);
                                break;
                            case PayType.PAY_TYPE_CARD:
                                billPayTypeItem.setCost(mCardBillTotalAmount);
                                billPayTypeItem.setCount(mCardBillCount);
                                break;
                        }
                    }

                    if (mNeedUpdateSummery) {
                        mNeedUpdateSummery = false;
                    }
                }

                mBillPayTypeAdapter.notifyDataSetChanged();

            });

        });
    }

    private void handleRestoreTable(BillManageItem item, boolean canOption) {
        if (!canOption) {
            ToastUtils.show("operate over time");
            return;
        }

        mSelectedBillItem = item;
        ThreadUtils.runOnIoThreadDelayed(() -> {
            int tableCode = item.getTableCode();
            OrderRecordEntity orderRecordEntity = DataBaseRepository.getInstance().getOrderByTableCode(item.getTableCode());
            //todo 离线模式这样判断，联网时需调接口
            boolean used = orderRecordEntity != null;
            if (used) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new RestoreTableUsedTipsDialog(BillManageActivity.this, () -> {
                            //choose table
                            ChooseTableActivity.start(BillManageActivity.this, tableCode, 0, OrderType.ORDER_TYPE_IN_HOUSE, ChooseTableActivity.FROM_RESTORE);
                        }).show();
                    }
                });
            } else {
                executeRestoreDB(item, tableCode, canOption);
            }
        });
    }

    private void handleModifyPayType(BillManageItem item, boolean canOption) {
        if (!canOption) {
            ToastUtils.show("operate over time");
            return;
        }
        new ModifyPayTypeDialog(BillManageActivity.this, item.getPayType(), newPayType -> {
            if (newPayType == item.getPayType()) {
                return;
            }
            ThreadUtils.runOnIoThreadDelayed(() -> {
                BillWithDishesRef billWithDishesRef = DataBaseRepository.getInstance().getBillById(item.getId());
                BillRecordEntity billRecordEntity = billWithDishesRef.getBill();
                billRecordEntity.setPayType(newPayType);
                DataBaseRepository.getInstance().updateBill(billRecordEntity);

                runOnUiThread(() -> {
                    getBillListData();
                });
            });
        }).show();
    }

    private void handleCancelBill(BillManageItem item, boolean canOption) {
        if (!canOption) {
            ToastUtils.show("operate over time");
            return;
        }

        ThreadUtils.runOnIoThreadDelayed(() -> {
            BillWithDishesRef billWithDishesRef = DataBaseRepository.getInstance().getBillById(item.getId());
            BillRecordEntity billRecordEntity = billWithDishesRef.getOrder();
            billRecordEntity.setCanceled(true);
            DataBaseRepository.getInstance().updateBill(billRecordEntity);

            runOnUiThread(() -> {
                getBillListData();
            });
        });
    }

    private void executeRestoreDB(BillManageItem item, int tableCode, boolean canOption) {
        OrderRecordEntity orderRecord = new OrderRecordEntity();
        orderRecord.setTableCode(tableCode);
        orderRecord.setOrderUserId(mAccountEntity.getUserId());
        orderRecord.setOrderType(item.getOrderType());
        orderRecord.setCreateTime(System.currentTimeMillis());
        DataBaseRepository.getInstance().addOrderRecord(orderRecord);
        OrderRecordEntity dbOrderRecord = DataBaseRepository.getInstance().getLastOrderRecord();
        long orderId = dbOrderRecord.getId();
        for (DishesItem dishesItem : item.getDishesList()) {
            OrderDishesRecordEntity orderDishesRecordEntity = new OrderDishesRecordEntity();
            orderDishesRecordEntity.setOrderId(orderId);
            orderDishesRecordEntity.setDishesCode(dishesItem.getCode());
            orderDishesRecordEntity.setCount(dishesItem.getCount());
            orderDishesRecordEntity.setRemark(dishesItem.getRemark());
            DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
        }

        //取消订单
        handleCancelBill(item, canOption);
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
                dishesItem.setFirstSortType(DishesFirstSortType.FOOD);
                dishesItem.setEnablePrint(true);
                dishesItem.setCount(i);
                dishesItem.setRemark("remark" + i);
                item.getDishesList().add(dishesItem);
                //add deshItem
                DishesItem dishesItem2 = new DishesItem();
                dishesItem2.setCode(i + "");
                dishesItem2.setName("Dishes" + i);
                dishesItem2.setPrice(i);
                dishesItem2.setFirstSortType(DishesFirstSortType.DRINK);
                dishesItem.setEnablePrint(true);
                dishesItem2.setCount(i);
                item.getDishesList().add(dishesItem2);
            }
            mBillManageList.add(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_CHOOSE_TABLE) {
            if (data != null) {
                int tableCode = data.getIntExtra(ExtraKey.RESULT_TABLE_CODE, 0);
                ThreadUtils.runOnIoThreadDelayed(() -> {
                    executeRestoreDB(mSelectedBillItem, tableCode, true);
                });
            }
        }
    }
}
