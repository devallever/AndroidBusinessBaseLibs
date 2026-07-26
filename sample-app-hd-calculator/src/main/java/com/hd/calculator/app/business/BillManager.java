package com.hd.calculator.app.business;

import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.operation.BillDailyReportEntity;
import com.hd.calculator.app.function.db.entity.operation.BillDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.BillWithDishesRef;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.TimeUtils;

import java.util.List;

public class BillManager {
    //static inner  class
    private static class SingletonHolder {
        private static final BillManager INSTANCE = new BillManager();
    }
    public static BillManager getIns() {
        return SingletonHolder.INSTANCE;
    }

    //生成上一天账单
    public void generateYesterdayBill() {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            //获取当前时间戳 调整后的时间
            long adjustedTimestamp = TimeUtils.getAdjustedTimestamp(System.currentTimeMillis());
            //获取上一天的时间日期
            String yesterdayDate = TimeUtils.getPreviousDayDate(adjustedTimestamp);
            //检查是否已有记录
            BillDailyReportEntity billRecordEntity = DataBaseRepository.getInstance().getBillDailyReportByTime(yesterdayDate);
            if (billRecordEntity == null) {
                //没记录，需要生成
                String[] date = yesterdayDate.split("-");
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);
                long[] timestamps = TimeUtils.get7amTimestamps(year,month,day);

                List<BillWithDishesRef> billList = DataBaseRepository.getInstance().getBillByCreateTime(timestamps[0],timestamps[1]);
                if (billList == null || billList.isEmpty()) {
                    //昨天没有账单，跳过
                    return;
                }

                BillDailyReportEntity lastRecord = DataBaseRepository.getInstance().getLastBillDailyReport();
                int code = 1;
                if (lastRecord != null) {
                    code = lastRecord.getCode() + 1;
                }
                BillDailyReportEntity billDailyReportEntity = new BillDailyReportEntity();
                billDailyReportEntity.setTime(yesterdayDate);
                billDailyReportEntity.setCode(code);
                float amount = 0;
                for (BillWithDishesRef billRef : billList) {
                    BillRecordEntity bill = billRef.getBill();
                    List<BillDishesRecordEntity> billDishesRecordEntityList = billRef.getDishesList();
                    amount = amount + bill.getAmount();
                    billDailyReportEntity.setLastBillTime(bill.getCreateTime());
                }
                billDailyReportEntity.setAmount(amount);
                billDailyReportEntity.setCreateTime(System.currentTimeMillis());

                DataBaseRepository.getInstance().addBillDailyReport(billDailyReportEntity);
            }

            //printAllBill
            List<BillDailyReportEntity> billDailyReportEntityList = DataBaseRepository.getInstance().getAllBillDailyReport();
            for (BillDailyReportEntity billDailyReportEntity : billDailyReportEntityList) {
                LogUtils.log("billDailyReportEntity = " + GsonUtils.toJson(billDailyReportEntity));
            }
        });
    }
}
