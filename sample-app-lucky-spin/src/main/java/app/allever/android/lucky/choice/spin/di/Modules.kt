package app.allever.android.lucky.choice.spin.di

import app.allever.android.lucky.choice.spin.data.LuckSpinDatabase
import app.allever.android.lucky.choice.spin.data.OptionDao
import app.allever.android.lucky.choice.spin.data.WheelDao
import app.allever.android.lucky.choice.spin.viewmodel.MainViewModel
import app.allever.android.lucky.choice.spin.viewmodel.WheelViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<LuckSpinDatabase> { LuckSpinDatabase.getInstance(get()) }
    single<WheelDao> { get<LuckSpinDatabase>().wheelDao() }
    single<OptionDao> { get<LuckSpinDatabase>().optionDao() }

    viewModel { MainViewModel() }
    viewModel { WheelViewModel(get(), get()) }
}