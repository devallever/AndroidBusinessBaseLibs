package com.hd.calculator.app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hd.calculator.app.BuildConfig;
import com.hd.calculator.app.R;
import com.hd.calculator.app.base.BaseActivity;
import com.hd.calculator.app.business.AccountManager;
import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.business.TableManager;
import com.hd.calculator.app.business.TakeoutTableManager;
import com.hd.calculator.app.constant.DishesFirstSortType;
import com.hd.calculator.app.constant.ExtraKey;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.databinding.HdcActivityChooseDishesBinding;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.DishesSortEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.function.printer.PrintOrderDishesRequireData;
import com.hd.calculator.app.function.printer.PrinterManager;
import com.hd.calculator.app.ui.adapter.KeyboardDishesAdapter;
import com.hd.calculator.app.ui.adapter.SecondSortAdapter;
import com.hd.calculator.app.ui.adapter.SelectorDishesAdapter;
import com.hd.calculator.app.ui.adapter.ThirdSortAdapter;
import com.hd.calculator.app.ui.dialog.CommonTipsDialog;
import com.hd.calculator.app.ui.dialog.RemarkDialog;
import com.hd.calculator.app.ui.dialog.UnOrderTipsDialog;
import com.hd.calculator.app.ui.item.DishesItem;
import com.hd.calculator.app.ui.item.SecondSortItem;
import com.hd.calculator.app.ui.item.ThirdSortItem;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.KeyboardUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.StringUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 选择菜品
 * 新建订单状态和待结账界面点餐都是这个界面，做好区分
 * <p>
 * <p>
 * 这比较负责，有空整理
 */
public class ChooseDishesActivity extends BaseActivity<HdcActivityChooseDishesBinding> {

    public static final int FROM_CREATE = 0;
    public static final int FROM_APPEND = 1;
    //list
    private final List<DishesItem> mChooseDishesList = new ArrayList<>();
    //这是新增记录
    private final List<DishesItem> mNewDishesList = new ArrayList<>();
    private final List<SecondSortItem> mSecondSortList = new ArrayList<>();
    private final Map<Long, List<ThirdSortItem>> mSecondSortId2ThirdSortMap = new HashMap<>();
    private final List<ThirdSortItem> mThirdSortList = new ArrayList<>();
    private final Map<Long, List<DishesItem>> mThirdSortId2DishesMap = new HashMap<>();
    //selectorList
    private final List<DishesItem> mSelectorDishesList = new ArrayList<>();
    private final Set<String> mNewDishesCodeSet = new HashSet<>();
    private final AccountEntity mAccount = AccountManager.getIns().getAccount();
    private KeyboardDishesAdapter mAdapter;
    //firstAdapter
    private SecondSortAdapter mSecondSortAdapter;
    //secondAdapter
    private ThirdSortAdapter mThirdSortAdapter;
    //selectorAdapter
    private SelectorDishesAdapter mSelectorDishesAdapter;
    private boolean mIsSelectorMode = false;
    private int mTableCode;
    private long mOrderId;
    private int mOrderType;
    private int mFrom;
    //这是从待结账界面新增菜牌列表传进来
    private final ArrayList<DishesItem> mDishesListFromExtra = new ArrayList<>();
    private int mDishesCount;//当前选择菜品数量
    private DishesEntity mSearchResultDishes;

    public static void start(Activity context, int tableCode, long orderId, int orderType, int from, ArrayList<DishesItem> dishesItemArrayList) {
        Intent intent = new Intent(context, ChooseDishesActivity.class);
        intent.putExtra(ExtraKey.TABLE_CODE, tableCode);
        intent.putExtra(ExtraKey.ORDER_ID, orderId);
        intent.putExtra(ExtraKey.ORDER_TYPE, orderType);
        intent.putExtra(ExtraKey.CHOOSE_DISHES_FROM, from);
        intent.putExtra(ExtraKey.DISHES_ITEM_LIST, dishesItemArrayList);
        context.startActivityForResult(intent, ExtraKey.REQUEST_CODE_ADD_DISHES);
    }

