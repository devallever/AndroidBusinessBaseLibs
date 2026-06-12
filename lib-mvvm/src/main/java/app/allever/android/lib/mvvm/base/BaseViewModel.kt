package app.allever.android.lib.mvvm.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import app.allever.android.lib.core.function.paging.PagingHelper
import io.github.studycwq.extension.asLiveData
import kotlinx.coroutines.flow.Flow

open class BaseViewModel : ViewModel() {
    protected val _loadMoreStatusLiveData  by lazy { MutableLiveData(false) }
    val loadMoreStatusLiveData by lazy { _loadMoreStatusLiveData.asLiveData() }
    protected val _refreshStatusLiveData by lazy { MutableLiveData(false) }
    val refreshStatusLiveData by lazy { _refreshStatusLiveData.asLiveData() }

    /***
     * Activity#onCreate()后调用
     * Fragment#onCreateView()后调用
     */
    open fun init() {

    }

    fun <T : Any> getPagingFlowData(pageSource: PagingSource<Int, T>): Flow<PagingData<T>> {
        return PagingHelper.getPager(pageSource).cachedIn(viewModelScope)
    }
}