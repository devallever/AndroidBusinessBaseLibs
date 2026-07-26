package com.hd.calculator.app.function.network;

import com.hd.calculator.app.function.network.post.PostTableData;
import com.hd.calculator.app.function.network.post.PostTableState;
import com.hd.calculator.app.function.network.post.PostUserLog;
import com.hd.calculator.app.function.network.response.AccountResponse;
import com.hd.calculator.app.function.network.response.DishesCategoryResponse;
import com.hd.calculator.app.function.network.response.EmptyResponse;
import com.hd.calculator.app.function.network.response.TableResponse;
import com.hd.calculator.app.function.network.response.TaxResponse;
import com.hd.calculator.app.function.network.response.UseTableResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    /***
     * 账号列表
     * @return
     */
    @GET("/api/app/account/list")
    Call<AccountResponse> getAccountList();

    /**
     * 菜品分类和菜品
     *
     * @return
     */
    @GET("/api/app/dishes/category/structure")
    Call<DishesCategoryResponse> getDishesCategoryStructure();

    /**
     * 税率信息
     *
     * @return
     */
    @GET("/api/app/tax/config/current")
    Call<TaxResponse> getTaxData();

    //餐台列表 根据区域id
    @GET("/api/app/dining/table/list")
    Call<TableResponse> getTableList(@Query("areaId") int areaId);

    @GET("/api/app/dining/table/use/list")
    Call<UseTableResponse> getUseTableList();

    /**8
     * 提交订单
     * 1.出单的时候上传整个订单整个内容
     * 2.强制覆盖的时候
     * @param body
     * @return
     */
    @POST("/api/app/dining/table/use/upload/data")
    Call<EmptyResponse> uploadOrder(@Body PostTableData body);

    @POST("/api/app/dining/table/use")
    Call<EmptyResponse> updateTableState(@Body PostTableState body);

    @POST("/api/common/public/log")
    Call<EmptyResponse> uploadUserLog(@Body PostUserLog log);

}
