package com.clean.wood.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.clean.wood.R
import com.clean.wood.databinding.FragmentSettingsBinding
import com.clean.wood.ui.dialog.RateUsDialog
import com.clean.wood.utils.Constant

class SettingsFragment : BaseFragment() {

    private val mRateUsDialog by lazy {
        RateUsDialog(requireContext())
    }

    override fun stackKey(): String {
        return "/settings"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSettingsBinding.inflate(layoutInflater)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.apply {
            includeTopBar.tvTitle.text = getString(R.string.setting)
            includeTopBar.ivBack.setOnClickListener {
                pop()
            }

            itemPrivacyContainer.setOnClickListener {
                pushFragment(WebFragment(Constant.PRIVACY_URL, getString(R.string.privacy)))
            }

            itemRateUsContainer.setOnClickListener {
                mRateUsDialog.show()
            }
        }

        return binding.root
    }

}