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
import app.allever.android.lucky.choice.spin.databinding.LsFragmentNumberBinding
import app.allever.android.lucky.choice.spin.utils.copyToClipboard
import app.allever.android.lucky.choice.spin.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class NumberFragment : AbstractFragment() {

    private var _binding: LsFragmentNumberBinding? = null
    private val binding get() = _binding!!
    private val viewModel : MainViewModel by activityViewModel()

    private val sp by lazy {
        requireActivity().getSharedPreferences("number", Context.MODE_PRIVATE)
    }

    companion object {
        const val SP_KEY_MIN = "min"
        const val SP_KEY_MAX = "max"
        const val SP_KEY_NUMBER_OF_RESULTS = "number_of_results"
        const val SP_KEY_REPETITIONS = "repetitions"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LsFragmentNumberBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemBars.top)
            insets
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set initial values
        binding.editTextMin.setText(sp.getInt(SP_KEY_MIN, 1).toString())
        binding.editTextMax.setText(sp.getInt(SP_KEY_MAX, 100).toString())
         sp.getInt(SP_KEY_NUMBER_OF_RESULTS, 1).let {
             binding.numberOfResults.text = it.toString()
             binding.slider.value = it.toFloat()
         }
        binding.repetitionsCheckBox.isChecked = sp.getBoolean(SP_KEY_REPETITIONS, false)


        // set listener for views
        binding.slider.addOnChangeListener { _, value, _ ->
            binding.numberOfResults.text = value.toInt().toString()
        }
        binding.repetitionsFrameLayout.setOnClickListener {
            binding.repetitionsCheckBox.toggle()
        }

        binding.copyButton.setOnClickListener {
            val numbers = viewModel.randomNumbers.value
            if (numbers.isNotEmpty()) {
                val text = numbers.joinToString(", ")
                requireContext().copyToClipboard(text)
                Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.fab.setOnClickListener {
            val min = binding.editTextMin.text.toString().toIntOrNull()
            val max = binding.editTextMax.text.toString().toIntOrNull()
            if (min == null || max == null) {
                Toast.makeText(requireContext(), "Please enter valid numbers", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (min >= max) {
                Toast.makeText(
                    requireContext(),
                    "Range max must be greater than min",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val numberOfResults = binding.slider.value.toInt()
            val repetitionsEnabled = binding.repetitionsCheckBox.isChecked
            if (numberOfResults > (max - min + 1) && !repetitionsEnabled) {
                Toast.makeText(
                    requireContext(),
                    "It's impossible to generate $numberOfResults unique numbers in the range $min-$max",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.randomNumbers.value = generateRandomNumbers(min, max, numberOfResults, repetitionsEnabled)

            sp.edit {
                putInt(SP_KEY_MIN, min)
                putInt(SP_KEY_MAX, max)
                putInt(SP_KEY_NUMBER_OF_RESULTS, numberOfResults)
                putBoolean(SP_KEY_REPETITIONS, repetitionsEnabled)
            }
        }


        // observe the data
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.randomNumbers.collectLatest { numbers ->
                    binding.copyButton.isVisible = numbers.isNotEmpty()
                    binding.placeholderIcon.isVisible = numbers.isEmpty()
                    binding.resultsNumberTextView.isVisible = numbers.isNotEmpty()
                    binding.resultsNumberTextView.text = numbers.joinToString(", ")
                    binding.sumLinearLayout.isVisible = numbers.size > 1
                    binding.sumTextView.text = numbers.sum().toString()
                    binding.averageLinearLayout.isVisible = numbers.size > 1
                    val average = numbers.map { it.toFloat() }.average()
                    binding.averageTextView.text = "%.2f".format(average)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateRandomNumbers(min: Int, max: Int, numberOfResults: Int, repetitionsEnabled: Boolean): List<Int> {
        val numbers = (min..max).toMutableList()
        val results = mutableListOf<Int>()
        repeat(numberOfResults) {
            val index = (0 .. numbers.lastIndex).random()
            results.add(numbers[index])
            if (!repetitionsEnabled) {
                numbers.removeAt(index)
            }
        }
        return results
    }
}