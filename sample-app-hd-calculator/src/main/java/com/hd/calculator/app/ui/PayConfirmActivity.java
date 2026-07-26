package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.lifecycle.MutableLiveData;

import com.hd.calculator.app.BuildConfig;
import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.BillCodeManager;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.databinding.ActivityPayConfirmBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.List;

/**
 * 付款
 */
public class PayConfirmActivity extends BaseActivity<ActivityPayConfirmBinding> {

    private final MutableLiveData<String> mPayTotalLiveData = new MutableLiveData<>();
    private final StringBuilder mPayTotalBuilder = new StringBuilder();

    private long mOriginOrderId;
    private long mOrderId;
    private int mOrderType;
    private int mTableCode;
    private int mPayType;
    //账单金额
    private float mAmount;
    //付款金额
    private float mPayTotal;
    private float mTips;
    private boolean mIsKeyboardInput = false;

    public static void startActivity(Activity context, long originOrderId, long orderId, int orderType, int tableCode, float amount, int payType) {
        Intent intent = new Intent(context, PayConfirmActivity.class);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORIGIN_ORDER_ID, originOrderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        intent.putExtra(ExtraKey.AMOUNT, amount);
        intent.putExtra(ExtraKey.PAY_TYPE, payType);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_PAY_CONFIRM);
    }

    @Override
    protected ActivityPayConfirmBinding getViewBinding() {
        return ActivityPayConfirmBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initExtraData();
        mPayTotal = mAmount;
        mPayTotalBuilder.append(mAmount);
        updatePayTotalValue();
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        if (mPayType == PayType.PAY_TYPE_CASH) {
            mBinding.includeTopBar.tvTitle.setText(getString(R.string.payment_cash));
        } else {
            mBinding.includeTopBar.tvTitle.setText(getString(R.string.payment_card));
        }
        mBinding.tvCost.setText(MoneyUtils.formatMoney(mAmount));
        initListener();
        mPayTotalLiveData.observe(this, builder -> {
            float payTotal = 0;
            if (!builder.isEmpty()) {
                payTotal = Float.parseFloat(builder.replace(",", ""));
            }
            mBinding.tvCost.setText(MoneyUtils.formatMoney(payTotal));

            if (payTotal < mAmount) {
                mTips = 0;
            } else {
                mTips = payTotal - mAmount;
            }
            mBinding.tvTips.setText("Trinkgeld：" + MoneyUtils.formatMoney(mTips));
        });
    }

    @Override
    protected void initData() {

    }

    private void initExtraData() {
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mOriginOrderId = getIntent().getLongExtra(ExtraKey.ORIGIN_ORDER_ID, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
        mAmount = getIntent().getFloatExtra(ExtraKey.AMOUNT, 0);
        mPayType = getIntent().getIntExtra(ExtraKey.PAY_TYPE, PayType.PAY_TYPE_CASH);
    }

    private void initListener() {
        mBinding.ivPayAdd.setOnClickListener(v -> {
            mPayTotal += 1;
            mPayTotalBuilder.delete(0, mPayTotalBuilder.length());
            mPayTotalBuilder.append(mPayTotal);
            mPayTotalLiveData.setValue(mPayTotal + "");
            mIsKeyboardInput = false;
            updatePayTotalValue();
        });
        mBinding.ivPayDel.setOnClickListener(v -> {
            mPayTotal -= 1;
            if (mPayTotal < mAmount) {
                mPayTotal = mAmount;
            }
            mPayTotalBuilder.delete(0, mPayTotalBuilder.length());
            mPayTotalBuilder.append(mPayTotal);
            mPayTotalLiveData.setValue(mPayTotal + "");
            mIsKeyboardInput = false;
            updatePayTotalValue();
        });
        mBinding.tvOne.setOnClickListener(v -> {
            append("1");
        });
        mBinding.tvTwo.setOnClickListener(v -> {
            append("2");
        });
        mBinding.tvThree.setOnClickListener(v -> {
            append("3");
        });
        mBinding.tvFour.setOnClickListener(v -> {
            append("4");
        });
        mBinding.tvFive.setOnClickListener(v -> {
            append("5");
        });
        mBinding.tvSix.setOnClickListener(v -> {
            append("6");
        });
        mBinding.tvSeven.setOnClickListener(v -> {
            append("7");
        });
        mBinding.tvEight.setOnClickListener(v -> {
            append("8");
        });
        mBinding.tvNight.setOnClickListener(v -> {
            append("9");
        });
        mBinding.tvZero.setOnClickListener(v -> {
            append("0");
        });
        mBinding.tvSplit.setOnClickListener(v -> {
            append(",");
        });
        mBinding.ivDel.setOnClickListener(v -> {
            if (mPayTotalBuilder.length() > 0) {
                mPayTotalBuilder.deleteCharAt(mPayTotalBuilder.length() - 1);
            }
            if (mPayTotalBuilder.length() == 0) {
                mPayTotalBuilder.append("0");
            }
            updatePayTotalValue();
        });
        mBinding.tvC.setOnClickListener(v -> {
            mIsKeyboardInput = false;
            if (mPayTotalBuilder.length() > 0) {
                mPayTotalBuilder.delete(0, mPayTotalBuilder.length());
            }
            mPayTotalBuilder.append(mAmount);
            updatePayTotalValue();
        });
        mBinding.tvOk.setOnClickListener(v -> {
            if (mPayTotal < mAmount) {
//                ToastUtils.show(mPayTotal+ ": mPayTotal < mAmount");//debug
                return;
            }
//            ToastUtils.show( mPayTotal + ": OK");
            handleBill();
        });
    }

    private void append(String number) {
        if (!mIsKeyboardInput) {
            mPayTotalBuilder.delete(0, mPayTotalBuilder.length());
            mIsKeyboardInput = true;
        }

        if (number.equals(",") && mPayTotalBuilder.length() == 0) {
            return;
        }

        if (number.equals(",") && mPayTotalBuilder.toString().contains(".")) {
            return;
        }


        String current = mPayTotalBuilder.toString();

        if (current.contains(".")) {
            //,后两位忽略
            String[] split = current.split("\\.");
            if (split.length > 1 && split[1].length() >= 2) {
                ToastUtils.show(mPayTotalBuilder.toString());
                return;
            }
        }

        if (number.equals(",")) {
            mPayTotalBuilder.append(".");
        } else {
            mPayTotalBuilder.append(number);
        }


        updatePayTotalValue();

    }

    private void handleBill() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<OrderDishesRecordEntity> dishesList = DataBaseRepository.getInstance().getOrderDishesListByOrderId(mOrderId);
            LogUtils.log("orderId = " + mOrderId + " dishes size = " + dishesList.size());
            //todo 这里应该产生一条账单记录，做上标记，做上标记是删除桌面订单方式
            BillRecordEntity billRecordEntity = new BillRecordEntity();
            billRecordEntity.setBillCode(BillCodeManager.getIns().getAndIncrement());
            if (AccountManager.getIns().getAccount() != null) {
                billRecordEntity.setBillUserId(AccountManager.getIns().getAccount().getUserId());
            } else {
                ToastUtils.show("未登录");
                return;
            }
            billRecordEntity.setTableCode(mTableCode);
            billRecordEntity.setFromDeleteTable(false);
            billRecordEntity.setCreateTime(System.currentTimeMillis());
            billRecordEntity.setOrderType(mOrderType);
            billRecordEntity.setPayType(mPayType);
            //支付金额 = 付款金额+消费
            billRecordEntity.setPayTotal(mPayTotal);
            //付款金额 = 价格 + 数量
//            dishesList.forEach(orderDishesRecordEntity -> {
//                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(orderDishesRecordEntity.getDishesCode());
//                billRecordEntity.setAmount(billRecordEntity.getAmount() + dishesEntity.getPrice() * item.getDishesCount());
//            });
            billRecordEntity.setAmount(mAmount);
            billRecordEntity.setTipsTotal(mPayTotal - mAmount);
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

            //删除订单
            DataBaseRepository.getInstance().deleteOrderByOrderId(mOrderId);

            TableManager.getIns().postOrderRecord(mTableCode, ActionType.BILL, false, null);

            //todo 如果是全额付款 回到主页
            PaymentActivity.startActivity(PayConfirmActivity.this, mOriginOrderId, mOriginOrderId, mOrderType, mTableCode, mAmount);
            setResult(RESULT_OK);
            finish();
//            startActivity(new Intent(PayConfirmActivity.this, MainActivity.class));
        });
    }

    private void updatePayTotalValue() {
        mPayTotal = MoneyUtils.formatMoney(mPayTotalBuilder.toString());
        mPayTotalLiveData.setValue(mPayTotalBuilder.toString());

        boolean enable = mPayTotal >= mAmount;
        mBinding.tvOk.setEnabled(enable);
        mBinding.tvOk.setClickable(enable);

        if (enable) {
            mBinding.tvOk.setBackgroundResource(R.drawable.shape_keyboard_make_order_bg);
        } else {
            mBinding.tvOk.setBackgroundResource(R.drawable.shape_keyboard_ok_bg_disable);
        }
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

}
