package com.hd.calculator.app;

import android.content.Intent;

import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.databinding.ActivityNavigationBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.network.NetworkCallback;
import com.hd.calculator.app.function.network.NetworkRepository;
import com.hd.calculator.app.function.network.response.AccountResponse;
import com.hd.calculator.app.function.printer.PrinterManager;
import com.hd.calculator.app.ui.BillManageActivity;
import com.hd.calculator.app.ui.BillSearchActivity;
import com.hd.calculator.app.ui.BossPwdActivity;
import com.hd.calculator.app.ui.CalculatorActivity;
import com.hd.calculator.app.ui.ChooseDishesActivity;
import com.hd.calculator.app.ui.ChooseTableActivity;
import com.hd.calculator.app.ui.DishesCancelActivity;
import com.hd.calculator.app.ui.MainActivity;
import com.hd.calculator.app.ui.ModifyBossPwdActivity;
import com.hd.calculator.app.ui.ModifyDishesCountActivity;
import com.hd.calculator.app.ui.OrderDetailActivity;
import com.hd.calculator.app.ui.PayConfirmActivity;
import com.hd.calculator.app.ui.PaymentActivity;
import com.hd.calculator.app.ui.ReportBossActivity;
import com.hd.calculator.app.ui.ReportWaiterActivity;
import com.hd.calculator.app.ui.SplitPaymentActivity;
import com.hd.calculator.app.ui.SplitTableActivity;
import com.hd.calculator.app.ui.TableManageActivity;
import com.hd.calculator.app.ui.dialog.TableOrderConflictTipsDialogBoss;
import com.hd.calculator.app.ui.dialog.TableOrderConflictTipsDialogWaiter;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.List;

public class NavigationActivity extends BaseActivity<ActivityNavigationBinding> {
    @Override
    protected ActivityNavigationBinding getViewBinding() {
        return ActivityNavigationBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        PrinterManager.getInstance().init(this);

        mBinding.btnMain.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, MainActivity.class));
        });
        mBinding.btnCalculator.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, CalculatorActivity.class));
        });
        mBinding.btnTransformTable.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ChooseTableActivity.class));
        });
        mBinding.btnNewOrder.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ChooseTableActivity.class));
        });
        mBinding.btnChooseDishes.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ChooseDishesActivity.class));
        });

        mBinding.btnOrderDetail.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, OrderDetailActivity.class));
        });
        mBinding.btnModifyDishesCount.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ModifyDishesCountActivity.class));
        });
        //boss
        mBinding.btnBossPwd.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, BossPwdActivity.class));
        });
        mBinding.btnChooseSplitTable.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ChooseTableActivity.class));
        });
        mBinding.btnSplitDishes.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, SplitTableActivity.class));
        });

        mBinding.btnPayment.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, PaymentActivity.class));
        });

        mBinding.btnPayConfirm.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, PayConfirmActivity.class));
        });
        mBinding.btnSplitPayment.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, SplitPaymentActivity.class));
        });
        mBinding.btnOrderManage.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, BillManageActivity.class));
        });
        mBinding.btnOrderCancel.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, DishesCancelActivity.class));
        });
        mBinding.btnTableManage.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, TableManageActivity.class));
        });
        mBinding.btnSearchOrder.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, BillSearchActivity.class));
        });
        mBinding.btnDailyReport.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ReportBossActivity.class));
        });
        mBinding.btnReportDetail.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ReportWaiterActivity.class));
        });
        mBinding.btnModifyPin.setOnClickListener(v -> {
            startActivity(new Intent(NavigationActivity.this, ModifyBossPwdActivity.class));
        });
        mBinding.btnPrint.setOnClickListener(v -> {
            ThreadUtils.runOnIoThreadDelayed(new Runnable() {
                @Override
                public void run() {
                    PrinterManager.getInstance().printDebugData();
                }
            });
        });
        mBinding.btnTestNetwork.setOnClickListener(v -> {
            ToastUtils.show("TestNetwork");
//            requestData();
        });
        mBinding.btnTestDatbase.setOnClickListener(v -> {
            ToastUtils.show("Test Database");
//            testDatabase();
        });
        mBinding.btnBossSolveOrderConflict.setOnClickListener(v -> {
            TableOrderConflictTipsDialogBoss dialogBoss = new TableOrderConflictTipsDialogBoss(this, 1);
            dialogBoss.setOptionClickListener(new TableOrderConflictTipsDialogBoss.OptionClickListener() {
                @Override
                public void onBossClickLocal() {
                    ToastUtils.show("Boss Click Local");
                }

                @Override
                public void onBossClickServer() {
                    ToastUtils.show("Boss Click Server");
                }
            });
            dialogBoss.show();
        });
        mBinding.btnWaiterSolveOrderConflict.setOnClickListener(v -> {
            TableOrderConflictTipsDialogWaiter dialogWaiter = new TableOrderConflictTipsDialogWaiter(this, 1);
            dialogWaiter.setOptionClickListener(new TableOrderConflictTipsDialogWaiter.OptionClickListener() {
                @Override
                public void onWaiterClickNotify() {
                    ToastUtils.show("Waiter Click Notify");
                }

                @Override
                public void onWaiterClickTransform() {
                    ToastUtils.show("Waiter Click Transform");
                }
            });
            dialogWaiter.show();
        });

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterManager.getInstance().release();
    }

    private void requestData() {
        NetworkRepository.getInstance().getAccountList(new NetworkCallback<AccountResponse>() {
            @Override
            public void onSuccess(AccountResponse data) {
                ThreadUtils.runOnIoThreadDelayed(() -> {
                    data.getData().forEach(account -> {
                        AccountEntity entity = new AccountEntity();
                        entity.setUserId(account.getId());
                        entity.setUserName(account.getUsername());
                        entity.setPassword(account.getPassword());
                        entity.setTransTablePermission(account.isMoveTable());
                        entity.setDeleteTablePermission(account.isCancelTable());
                        entity.setRestoreTablePermission(account.isRecoverTable());
                        entity.setCancelOrderPermission(account.isCancelBill());
                        entity.setReduceDishesCountPermission(account.isCancelFoodDrinks());
                        entity.setViewDailyBillPermission(account.isViewDayBill());
                        entity.setLocalModePermission(account.isLocalMode());
                        DataBaseRepository.getInstance().addAccount(entity);
                    });

                    printAllAccount();
                });
            }
        });

        NetworkRepository.getInstance().getDishesCategoryStructure(null);

        NetworkRepository.getInstance().getTaxData(null);


    }

    private void testDatabase() {
        //new AccountEntity
        ThreadUtils.runOnIoThreadDelayed(new Runnable() {
            @Override
            public void run() {
                AccountEntity accountEntity = new AccountEntity();
                accountEntity.setUserId(2);
                accountEntity.setUserName("admin");
                accountEntity.setPassword("123456");
                DataBaseRepository.getInstance().addAccount(accountEntity);

                printAllAccount();
            }
        });

    }

    private void printAllAccount() {
        ThreadUtils.runOnIoThreadDelayed(new Runnable() {
            @Override
            public void run() {
                List<AccountEntity> accountList = DataBaseRepository.getInstance().getAccountList();
                for (AccountEntity accountEntity : accountList) {
                    LogUtils.log("account = " + GsonUtils.toJson(accountEntity));
                }
            }
        });
    }
}
