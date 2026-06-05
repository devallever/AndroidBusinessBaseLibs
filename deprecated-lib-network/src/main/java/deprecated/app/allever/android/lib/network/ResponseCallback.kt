package deprecated.app.allever.android.lib.network

import deprecated.app.allever.android.lib.network.response.NetResponse

interface ResponseCallback<DATA> {
    fun onSuccess(response: NetResponse<DATA>)
    fun onFail(response: NetResponse<DATA>)
}