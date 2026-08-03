package app.allever.android.lucky.choice.spin.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.allever.android.lucky.choice.spin.LuckSpinApplication
import app.allever.android.lucky.choice.spin.R
import app.allever.android.lucky.choice.spin.WheelAdapter
import app.allever.android.lucky.choice.spin.WheelModel
import app.allever.android.lucky.choice.spin.activity.WheelActivity
import app.allever.android.lucky.choice.spin.activity.WheelCreateActivity
import app.allever.android.lucky.choice.spin.databinding.FragmentWheelBinding
import app.allever.android.lucky.choice.spin.viewmodel.WheelViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class WheelFragment : Fragment() {

    private var _binding: FragmentWheelBinding? = null
    private val binding get() = _binding!!
    private lateinit var wheelAdapter: WheelAdapter
    private val viewModel: WheelViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWheelBinding.inflate(inflater, container, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemInsets.top)
            insets
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.menu.findItem(R.id.action_privacy_policy).setOnMenuItemClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = "".toUri()
            startActivity(intent)
            true
        }

        wheelAdapter = WheelAdapter(
            onWheelClick = {
                WheelActivity.start(
                    context = requireContext(),
                    title = it.wheel.name,
                    data = it.options.map { option ->  option.name }
                )
            },
            onWheelEditClick = {
                WheelCreateActivity.start(
                    context = requireContext(),
                    wheelId = it.wheel.id,
                    wheelTitle = it.wheel.name,
                    wheelOptions = it.options.map { option -> option.name }
                )
            }
        )
        binding.recyclerViewWheels.adapter = wheelAdapter

        binding.fab.setOnClickListener {
            launchCreate()
        }

        binding.emptyAdd.setOnClickListener {
            if (WheelModel.firstClickHomeCenterAdd) {
                WheelModel.firstClickHomeCenterAdd = false
                launchCreate()
            } else {
                launchCreate()
            }

        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allWheelsAndOptions.collectLatest { wheels ->
                    wheelAdapter.submitList(wheels)

                    binding.recyclerViewWheels.isVisible = wheels.isNotEmpty()
                    binding.emptyGroup.isVisible = wheels.isEmpty()
                }
            }
        }

    }

    private fun launchCreate() {
        val intent = Intent(requireContext(), WheelCreateActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}