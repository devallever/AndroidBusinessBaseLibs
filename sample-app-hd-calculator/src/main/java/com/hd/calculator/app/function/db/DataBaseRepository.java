package com.hd.calculator.app.function.db;

import androidx.annotation.Nullable;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.hd.calculator.app.MyApp;
import com.hd.calculator.app.constant.PayType;
import com.hd.calculator.app.function.db.dao.AccountDao;
import com.hd.calculator.app.function.db.dao.BillDailyReportDao;
import com.hd.calculator.app.function.db.dao.BillRecordDao;
import com.hd.calculator.app.function.db.dao.DishesDao;
import com.hd.calculator.app.function.db.dao.DishesSortDao;
import com.hd.calculator.app.function.db.dao.OrderRecordDao;
import com.hd.calculator.app.function.db.dao.ReduceDishesRecordDao;
import com.hd.calculator.app.function.db.dao.TableDao;
import com.hd.calculator.app.function.db.dao.TaxDao;
import com.hd.calculator.app.function.db.entity.AccountEntity;
import com.hd.calculator.app.function.db.entity.DishesEntity;
import com.hd.calculator.app.function.db.entity.DishesSortEntity;
import com.hd.calculator.app.function.db.entity.TableEntity;
import com.hd.calculator.app.function.db.entity.TaxEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDailyReportEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.function.db.entity.operation.ReduceDishesRecordEntity;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.sql.Time;
import java.util.List;

public class DataBaseRepository {

    private AppDatabase mAppDatabase;
    private AccountDao mAccountDao;
    //dishesSortDao
    private DishesSortDao mDishesSortDao;
    //dishesDao
    private DishesDao mDishesDao;

    private TaxDao mTaxDao;

    private TableDao mTableDao;

    private OrderRecordDao mOrderRecordDao;

    private BillRecordDao mBillRecordDao;

    private ReduceDishesRecordDao mReduceDishesRecordDao;

    private BillDailyReportDao mBillDailyReportDao;

    public static DataBaseRepository getInstance() {
        return DataBaseRepositoryHolder.instance;
    }

    public List<DishesSortEntity> getFirstLevelSortList() {
        return mDishesSortDao.getFirstLevelSortList();
    }

    public void init() {
        mAppDatabase = AppDatabase.getDatabase(MyApp.context);
        mAccountDao = mAppDatabase.accountDao();
        mDishesSortDao = mAppDatabase.dishesSortDao();
        mDishesDao = mAppDatabase.dishesDao();
        mTaxDao = mAppDatabase.taxDao();
        mTableDao = mAppDatabase.tableDao();
        mOrderRecordDao = mAppDatabase.orderRecordDao();
        mBillRecordDao = mAppDatabase.billRecordDao();
        mReduceDishesRecordDao = mAppDatabase.reduceDishesRecordDao();
        mBillDailyReportDao = mAppDatabase.billDailyReportDao();
    }

    //Account///////////////////////////////////////////////////////////////////////////////////////
    public void addAccount(AccountEntity accountEntity) {
        mAccountDao.addAccount(accountEntity);
    }

    //deleteAllAccount
    public void deleteAllAccount() {
        mAccountDao.deleteAll();
    }

    //deleteWaiterAccount
    public void deleteWaiterAccount() {
        mAccountDao.deleteWaiterAccount();
    }

    //getBossAccount
    public AccountEntity getBossAccount() {
        return mAccountDao.getBossAccount();
    }

    //getByUserId
    public AccountEntity getByUserId(long userId) {
        return mAccountDao.getByUserId(userId);
    }

    //getByPassword
    public @Nullable AccountEntity getByPassword(String password) {
        return mAccountDao.getByPassword(password);
    }

    //getAccountList
    public List<AccountEntity> getAccountList() {
        return mAccountDao.getAccountList();
    }

    public void updateAccount(AccountEntity accountEntity) {
        mAccountDao.update(accountEntity);
    }

    public AccountEntity getAccountByUserId(long id) {
        return mAccountDao.getAccountByUserId(id);
    }

    public void deleteAccount(long userId) {
        mAccountDao.deleteAccount(userId);
    }

    //DishesSort///////////////////////////////////////////////////////////////////////////////////////
    //addDishesSort
    public void addDishesSort(DishesSortEntity dishesSortEntity) {
        mDishesSortDao.addDishesSort(dishesSortEntity);
    }

    //deleteAllDishesSort
    public void deleteAllDishesSort() {
        mDishesSortDao.deleteAll();
    }

    //getDishesSortList
    public List<DishesSortEntity> getDishesSortList() {
        return mDishesSortDao.getAll();
    }

