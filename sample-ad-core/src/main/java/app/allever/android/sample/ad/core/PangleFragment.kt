package app.allever.android.sample.ad.core

import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.provider.pangle.PangleAdProvider
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.base.BaseAdProviderFragment
import app.allever.android.sample.ad.core.config.ProviderConfigConstants
import app.allever.android.sample.ad.core.databinding.FragmentBaseAdProviderBinding

class PangleFragment : BaseAdProviderFragment<FragmentBaseAdProviderBinding, BaseViewModel>() {

    override val providerName: String = PangleAdProvider.PROVIDER_NAME

    override val providerConfig = ProviderConfigConstants.PANGLE

    override fun getProviderClass(): Class<out IAdProvider> = PangleAdProvider::class.java
}
