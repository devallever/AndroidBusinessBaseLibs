package com.hd.calculator.app.business;


import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.hd.calculator.app.business.model.LocalUnUploadTableData;
import com.hd.calculator.app.constant.OrderType;
import com.hd.calculator.app.constant.log.ActionType;
import com.hd.calculator.app.function.UserLog;
import com.hd.calculator.app.function.db.DataBaseRepository;
import com.hd.calculator.app.function.db.entity.operation.OrderDishesRecordEntity;
import com.hd.calculator.app.function.db.entity.operation.OrderWithDishesRef;
import com.hd.calculator.app.function.network.NetworkCallback;
import com.hd.calculator.app.function.network.NetworkRepository;
import com.hd.calculator.app.function.network.post.PostTableData;
import com.hd.calculator.app.function.network.post.PostTableState;
import com.hd.calculator.app.function.network.post.PostUserLog;
import com.hd.calculator.app.function.network.post.TableUseData;
import com.hd.calculator.app.function.network.response.EmptyResponse;
import com.hd.calculator.app.function.network.response.UseTableResponse;
import com.hd.calculator.app.function.network.response.UseTableResponseData;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.ThreadUtils;
import com.hd.calculator.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableManager {
    public static TableManager getIns() {
        return SingletonHolder.INSTANCE;
    }

    private final Map<Integer, PostTableData> mUploadTableCodeDataMap = new ConcurrentHashMap<>();

    public String getDisplayTableName(int tableCode, int orderType) {
        if (orderType == OrderType.ORDER_TYPE_IN_HOUSE) {
            return "T-" + tableCode;
        } else {
            return "T-A" + tableCode;
        }
    }

    public void getUseTableList(TableUseCallback callback) {
        NetworkRepository.getInstance().getUseTableList(new NetworkCallback<>() {
            @Override
            public void onSuccess(UseTableResponse data) {
//                LogUtils.log("getUseTableList = " + GsonUtils.toJson(data));
                Map<Integer, UseTableResponseData> usedTableOrderMap = new HashMap<>();
                for (UseTableResponseData datum : data.getData()) {
                    if (datum.getTableData() != null ) {
                        usedTableOrderMap.put(datum.getTableCode(), datum);
                    }
                }
                if (callback != null) {
                    callback.onResult(usedTableOrderMap);
                }
            }

            @Override
            public void onFailure(String msg) {
                if (callback != null) {
                    callback.onResult(new HashMap<>());
                }
            }
        });
    }

    public void checkTableCanUsed(int tableCode, @NonNull CheckTableUsedCallback callback) {
        PostTableState postTableState = new PostTableState(tableCode, AccountManager.getIns().getAccount().getUserId());
        NetworkRepository.getInstance().updateTableState(postTableState, new NetworkCallback<>() {

            @Override
            public void onSuccess(EmptyResponse data) {
                boolean inUse = data.getCode() != 200;//不能占用
                callback.onResult(data.getCode() == 200);
            }

            @Override
            public void onFailure(String msg) {
                NetworkCallback.super.onFailure(msg);
                callback.onResult(true);
                unLockTableUse(tableCode, null);
            }
        });
    }

    public void unLockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, false, finishTask);
    }

    public void lockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, true, false, finishTask);
    }

    public void forceLockTableUse(int tableCode, Runnable finishTask) {
        updateTableState(tableCode, true, true, finishTask);
    }

    /***
     *
     * @param type 用户操作类型 ActionType
     * 订单记录
     * 1.最开始能进入点餐界面，就要占用
     * 2.出单
     * 3.结账
     * 4.菜品数量变更
     * 5.输入boss密码强制覆盖
     **/
    public void postOrderRecord(int tableCode, int type, boolean force, Runnable finishTask) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            PostTableData postTableUse = new PostTableData();
            List<OrderWithDishesRef> orderWithDishesRefs = DataBaseRepository.getInstance().getInHouseOrderWithDishesByTableCode(tableCode);
            OrderWithDishesRef  orderWithDishesRef = null;
            if (!orderWithDishesRefs.isEmpty()) {
                orderWithDishesRef = orderWithDishesRefs.get(0);
                orderWithDishesRef.getOrder().setOrderUserId(AccountManager.getIns().getAccount().getUserId());

                if (orderWithDishesRefs.size() > 1) {
                    orderWithDishesRef.getDishesList().clear();
                    for (OrderWithDishesRef withDishesRef : orderWithDishesRefs) {
                        for (OrderDishesRecordEntity dishesRecord : withDishesRef.getDishesList()) {
                            orderWithDishesRef.getDishesList().add(dishesRecord);
                        }
                    }
                }
            }

            postTableUse.setTableCode(tableCode);
            if (orderWithDishesRef != null) {
                postTableUse.setUserId(orderWithDishesRef.getOrder().getOrderUserId());
            } else {
                postTableUse.setUserId(AccountManager.getIns().getAccount().getUserId());
            }
