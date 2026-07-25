package app.allever.android.ai.qr.scanner.ui

import android.app.AlertDialog
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.android.absbase.utils.DeviceUtils
import com.android.absbase.utils.ResourcesUtils
import com.google.zxing.client.android.PreferencesActivity
import app.allever.android.ai.qr.scanner.Config
import com.allever.app.qr.code.scaner.R
import app.allever.android.ai.qr.scanner.ui.widget.preference.PreferenceFragment
import app.allever.android.lib.core.helper.ActivityHelper
import app.allever.android.lib.core.helper.FeedbackHelper
import app.allever.android.lib.core.helper.ShareHelper
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList

class SettingFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    companion object {
        const val KEY_FEEDBACK = "preferences_feedback"
        const val KEY_ABOUT = "preferences_about"
        const val KEY_PREMIUM = "preferences_premium"
        const val KEY_SHARE = "preferences_share"
        const val KEY_SUPPORT = "preferences_support"
    }

    private var checkBoxPrefs: Array<TwoStatePreference?>? = null

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        if (Config.settingCompleteVersion) {
            addPreferencesFromResource(R.xml.setting_preferences)
        } else {
            addPreferencesFromResource(R.xml.setting_preferences_sample)
        }

        val preferences = preferenceScreen
        preferences.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        checkBoxPrefs = findDecodePrefs(preferences,
                PreferencesActivity.KEY_DECODE_1D_PRODUCT,
                PreferencesActivity.KEY_DECODE_1D_INDUSTRIAL,
                PreferencesActivity.KEY_DECODE_QR,
                PreferencesActivity.KEY_DECODE_DATA_MATRIX,
                PreferencesActivity.KEY_DECODE_AZTEC,
                PreferencesActivity.KEY_DECODE_PDF417)
        disableLastCheckedPref()

//        preferences.findPreference(KEY_FEEDBACK)?.onPreferenceClickListener = this
        preferences.findPreference(KEY_ABOUT)?.onPreferenceClickListener = this
//        preferences.findPreference(KEY_PREMIUM)?.onPreferenceClickListener = this
//        preferences.findPreference(KEY_SHARE)?.onPreferenceClickListener = this
//        preferences.findPreference(KEY_SUPPORT)?.onPreferenceClickListener = this


        val customProductSearch = preferences.findPreference(PreferencesActivity.KEY_CUSTOM_PRODUCT_SEARCH) as? EditTextPreference
        customProductSearch?.onPreferenceChangeListener = CustomSearchURLValidator()
    }

    override fun onCreateView(paramLayoutInflater: LayoutInflater, paramViewGroup: ViewGroup?, paramBundle: Bundle?): View? {
        val view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle)
        (view?.findViewById(android.R.id.list) as? ListView)?.let {
            it.setBackgroundResource(R.color.white)
            it.divider = ColorDrawable(ResourcesUtils.resources.getColor(R.color.qr_history_list_item_dividing))
            it.dividerHeight = DeviceUtils.dip2px(1f)
            it

        }
        return view
    }

    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: Preference?): Boolean {
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            KEY_ABOUT -> {
                ActivityHelper.startActivity(AboutActivity::class.java)
                true
            }
            KEY_FEEDBACK -> {
                FeedbackHelper.feedback(activity)
                true
            }
            KEY_SHARE -> {
                ShareHelper.shareText(this, "")
                true
            }
            KEY_SUPPORT -> {
                supportUs()
                true
            }
            else -> false
        }
    }

    private fun supportUs() {
//        AlertDialog.Builder(activity!!)
//            .setTitle("温馨提示")
//            .setMessage("该操作会消耗一定的数据流量，您要观看吗?")
//            .setPositiveButton("立即观看") { dialog, which ->
//                dialog.dismiss()
//            }
//            .setNegativeButton("残忍拒绝") { dialog, which ->
//                dialog.dismiss()
//                toast("您可以点击小广告，也是对我们的一种支持。")
//            }
//            .create()
//            .show()
    }


    private fun findDecodePrefs(preferences: PreferenceScreen, vararg keys: String): Array<TwoStatePreference?> {
        val prefs = arrayOfNulls<TwoStatePreference>(keys.size)
        for (i in keys.indices) {
            val pref = preferences.findPreference(keys[i])
            if (pref is TwoStatePreference) {
                prefs[i] = pref
            }
        }
        return prefs
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        disableLastCheckedPref()
    }

    private fun disableLastCheckedPref() {
        val checked = ArrayList<TwoStatePreference>(checkBoxPrefs!!.size)
        for (pref in checkBoxPrefs!!) {
            if (pref?.isChecked == true) {
                checked.add(pref)
            }
        }
        val disable = checked.size <= 1
        for (pref in checkBoxPrefs!!) {
            pref?.isEnabled = !(disable && checked.contains(pref))
        }
    }

    private inner class CustomSearchURLValidator : Preference.OnPreferenceChangeListener {
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            if (!isValid(newValue)) {
                val builder = AlertDialog.Builder(preference.context)
                builder.setTitle(com.google.zxing.client.android.R.string.zxing_msg_error)
                builder.setMessage(com.google.zxing.client.android.R.string.msg_invalid_value)
                builder.setCancelable(true)
                builder.show()
                return false
            }
            return true
        }

        private fun isValid(newValue: Any?): Boolean {
            // Allow empty/null value
            if (newValue == null) {
                return true
            }
            var valueString = newValue.toString()
            if (valueString.isEmpty()) {
                return true
            }
            // Before validating, remove custom placeholders, which will not
            // be considered valid parts of the URL in some locations:
            // Blank %t and %s:
            valueString = valueString.replace("%[st]".toRegex(), "")
            // Blank %f but not if followed by digit or a-f as it may be a hex sequence
            valueString = valueString.replace("%f(?![0-9a-f])".toRegex(), "")
            // Require a scheme otherwise:
            try {
                val uri = URI(valueString)
                return uri.scheme != null
            } catch (use: URISyntaxException) {
                return false
            }

        }
    }

}
