package app.allever.android.lucky.choice.spin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.allever.android.lucky.choice.spin.data.Option
import app.allever.android.lucky.choice.spin.data.OptionDao
import app.allever.android.lucky.choice.spin.data.Wheel
import app.allever.android.lucky.choice.spin.data.WheelDao
import app.allever.android.lucky.choice.spin.data.WheelWithOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WheelViewModel(
    private val wheelDao: WheelDao,
    private val optionDao: OptionDao
) : ViewModel() {

    fun createWheel(name: String, options: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val wheel = Wheel(name = name)
            val wheelId = wheelDao.insertWheel(wheel)
            val optionsEntities = options.map { Option(wheelId = wheelId, name = it) }
            optionDao.insertOptions(optionsEntities)
        }
    }

    fun deleteWheel(extraWheelId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            wheelDao.deleteWheelById(extraWheelId)
        }
    }

    fun updateWheel(extraWheelId: Long, wheelName: String, options: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            optionDao.deleteOptionsByWheelId(extraWheelId)
            wheelDao.updateWheelNameById(extraWheelId, wheelName)
            val optionsEntities = options.map { Option(wheelId = extraWheelId, name = it) }
            optionDao.insertOptions(optionsEntities)
        }
    }

    val allWheelsAndOptions: Flow<List<WheelWithOptions>> = wheelDao.getWheelsAndOptions()
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
}