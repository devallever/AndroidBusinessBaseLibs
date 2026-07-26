package com.hd.calculator.app.function.sync;

import com.hd.calculator.app.BuildConfig;
import com.hd.calculator.app.business.TaxManager;
import com.hd.calculator.app.constant.DishesSortLevelType;
import com.hd.calculator.app.constant.TaxType;
import com.hd.calculator.app.constant.ZoneType;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.DishesSortEntity;
import com.hd.calculator.app.function.db.entity.TableEntity;
import com.hd.calculator.app.function.db.entity.TaxEntity;
import com.hd.calculator.app.function.network.NetworkRepository;
import com.hd.calculator.app.function.network.response.RateData;
import com.hd.calculator.app.function.network.response.TableData;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataSyncManager {
    public static DataSyncManager getInstance() {
        return DataSyncManagerHolder.instance;
    }

    public void syncData() {
        fetchAccountData(null);
        fetchTableData();
        fetchTaxData();
        fetchDishesData();
    }

    public void fetchAccountData(Runnable finishTask) {
        NetworkRepository.getInstance().getAccountList(data -> {
            if (data.getData().isEmpty()) {
                return;
            }
            Set<Long> onLineUserIdSet = new HashSet<>();
            ThreadUtils.runOnIoThreadDelayed(() -> {
                AccountEntity bossAccount = DataBaseRepository.getInstance().getBossAccount();
//                DataBaseRepository.getInstance().deleteWaiterAccount();
                data.getData().forEach(account -> {
                    onLineUserIdSet.add(account.getId());
                    AccountEntity entity = DataBaseRepository.getInstance().getAccountByUserId(account.getId());
                    boolean update = true;
                    if (entity == null) {
                        entity = new AccountEntity();
                        update = false;
                    }
                    entity.setUserId(account.getId());
                    entity.setUserName(account.getUsername());
                    if (!account.isBoss()) {
                        entity.setPassword(account.getPassword());
                    }
                    entity.setTransTablePermission(account.isMoveTable());
                    entity.setDeleteTablePermission(account.isCancelTable());
                    entity.setRestoreTablePermission(account.isRecoverTable());
                    entity.setCancelOrderPermission(account.isCancelBill());
                    entity.setReduceDishesCountPermission(account.isCancelFoodDrinks());
                    entity.setViewDailyBillPermission(account.isViewDayBill());
                    entity.setLocalModePermission(account.isLocalMode());
                    entity.setBoss(account.isBoss());
                    if (account.isBoss()) {
                        if (bossAccount == null) {
                            entity.setPassword(account.getPassword());
                            DataBaseRepository.getInstance().addAccount(entity);
                        }
                    } else {
                        if (update) {
                            DataBaseRepository.getInstance().updateAccount(entity);
                        } else {
                            DataBaseRepository.getInstance().addAccount(entity);
                        }
                    }
                });

                for (AccountEntity accountEntity : DataBaseRepository.getInstance().getAccountList()) {
                    if (!onLineUserIdSet.contains(accountEntity.getUserId())) {
                        DataBaseRepository.getInstance().deleteAccount(accountEntity.getUserId());
                    }
                }

                if (finishTask != null) {
                    finishTask.run();
                }

                if (BuildConfig.DEBUG) {
                    printAllAccount();
                }
            });
        });
    }

    private void fetchTableData() {
        int zone1 = ZoneType.ZONE_TYPE_ZONE_1;
        NetworkRepository.getInstance().getTableList(zone1, data -> {
            if (data.getData().isEmpty()) {
                return;
            }
            ThreadUtils.runOnIoThreadDelayed(() -> {
                DataBaseRepository.getInstance().deleteTableByZone(zone1);
                handleAddTableData(data.getData(), zone1);
            });
        });

        int zone2 = ZoneType.ZONE_TYPE_ZONE_2;
        NetworkRepository.getInstance().getTableList(zone2, data -> {
            if (data.getData().isEmpty()) {
                return;
            }
            ThreadUtils.runOnIoThreadDelayed(() -> {
                DataBaseRepository.getInstance().deleteTableByZone(zone2);
                handleAddTableData(data.getData(), zone2);
            });
        });
    }

    private void handleAddTableData(List<TableData> data, int zone) {
        data.forEach(table -> {
            TableEntity tableEntity = new TableEntity();
            tableEntity.setTableId( table.getId());
            tableEntity.setCode(table.getTableNo());
            tableEntity.setName(table.getTableName());
            tableEntity.setZone(zone);
            ThreadUtils.runSingleThread(() -> {
                DataBaseRepository.getInstance().addTable(tableEntity);
            });
        });
    }

    private void fetchDishesData() {
        NetworkRepository.getInstance().getDishesCategoryStructure(data -> {
            if (data.getData().isEmpty()) {
//                LogUtils.log("getDishesCategoryStructure error");
                return;
            }
            ThreadUtils.runOnIoThreadDelayed(() -> {
                //deleteDishes
                DataBaseRepository.getInstance().deleteAllDishes();
                DataBaseRepository.getInstance().deleteAllDishesSort();
                data.getData().forEach(firstSort -> {
//                    LogUtils.log("firstSort = " + GsonUtils.toJson(firstSort));
                    //firstSort
                    DishesSortEntity firstSortEntity = new DishesSortEntity();
                    firstSortEntity.setSortId(firstSort.getId());
                    //parent
                    firstSortEntity.setParentSortId(0);
                    firstSortEntity.setName(firstSort.getName());
                    firstSortEntity.setLevel(DishesSortLevelType.DISHES_SORT_LEVEL_ONE);
                    DataBaseRepository.getInstance().addDishesSort(firstSortEntity);

                    //second sort
                    firstSort.getChildren().forEach(secondSort -> {
//                        LogUtils.log("secondSort = " + GsonUtils.toJson(secondSort));
                        DishesSortEntity secondSortEntity = new DishesSortEntity();
                        secondSortEntity.setSortId(secondSort.getId());
                        //parentSortId
                        secondSortEntity.setParentSortId(firstSort.getId());
                        secondSortEntity.setName(secondSort.getName());
                        secondSortEntity.setLevel(DishesSortLevelType.DISHES_SORT_LEVEL_TWO);
                        DataBaseRepository.getInstance().addDishesSort(secondSortEntity);
                        //third sort
                        secondSort.getChildren().forEach(thirdSort -> {
//                            LogUtils.log("thirdSort = " + GsonUtils.toJson(thirdSort));
                            DishesSortEntity thirdSortEntity = new DishesSortEntity();
                            thirdSortEntity.setSortId(thirdSort.getId());
                            thirdSortEntity.setParentSortId(secondSort.getId());
                            thirdSortEntity.setName(thirdSort.getName());
                            thirdSortEntity.setLevel(DishesSortLevelType.DISHES_SORT_LEVEL_THREE);
                            DataBaseRepository.getInstance().addDishesSort(thirdSortEntity);
                            //dishes
                            thirdSort.getDishesList().forEach(dishes -> {
//                                LogUtils.log("dishes = " + GsonUtils.toJson(dishes));
                                DishesEntity dishesEntity = new DishesEntity();
                                dishesEntity.setDishesId(dishes.getId());
                                dishesEntity.setCode(dishes.getPlu());
                                dishesEntity.setBuffetType(dishes.getBuffetType());
                                dishesEntity.setName(dishes.getItemDe());
                                dishesEntity.setPrice(dishes.getPrice());
                                dishesEntity.setFirstId(firstSort.getId());
                                dishesEntity.setSecondId(secondSort.getId());
                                dishesEntity.setThirdId(thirdSort.getId());
                                dishesEntity.setTaxInHouse(dishes.getTaxInhouse());
                                dishesEntity.setTaxTakeout(dishes.getTaxTakeout());
                                dishesEntity.setEnablePrint(dishes.isEnablePrint());
                                dishesEntity.setFirstSortType(firstSort.getId());
                                DataBaseRepository.getInstance().addDishes(dishesEntity);
                            });
                        });

                    });
                });

                if (BuildConfig.DEBUG) {
                    printAllDishesSort();
                    printAllDishes();
                }
            });

        });
    }

    private void fetchTaxData() {
        NetworkRepository.getInstance().getTaxData(data -> {
            ThreadUtils.runOnIoThreadDelayed(() -> {
                //deleteALLtAX
                DataBaseRepository.getInstance().deleteAllTax();

                RateData drinksInhausTax = data.getData().getDrinksInhausTax();
                TaxEntity drinksInhausTaxEntity = new TaxEntity();
                drinksInhausTaxEntity.setRate(drinksInhausTax.getRate());
                drinksInhausTaxEntity.setSign(drinksInhausTax.getSign());
                drinksInhausTaxEntity.setType(TaxType.DRINK_IN_HOUSE_RATE);
                DataBaseRepository.getInstance().addTax(drinksInhausTaxEntity);

                RateData drinksTakeoutTax = data.getData().getDrinksTakeoutTax();
                TaxEntity drinksTakeoutTaxEntity = new TaxEntity();
                drinksTakeoutTaxEntity.setRate(drinksTakeoutTax.getRate());
                drinksTakeoutTaxEntity.setSign(drinksTakeoutTax.getSign());
                drinksTakeoutTaxEntity.setType(TaxType.DRINK_TAKEOUT_RATE);
                DataBaseRepository.getInstance().addTax(drinksTakeoutTaxEntity);

                RateData foodInhausTax = data.getData().getFoodInhausTax();
                TaxEntity foodInhausTaxEntity = new TaxEntity();
                foodInhausTaxEntity.setRate(foodInhausTax.getRate());
                foodInhausTaxEntity.setSign(foodInhausTax.getSign());
                foodInhausTaxEntity.setType(TaxType.FOOD_IN_HOUSE_RATE);
                DataBaseRepository.getInstance().addTax(foodInhausTaxEntity);

                //foodTakeoutTax
                RateData foodTakeoutTax = data.getData().getFoodTakeoutTax();
                TaxEntity foodTakeoutTaxEntity = new TaxEntity();
                foodTakeoutTaxEntity.setRate(foodTakeoutTax.getRate());
                foodTakeoutTaxEntity.setSign(foodTakeoutTax.getSign());
                foodTakeoutTaxEntity.setType(TaxType.FOOD_TAKEOUT_RATE);
                DataBaseRepository.getInstance().addTax(foodTakeoutTaxEntity);

                TaxManager.getIns().updateTaxData();

                if (BuildConfig.DEBUG) {
                    printAllTax();
                }

            });
        });
    }

    private void printAllAccount() {
//        ThreadUtils.runOnIoThreadDelayed(() -> {
//            List<AccountEntity> accountList = DataBaseRepository.getInstance().getAccountList();
//            for (AccountEntity accountEntity : accountList) {
//                LogUtils.log("db account = " + GsonUtils.toJson(accountEntity));
//            }
//        });
    }

    private void printAllDishesSort() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
//            List<DishesSortEntity> dishesSortList = DataBaseRepository.getInstance().getDishesSortList();
//            for (DishesSortEntity dishesSortEntity : dishesSortList) {
//                LogUtils.log("db dishesSort = " + GsonUtils.toJson(dishesSortEntity));
//            }
        });
    }

    private void printAllDishes() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
