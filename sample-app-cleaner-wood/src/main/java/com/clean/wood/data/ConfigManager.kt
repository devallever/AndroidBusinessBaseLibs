package com.clean.wood.data


class ConfigManager private constructor() {
    companion object {
        val ins by lazy {
            ConfigManager()
        }
    }

    fun fetchConfig() {

    }

}