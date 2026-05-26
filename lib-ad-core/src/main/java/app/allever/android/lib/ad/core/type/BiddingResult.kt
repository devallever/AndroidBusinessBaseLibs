package app.allever.android.lib.ad.core.type

data class BiddingResult(
    val providerType: String,
    val eCPM: Double,
    val adType: AdType,
    val loadTime: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedPrice: String
        get() = "$%.2f".format(eCPM)

    override fun toString(): String {
        return "BiddingResult(provider='$providerType', eCPM=\$${formattedPrice}, " +
                "adType=${adType.name}, time=${loadTime}ms)"
    }
}
