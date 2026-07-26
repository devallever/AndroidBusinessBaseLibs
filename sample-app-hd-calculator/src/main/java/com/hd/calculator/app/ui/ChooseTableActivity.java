package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.ActivityChooseTableBinding;
import com.hd.calculator.app.function.UserLog;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.TableEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.ui.dialog.RestoreTableUsedTipsDialog;
import com.hd.calculator.app.util.EventUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

/**
 * 转移/创建/拆分/恢复功能选桌
 */
public class ChooseTableActivity extends BaseActivity<ActivityChooseTableBinding> {

    //创建
    public static final int FROM_CREATE = 0;
    //转移
    public static final int FROM_TRANSFER = 1;
    //恢复
    public static final int FROM_RESTORE = 2;
    //拆分
    public static final int FROM_SPLIT_ORDER = 3;
    private final StringBuilder mInputText = new StringBuilder();
    private int mFrom;
    private int mTableCode;
    private long mOrderId;
    private int mOrderType = OrderType.ORDER_TYPE_IN_HOUSE;
    //返回选中桌号
    private int mResultTableCode;
    private TableEntity mTableEntity;

    /***
     * @param context
     * @param tableCode 当前桌号，如果是新建桌可以传0
     * @param from 0创建，1转移桌，2恢复桌，3拆分桌
     */
    public static void start(Activity context, int tableCode, long orderId, int orderType, int from) {
        Intent intent = new Intent(context, ChooseTableActivity.class);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.CHOOSE_TABLE_FROM, from);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_CHOOSE_TABLE);
    }

    @Override
    protected ActivityChooseTableBinding getViewBinding() {
        return ActivityChooseTableBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        mBinding.includeTopBar.ivBack.setOnClickListener(v -> finish());
        initExtraData();
        mInputText.append(0);
        updateConfirmButton();
        updateTips();
        initClickListener();
    }

    @Override
    protected void initData() {
        if (mTableCode != 0) {
            ThreadUtils.runOnIoThreadDelayed(() -> {
                mTableEntity = DataBaseRepository.getInstance().getByTableId(mTableCode);
                runOnUiThread(this::updateTypeUi);
            });
        } else {
            runOnUiThread(this::updateTypeUi);
        }
    }

    private void initExtraData() {
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
        mFrom = getIntent().getIntExtra(ExtraKey.CHOOSE_TABLE_FROM, FROM_CREATE);
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
    }

    private void initClickListener() {
        mBinding.tvOne.setOnClickListener(v -> {
            appendNumber(1);
        });
        mBinding.tvTwo.setOnClickListener(v -> {
            appendNumber(2);
        });
        mBinding.tvThree.setOnClickListener(v -> {
            appendNumber(3);
        });
        mBinding.tvFour.setOnClickListener(v -> {
            appendNumber(4);

        });
        mBinding.tvFive.setOnClickListener(v -> {
            appendNumber(5);
        });
        mBinding.tvSix.setOnClickListener(v -> {
            appendNumber(6);

        });
        mBinding.tvSeven.setOnClickListener(v -> {
            appendNumber(7);
        });
        mBinding.tvEight.setOnClickListener(v -> {
            appendNumber(8);

        });
        mBinding.tvNight.setOnClickListener(v -> {
            appendNumber(9);
        });
        mBinding.tvZero.setOnClickListener(v -> {
            appendNumber(0);
        });
        mBinding.ivDelete.setOnClickListener(v -> {
            reduceNumber();
        });
        mBinding.tvAbc.setOnClickListener(v -> {

        });
        mBinding.btnOk.setOnClickListener(v -> {
            actionNext();
        });
    }

    private void actionNext() {
        if (mResultTableCode == mTableCode && mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            ToastUtils.show("Same table");
            return;
        }

        TableManager.getIns().checkTableCanUsed(mResultTableCode, canUse -> {
            if (!canUse) {
                ToastUtils.show("正在使用中");
            } else {
                ThreadUtils.runOnIoThreadDelayed(() -> {
                    //checkExist
                    TableEntity resultTableEntity = DataBaseRepository.getInstance().getTableByCode(mResultTableCode);
                    if (resultTableEntity == null) {
                        ToastUtils.show("not exist");
                        return;
                    }
                    OrderRecordEntity resultOrderEntity = DataBaseRepository.getInstance().getOrderByTableCode(mResultTableCode);
                    boolean used = resultOrderEntity != null;
                    switch (mFrom) {
                        case FROM_CREATE:
                            if (used) {
                                //待结账
                                OrderDetailActivity.startActivity(ChooseTableActivity.this, resultOrderEntity.getId(), resultOrderEntity.getOrderType(), resultOrderEntity.getTableCode());
                            } else {
                                //点餐
//                                TableManager.getIns().postOrderRecord(mResultTableCode, false, null);
                                ChooseDishesActivity.start(this, mResultTableCode, 0, mOrderType, ChooseDishesActivity.FROM_CREATE, null);
                            }
                            finish();
                            break;
                        case FROM_TRANSFER:
                            //转移桌
                            if (used) {
                                //所选桌正在使用，
                                // 1.修改原号桌的菜品关联到所选的桌的订单id
                                DataBaseRepository.getInstance().getOrderDishesListByOrderId(mOrderId).forEach(orderDishesRecordEntity -> {
                                    orderDishesRecordEntity.setOrderId(resultOrderEntity.getId());
                                    DataBaseRepository.getInstance().updateOrderDishes(orderDishesRecordEntity);
                                });
                                TableManager.getIns().postOrderRecord(mResultTableCode, ActionType.TRANSFER, false, null);
                                // 添加删除订单日志
                                EventUtils.logDeleteOrderEvent(mOrderId, "换桌操作后删除原桌订单");
                                //2. 删除旧号桌的订单(目前先删除，后期可能只改变状态)
                                DataBaseRepository.getInstance().deleteOrderByOrderId(mOrderId);
                                TableManager.getIns().postOrderRecord(mTableCode,  ActionType.TRANSFER, false, null);
                            } else {
                                //所选桌是空桌
                                //1.创建订单
                                OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
                                orderRecordEntity.setTableCode(mResultTableCode);
                                orderRecordEntity.setOrderUserId(AccountManager.getIns().getAccount().getUserId());
                                orderRecordEntity.setOrderType(mOrderType);
                                orderRecordEntity.setCreateTime(System.currentTimeMillis());
                                DataBaseRepository.getInstance().addOrderRecord(orderRecordEntity);
                                long orderId = DataBaseRepository.getInstance().getLastOrderRecord().getId();
                                //2.修改原号桌的菜品关联到所选的桌的订单id
                                DataBaseRepository.getInstance().getOrderDishesListByOrderId(mOrderId).forEach(oldDishes -> {
                                    oldDishes.setOrderId(orderId);
                                    DataBaseRepository.getInstance().updateOrderDishes(oldDishes);
                                });
                                TableManager.getIns().postOrderRecord(mResultTableCode,  ActionType.TRANSFER, false, null);
                                // 添加删除订单日志
                                EventUtils.logDeleteOrderEvent(mOrderId, "换桌到空桌后删除原桌订单");
                                //3. 删除旧号桌的订单(目前先删除，后期可能只改变状态)
                                DataBaseRepository.getInstance().deleteOrderByOrderId(mOrderId);
                                TableManager.getIns().postOrderRecord(mTableCode,  ActionType.TRANSFER, false, null);
                            }
                            TableManager.getIns().unLockTableUse(mResultTableCode, null);
                            finish();
                            break;
                        case FROM_RESTORE:
                            ThreadUtils.runOnIoThreadDelayed(() -> {
                                //恢复桌
                                //todo 离线模式这样判断，联网时需调接口
                                if (used) {
                                    runOnUiThread(() -> {
                                        new RestoreTableUsedTipsDialog(ChooseTableActivity.this, () -> {
                                        }).show();
                                    });
                                } else {
                                    Intent intent = new Intent();
                                    intent.putExtra(ExtraKey.RESULT_TABLE_CODE, mResultTableCode);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            });
                            break;
                        case FROM_SPLIT_ORDER:
                            Intent intent = new Intent();
                            intent.putExtra(ExtraKey.RESULT_TABLE_CODE, mResultTableCode);
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                    }
                });
            }
        });


    }

    private void appendNumber(int number) {
        if (mInputText.length() == 1 && mInputText.toString().equals("0")) {
            mInputText.delete(mInputText.length() - 1, mInputText.length());
        }

        if (mInputText.length() > 6) {
            return;
        }
        //stringBuilder
        mInputText.append(number);
        mResultTableCode = Integer.parseInt(mInputText.toString());
        mBinding.tvTableNum.setText(mInputText);
        updateConfirmButton();
        updateTips();
    }

    private void reduceNumber() {
        //删除stringBuilder 上一个输入端字符
        if (mInputText.length() > 0) {
            mInputText.delete(mInputText.length() - 1, mInputText.length());
        }
        if (mInputText.length() == 0) {
            mInputText.append("0");
        }
        mResultTableCode = Integer.parseInt(mInputText.toString());
        mBinding.tvTableNum.setText(mInputText);
        updateConfirmButton();
        updateTips();

    }

    private void updateTips() {
        switch (mFrom) {
            case FROM_CREATE:
                updateFromCreateInputTips();
                break;
            case FROM_TRANSFER:
                break;
        }
    }

    private void updateFromCreateInputTips() {
        if (mInputText.toString().equals("0")) {
            mBinding.tvNewOrderTips.setText(getString(R.string.choose_table_from_create_empty_tips));
        } else {
            mBinding.tvNewOrderTips.setText(getString(R.string.choose_table_from_create_inputed));
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateTypeUi() {
        //update title
        switch (mFrom) {
            case FROM_CREATE:
                mBinding.includeTopBar.tvTitle.setText(R.string.choose_table);
                mBinding.tvNewOrderTips.setVisibility(View.VISIBLE);
                mBinding.tvTips.setVisibility(View.GONE);
                mBinding.tvCurrentTable.setVisibility(View.GONE);
                break;
            case FROM_TRANSFER:
                mBinding.includeTopBar.tvTitle.setText(R.string.transform);
                mBinding.tvNewOrderTips.setVisibility(View.GONE);
                mBinding.tvTips.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setText("Aktuell：Tisch " + mTableCode);
                break;
            case FROM_RESTORE:
                mBinding.includeTopBar.tvTitle.setText(R.string.restore_table);
                mBinding.tvNewOrderTips.setVisibility(View.GONE);
                mBinding.tvTips.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setText("Aktuell：Tisch " + mTableCode);
                break;
            case FROM_SPLIT_ORDER:
                mBinding.includeTopBar.tvTitle.setText(TableManager.getIns().getDisplayTableName(mTableCode, mOrderType));
                mBinding.tvNewOrderTips.setVisibility(View.GONE);
                mBinding.tvTips.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setVisibility(View.VISIBLE);
                mBinding.tvCurrentTable.setText("Aktuell：Tisch " + mTableCode);
                break;
        }
    }

    private void updateConfirmButton() {
        if (mResultTableCode == 0) {
            mBinding.btnOk.setEnabled(false);
            //setBackgroundResource
            mBinding.btnOk.setBackgroundResource(R.drawable.shape_gray_r45);
        } else {
            mBinding.btnOk.setEnabled(true);
            //setBackgroundResource
            mBinding.btnOk.setBackgroundResource(R.drawable.shape_green_r45);
        }
    }
}
