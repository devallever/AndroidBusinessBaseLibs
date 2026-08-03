package app.allever.android.lucky.choice.spin.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel: ViewModel() {

    val randomNumbers : MutableStateFlow<List<Int>> = MutableStateFlow(emptyList())
    val randomPassword : MutableStateFlow<String> = MutableStateFlow("")

}