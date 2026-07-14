package com.allever.android.card.text.pic.text.model

import com.allever.android.card.text.pic.text.R
import com.allever.android.card.text.pic.text.util.GsonHelper
import com.allever.android.card.text.pic.text.util.StoreManager
import com.allever.android.card.text.pic.text.util.log
import com.allever.android.card.text.pic.text.util.toJson

object TextCardCore {
    var cardData = getCardDataObj()

    val iconTypeData = mutableListOf(
        IconTypeData(
            "Abstract", mutableListOf(
                IconData(R.drawable.icon_abstract_01, true),
                IconData(R.drawable.icon_abstract_02),
                IconData(R.drawable.icon_abstract_03),
                IconData(R.drawable.icon_abstract_04),
                IconData(R.drawable.icon_abstract_05),
                IconData(R.drawable.icon_abstract_06),
                IconData(R.drawable.icon_abstract_07),
                IconData(R.drawable.icon_abstract_08),
                IconData(R.drawable.icon_abstract_09),
                IconData(R.drawable.icon_abstract_10),
            )
        ),
        IconTypeData(
            "Object & Tool", mutableListOf(
                IconData(R.drawable.icon_tool_01),
                IconData(R.drawable.icon_tool_02),
                IconData(R.drawable.icon_tool_03),
                IconData(R.drawable.icon_tool_04),
                IconData(R.drawable.icon_tool_05),
                IconData(R.drawable.icon_tool_06),
                IconData(R.drawable.icon_tool_07),
                IconData(R.drawable.icon_tool_08),
                IconData(R.drawable.icon_tool_09),
                IconData(R.drawable.icon_tool_10),
                IconData(R.drawable.icon_tool_11),
                IconData(R.drawable.icon_tool_12),
                IconData(R.drawable.icon_tool_13),
                IconData(R.drawable.icon_tool_14),
                IconData(R.drawable.icon_tool_15),
                IconData(R.drawable.icon_tool_16),
                IconData(R.drawable.icon_tool_17),
                IconData(R.drawable.icon_tool_18),


                )
        ),
        IconTypeData(
            "Device", mutableListOf(
                IconData(R.drawable.icon_device_01),
                IconData(R.drawable.icon_device_02),
                IconData(R.drawable.icon_device_03),
                IconData(R.drawable.icon_device_04),
                IconData(R.drawable.icon_device_05),
                IconData(R.drawable.icon_device_06),
                IconData(R.drawable.icon_device_07),
                IconData(R.drawable.icon_device_08),
                IconData(R.drawable.icon_device_09),
                IconData(R.drawable.icon_device_10),
                IconData(R.drawable.icon_device_11),
                IconData(R.drawable.icon_device_12),
                IconData(R.drawable.icon_device_13),
                IconData(R.drawable.icon_device_14),
                IconData(R.drawable.icon_device_15),
                IconData(R.drawable.icon_device_16),
                IconData(R.drawable.icon_device_17),
                IconData(R.drawable.icon_device_18),
                IconData(R.drawable.icon_device_19),
                IconData(R.drawable.icon_device_20),
                IconData(R.drawable.icon_device_21),
                IconData(R.drawable.icon_device_22),
                IconData(R.drawable.icon_device_23),
                IconData(R.drawable.icon_device_24),
                IconData(R.drawable.icon_device_25),
                IconData(R.drawable.icon_device_26),
                IconData(R.drawable.icon_device_27),
                IconData(R.drawable.icon_device_28),
                IconData(R.drawable.icon_device_29),
                IconData(R.drawable.icon_device_30),
                IconData(R.drawable.icon_device_31),
                IconData(R.drawable.icon_device_32),

                )
        ),
        IconTypeData(
            "Human", mutableListOf(
                IconData(R.drawable.icon_human_01),
                IconData(R.drawable.icon_human_02),
                IconData(R.drawable.icon_human_03),
                IconData(R.drawable.icon_human_04),
                IconData(R.drawable.icon_human_05),
                IconData(R.drawable.icon_human_06),
                IconData(R.drawable.icon_human_07),
                IconData(R.drawable.icon_human_08),
                IconData(R.drawable.icon_human_09),
                IconData(R.drawable.icon_human_10),
                IconData(R.drawable.icon_human_11),
                IconData(R.drawable.icon_human_12),
                IconData(R.drawable.icon_human_13),
                IconData(R.drawable.icon_human_14),
                IconData(R.drawable.icon_human_15),
                IconData(R.drawable.icon_human_16),
                IconData(R.drawable.icon_human_17),
                IconData(R.drawable.icon_human_18),
                IconData(R.drawable.icon_human_19),
                IconData(R.drawable.icon_human_20),
                IconData(R.drawable.icon_human_21),
                IconData(R.drawable.icon_human_22),
                IconData(R.drawable.icon_human_23),
                IconData(R.drawable.icon_human_24),

                )
        ),
        IconTypeData(
            "Nature", mutableListOf(
                IconData(R.drawable.icon_nature_01),
                IconData(R.drawable.icon_nature_02),
                IconData(R.drawable.icon_nature_03),
                IconData(R.drawable.icon_nature_04),
                IconData(R.drawable.icon_nature_05),
                IconData(R.drawable.icon_nature_06),
                IconData(R.drawable.icon_nature_07),
                IconData(R.drawable.icon_nature_08),
                IconData(R.drawable.icon_nature_09),
                IconData(R.drawable.icon_nature_10),
                IconData(R.drawable.icon_nature_11),

                )
        ),
        IconTypeData(
            "Travel", mutableListOf(
                IconData(R.drawable.icon_travel_01),
                IconData(R.drawable.icon_travel_02),
                IconData(R.drawable.icon_travel_03),
                IconData(R.drawable.icon_travel_04),
                IconData(R.drawable.icon_travel_05),
                IconData(R.drawable.icon_travel_06),
                IconData(R.drawable.icon_travel_07),
                IconData(R.drawable.icon_travel_08),
                IconData(R.drawable.icon_travel_09),
                IconData(R.drawable.icon_travel_10),
                IconData(R.drawable.icon_travel_11),
                IconData(R.drawable.icon_travel_12),


                )
        )
    ).apply {
        map {
            it.iconList.map {
                if (it.icon == cardData.iconResId) {
                    it.selected = true
                } else {
                    it.selected = false
                }
            }
        }
    }

    val dateTimeFormatData = mutableListOf<DateTimeItem>().apply {
        val time = System.currentTimeMillis()
        DateFormat.FORMAT_LIST.map {
            add(DateTimeItem(time, it, it == cardData.dateFormatType))
        }
    }

    val wordCountFormatData = mutableListOf<WordFormatItem>().apply {
        WordCountFormat.FORMAT_LIST.map {
            add(WordFormatItem(it, it == cardData.wordCountFormatType))
        }
    }

    private const val KEY_CARD_DATA = "KEY_CARD_DATA"

    fun saveCardData() {
        val cardJson = cardData.toJson()
        log("saveCardData: $cardJson")
        StoreManager.putString(KEY_CARD_DATA, cardJson)
    }

    fun getCardDataObj(): CardData {
        val data = StoreManager.getString(KEY_CARD_DATA)
        log("getCardData: ${data}")
        if (data.isEmpty()) {
            return CardData()
        }
        val cardData = GsonHelper.fromJson(data, CardData::class.java) ?: CardData()
        return cardData
    }

}