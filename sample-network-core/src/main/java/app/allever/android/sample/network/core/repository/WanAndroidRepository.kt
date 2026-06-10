package app.allever.android.sample.network.core.repository

import app.allever.android.lib.network.core.NetCore
import app.allever.android.sample.network.core.BaseResponse
import app.allever.android.sample.network.core.BannerData

/**
 * 玩 Android API 仓库 — 演示 Repository 层封装
 *
 * 所有方法返回 [BaseResponse]，**永不抛异常**。
 * 调用方只需判断 errorCode 即可：
 * ```kotlin
 * val resp = WanAndroidRepository.getBanner()
 * if (resp.errorCode == 0) {
 *     // 成功：resp.data 就是业务数据
 * } else {
 *     // 失败：resp.errorMsg 包含错误信息（网络错误 / 业务错误统一处理）
 * }
 * ```
 */
object WanAndroidRepository {

    // ==================== 首页相关 ====================

    /**
     * 获取首页 Banner 列表
     */
    suspend fun getBanner(): BaseResponse<List<BannerData>> = NetCore.get("/banner/json")
}
