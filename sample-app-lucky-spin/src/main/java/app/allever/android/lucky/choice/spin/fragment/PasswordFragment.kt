package app.allever.android.lucky.choice.spin.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lib.core.base.AbstractFragment
import app.allever.android.lucky.choice.spin.databinding.LsFragmentPasswordBinding
import app.allever.android.lucky.choice.spin.utils.RandomPasswordHelper
import app.allever.android.lucky.choice.spin.utils.copyToClipboard
import app.allever.android.lucky.choice.spin.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PasswordFragment : AbstractFragment() {

    private var _binding: LsFragmentPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModel()

    private val sp by lazy {
        requireActivity().getSharedPreferences("password", Context.MODE_PRIVATE)
    }

    companion object {
        const val SP_KEY_PASSWORD_LENGTH = "password_length"
        const val SP_KEY_PASSWORD_UPPERCASE = "password_uppercase"
        const val SP_KEY_PASSWORD_LOWERCASE = "password_lowercase"
        const val SP_KEY_PASSWORD_DIGITS = "password_digits"
        const val SP_KEY_PASSWORD_SPECIAL = "password_special"
        const val SP_KEY_PASSWORD_AMBIGUOUS = "password_ambiguous"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LsFragmentPasswordBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.updatePadding(top = systemInsets.top)
            insets
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set initial value from shared preferences
        sp.getInt(SP_KEY_PASSWORD_LENGTH, 10).also {
            binding.passwordLength.text = it.toString()
            binding.slider.value = it.toFloat()
        }
        binding.uppercase.isChecked = sp.getBoolean(SP_KEY_PASSWORD_UPPERCASE, true)
        binding.lowercase.isChecked = sp.getBoolean(SP_KEY_PASSWORD_LOWERCASE, true)
        binding.digits.isChecked = sp.getBoolean(SP_KEY_PASSWORD_DIGITS, true)
        binding.special.isChecked = sp.getBoolean(SP_KEY_PASSWORD_SPECIAL, false)
        binding.ambiguous.isChecked = sp.getBoolean(SP_KEY_PASSWORD_AMBIGUOUS, false)


        // set listener for view
        binding.slider.addOnChangeListener { _, value, _ ->
            binding.passwordLength.text = value.toInt().toString()
        }
        binding.ambiguousFrameLayout.setOnClickListener {
            binding.ambiguous.toggle()
        }

        binding.copyButton.setOnClickListener {
            val password = viewModel.randomPassword.value
            if (password.isNotEmpty()) {
                requireContext().copyToClipboard(password)
                Toast.makeText(requireContext(), "Password copied", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fab.setOnClickListener {
            RandomPasswordHelper.generate(
                includeUppercase = binding.uppercase.isChecked,
                includeLowercase = binding.lowercase.isChecked,
                includeNumbers = binding.digits.isChecked,
                includeSpecial = binding.special.isChecked,
                includeAmbiguous = binding.ambiguous.isChecked,
                length = binding.slider.value.toInt()
            ).also {
                viewModel.randomPassword.value = it
            }

            sp.edit {
                putInt(SP_KEY_PASSWORD_LENGTH, binding.slider.value.toInt())
                putBoolean(SP_KEY_PASSWORD_UPPERCASE, binding.uppercase.isChecked)
                putBoolean(SP_KEY_PASSWORD_LOWERCASE, binding.lowercase.isChecked)
                putBoolean(SP_KEY_PASSWORD_DIGITS, binding.digits.isChecked)
                putBoolean(SP_KEY_PASSWORD_SPECIAL, binding.special.isChecked)
                putBoolean(SP_KEY_PASSWORD_AMBIGUOUS, binding.ambiguous.isChecked)
            }
        }

        // observe data from view model
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.randomPassword.collect { password ->
                    binding.password.isVisible = password.isNotEmpty()
                    binding.copyButton.isVisible = password.isNotEmpty()
                    binding.placeholderIcon.isVisible = password.isEmpty()
                    binding.password.text = password
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}