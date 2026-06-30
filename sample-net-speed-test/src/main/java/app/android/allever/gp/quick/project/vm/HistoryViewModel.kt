package app.android.allever.gp.quick.project.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.android.allever.gp.quick.project.SpeedTest
import app.android.allever.gp.quick.project.core.Record
import app.android.allever.gp.quick.project.ui.adapter.HistoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel: BaseViewModel() {
    var adapter = HistoryAdapter()
    val recordLiveData = MutableLiveData<MutableList<Record>>()
    var isTimeAsc = true
    var isNetworkTypeAsc = true
    var isDownloadSpeedAsc = true
    var isUploadSpeedAsc = true

    fun fetchData() {
        viewModelScope.launch (Dispatchers.IO){
            recordLiveData.postValue(mutableListOf<Record>().apply {
                addAll(SpeedTest.getAllRecord())
            })
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            SpeedTest.deleteAllRecord()
            fetchData()
        }
    }

    fun deleteRecord(record: Record) {
        viewModelScope.launch(Dispatchers.IO) {
            record.delete()
            fetchData()
        }
    }

    fun fetchDataByTimeOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            isTimeAsc = !isTimeAsc
            recordLiveData.postValue(mutableListOf<Record>().apply {
                addAll(SpeedTest.getAllByOrderTime(isTimeAsc))
            })
        }
    }

    fun fetchDataByNetworkTypeOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            isNetworkTypeAsc = !isNetworkTypeAsc
            recordLiveData.postValue(mutableListOf<Record>().apply {
                addAll(SpeedTest.getAllByOrderNetworkType(isNetworkTypeAsc))
            })
        }
    }

    fun fetchDataByDownloadSpeedOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            isDownloadSpeedAsc = !isDownloadSpeedAsc
            recordLiveData.postValue(mutableListOf<Record>().apply {
                addAll(SpeedTest.getAllByOrderDownloadSpeed(isDownloadSpeedAsc))
            })
        }
    }

    fun fetchDataByUploadSpeedOrder() {
        viewModelScope.launch(Dispatchers.IO) {
            isUploadSpeedAsc = !isUploadSpeedAsc
            recordLiveData.postValue(mutableListOf<Record>().apply {
                addAll(SpeedTest.getAllByOrderUploadSpeed(isUploadSpeedAsc))
            })
        }
    }
}