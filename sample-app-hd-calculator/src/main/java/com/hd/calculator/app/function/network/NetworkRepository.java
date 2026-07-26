package com.hd.calculator.app.function.network;

import com.hd.calculator.app.business.Config;
import com.hd.calculator.app.function.network.post.PostTableData;
import com.hd.calculator.app.function.network.post.PostTableState;
import com.hd.calculator.app.function.network.post.PostUserLog;
import com.hd.calculator.app.function.network.response.AccountResponse;
import com.hd.calculator.app.function.network.response.DishesCategoryResponse;
import com.hd.calculator.app.function.network.response.EmptyResponse;
import com.hd.calculator.app.function.network.response.TableResponse;
import com.hd.calculator.app.function.network.response.TaxResponse;
import com.hd.calculator.app.function.network.response.UseTableResponse;
import com.hd.calculator.app.util.GsonUtils;
import com.hd.calculator.app.util.LogUtils;
import com.hd.calculator.app.util.NetworkUtils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkRepository {

    private Retrofit mRetrofit;

    private ApiService mApiService;

    public static NetworkRepository getInstance() {
        return NetworkUtilsHolder.instance;
    }

    public void init() {
        initRetrofit();
    }

    //initRetrofit
    private void initRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(new AuthorizationInterceptor()).addInterceptor(new HttpLoggingInterceptor()).build();
        mRetrofit = new Retrofit.Builder().client(okHttpClient).addConverterFactory(GsonConverterFactory.create()).baseUrl("http://api.dmzorder.com/").build();
        mApiService = mRetrofit.create(ApiService.class);
    }

    public boolean canRequestApi() {
        if (Config.isLocalMode()) {
            return false;
        }
        return NetworkUtils.isNetworkAvailable();
    }

    public void getAccountList(NetworkCallback<AccountResponse> callback) {
        mApiService.getAccountList().enqueue(new DefaultRetrofitCallback<>(callback));
    }

    public void getDishesCategoryStructure(final NetworkCallback<DishesCategoryResponse> callback) {
        mApiService.getDishesCategoryStructure().enqueue(new DefaultRetrofitCallback<>(callback));
    }

    //TaxData
    public void getTaxData(final NetworkCallback<TaxResponse> callback) {
        mApiService.getTaxData().enqueue(new DefaultRetrofitCallback<>(callback));
    }

    //餐台列表
    public void getTableList(int areaId, final NetworkCallback<TableResponse> callback) {
        mApiService.getTableList(areaId).enqueue(new DefaultRetrofitCallback<>(callback));
    }

    public void getUseTableList(final NetworkCallback<UseTableResponse> callback) {
        mApiService.getUseTableList().enqueue(new DefaultRetrofitCallback<>(callback));
    }

    //post
    public void postOrderRecord(PostTableData postOrderRecord, final NetworkCallback<EmptyResponse> callback) {
        LogUtils.log("postOrderRecord = " + GsonUtils.toJson(postOrderRecord));
        mApiService.uploadOrder(postOrderRecord).enqueue(new DefaultRetrofitCallback<>(callback));
    }

    public void updateTableState(PostTableState postTableState, final NetworkCallback<EmptyResponse> callback) {
        LogUtils.log("updateTableState = " + GsonUtils.toJson(postTableState));
        mApiService.updateTableState(postTableState).enqueue(new DefaultRetrofitCallback<>(callback));
    }

    public void uploadUserLog(PostUserLog log, final NetworkCallback<EmptyResponse> callback) {
        LogUtils.log("uploadUserLog = " + GsonUtils.toJson(log));
        mApiService.uploadUserLog(log).enqueue(new DefaultRetrofitCallback<>(callback));
    }


    //inner static class
    private static class NetworkUtilsHolder {
        public static NetworkRepository instance = new NetworkRepository();
    }
}