//            postTableUse.setUserId(AccountManager.getIns().getAccount().getUserId());
            TableUseData tableUseData = new TableUseData();
            tableUseData.setOrder(orderWithDishesRef);
            postTableUse.setTableData(tableUseData);
            NetworkCallback<EmptyResponse> callback = new NetworkCallback<>() {
                @Override
                public void onSuccess(EmptyResponse data) {
                    if (data.getCode() == 200) {
                        LogUtils.log("postOrderRecord success = " + GsonUtils.toJson(data));
                        mUploadTableCodeDataMap.remove(tableCode);
                        updateLocalUnloadTableData();
                    }
                }

                @Override
                public void onFailure(String msg) {
                    LogUtils.log("postOrderRecord fail = " + msg);
                    LogUtils.log("mUploadTableCodeDataMap = " + GsonUtils.toJson(mUploadTableCodeDataMap));
                    //todo 回调上传失败
                    ToastUtils.show("上传失败");
                }
            };

            if (orderWithDishesRef == null) {
                mUploadTableCodeDataMap.put(tableCode, postTableUse);
                updateLocalUnloadTableData();
                NetworkRepository.getInstance().postOrderRecord(postTableUse, callback);
                logUserAction(type, postTableUse);
            } else {
                if (orderWithDishesRef.getOrder().getOrderType() == OrderType.ORDER_TYPE_IN_HOUSE) {
                    mUploadTableCodeDataMap.put(tableCode, postTableUse);
                    updateLocalUnloadTableData();
                    NetworkRepository.getInstance().postOrderRecord(postTableUse, callback);
                    logUserAction(type, postTableUse);
                }
            }

            if (finishTask != null) {
                finishTask.run();
            }
        });
    }

    private void logUserAction(int type, PostTableData postTableUse) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            List< OrderWithDishesRef> all = DataBaseRepository.getInstance().getAllInHouseOrderRecord();
            String allMsg = GsonUtils.toJson(all);
            String text = GsonUtils.toJson(postTableUse);
            LogUtils.log("postTableUse.type = " + ActionType.getTypeName(type));
            LogUtils.log("postTableUse.all = " + allMsg);
            LogUtils.log("postTableUse.text = " + text);
            PostUserLog userLog = new PostUserLog();
            userLog.setType(type);
            userLog.setAllMsg(allMsg);
            userLog.setText(text);
            String userLogString = GsonUtils.toJson(userLog);
            LogUtils.log("postTableUse.userLogString = " + userLogString);
            UserLog.log(userLog);
        });
    }

    public void updateTableState(int tableCode, boolean inuse, Runnable finishTask) {
        updateTableState(tableCode, inuse, false, finishTask);
    }

    public void updateTableState(int tableCode, boolean inuse, boolean force, Runnable finishTask) {
        ThreadUtils.runOnIoThreadDelayed(() -> {
            PostTableState postTableState = new PostTableState();
            postTableState.setTableCode(tableCode);
            postTableState.setUserId(AccountManager.getIns().getAccount().getUserId());
            postTableState.setInuse(inuse);
            postTableState.setForce(force);
            NetworkRepository.getInstance().updateTableState(postTableState, new NetworkCallback<>() {
                @Override
                public void onSuccess(EmptyResponse data) {
                    LogUtils.log("updateTableState = " + GsonUtils.toJson(data));
                    if (finishTask != null) {
                        finishTask.run();
                    }
                }

                @Override
                public void onFailure(String msg) {
                    if (finishTask != null) {
                        finishTask.run();
                    }
                }
            });
        });
    }

    private synchronized void updateLocalUnloadTableData() {
        LocalUnUploadTableData localUnUploadTableData = new LocalUnUploadTableData();
        List<PostTableData> postTableDataList = new ArrayList<>();
        for (Map.Entry<Integer, PostTableData> integerStringEntry : mUploadTableCodeDataMap.entrySet()) {
            postTableDataList.add(integerStringEntry.getValue());
        }
        localUnUploadTableData.setList(postTableDataList);
        Config.setLocalUnUploadTableData(GsonUtils.toJson(localUnUploadTableData));
    }

    public void removeLocalUnloadTableData(int tableCode) {
        mUploadTableCodeDataMap.remove(tableCode);
        updateLocalUnloadTableData();
    }

    public void removeAllLocalUnloadTableData() {
        mUploadTableCodeDataMap.clear();
        updateLocalUnloadTableData();
    }

    /**
     * 回到主界面后，检测本地未上传的订单
     */
    public void uploadLocalUnloadTableData() {
        String localUnUploadTableData = Config.getLocalUnUploadTableData();
        mUploadTableCodeDataMap.clear();
        if (!TextUtils.isEmpty(localUnUploadTableData)) {
            LocalUnUploadTableData localUnUploadTableDataBean = GsonUtils.fromJson(localUnUploadTableData, LocalUnUploadTableData.class);
            for (PostTableData postTableData : localUnUploadTableDataBean.getList()) {
                mUploadTableCodeDataMap.put(postTableData.getTableCode(), postTableData);
                forceLockTableUse(postTableData.getTableCode(), () -> {
                    NetworkRepository.getInstance().postOrderRecord(postTableData, new NetworkCallback<EmptyResponse>() {
                        @Override
                        public void onSuccess(EmptyResponse data) {
                            if (data.getCode() == 200) {
                                mUploadTableCodeDataMap.remove(postTableData.getTableCode());
                                updateLocalUnloadTableData();
                            }
                            updateTableState(postTableData.getTableCode(), false, false, null);
                        }

                        @Override
                        public void onFailure(String msg) {
                            updateTableState(postTableData.getTableCode(), false, false, null);
                        }
                    });

                    //上传离线数据
                    logUserAction(ActionType.UPLOAD_OFFLINE_DATA, postTableData);
                });

                LogUtils.log("uploadLocalUnloadTableData " + postTableData.getTableCode() + " = " + GsonUtils.toJson(postTableData));
            }
        }
    }

    public boolean checkHasLocalUnloadTableData() {
        String localUnUploadTableData = Config.getLocalUnUploadTableData();
        if (!TextUtils.isEmpty(localUnUploadTableData)) {
            LocalUnUploadTableData localUnUploadTableDataBean = GsonUtils.fromJson(localUnUploadTableData, LocalUnUploadTableData.class);
            if (localUnUploadTableDataBean == null) {
                return false;
            }
            return !localUnUploadTableDataBean.getList().isEmpty();
        } else {
            return false;
        }
    }

    public String getLocalUnloadTableDataTableCode() {
        String localUnUploadTableData = Config.getLocalUnUploadTableData();
        if (!TextUtils.isEmpty(localUnUploadTableData)) {
            LocalUnUploadTableData localUnUploadTableDataBean = GsonUtils.fromJson(localUnUploadTableData, LocalUnUploadTableData.class);
            StringBuilder tableCode = new StringBuilder();
            for (PostTableData postTableData : localUnUploadTableDataBean.getList()) {
                tableCode.append("T-");
                tableCode.append(postTableData.getTableCode());
                //最后一个不加逗号
                if (postTableData != localUnUploadTableDataBean.getList().get(localUnUploadTableDataBean.getList().size() - 1)) {
                    tableCode.append(",");
                }
            }
            return  tableCode.toString();
        } else {
            return "";
        }
    }

    public interface TableUseCallback {
        void onResult(Map<Integer, UseTableResponseData> usedTableOrderMap);
    }

    public interface CheckTableUsedCallback {
        void onResult(boolean canUse);
    }

    //inner static class
    private static class SingletonHolder {
        private static final TableManager INSTANCE = new TableManager();
    }

}
