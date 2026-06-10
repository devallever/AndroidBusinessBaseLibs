package z.app.allever.android.sample.jetpack.network

import app.allever.android.lib.network.core.NetCore

object AppRepository {
    suspend fun getHomePageArticleList(currentPage: Int): BaseResponse<WanAndroidPageData> {
        return NetCore.getT("article/list/${currentPage}/json")
    }
}