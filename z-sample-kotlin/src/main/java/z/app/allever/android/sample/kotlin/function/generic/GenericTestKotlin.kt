package z.app.allever.android.sample.kotlin.function.generic

fun main() {

    val animalList = mutableListOf<Animal>()
    val catList = mutableListOf<Cat>()
    val garfieldCatList = mutableListOf<Cat>()

    var catListOut: MutableList<out Cat> = catList
//    catListOut = animalList
    catListOut = garfieldCatList

    var catListIn: MutableList<in Cat>
    catListIn = catList
    catListIn = animalList
    catListIn = garfieldCatList
}
