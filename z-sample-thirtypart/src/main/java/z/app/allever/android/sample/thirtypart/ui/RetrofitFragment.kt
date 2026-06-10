package z.app.allever.android.sample.thirtypart.ui

import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.common.ListFragment
import app.allever.android.lib.common.ListViewModel
import app.allever.android.lib.common.adapter.TextClickAdapter
import app.allever.android.lib.common.adapter.bean.TextClickItem
import app.allever.android.lib.common.databinding.FragmentListBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RetrofitFragment : ListFragment<FragmentListBinding, ListViewModel, TextClickItem>() {
    override fun getAdapter() = TextClickAdapter()

    override fun getList() = mutableListOf<TextClickItem>()

    private fun initRetrofit() {
        val retrofit = Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ServiceApi::class.java)

        lifecycleScope.launch {
            api.login()
        }


    }

    private interface ServiceApi {
        @GET("/login")
        suspend fun login(): BaseResponse<Any>
    }

    private class BaseResponse<T>(
        val code: Int,
        val message: String,
        val data: T
    )
}