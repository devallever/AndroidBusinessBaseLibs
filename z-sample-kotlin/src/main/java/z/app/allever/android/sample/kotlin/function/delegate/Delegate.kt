package z.app.allever.android.sample.kotlin.function.delegate

import kotlin.reflect.KProperty

class Delegate {

    var propValue: Student? = null

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): Student? {
        return propValue
    }

    operator fun setValue(nothing: Nothing?, property: KProperty<*>, any: Student?) {
        propValue = any
    }
}