    //second
    public List<DishesSortEntity> getSecondLevelSortListByFirstId(long sortId) {
        return mDishesSortDao.getSecondLevelSortListByFirstId(sortId);
    }

    //third
    public List<DishesSortEntity> getThirdLevelSortListBySecondId(long sortId) {
        return mDishesSortDao.getThirdLevelSortListBySecondId(sortId);
    }

    //Dishes///////////////////////////////////////////////////////////////////////////////////////
    //addDishes
    public void addDishes(DishesEntity dishesEntity) {
        mDishesDao.addDishes(dishesEntity);
    }

    //deleteAllDishes
    public void deleteAllDishes() {
        mDishesDao.deleteAll();
    }

    //getDishesList
    public List<DishesEntity> getDishesList() {
        return mDishesDao.getAll();
    }

    //getDishesByCode
    public DishesEntity getDishesByCode(String code) {
        return mDishesDao.getByCode(code);
    }

    //getByThirdSortId
    public List<DishesEntity> getDishesListByThirdId(long thirdId) {
        return mDishesDao.getDishesListByThirdId(thirdId);
    }

    //Tax///////////////////////////////////////////////////////////////////////////////////////
    //addTax
    public void addTax(TaxEntity taxEntity) {
        mTaxDao.addTax(taxEntity);
    }

    //deleteAllTax
    public void deleteAllTax() {
        mTaxDao.deleteAll();
    }

    //getTaxList
    public List<TaxEntity> getTaxList() {
        return mTaxDao.getAll();
    }

    //Table///////////////////////////////////////////////////////////////////////////////////////
    //addTable
    public void addTable(TableEntity tableEntity) {
        mTableDao.addTable(tableEntity);
    }

    //deleteALL
    public void deleteAllTable() {
        mTableDao.deleteAll();
    }

    //deleteTableByZone
    public void deleteTableByZone(int zone) {
        mTableDao.deleteByZone(zone);
    }

    //getTableList
    public List<TableEntity> getTableList() {
        return mTableDao.getAll();
    }

    //getTableByZone
    public List<TableEntity> getTableByZone(int zone) {
        return mTableDao.getByZone(zone);
    }

    //getByTableId
    public TableEntity getByTableId(int tableId) {
        return mTableDao.getByTableId(tableId);
    }

    //getTableByCode
    public TableEntity getTableByCode(int code) {
        return mTableDao.getByTableCode(code);
    }

    //OrderRecord///////////////////////////////////////////////////////////////////////////////////
    public void addOrderRecord(OrderRecordEntity orderRecordEntity) {
        mOrderRecordDao.addOrderRecord(orderRecordEntity);
    }

    public void addOrderDishesRecord(OrderDishesRecordEntity orderDishesRecordEntity) {
        mOrderRecordDao.addOrderDishesRecord(orderDishesRecordEntity);
    }

    public List<OrderWithDishesRef> getAllOrderRecord() {
        return mOrderRecordDao.getAllOrderRecord();
    }

    public List<OrderWithDishesRef> getAllInHouseOrderRecord() {
        return mOrderRecordDao.getAllInHouseOrderRecord();
    }

    //getLastOrderRecord
    public OrderRecordEntity getLastOrderRecord() {
        return mOrderRecordDao.getLastOrderRecord();
    }

    //getOrderByTableCode
    public OrderRecordEntity getOrderByTableCode(int tableCode) {
        return mOrderRecordDao.getByTableCode(tableCode);
    }

    public OrderRecordEntity getInHouseOrderByTableCode(int tableCode) {
        return mOrderRecordDao.getInHouseByTableCode(tableCode);
    }

    public List<OrderWithDishesRef> getOrderWithDishesByTableCode(int tableCode) {
        return mOrderRecordDao.getOrderWithDishesByTableCode(tableCode);
    }

    public List<OrderWithDishesRef> getInHouseOrderWithDishesByTableCode(int tableCode) {
        return mOrderRecordDao.getInHouseOrderWithDishesByTableCode(tableCode);
    }

    //getTakeoutOrderByTableCode
    public OrderRecordEntity getTakeoutOrderByTableCode(int tableCode) {
        return mOrderRecordDao.getTakeoutOrderByTableCode(tableCode);
    }

    //getOrderById
    public OrderWithDishesRef getOrderById(long id) {
        return mOrderRecordDao.getByOrderId(id);
    }

    //getUnPaidOrder
    public List<OrderWithDishesRef> getUnPaidOrder() {
        return mOrderRecordDao.getUnPaidOrder();
    }

    //updateOrder
    public void updateOrder(OrderRecordEntity orderRecordEntity) {
        mOrderRecordDao.update(orderRecordEntity);
    }