//            List<DishesEntity> dishesList = DataBaseRepository.getInstance().getDishesList();
//            for (DishesEntity dishesEntity : dishesList) {
//                LogUtils.log("db dishes = " + GsonUtils.toJson(dishesEntity));
//            }
        });
    }

    private void printAllTax() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
//            List<TaxEntity> taxList = DataBaseRepository.getInstance().getTaxList();
//            for (TaxEntity taxEntity : taxList) {
//                LogUtils.log("db tax = " + GsonUtils.toJson(taxEntity));
//            }
        });
    }

    private void printAllTable() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List<TableEntity> tableList = DataBaseRepository.getInstance().getTableList();
            for (TableEntity tableEntity : tableList) {
                LogUtils.log("db table = " + GsonUtils.toJson(tableEntity));
            }
        });
    }

    private void addTestTableData() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //DELETEALL
            DataBaseRepository.getInstance().deleteAllTable();
            //for 1 - > 100
            for (int i = 1; i <= 100; i++) {
                TableEntity tableEntity = new TableEntity();
                tableEntity.setTableId(i);
                tableEntity.setCode(i);
                tableEntity.setName("T-" + i);
                if (i <= 50) {
                    tableEntity.setZone(ZoneType.ZONE_TYPE_ZONE_1);
                } else {
                    tableEntity.setZone(ZoneType.ZONE_TYPE_ZONE_2);
                }

                DataBaseRepository.getInstance().addTable(tableEntity);
            }

            if (BuildConfig.DEBUG) {
                printAllTable();
            }
        });
    }

    //inner static class
    private static class DataSyncManagerHolder {
        public static DataSyncManager instance = new DataSyncManager();
    }
}
