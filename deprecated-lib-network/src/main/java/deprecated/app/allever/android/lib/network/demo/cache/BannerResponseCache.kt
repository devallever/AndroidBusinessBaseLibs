package deprecated.app.allever.android.lib.network.demo.cache

import deprecated.app.allever.android.lib.network.demo.reponse.BannerData
import deprecated.app.allever.android.lib.network.demo.reponse.BaseResponse
import deprecated.app.allever.android.lib.network.cache.ResponseCache

class BannerResponseCache : ResponseCache<BaseResponse<List<BannerData>>>() {

    override fun cacheKey(): String {
        return "banner"
    }
}