package z.app.allever.android.sample.kotlin.function.coroutine.retrofit

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/***
 * {"sentences":[{"trans":"串","orig":"string","backend":2},{"translit":"Chuàn","src_translit":"striNG"}],"dict":[{"pos":"名词","terms":["串","弦","线","绳","绳子","细线","鞭","绲"],"entry":[{"word":"串","reverse_translation":["string"],"score":0.13323711},{"word":"弦","reverse_translation":["string","chord","bowstring","hypotenuse","subtense","string of musical instrument"],"score":0.016418032},{"word":"线","reverse_translation":["line","wire","thread","string","route","filament"],"score":0.0058540297},{"word":"绳","reverse_translation":["rope","cord","string"],"score":0.00477792},{"word":"绳子","reverse_translation":["rope","string","cord"],"score":0.0023652418},{"word":"细线","reverse_translation":["thread","string"],"score":2.2698537E-4},{"word":"鞭","reverse_translation":["whip","lash","string","firecracker","iron staff used as a weapon"],"score":8.139759E-6},{"word":"绲","reverse_translation":["cord","embroidered sash","string"],"score":2.4439987E-6}],"base_form":"string","pos_enum":1},{"pos":"动词","terms":["纫"],"entry":[{"word":"纫","reverse_translation":["thread","string"],"score":4.860472E-6}],"base_form":"string","pos_enum":2}],"src":"en","confidence":0.9488189,"ld_result":{"srclangs":["en"],"srclangs_confidences":[0.9488189],"extended_srclangs":["en"]}}
 */
@Keep
data class TranslationResponse(
    /**
     * sentences : [{"trans":"串","orig":"string","backend":2},{"translit":"Chuàn","src_translit":"striNG"}]
     * dict : [{"pos":"名词","terms":["串","弦","线","绳","绳子","细线","鞭","绲"],"entry":[{"word":"串","reverse_translation":["string"],"score":0.13323711},{"word":"弦","reverse_translation":["string","chord","bowstring","hypotenuse","subtense","string of musical instrument"],"score":0.016418032},{"word":"线","reverse_translation":["line","wire","thread","string","route","filament"],"score":0.0058540297},{"word":"绳","reverse_translation":["rope","cord","string"],"score":0.00477792},{"word":"绳子","reverse_translation":["rope","string","cord"],"score":0.0023652418},{"word":"细线","reverse_translation":["thread","string"],"score":2.2698537E-4},{"word":"鞭","reverse_translation":["whip","lash","string","firecracker","iron staff used as a weapon"],"score":8.139759E-6},{"word":"绲","reverse_translation":["cord","embroidered sash","string"],"score":2.4439987E-6}],"base_form":"string","pos_enum":1},{"pos":"动词","terms":["纫"],"entry":[{"word":"纫","reverse_translation":["thread","string"],"score":4.860472E-6}],"base_form":"string","pos_enum":2}]
     * src : en
     * confidence : 0.9488189
     * ld_result : {"srclangs":["en"],"srclangs_confidences":[0.9488189],"extended_srclangs":["en"]}
     */
    val src: String,
    val confidence: Double,
    @SerializedName("ld_result") val ldResult: LdResult? = null,
    val sentences: List<Sentences>? = null,
    val dict: List<Dict>? = null
)

@Keep
data class LdResult(
    @SerializedName("srclangs") val srcLang: List<String>? = null,
    @SerializedName("srclangs_confidences") val srcLangConfidences: List<Double>? = null,
    @SerializedName("extended_srclangs") val extendedSrcLang: List<String>? = null
)

@Keep
data class Sentences(
    /**
     * trans : 串
     * orig : string
     * backend : 2
     * translit : Chuàn
     * src_translit : striNG
     */

    val trans: String,
    val orig: String,
    val backend: Int = 0,
    @SerializedName("translit") val transLit: String? = null,
    @SerializedName("src_translit") val srcTransLit: String? = null
)

@Keep
data class Dict(
    /**
     * pos : 名词
     * terms : ["串","弦","线","绳","绳子","细线","鞭","绲"]
     * entry : [{"word":"串","reverse_translation":["string"],"score":0.13323711},{"word":"弦","reverse_translation":["string","chord","bowstring","hypotenuse","subtense","string of musical instrument"],"score":0.016418032},{"word":"线","reverse_translation":["line","wire","thread","string","route","filament"],"score":0.0058540297},{"word":"绳","reverse_translation":["rope","cord","string"],"score":0.00477792},{"word":"绳子","reverse_translation":["rope","string","cord"],"score":0.0023652418},{"word":"细线","reverse_translation":["thread","string"],"score":2.2698537E-4},{"word":"鞭","reverse_translation":["whip","lash","string","firecracker","iron staff used as a weapon"],"score":8.139759E-6},{"word":"绲","reverse_translation":["cord","embroidered sash","string"],"score":2.4439987E-6}]
     * base_form : string
     * pos_enum : 1
     */

    val pos: String? = null,
    @SerializedName("base_form") val basForm: String? = null,
    @SerializedName("pos_enum")val posEnum: Int = 0,
    val terms: List<String>? = null,
    val entry: List<Entry>? = null
)

@Keep
data class Entry(
    /**
     * word : 串
     * reverse_translation : ["string"]
     * score : 0.13323711
     */

    val word: String? = null,
    val score: Double = 0.toDouble(),
    @SerializedName("reverse_translation") val reverseTranslation: List<String>? = null
)