package app.allever.android.lucky.choice.spin.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.isVisible
import app.allever.android.lib.core.base.AbstractFragment
import app.allever.android.lucky.choice.spin.databinding.LsFragmentFingerBinding
import io.noties.markwon.Markwon

class FingerFragment: AbstractFragment() {

    private var _binding: LsFragmentFingerBinding? = null
    private val binding get() = _binding!!
    private val markwon by lazy { Markwon.create(requireContext()) }

    private val sp by lazy { requireActivity().getSharedPreferences("finger", Context.MODE_PRIVATE) }

    companion object {
        const val SP_KEY_COUNT = "count"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LsFragmentFingerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tips2Format = "Please put %d or more fingers on the screen."

        sp.getInt(SP_KEY_COUNT, 1).let {
            binding.chooser.count = it
            binding.numberOfParticipantsToSelectValue.text = it.toString()
            binding.slider.value = it.toFloat()
            binding.tips2.text = tips2Format.format(it + 1)
        }

        markwon.setMarkdown(
            binding.tips,
            """
                1. Invite multiple users to participate in the selection process.    
                2. Each participant places one finger on the screen simultaneously.  
                3. Keep fingers on the screen without lifting them.  
                4. After a few seconds, one or more finger(s) will be randomly highlighted, indicating the selected participant(s).
            """.trimIndent()
        )

        binding.chooser.onGameOver = {
        }

        binding.chooser.onButtonVisibilityChanged = {
            binding.group.isVisible = it
        }

        binding.slider.addOnChangeListener { _, value, fromUser ->
            binding.chooser.count = value.toInt()
            binding.numberOfParticipantsToSelectValue.text = value.toInt().toString()
            binding.tips2.text = tips2Format.format(value.toInt() + 1)

            if (fromUser) {
                sp.edit {
                    putInt(SP_KEY_COUNT, value.toInt())
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}