    @Override
    protected HdcActivityChooseDishesBinding getViewBinding() {
        return HdcActivityChooseDishesBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView() {
        initExtraData();
        if (mOrderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            mBinding.tvTitle.setText("T-" + mTableCode);
        } else {
            mBinding.tvTitle.setText("T-A" + mTableCode);
        }
        initChooseDishes();
        initSort();
        initListener();

        updateInputTypeContent(mIsSelectorMode);
        updatePrintSwitchUI();
    }

    @Override
    protected void initData() {
        getSortListData();
        if (isFromAppend()) {
            getOrderDishesListData();
        }
    }

    private void initExtraData() {
        mTableCode = getIntent().getIntExtra(ExtraKey.TABLE_CODE, 0);
        mOrderId = getIntent().getLongExtra(ExtraKey.ORDER_ID, 0);
        mOrderType = getIntent().getIntExtra(ExtraKey.ORDER_TYPE, OrderType.ORDER_TYPE_IN_HOUSE);
        mFrom = getIntent().getIntExtra(ExtraKey.CHOOSE_DISHES_FROM, FROM_CREATE);
        ArrayList<DishesItem> dishesItemArrayList = getIntent().getParcelableArrayListExtra(ExtraKey.DISHES_ITEM_LIST);
        if (dishesItemArrayList != null) {
            mDishesListFromExtra.addAll(dishesItemArrayList);
            mNewDishesList.addAll(dishesItemArrayList);
            for (DishesItem item : mNewDishesList) {
                mNewDishesCodeSet.add(item.getCode());
            }
        }
    }

    private void initListener() {
        mBinding.ivBack.setOnClickListener(v -> {
            handleBack();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        mBinding.ivPrintTicket.setOnClickListener(v -> {
            boolean currentPrintSwitch = Config.getPrintSwitch();
            Config.setPrintSwitch(!currentPrintSwitch);
            updatePrintSwitchUI();
        });
        mBinding.ivPreviewTicket.setOnClickListener(v -> {
            PreviewTicketActivity.startActivity(ChooseDishesActivity.this, mOrderId, mOrderType, mTableCode, (ArrayList<DishesItem>) mChooseDishesList);
        });

        mBinding.ivInputType.setOnClickListener(v -> {
            updateInputTypeContent(!mIsSelectorMode);
            initSelectorSortAndDishesData();
            updateKeyboardOkAndMakeOrderButtonStyle();
        });

        mBinding.tvRemark.setOnClickListener(v -> {
            if (mSearchResultDishes != null) {
                new RemarkDialog(this, new RemarkDialog.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(String remark) {
                        addDishes(remark);
                    }
                }).show();
            }
        });

        mBinding.tvOne.setOnClickListener(v -> {
            appendCharacter("1");
        });
        mBinding.tvTwo.setOnClickListener(v -> {
            appendCharacter("2");
        });
        mBinding.tvThree.setOnClickListener(v -> {
            appendCharacter("3");
        });
        mBinding.tvFour.setOnClickListener(v -> {
            appendCharacter("4");
        });
        mBinding.tvFive.setOnClickListener(v -> {
            appendCharacter("5");
        });
        mBinding.tvSix.setOnClickListener(v -> {
            appendCharacter("6");
        });
        mBinding.tvSeven.setOnClickListener(v -> {
            appendCharacter("7");
        });
        mBinding.tvEight.setOnClickListener(v -> {
            appendCharacter("8");
        });
        mBinding.tvNight.setOnClickListener(v -> {
            appendCharacter("9");
        });
        mBinding.tvZero.setOnClickListener(v -> {
            appendCharacter("0");
        });
        mBinding.ivDelete.setOnClickListener(v -> {
            deleteLastDigit();
        });
        mBinding.tvAbc.setOnClickListener(v -> {
            showKeyboard();
        });
        mBinding.tvStar.setOnClickListener(v -> {
            appendCharacter("*");
        });
        mBinding.tvOk.setOnClickListener(v -> {
            addDishes("");
        });
        mBinding.tvMakeOrder.setOnClickListener(v -> {
            handleMakeOrder(null);
        });
        mBinding.tvSelectorMakeOrder.setOnClickListener(v -> {
            handleMakeOrder(null);
        });

        mBinding.tvInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    mBinding.tvInput.setVisibility(View.GONE);
                } else {
                    mBinding.tvInput.setVisibility(View.VISIBLE);
                }
                searchDishes();
            }
        });

    }

    private void initChooseDishes() {
        mAdapter = new KeyboardDishesAdapter(mChooseDishesList);
        mBinding.rvChooseDishes.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvChooseDishes.setAdapter(mAdapter);
        mAdapter.setOptionClickListener(new KeyboardDishesAdapter.OptionClickListener() {

            @Override
            public void onClickAdd(int position, DishesItem item) {
                if (item.isOrdered()) {
                    //效果同待结账界面的add
                    //已出单的菜品创建新的item
                    DishesItem newDishesItem = null;
                    boolean addItem = true;
                    //遍历：是否已经添加过新菜牌
                    for (DishesItem dishesItem : mNewDishesList) {
                        if (dishesItem.getCode().equals(item.getCode())) {
                            newDishesItem = dishesItem;
                            newDishesItem.setCount(newDishesItem.getCount() + 1);
                            addItem = false;
                            break;
                        }
                    }

                    //没有，copy
                    if (newDishesItem == null) {
                        newDishesItem = item.copy();
                    }

                    //判断备注是否一致
                    if (item.getRemark().isEmpty()) {
                        if (addItem) {
                            mChooseDishesList.add(0, newDishesItem);
                        }
                        mAdapter.notifyDataSetChanged();
                        updateNewDishesList();
                    } else {
                        newDishesItem.setRemark(item.getRemark());
                        DishesItem finalNewDishesItem = newDishesItem;
                        boolean finalAddItem = addItem;
                        new CommonTipsDialog(ChooseDishesActivity.this, "Neuen Artikelmit MEMO hinzufuegen?", new CommonTipsDialog.ClickListener() {
                            @Override
                            public void onClickOk(DialogInterface dialog) {
                                //因为copy时候已经设置了备注，所以这里不需要设置备注，相反，选择取消是删除备注
                                if (finalAddItem) {
                                    mChooseDishesList.add(0, finalNewDishesItem);
                                }
                                mAdapter.notifyDataSetChanged();
                                updateNewDishesList();
                                dialog.dismiss();
                            }

                            @Override
                            public void onClickCancel(DialogInterface dialog) {
                                finalNewDishesItem.setRemark("");
                                if (finalAddItem) {
                                    mChooseDishesList.add(0, finalNewDishesItem);
                                }
                                mAdapter.notifyDataSetChanged();
                                updateNewDishesList();
                                dialog.dismiss();
                            }
                        }).show();
                    }

                } else {
                    int count = item.getCount();
                    item.setCount(count + 1);
                    int index = mChooseDishesList.indexOf(item);
                    mAdapter.notifyItemChanged(index, index);
                }

                updateNewDishesList();
                updateKeyboardOkAndMakeOrderButtonStyle();
            }

            @Override
            public void onClickItem(int position, DishesItem item) {
                if (item.isOrdered()) {
                    return;
                }
                int count = item.getCount();
                item.setCount(count + 1);
                int index = mChooseDishesList.indexOf(item);
                mAdapter.notifyItemChanged(index, index);
                updateNewDishesList();
                updateKeyboardOkAndMakeOrderButtonStyle();
            }

            @Override
            public void onClickDel(int position, DishesItem item) {
                int count = item.getCount();
                int newCount = count - 1;
                int index = mChooseDishesList.indexOf(item);
                if (newCount <= 0) {
                    //remove
                    mChooseDishesList.remove(index);
                    mNewDishesCodeSet.remove(item.getCode());
                    mSearchResultDishes = null;
                    mAdapter.notifyDataSetChanged();
                    updateEmptyStyleConfirm();
                } else {
                    item.setCount(newCount);
                    mAdapter.notifyItemChanged(index, index);
                }
                updateNewDishesList();
                updateKeyboardOkAndMakeOrderButtonStyle();
            }
        });
    }

    private void initSort() {
        mBinding.rvSecondSort.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mSecondSortAdapter = new SecondSortAdapter(mSecondSortList);
        mBinding.rvSecondSort.setAdapter(mSecondSortAdapter);
        mSecondSortAdapter.setItemClickListener(secondSortItem -> {
            for (SecondSortItem item : mSecondSortList) {
                item.setSelect(item.hashCode() == secondSortItem.hashCode());
            }
            mSecondSortAdapter.notifyDataSetChanged();

            mThirdSortList.clear();
            mSelectorDishesList.clear();

            mThirdSortList.addAll(mSecondSortId2ThirdSortMap.get(secondSortItem.getId()));
            if (!mThirdSortList.isEmpty()) {
                for (ThirdSortItem thirdSortItem : mThirdSortList) {
                    thirdSortItem.setSelect(false);
                }
                mThirdSortList.get(0).setSelect(true);

                mSelectorDishesList.addAll(mThirdSortId2DishesMap.get(mThirdSortList.get(0).getId()));
            }

            mThirdSortAdapter.notifyDataSetChanged();
            mSelectorDishesAdapter.notifyDataSetChanged();

        });

        //third sort
        mBinding.rvThirdSort.setLayoutManager(new LinearLayoutManager(this));
        mThirdSortAdapter = new ThirdSortAdapter(mThirdSortList);
        mBinding.rvThirdSort.setAdapter(mThirdSortAdapter);
        mThirdSortAdapter.setItemClickListener(thirdSortItem -> {
            for (ThirdSortItem item : mThirdSortList) {
                item.setSelect(item.hashCode() == thirdSortItem.hashCode());
            }
            mThirdSortAdapter.notifyDataSetChanged();

            mSelectorDishesList.clear();
            mSelectorDishesList.addAll(mThirdSortId2DishesMap.get(thirdSortItem.getId()));
            mSelectorDishesAdapter.notifyDataSetChanged();

        });

        //selector
        mBinding.rvDishes.setLayoutManager(new LinearLayoutManager(this));
        mSelectorDishesAdapter = new SelectorDishesAdapter(mSelectorDishesList);
        mBinding.rvDishes.setAdapter(mSelectorDishesAdapter);
        mSelectorDishesAdapter.setOptionClickListener(item -> {
            String code = item.getCode();
            int count = item.getCount();
            DishesItem inListItem = null;
            for (DishesItem dishesItem : mChooseDishesList) {
                if (dishesItem.getCode().equals(code)) {
                    inListItem = dishesItem;
                    break;
                }
            }
            if (inListItem == null) {
                if (count > 0) {
                    if (isFromAppend()) {
                        mChooseDishesList.add(0, item);
                    } else {
                        mChooseDishesList.add(item);
                    }
                }
            } else {
                inListItem.setCount(count);
                if (count == 0) {
                    mChooseDishesList.remove(inListItem);
                }
            }
            updateNewDishesList();
            mAdapter.notifyDataSetChanged();

            //更新键盘ok&出单按钮
            updateKeyboardOkAndMakeOrderButtonStyle();
        });
    }

    private boolean isFromAppend() {
        return mFrom == FROM_APPEND;
    }

    private void getOrderDishesListData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
            if (orderWithDishesRef == null) {
                //找不到就是全部付款，后删除订单找不到了
                finish();
                return;
            }
            mChooseDishesList.clear();
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
                mChooseDishesList.add(dishesItem);
            }

            //添加新菜
            if (!mDishesListFromExtra.isEmpty()) {
                int count = mDishesListFromExtra.size();
                //for i = count - 1
                for (int i = count - 1; i >= 0; i--) {
                    mChooseDishesList.add(0, mDishesListFromExtra.get(i));
                }
            }

            runOnUiThread(() -> {
                mAdapter.notifyDataSetChanged();
                updateKeyboardOkAndMakeOrderButtonStyle();
            });
        });
    }

    private void getSortListData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            Set<String> orderedDishesCodeSet = new HashSet<>();
            if (isFromAppend()) {
                //获取已出单菜单的code，不再添加
                OrderWithDishesRef orderWithDishesRef = DataBaseRepository.getInstance().getOrderById(mOrderId);
                for (OrderDishesRecordEntity dishesRecord : orderWithDishesRef.getDishesList()) {
                    orderedDishesCodeSet.add(dishesRecord.getDishesCode());
                }
            }
            //获取分类
            List<DishesSortEntity> firstLevelSortList = DataBaseRepository.getInstance().getFirstLevelSortList();
            List<DishesSortEntity> allSecondLevelSortList = new ArrayList<>();
            for (DishesSortEntity firstSort : firstLevelSortList) {
//                LogUtils.log("db firstSort = " + GsonUtils.toJson(firstSort));
                List<DishesSortEntity> secondLevelSortList = DataBaseRepository.getInstance().getSecondLevelSortListByFirstId(firstSort.getSortId());
                allSecondLevelSortList.addAll(secondLevelSortList);
            }

            mSecondSortList.clear();
            for (DishesSortEntity secondSort : allSecondLevelSortList) {
//                LogUtils.log("db secondSort = " + GsonUtils.toJson(secondSort));
                SecondSortItem secondSortItem = new SecondSortItem();
                secondSortItem.setName(secondSort.getName());
                secondSortItem.setId(secondSort.getSortId());
                secondSortItem.setSelect(false);
                mSecondSortList.add(secondSortItem);

                List<DishesSortEntity> thirdLevelSortList = DataBaseRepository.getInstance().getThirdLevelSortListBySecondId(secondSort.getSortId());
                mSecondSortId2ThirdSortMap.put(secondSort.getSortId(), new ArrayList<>());
                for (DishesSortEntity thirdSort : thirdLevelSortList) {
//                    LogUtils.log("db thirdSort = " + GsonUtils.toJson(thirdSort));
                    ThirdSortItem thirdSortItem = new ThirdSortItem();
                    thirdSortItem.setId(thirdSort.getSortId());
                    thirdSortItem.setName(thirdSort.getName());
                    thirdSortItem.setSelect(false);
                    mSecondSortId2ThirdSortMap.get(secondSort.getSortId()).add(thirdSortItem);


                    mThirdSortId2DishesMap.put(thirdSort.getSortId(), new ArrayList<>());
                    List<DishesEntity> dishesList = DataBaseRepository.getInstance().getDishesListByThirdId(thirdSort.getSortId());
                    for (DishesEntity dishesEntity : dishesList) {
                        DishesItem dishesItem = new DishesItem();
                        dishesItem.setCode(dishesEntity.getCode());
                        dishesItem.setName(dishesEntity.getName());
                        dishesItem.setPrice(dishesEntity.getPrice());
                        dishesItem.setFirstSortType( dishesEntity.getFirstSortType());
                        dishesItem.setEnablePrint(dishesEntity.isEnablePrint());
                        dishesItem.setCount(0);
                        dishesItem.setOrdered(false);
                        dishesItem.setRemark("");
                        if (!orderedDishesCodeSet.contains(dishesEntity.getCode())) {
                            mThirdSortId2DishesMap.get(thirdSort.getSortId()).add(dishesItem);
                        }
                    }
                }
            }

            initSelectorSortAndDishesData();

        });
    }

    private void initSelectorSortAndDishesData() {
        if (!mSecondSortList.isEmpty()) {
            //默认选中第一个
            for (SecondSortItem secondSortItem : mSecondSortList) {
                secondSortItem.setSelect(false);
            }
            mSecondSortList.get(0).setSelect(true);

            ThirdSortItem thirdSortItem = mSecondSortId2ThirdSortMap.get(mSecondSortList.get(0).getId()).get(0);
            thirdSortItem.setSelect(true);
            mThirdSortList.clear();
            mThirdSortList.addAll(mSecondSortId2ThirdSortMap.get(mSecondSortList.get(0).getId()));
            if (!mThirdSortList.isEmpty()) {
                for (ThirdSortItem sortItem : mThirdSortList) {
                    sortItem.setSelect(false);
                }
                mThirdSortList.get(0).setSelect(true);
            }

            mSelectorDishesList.clear();
            mSelectorDishesList.addAll(mThirdSortId2DishesMap.get(thirdSortItem.getId()));
        }

        runOnUiThread(() -> {
            mSecondSortAdapter.notifyDataSetChanged();
            mThirdSortAdapter.notifyDataSetChanged();

            updateSelectedSelectorDishesList();
        });
    }

    private void updateSelectedSelectorDishesList() {
        Map<String, Integer> dishesCode2CountMap = new HashMap<>();
        Set<String> dishesCodeSet = new HashSet<>();
        for (DishesItem dishesItem : mChooseDishesList) {
            dishesCode2CountMap.put(dishesItem.getCode(), dishesItem.getCount());
            dishesCodeSet.add(dishesItem.getCode());
        }

        mThirdSortId2DishesMap.forEach((thirdSortId, dishesList) -> {
            for (DishesItem dishesItem : dishesList) {
                if (dishesCodeSet.contains(dishesItem.getCode())) {
                    dishesItem.setCount(dishesCode2CountMap.get(dishesItem.getCode()));
                } else {
                    dishesItem.setCount(0);
                }
            }
        });

        mSelectorDishesAdapter.notifyDataSetChanged();
    }


    private void handleBack() {
        if (mOrderType == OrderType.ORDER_TYPE_TAKE_OUT) {
            TakeoutTableManager.getIns().reduceCount();
            finish();
        } else if (mFrom == FROM_APPEND) {
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
        } else {
            finish();
        }

    }

    private void handleMakeOrder(Boolean forcePrint) {
        switch (mFrom) {
            case FROM_CREATE:
                handleMakeOrderFromCreate();
                break;
            case FROM_APPEND:
                handleMakeOrderFromAppend(forcePrint);
                break;
        }
    }

    private void handleClickBack(Boolean forcePrint) {
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

    private void handleMakeOrderFromAppend(Boolean forcePrint) {
        if (mNewDishesList.isEmpty()) {
            return;
        }

        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<PrintOrderDishesRequireData> printOrderDishesRequireDataList = new ArrayList<>();
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
                    //remark
                    orderDishesRecordEntity.setRemark(dishesItem.getRemark());
                    orderDishesRecordEntity.setCount(dishesItem.getCount());
                    DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
                } else {
                    int newCount = orderDishesRecordEntity.getCount() + dishesItem.getCount();
                    orderDishesRecordEntity.setCount(newCount);
                    DataBaseRepository.getInstance().updateOrderDishes(orderDishesRecordEntity);
                }


                if (dishesItem.isEnablePrint()) {
                    printOrderDishesRequireDataList.add(new PrintOrderDishesRequireData(dishesItem.getCount(), dishesItem.getCode(), dishesItem.getName(), dishesItem.getRemark()));
                } else {
                    LogUtils.log("不打印：" + dishesItem.getName() + " " + dishesItem.getCode());
                }
            });

            TableManager.getIns().postOrderRecord(mTableCode,  ActionType.MAKE_ORDER, false, null);

            if (BuildConfig.DEBUG) {
                printAllOrderRecord();
            }
            runOnUiThread(() -> {
                Intent intent = new Intent();
                intent.putExtra(ExtraKey.RESULT_TABLE_CODE, mTableCode);
                setResult(RESULT_OK, intent);
                finish();
            });

            if (forcePrint == null) {
                if (Config.getPrintSwitch()) {
                    ThreadUtils.runOnIoThreadDelayed(() -> {
                        PrinterManager.getInstance().printOrder(mOrderId, printOrderDishesRequireDataList);
                    });
                }
            } else {
                if (forcePrint) {
                    ThreadUtils.runOnIoThreadDelayed(() -> {
                        PrinterManager.getInstance().printOrder(mOrderId, printOrderDishesRequireDataList);
                    });
                }
            }

        });
    }

    private void handleMakeOrderFromCreate() {
        if (mChooseDishesList.isEmpty()) {
            return;
        }

        ThreadUtils.runOnIoThreadDelayed(() -> {
            OrderRecordEntity orderRecordEntity = new OrderRecordEntity();
            orderRecordEntity.setTableCode(mTableCode);
            orderRecordEntity.setOrderUserId(mAccount.getUserId());
            orderRecordEntity.setOrderType(mOrderType);
            orderRecordEntity.setCreateTime(System.currentTimeMillis());
            DataBaseRepository.getInstance().addOrderRecord(orderRecordEntity);
            long orderId = DataBaseRepository.getInstance().getLastOrderRecord().getId();
            List<PrintOrderDishesRequireData> printOrderDishesRequireDataList = new ArrayList<>();
            mChooseDishesList.forEach(dishesItem -> {
                OrderDishesRecordEntity orderDishesRecordEntity = new OrderDishesRecordEntity();
                orderDishesRecordEntity.setOrderId(orderId);
                orderDishesRecordEntity.setDishesCode(dishesItem.getCode());
                //remark
                orderDishesRecordEntity.setRemark(dishesItem.getRemark());
                orderDishesRecordEntity.setCount(dishesItem.getCount());
                DataBaseRepository.getInstance().addOrderDishesRecord(orderDishesRecordEntity);
                if (dishesItem.isEnablePrint()) {
                    PrintOrderDishesRequireData printOrderDishesRequireData = new PrintOrderDishesRequireData(dishesItem.getCount(), dishesItem.getCode(), dishesItem.getName(), dishesItem.getRemark());
                    printOrderDishesRequireDataList.add(printOrderDishesRequireData);
                } else {
                    LogUtils.log("不打印：" + dishesItem.getName() + " " + dishesItem.getCode());
                }

            });

            TableManager.getIns().postOrderRecord(orderRecordEntity.getTableCode(),  ActionType.MAKE_ORDER, false, null);

            if (BuildConfig.DEBUG) {
                printAllOrderRecord();
            }
            finish();

            if (Config.getPrintSwitch()) {
                PrinterManager.getInstance().printOrder(orderId, printOrderDishesRequireDataList);
            }

        });
    }

    private void showKeyboard() {
        //弹出键盘
//        KeyboardUtils.showKeyboard(this, mBinding.tvInput, 0);
        KeyboardUtils.openKeyboard(mBinding.tvInput, this);
    }

    private void searchDishes() {
        updateEmptyStyleConfirm();

        String input = mBinding.tvInput.getText().toString();
        if (input.startsWith("*")) {
            return;
        }

        ThreadUtils.runOnIoThreadDelayed(() -> {
            String dishesCode;
            if (input.contains("*")) {
                String[] split = input.split("\\*");
                if (split.length == 1) {
                    return;
                }
                dishesCode = split[1];
            } else {
                dishesCode = input;
            }

            mSearchResultDishes = DataBaseRepository.getInstance().getDishesByCode(dishesCode);
            runOnUiThread(() -> {
                if (mSearchResultDishes != null) {
                    mBinding.tvDishesName.setText(mSearchResultDishes.getName());
                    mBinding.tvDishesName.setVisibility(View.VISIBLE);
                    mBinding.tvInputTips.setVisibility(View.GONE);
                    mBinding.tvRemark.setBackgroundResource(R.drawable.hdc_shape_keyboard_remark_enable_bg);
                    mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_enable);
                } else {
                    mBinding.tvDishesName.setVisibility(View.GONE);
                    mBinding.tvInputTips.setVisibility(View.VISIBLE);
                    mBinding.tvRemark.setBackgroundResource(R.drawable.hdc_shape_keyboard_remark_disable_bg);
                    mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_disable);
                }
            });
        });
    }

    //如果是新增菜，从列表上面开始添加
    private void addDishes(String remark) {
        if (mSearchResultDishes == null) {
            return;
        }
        String input = mBinding.tvInput.getText().toString();
        mDishesCount = 1;
        if (input.contains("*")) {
            String[] split = input.split("\\*");
            boolean isInteger = StringUtils.canConvertToInt(split[0]);
            if (!isInteger) {
                ToastUtils.show("count must be number");
                return;
            }
            mDishesCount = Integer.parseInt(split[0]);
        }

        DishesItem item = new DishesItem();
        item.setCode(mSearchResultDishes.getCode());
        item.setName(mSearchResultDishes.getName());
        item.setPrice(mSearchResultDishes.getPrice());
        item.setFirstSortType(mSearchResultDishes.getFirstSortType());
        item.setEnablePrint(mSearchResultDishes.isEnablePrint());
        item.setRemark(remark);
        item.setCount(mDishesCount);
        //判断添加还是合并
        if (mNewDishesCodeSet.contains(mSearchResultDishes.getCode())) {
            //合并
            mChooseDishesList.forEach(dishesItem -> {
                if (!dishesItem.isOrdered() && dishesItem.getCode().equals(mSearchResultDishes.getCode())) {
                    int index = mChooseDishesList.indexOf(dishesItem);
                    int newCount = dishesItem.getCount() + mDishesCount;
                    dishesItem.setCount(newCount);
                    mAdapter.notifyItemChanged(index, index);
                }
            });
        } else {
            //添加
            if (isFromAppend()) {
                mChooseDishesList.add(0, item);
            } else {
                mChooseDishesList.add(item);
            }
            mAdapter.notifyDataSetChanged();
            mNewDishesCodeSet.add(item.getCode());
        }

        updateNewDishesList();

        mSearchResultDishes = null;
        mBinding.tvInput.setText("");
        mBinding.tvInput.setVisibility(View.GONE);
        mBinding.tvInputTips.setVisibility(View.VISIBLE);
        mBinding.tvDishesName.setVisibility(View.GONE);
        mBinding.tvRemark.setBackgroundResource(R.drawable.hdc_shape_keyboard_remark_disable_bg);
        mBinding.tvOk.setVisibility(View.GONE);
        mBinding.tvMakeOrder.setVisibility(View.VISIBLE);
    }

    private void appendCharacter(String character) {
        String current = mBinding.tvInput.getText().toString();
        // 如果当前是初始的"0"，替换为输入的字符
        if (current.equals("0")) {
            mBinding.tvInput.setText(character);
        } else {
            if (!current.contains("*")) {
                mBinding.tvInput.append(character);
            } else {
                if (!"*".equals(character)) {
                    mBinding.tvInput.append(character);
                }
            }
        }

        mBinding.tvInput.setVisibility(View.VISIBLE);

        searchDishes();
    }

    private void deleteLastDigit() {
        String current = mBinding.tvInput.getText().toString();

        if (!current.isEmpty()) {
            // 如果只剩1个字符，删除后设置为0
            if (current.length() == 1) {
                mBinding.tvInput.setText("");
            } else {
                mBinding.tvInput.setText(current.substring(0, current.length() - 1));
            }
        }

        if (mBinding.tvInput.getText().toString().isEmpty()) {
            mBinding.tvInput.setVisibility(View.GONE);
            mBinding.tvDishesName.setVisibility(View.GONE);
            mBinding.tvInputTips.setVisibility(View.VISIBLE);
        } else {
            mBinding.tvInput.setVisibility(View.VISIBLE);
            mBinding.tvDishesName.setVisibility(View.GONE);
            mBinding.tvInputTips.setVisibility(View.VISIBLE);
        }

        searchDishes();
        updateKeyboardOkAndMakeOrderButtonStyle();
    }

    private void updateNewDishesList() {
        mNewDishesList.clear();
        for (DishesItem dishesItem : mChooseDishesList) {
            if (!dishesItem.isOrdered()) {
                mNewDishesList.add(dishesItem);
            }
        }
    }

    private void updateInputTypeContent(boolean isSelectorMode) {
        mIsSelectorMode = isSelectorMode;
        if (mIsSelectorMode) {
            mBinding.selectorInputContainer.setVisibility(View.VISIBLE);
            mBinding.keyboardInputContainer.setVisibility(View.GONE);
            mBinding.ivInputType.setImageResource(R.drawable.hdc_ic_choose_dishes_menu_input_type_keyboard);
        } else {
            mBinding.selectorInputContainer.setVisibility(View.GONE);
            mBinding.keyboardInputContainer.setVisibility(View.VISIBLE);
            mBinding.ivInputType.setImageResource(R.drawable.hdc_ic_choose_dishes_menu_input_type_selector);
        }
    }

    private void updateEmptyStyleConfirm() {
        mBinding.tvOk.setVisibility(View.VISIBLE);
        mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_disable);
        mBinding.tvMakeOrder.setVisibility(View.GONE);
    }

    private void updatePrintSwitchUI() {
        if (Config.getPrintSwitch()) {
            mBinding.ivPrintTicket.setImageResource(R.drawable.hdc_ic_choose_dishes_menu_print_ticket_enable);
        } else {
            mBinding.ivPrintTicket.setImageResource(R.drawable.hdc_ic_choose_dishes_menu_print_ticket_disable);
        }
    }

    private void updateKeyboardOkAndMakeOrderButtonStyleFromAppend() {
        //如过是从追加，判断mNewDishesList不为空就能出单
        if (!isFromAppend()) {
        }


    }

    /**
     * 首次点餐有效
     */
    private void updateKeyboardOkAndMakeOrderButtonStyle() {
        List<DishesItem> target;
        if (isFromAppend()) {
            target = mNewDishesList;
        } else {
            target = mChooseDishesList;
        }
        if (target.isEmpty()) {
            if (mSearchResultDishes == null) {
                mBinding.tvMakeOrder.setVisibility(View.GONE);
                mBinding.tvOk.setVisibility(View.VISIBLE);
                mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_disable);
                mBinding.tvSelectorMakeOrder.setBackgroundResource(R.drawable.hdc_shape_gray_r45);
            } else {
                //首次搜索出菜牌
                mBinding.tvMakeOrder.setVisibility(View.GONE);
                mBinding.tvOk.setVisibility(View.VISIBLE);
                mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_enable);
                mBinding.tvSelectorMakeOrder.setBackgroundResource(R.drawable.hdc_shape_gray_r45);
            }

        } else {
            mBinding.tvSelectorMakeOrder.setBackgroundResource(R.drawable.hdc_shape_blue_r45);

            //输入中
            String input = mBinding.tvInput.getText().toString();
            if (input.isEmpty()) {
                //出单
                mBinding.tvMakeOrder.setVisibility(View.VISIBLE);
                mBinding.tvOk.setVisibility(View.GONE);
            } else {
                if (mSearchResultDishes == null) {
                    //搜不到//ok 灰色
                    mBinding.tvOk.setVisibility(View.VISIBLE);
                    mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_disable);
                } else {
                    //搜到，ok 绿色
                    mBinding.tvOk.setVisibility(View.VISIBLE);
                    mBinding.tvOk.setBackgroundResource(R.drawable.hdc_shape_keyboard_ok_bg_enable);
                }
            }
        }

        //处理isFromAppend
        if (isFromAppend()) {
            if (mNewDishesList.isEmpty()) {
                return;
            }

            if (mSearchResultDishes == null) {
                //显示出单
                mBinding.tvOk.setVisibility(View.GONE);
                mBinding.tvMakeOrder.setVisibility(View.VISIBLE);
                mBinding.tvSelectorMakeOrder.setVisibility(View.VISIBLE);
            }
        }
    }

    private void initTestData() {
        //for
//        for (int i = 1; i <= 2; i++) {
//            DishesItem item = new DishesItem();
//            item.setCode(i + "");
//            item.setName("Dishes" + i);
//            item.setPrice(i * 10);
//            item.setCount(i);
////            item.setRemarkList(new ArrayList<>());
//            if (i == 1) {
//                item.getRemarkList().add("Remark" + "1");
//                item.getRemarkList().add("Remark" + "2");
//            }
//            mChooseDishesList.add(item);
//        }
//        runOnUiThread(() -> {
//            mAdapter.notifyDataSetChanged();
//        });

        //for
        for (int i = 0; i < 10; i++) {
            SecondSortItem item = new SecondSortItem();
            item.setName("Sort" + i);
            item.setId(i);
            item.setSelect(i == 0);
            mSecondSortList.add(item);
        }
        runOnUiThread(() -> {
            mSecondSortAdapter.notifyDataSetChanged();
        });

        //second
        for (int i = 0; i < 15; i++) {
            ThirdSortItem item = new ThirdSortItem();
            item.setName("Sort" + i);
            item.setId(i);
            item.setSelect(i == 0);
            mThirdSortList.add(item);
        }
        runOnUiThread(() -> {
            mThirdSortAdapter.notifyDataSetChanged();
        });

        //selector data
        for (int i = 1; i < 15; i++) {
            DishesItem item = new DishesItem();
            item.setName("Dishes" + i);
            item.setCode(i + "");
            item.setPrice(i);
            item.setFirstSortType( DishesFirstSortType.FOOD);
            item.setEnablePrint(true);
            item.setCount(i);
            mSelectorDishesList.add(item);
        }
        runOnUiThread(() -> {
            mSelectorDishesAdapter.notifyDataSetChanged();
        });
    }

    private void printAllOrderRecord() {
        //for
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<OrderWithDishesRef> orderWithDishesRefs = DataBaseRepository.getInstance().getAllOrderRecord();
            for (OrderWithDishesRef orderWithDishesRef : orderWithDishesRefs) {
                LogUtils.log("db order = " + GsonUtils.toJson(orderWithDishesRef));
            }
        });
    }
}
