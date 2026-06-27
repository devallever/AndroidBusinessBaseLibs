package com.clean.wood.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clean.wood.data.AdManager
import com.clean.wood.utils.Constant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    var progressLiveData = MutableLiveData(0)
    private var mProgressStep = Constant.ANIMATION_MAX_DURATION / 100
    private var finishProgress = false
    private var startProgressTime = 0L
    private var cancelProgress = false
    var fromBg = false
    var clickAgreePermission = false

    fun startProgress() {
        startProgressTime = System.currentTimeMillis()
        viewModelScope.launch {
            while (progressLiveData.value!! < 100 && !finishProgress && !cancelProgress) {
                val progress = progressLiveData.value!! + 1
                progressLiveData.postValue(progress)
                delay(mProgressStep)
            }
        }
    }

    //when ad fail || ready
     fun finishProgress() {
        finishProgress = true
        mProgressStep = 10L
        val endTime = System.currentTimeMillis()
        val duration = endTime - startProgressTime
        if (duration < Constant.ANIMATION_MIN_DURATION) {
            mProgressStep = (Constant.ANIMATION_MIN_DURATION - duration) / (100 - progressLiveData.value!!)
        }
        viewModelScope.launch {
            while (progressLiveData.value!! < 100 && !cancelProgress) {
                val progress = progressLiveData.value!! + 1
                progressLiveData.postValue(progress)
//                if (progress == 100) {
//                    log("duration = ${System.currentTimeMillis() - startProgressTime}")
//                }
                delay(mProgressStep)
            }
        }
    }

    fun checkAd() {
        AdManager.ins.checkAd(Constant.AdPosition.HomeNative)
        AdManager.ins.checkAd(Constant.AdPosition.BackupNative)

        AdManager.ins.checkAd(Constant.AdPosition.SplashInter)
        AdManager.ins.checkAd(Constant.AdPosition.EnterInter)
        AdManager.ins.checkAd(Constant.AdPosition.BackupInter)
    }

    fun cancelProgress() {
        cancelProgress = true
    }

}