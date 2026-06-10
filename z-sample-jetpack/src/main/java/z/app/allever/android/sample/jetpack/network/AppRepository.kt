package z.app.allever.android.sample.jetpack.network

import app.allever.android.lib.network.core.NetCore

object AppRepository {
    suspend fun getHomePageArticleList(currentPage: Int): BaseResponse<WanAndroidPageData> {
        return NetCore.get<BaseResponse<WanAndroidPageData>>("article/list/${currentPage}/json")
    }
}