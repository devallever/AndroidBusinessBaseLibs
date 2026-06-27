package com.clean.wood.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clean.wood.data.AppManager
import kotlinx.coroutines.launch

class ScanAppViewModel: ViewModel() {
    fun scanApp() {
        viewModelScope.launch {
            val appList = AppManager.ins.scanApp()
        }
    }
}