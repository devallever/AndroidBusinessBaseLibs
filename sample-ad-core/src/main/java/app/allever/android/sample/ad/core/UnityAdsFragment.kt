package app.allever.android.sample.ad.core

import app.allever.android.lib.ad.core.base.IAdProvider
import app.allever.android.lib.ad.provider.bigo.BigoAdProvider
import app.allever.android.lib.ad.provider.unity.UnityAdProvider
import app.allever.android.lib.common.databinding.FragmentTabBinding
import app.allever.android.lib.mvvm.base.BaseViewModel
import app.allever.android.sample.ad.core.base.BaseAdProviderFragment
import app.allever.android.sample.ad.core.config.ProviderConfigConstants

class UnityAdsFragment : BaseAdProviderFragment<FragmentTabBinding, BaseViewModel>() {

    override val providerName: String = UnityAdProvider.PROVIDER_NAME

    override val providerConfig = ProviderConfigConstants.UNITYADS

    override fun getProviderClass(): Class<out IAdProvider> = UnityAdProvider::class.java
}