    public List<OrderDishesRecordEntity> getOrderDishesListByOrderId(long orderId) {
        return mOrderRecordDao.getOrderDishesListByOrderId(orderId);
    }

    public void updateOrderDishes(OrderDishesRecordEntity orderDishesRecordEntity) {
        mOrderRecordDao.updateOrderDishesRecord(orderDishesRecordEntity);
    }

    public void updateOrderDishesListRecord(List<OrderDishesRecordEntity> orderDishesRecordEntityList) {
        mOrderRecordDao.updateOrderDishesListRecord(orderDishesRecordEntityList);
    }

    public void deleteOrderByOrderId(long orderId) {
        mOrderRecordDao.deleteOrderRecordByOrderId(orderId);
    }

    public void deleteOrderDishesById(long id) {
        mOrderRecordDao.deleteOrderDishesById(id);
    }

    //getOrderDishesById
    public OrderDishesRecordEntity getOrderDishesById(long id) {
        return mOrderRecordDao.getOrderDishesById(id);
    }

    public void deleteTodayOrder() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        String todayEnd = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[1]);
        LogUtils.log("today: " + todayStart + " - " + todayEnd);
        mOrderRecordDao.deleteByCreateTime(time[0], time[1]);
    }

    public void deleteTodayBeforeOrder() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        LogUtils.log("today:Before " + todayStart);
        mOrderRecordDao.deleteBeforeByTime(time[0]);
    }

    //BillRecord///////////////////////////////////////////////////////////////////////////////////
    //addBillRecord
    public void addBillRecord(BillRecordEntity billRecordEntity) {
        mBillRecordDao.addBillRecord(billRecordEntity);
    }

    //addBillWithDishesRecord
    public void addBillDishesRecord(BillDishesRecordEntity billWithDishesRef) {
        mBillRecordDao.addBillDishesRecord(billWithDishesRef);
    }

    //getAllBillWithDishesList
    public List<BillWithDishesRef> getAllBillWithDishesList() {
        return mBillRecordDao.getBillWithDishesList();
    }

    //getLastBillRecord
    public BillRecordEntity getLastBillRecord() {
        return mBillRecordDao.getLastBillRecord();
    }

    public List<BillWithDishesRef> getBillList(long selectedUserId, long selectedDate, int selectPayType, String selectSortCondition) {
        long[] oneDay7amTimestamps = TimeUtils.get7amTimestamps(selectedDate);
        long startTime = oneDay7amTimestamps[0];
        long endTime = oneDay7amTimestamps[1];
        String sql = "SELECT * FROM bill_record WHERE billUserId = " + selectedUserId + " AND payType = " + selectPayType + " AND createTime >= " + startTime + " AND createTime < " + endTime + " ORDER BY " + selectSortCondition;
        if (selectedUserId == 0) {
            if (selectPayType == PayType.PAY_TYPE_ALL) {
                sql = "SELECT * FROM bill_record WHERE createTime >= " + startTime + " AND createTime < " + endTime + " ORDER BY " + selectSortCondition;
            } else {
                sql = "SELECT * FROM bill_record WHERE payType = " + selectPayType + " AND createTime >= " + startTime + " AND createTime < " + endTime + " ORDER BY " + selectSortCondition;
            }
        } else {
            if (selectPayType == PayType.PAY_TYPE_ALL) {
                sql = "SELECT * FROM bill_record WHERE billUserId = " + selectedUserId + " AND createTime >= " + startTime + " AND createTime < " + endTime + " ORDER BY " + selectSortCondition;
            } else {
                sql = "SELECT * FROM bill_record WHERE billUserId = " + selectedUserId + " AND payType = " + selectPayType + " AND createTime >= " + startTime + " AND createTime < " + endTime + " ORDER BY " + selectSortCondition;
            }
        }

        LogUtils.log("boss sql = " + sql);

        return mBillRecordDao.getBySql(new SimpleSQLiteQuery(sql));

    }

    //getBillByTableCodeOrderByTimeDesc
    public List<BillWithDishesRef> getBillByTableCodeOrderByTimeDesc(int tableCode) {
        return mBillRecordDao.getByTableCodeOrderByCreateTimeDesc(tableCode);
    }

    //getBillById
    public BillWithDishesRef getBillById(long id) {
        return mBillRecordDao.getById(id);
    }

    //updateBill
    public void updateBill(BillRecordEntity billRecordEntity) {
        mBillRecordDao.updateBillRecord(billRecordEntity);
    }

    //getBillByUserIdAndCreateTime
    public List<BillWithDishesRef> getTodayBillByUserId(long billUserId) {
        long[] oneDay7amTimestamps = TimeUtils.get7amTimestamps(TimeUtils.getAdjustedTimestamp(System.currentTimeMillis()));
        return mBillRecordDao.getBillByUserIdAndCreateTime(billUserId, oneDay7amTimestamps[0], oneDay7amTimestamps[1]);
    }

    //getFirstBill
    public BillWithDishesRef getFirstBill() {
        return mBillRecordDao.getFirstBill();
    }

    //getBillByCreateTime
    public List<BillWithDishesRef> getBillByCreateTime(long startTime, long endTime) {
        return mBillRecordDao.getByCreateTime(startTime, endTime);
    }


    //deleteToday
    public void deleteTodayBill() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        String todayEnd = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[1]);
        LogUtils.log("today: " + todayStart + " - " + todayEnd);
        mBillRecordDao.deleteByCreateTime(time[0], time[1]);
    }

    public void deleteTodayBeforeBill() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        LogUtils.log("today:Before " + todayStart);
        mBillRecordDao.deleteBeforeByTime(time[0]);
    }

    //educeDishesRecord///////////////////////////////////////////////////////////////////////////////////
    //ADD reduceDishesRecord
    public void addReduceDishesRecord(ReduceDishesRecordEntity entity) {
        mReduceDishesRecordDao.add(entity);
    }

    //get reduceDishesRecord by createTime
    public List<ReduceDishesRecordEntity> getReduceDishesRecordByCreateTime(long startTime, long endTime) {
        return mReduceDishesRecordDao.getListByCreateTime(startTime, endTime);
    }

    //getAllReduceDishesRecord
    public List<ReduceDishesRecordEntity> getAllReduceDishesRecord() {
        return mReduceDishesRecordDao.getAllList();
    }

    //deleteToday
    public void deleteTodayReduceDishesRecord() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        String todayEnd = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[1]);
        LogUtils.log("today: " + todayStart + " - " + todayEnd);
        mReduceDishesRecordDao.deleteByCreateTime( time[0], time[1]);
    }

    //deleteTodayBefore
    public void deleteTodayBeforeReduceDishesRecord() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        LogUtils.log("today:Before " + todayStart);
        mReduceDishesRecordDao.deleteBeforeByTime( time[0]);
    }

    //BillDailyReport///////////////////////////////////////////////////////////////////////////////////
    public void addBillDailyReport(BillDailyReportEntity billDailyReportEntity) {
        mBillDailyReportDao.addBillDailyReport(billDailyReportEntity);
    }

    public List<BillDailyReportEntity> getAllBillDailyReport() {
        return mBillDailyReportDao.getAll();
    }

    /**
     * 根据时间获取日报
     * @param time yyyy-MM-dd
     * @return
     */
    public BillDailyReportEntity getBillDailyReportByTime(String time) {
        return mBillDailyReportDao.getByTime(time);
    }

    public BillDailyReportEntity getLastBillDailyReport() {
        return mBillDailyReportDao.getLast();
    }

    //deleteToday
    public void deleteTodayBillDailyReport() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        String todayEnd = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[1]);
        LogUtils.log("today: " + todayStart + " - " + todayEnd);
        List<BillDailyReportEntity> billDailyReportEntityList = mBillDailyReportDao.getAll();
        billDailyReportEntityList.forEach(billDailyReportEntity -> {
            LogUtils.log("today billDailyReportEntity creteTime = " + TimeUtils.formatTimestampToDDMMYYYYHHmm(billDailyReportEntity.getCreateTime()));
        });
        mBillDailyReportDao.deleteByCreateTime( time[0], time[1]);
    }

    //deleteTodayBefore
    public void deleteTodayBeforeBillDailyReport() {
        long[] time = TimeUtils.getCurrentDay7amTimestamps();
        String todayStart = TimeUtils.formatTimestampToDDMMYYYYHHmm(time[0]);
        LogUtils.log("today:Before " + todayStart);
        List<BillDailyReportEntity> billDailyReportEntityList = mBillDailyReportDao.getAll();
        billDailyReportEntityList.forEach(billDailyReportEntity -> {
            LogUtils.log("today billDailyReportEntity creteTime = " + TimeUtils.formatTimestampToDDMMYYYYHHmm(billDailyReportEntity.getCreateTime()));
        });
        mBillDailyReportDao.deleteBeforeByTime( time[0]);
    }

    //common////////////////////////////////////////////////////////////////////////////////////////
    private void getTodayStartTime() {
    }

    //inner static  class
    private static class DataBaseRepositoryHolder {
        private static final DataBaseRepository instance = new DataBaseRepository();
    }
}
