package org.xm.app.virtual.call.ad

import com.allever.lib.ad.AdBusiness
import com.allever.lib.ad.admob.AdMobBusiness
import com.allever.lib.ad.chain.IAdBusiness
import com.allever.lib.ad.chain.IAdBusinessFactory
import com.allever.lib.ad.mimo.MiMoBusiness

class AdFactory : IAdBusinessFactory {
    override fun getAdBusiness(businessName: String): IAdBusiness? {
        return when (businessName) {
            AdBusiness.A -> {
                AdMobBusiness
//                null
            }
            AdBusiness.MI -> {
                MiMoBusiness
            }
            else -> {
                null
            }
        }
    }
}