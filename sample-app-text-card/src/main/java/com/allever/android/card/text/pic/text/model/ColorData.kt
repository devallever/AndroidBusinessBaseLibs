package com.allever.android.card.text.pic.text.model

data class ColorData(
    val name: String,
    val colorValue: MutableList<String>,
    var selected: Boolean = false
) {


    // ColorData("", mutableListOf("#", "#"))
    companion object {
        //template media light
        val MEDIA_BLUE_CYAN_GRADIENT =
            ColorData("Blue cyan gradient", mutableListOf("#78dac5", "#60a5f9"))
        val MEDIA_BLUE_PINK_GRADIENT =
            ColorData("Blue pink gradient", mutableListOf("#d2c9d9", "#39befa"))
        val MEDIA_BLUE_LIME_GRADIENT =
            ColorData("Blue lime gradient", mutableListOf("#a0e59b", "#7fc3ed"))
        val MEDIA_PINK_BLUE_GRADIENT =
            ColorData("Pink blue gradient", mutableListOf("#39befa", "#d2c9d9"))
        val MEDIA_BLUE_VIOLET_GRADIENT =
            ColorData("Blue violet gradient", mutableListOf("#b3bffd", "#8b8cf9"))
        val MEDIA_SKY_ORANGE_GRADIENT =
            ColorData("Sky orange gradient", mutableListOf("#e2d4c5", "#9fcbfd"))
        val MEDIA_PURPLE_YELLOW_GRADIENT =
            ColorData("Purple yellow gradient", mutableListOf("#f3e7d7", "#dabdff"))
        val MEDIA_PINK_GRADIENT = ColorData("Pink gradient", mutableListOf("#fdbdbd", "#fa8280"))
        val MEDIA_PINK_RED_GRADIENT =
            ColorData("Pink red gradient", mutableListOf("#f67286", "#f471b5"))
        val MEDIA_RED_ORANGE_GRADIENT =
            ColorData("Red orange gradient", mutableListOf("#fdab99", "#fdc283"))
        val MEDIA_LIGHT_DARK_ORANGE_GRADIENT =
            ColorData("Dark orange gradient", mutableListOf("#fde589", "#fd9641"))
        val MEDIA_LIGHT_BLUE_PURPLE_GRADIENT =
            ColorData("Light blue purple gradient", mutableListOf("#c4d8fd", "#d3b4fd"))
        val MEDIA_YELLOW_ORANGE_GRADIENT =
            ColorData("Yellow orange gradient", mutableListOf("#FDF19C", "#FEDC63"))
        val MEDIA_GREEN_GRADIENT = ColorData("Green gradient", mutableListOf("#80EDAF", "#50DB9F"))
        val MEDIA_GRAY_GRADIENT = ColorData("Gray gradient", mutableListOf("#f8f8f8", "#f4f4f4"))


        //template media dark
        val MEDIA_DARK_BLUE_GRADIENT =
            ColorData("Dark blue gradient", mutableListOf("#14214d", "#061735"))
        val MEDIA_DARK_INDIGO_GRADIENT =
            ColorData("Dark indigo gradient", mutableListOf("#272767", "#100d21"))
        val MEDIA_DARK_SKY_GRADIENT =
            ColorData("Dark sky gradient", mutableListOf("#0c405e", "#071420"))
        val MEDIA_DARK_CYAN_GRADIENT =
            ColorData("Dark cyan gradient", mutableListOf("#0f3346", "#071320"))
        val MEDIA_DARK_VIOLET_GRADIENT =
            ColorData("Dark violet gradient", mutableListOf("#40197d", "#150a25"))
        val MEDIA_DARK_FUSHSIA_GRADIENT =
            ColorData("Dark fushsia gradient", mutableListOf("#591868", "#1f0d3d"))
        val MEDIA_DARK_ROSE_GRADIENT =
            ColorData("Dark rose gradient", mutableListOf("#70102c", "#1d080c"))
        val MEDIA_DARK_EMERALD_GRADIENT =
            ColorData("Dark emerald gradient", mutableListOf("#0d4224", "#03160c"))
        val MEDIA_DARK_GRAY_GRADIENT =
            ColorData("Dark gray gradient", mutableListOf("#1a2432", "#0d1221"))
        val MEDIA_DARK_SLATE_GRADIENT =
            ColorData("Dark slate gradient", mutableListOf("#192437", "#0d1025"))
        val MEDIA_DARK_NEUTRAL_GRADIENT =
            ColorData("Dark neutral gradient", mutableListOf("#343434", "#0f0f0f"))
        val MEDIA_DARK_STONE_GRADIENT =
            ColorData("Dark stone gradient", mutableListOf("#44423d", "#191412"))

        //Template Solid light
        val SOLID_CYAN = ColorData("Cyan", mutableListOf("#07b6d1", "#07b6d1"))
        val SOLID_LIGHT_CYAN = ColorData("Light cyan", mutableListOf("#24d1ec", "#24d1ec"))
        val SOLID_BLUE = ColorData("Blue", mutableListOf("#3c82f6", "#3c82f6"))
        val SOLID_LIGHT_BLUE = ColorData("Light blue", mutableListOf("#60A5FA", "#60A5FA"))
        val SOLID_TEAL = ColorData("Teal", mutableListOf("#15B8A6", "#15B8A6"))
        val SOLID_GREEN = ColorData("Green", mutableListOf("#22c55d", "#22c55d"))
        val SOLID_LIGHT_GREEN = ColorData("Light green", mutableListOf("#49DE80", "#49DE80"))
        val SOLID_EMERALD = ColorData("Emerald", mutableListOf("#12b980", "#12b980"))
        val SOLID_ROSE = ColorData("Rose", mutableListOf("#F87171", "#F87171"))
        val SOLID_LIGHT_ROSE = ColorData("Light rose", mutableListOf("#F471B7", "#F471B7"))
        val SOLID_LIGHT_PURPLE = ColorData("Light purple", mutableListOf("#c084fc", "#c084fc"))
        val SOLID_ORANGE = ColorData("Orange", mutableListOf("#f97316", "#f97316"))
        val SOLID_LIGHT_ORANGE = ColorData("Light orange", mutableListOf("#fb923c", "#fb923c"))
        val SOLID_YELLOW = ColorData("Yellow", mutableListOf("#facc14", "#facc14"))
        val SOLID_WHITE = ColorData("White", mutableListOf("#e4e4e7", "#e4e4e7"))

        //Template solid dark
        val SOLID_DARK_GRAY_RED = ColorData("Gray red", mutableListOf("#7F1C1E", "#7F1C1E"))
        val SOLID_DARK_GRAY_GREEN = ColorData("Gray green", mutableListOf("#14532D", "#14532D"))
        val SOLID_DARK_GRAY_VIOLET = ColorData("Gray violit", mutableListOf("#250f4c", "#250f4c"))
        val SOLID_DARK_GRAY_INDIGO = ColorData("Gray indigo", mutableListOf("#181637", "#181637"))
        val SOLID_DARK_GRAY_BLUE = ColorData("Gray blue", mutableListOf("#172554", "#172554"))
        val SOLID_DARK_GRAY_CYAN = ColorData("Gray cyan", mutableListOf("#093344", "#093344"))
        val SOLID_DARK_DARK_CYAN = ColorData("Dark cyan", mutableListOf("#0a191f", "#0a191f"))
        val SOLID_DARK_DARK_INDIGO = ColorData("Dark indigo", mutableListOf("#07060f", "#07060f"))
        val SOLID_DARK_DARK_GREEN = ColorData("Dark green", mutableListOf("#021b0e", "#021b0e"))
        val SOLID_DARK_DARK_BLUE = ColorData("Dark blue", mutableListOf("#010512", "#010512"))
        val SOLID_DARK_DARK_VIOLET = ColorData("Dark violet", mutableListOf("#0d0816", "#0d0816"))
        val SOLID_DARK_LIGHT_WARM_GRAY =
            ColorData("Light warm gray", mutableListOf("#262626", "#262626"))
        val SOLID_DARK_LIGHT_COLD_GRAY =
            ColorData("Light cold gray", mutableListOf("#1e293b", "#1e293b"))
        val SOLID_DARK_BLACK = ColorData("Black", mutableListOf("#0a0a0a", "#0a0a0a"))

        //ColorData("", mutableListOf("#", "#"))

        //template Bento light
        val BENTO_BLUE_CYAN_GRADIENT =
            ColorData("Blue cyan gradient", mutableListOf("#78dac5", "#60a5f9"))
        val BENTO_BLUE_PINK_GRADIENT =
            ColorData("Blue pink gradient", mutableListOf("#d2c9d9", "#39befa"))
        val BENTO_BLUE_LIME_GRADIENT =
            ColorData("Blue lime gradient", mutableListOf("#a0e59b", "#7fc3ed"))
        val BENTO_PINK_BLUE_GRADIENT =
            ColorData("Pink blue gradient", mutableListOf("#39befa", "#d2c9d9"))
        val BENTO_BLUE_VIOLET_GRADIENT =
            ColorData("Blue violet gradient", mutableListOf("#b3bffd", "#8b8cf9"))
        val BENTO_SKY_ORANGE_GRADIENT =
            ColorData("Sky orange gradient", mutableListOf("#e2d4c5", "#9fcbfd"))
        val BENTO_PURPLE_YELLOW_GRADIENT =
            ColorData("Purple yellow gradient", mutableListOf("#f3e7d7", "#dabdff"))
        val BENTO_PINK_GRADIENT = ColorData("Pink gradient", mutableListOf("#fdbdbd", "#fa8280"))
        val BENTO_PINK_RED_GRADIENT =
            ColorData("Pink red gradient", mutableListOf("#f67286", "#f471b5"))
        val BENTO_RED_ORANGE_GRADIENT =
            ColorData("Red orange gradient", mutableListOf("#fdab99", "#fdc283"))
        val BENTO_LIGHT_DARK_ORANGE_GRADIENT =
            ColorData("Dark orange gradient", mutableListOf("#fde589", "#fd9641"))
        val BENTO_LIGHT_BLUE_PURPLE_GRADIENT =
            ColorData("Light blue purple gradient", mutableListOf("#c4d8fd", "#d3b4fd"))
        val BENTO_YELLOW_ORANGE_GRADIENT =
            ColorData("Yellow orange gradient", mutableListOf("#FDF19C", "#FEDC63"))
        val BENTO_GREEN_GRADIENT = ColorData("Green gradient", mutableListOf("#80EDAF", "#50DB9F"))
        val BENTO_GRAY_GRADIENT = ColorData("Gray gradient", mutableListOf("#f8f8f8", "#f4f4f4"))


        // ColorData("", mutableListOf("#", "#"))
    }
}