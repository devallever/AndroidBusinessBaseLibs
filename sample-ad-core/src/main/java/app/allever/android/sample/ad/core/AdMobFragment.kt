package app.allever.android.sample.ad.core

import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.provider.admob.AdMobAdProvider
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.base.BaseAdProviderFragment
import app.allever.android.sample.ad.core.config.ProviderConfigConstants
import app.allever.android.sample.ad.core.databinding.FragmentBaseAdProviderBinding

class AdMobFragment : BaseAdProviderFragment<FragmentBaseAdProviderBinding, BaseViewModel>() {

    override val providerName: String = AdMobAdProvider.PROVIDER_NAME

    override val providerConfig = ProviderConfigConstants.ADMOB

    override fun getProviderClass(): Class<out IAdProvider> = AdMobAdProvider::class.java
}
