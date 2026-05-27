package app.allever.android.sample.ad.core

import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.provider.bigo.BigoAdProvider
import app.allever.android.lib.common.databinding.FragmentTabBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.base.BaseAdProviderFragment
import app.allever.android.sample.ad.core.config.ProviderConfigConstants

class BigoFragment : BaseAdProviderFragment<FragmentTabBinding, BaseViewModel>() {

    override val providerName: String = BigoAdProvider.PROVIDER_NAME

    override val providerConfig = ProviderConfigConstants.BIGO

    override fun getProviderClass(): Class<out IAdProvider> = BigoAdProvider::class.java
}
