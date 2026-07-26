package com.hd.calculator.app.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.databinding.HdcActivityPaymentBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.util.MoneyUtils;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 预付款页 -
 */
public class PaymentActivity extends BaseActivity<HdcActivityPaymentBinding> {

    private long mOrderId;
    private long mOriginOrderId;
    private int mOrderType;
    private int mTableCode;

    private float mAmount;

    public static void startActivity(Activity context, long originOrderId, long orderId, int orderType, int tableCode, float amount) {
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORIGIN_ORDER_ID, originOrderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        intent.putExtra(ExtraKey.AMOUNT, amount);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_PAYMENT);
    }


    @Override
    protected HdcActivityPaymentBinding getViewBinding() {
        return HdcActivityPaymentBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        initExtraData();
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            mBinding.includeTopBar.tvTitle.setText("Zahlung (Tisch " + mTableCode + ")");
        } else {
            mBinding.includeTopBar.tvTitle.setText("Zahlung (T-A" + mTableCode + ")");
        }
        mBinding.itemSplitPay.setOnClickListener(v -> {
            SplitPaymentActivity.start(this,mOriginOrderId,  mOrderId, mOrderType, mTableCode);
        });
        mBinding.btnPayCard.setOnClickListener(v -> {
            PayConfirmActivity.startActivity(this, mOriginOrderId, mOrderId, mOrderType, mTableCode, mAmount, PayType.PAY_TYPE_CARD);
        });
        mBinding.btnPayCash.setOnClickListener(v -> {
            PayConfirmActivity.startActivity(this, mOriginOrderId, mOrderId, mOrderType, mTableCode, mAmount, PayType.PAY_TYPE_CASH);
        });
        mBinding.btnPreviewTicket.setOnClickListener(v -> {
            ThreadUtils.runOnIoThreadDelayed(() -> {
                ArrayList<DishesItem> dishesItemList = new ArrayList<>();
                OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
                for (OrderDishesRecordEntity dishesRecord : orderWithDishesRef.getDishesList()) {
                    DishesItem dishesItem = new DishesItem();
                    dishesItem.setCode(dishesRecord.getDishesCode());
                    DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(dishesRecord.getDishesCode());
                    dishesItem.setPrice(dishesEntity.getPrice());
                    dishesItem.setName(dishesEntity.getName());
                    dishesItem.setCount(dishesRecord.getCount());
                    dishesItem.setOrdered(false);
                    dishesItem.setRemark(dishesRecord.getRemark());
                    dishesItem.setFirstSortType( dishesEntity.getFirstSortType());
                    dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                    dishesItem.setCanceled(false);
                    dishesItemList.add(dishesItem);
                }

                PreviewTicketActivity.startActivity(this, mOrderId, mOrderType, mTableCode, dishesItemList);

            });
        });

    }

    @Override
    protected void initData() {
        getOrderData();
    }

    private void initExtraData() {
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mOriginOrderId = getIntent().getLongExtra(ExtraKey.ORIGIN_ORDER_ID, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
//        mAmount = getIntent().getFloatExtra(ExtraKey.AMOUNT, 0);
    }

    private void getOrderData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //checkOriginOrderId是否全部结算完
            OrderWithDishesRef originOrderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOriginOrderId);
            if (originOrderWithDishesRef == null || originOrderWithDishesRef.getDishesList().isEmpty()) {

                //startMain
                startActivity(new Intent(PaymentActivity.this, MainActivity.class));
                finish();
                return;
            }

            mAmount = 0;
            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
            for (OrderDishesRecordEntity dishesRecord : orderWithDishesRef.getDishesList()) {
                DishesEntity dishesEntity = DataBaseRepository.getInstance().getDishesByCode(dishesRecord.getDishesCode());
                mAmount += dishesEntity.getPrice() * dishesRecord.getCount();
            }
            runOnUiThread(() -> {
                mBinding.tvCost.setText(MoneyUtils.formatMoney(mAmount));
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_SPLIT_PAY) {
            setResult(RESULT_OK);
            finish();
        } else if (resultCode == RESULT_OK && requestCode == ExtraKey.REQUEST_CODE_PAY_CONFIRM) {
            finish();
        }
    }
}
