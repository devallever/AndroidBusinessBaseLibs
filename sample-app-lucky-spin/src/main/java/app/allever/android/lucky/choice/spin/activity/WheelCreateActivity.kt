package app.allever.android.lucky.choice.spin.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import app.allever.android.lib.core.base.AbstractActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import app.allever.android.lucky.choice.spin.R
import app.allever.android.lucky.choice.spin.WheelModel
import app.allever.android.lucky.choice.spin.databinding.LsActivityWheelCreateBinding
import app.allever.android.lucky.choice.spin.databinding.LsItemOptionBinding
import app.allever.android.lucky.choice.spin.viewmodel.WheelViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WheelCreateActivity : AbstractActivity() {

    private lateinit var binding: LsActivityWheelCreateBinding
    private lateinit var saveMenuItem: MenuItem
    private lateinit var deleteMenuItem: MenuItem
    private val viewModel: WheelViewModel by viewModel()

    private var editMode = false
    private var extraWheelId: Long? = null
    private var extraWheelTitle: String? = null
    private var extraWheelOptions : List<String>? = null

    companion object {
        private const val EXTRA_WHEEL_ID = "wheel_id"
        private const val EXTRA_WHEEL_TITLE = "wheel_title"
        private const val EXTRA_WHEEL_OPTIONS = "wheel_options"

        fun start(
            context: Context,
            wheelId: Long = -1L,
            wheelTitle: String? = null,
            wheelOptions: List<String>? = null
        ) {
            val intent = Intent(context, WheelCreateActivity::class.java).apply {
                putExtra(EXTRA_WHEEL_ID, wheelId)
                putExtra(EXTRA_WHEEL_TITLE, wheelTitle)
                if (wheelOptions != null) putStringArrayListExtra(EXTRA_WHEEL_OPTIONS, ArrayList(wheelOptions))
            }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = LsActivityWheelCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // deal with the window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { view, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = systemInsets.top)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navInsets.bottom)
            insets
        }

        // get the extras
        extraWheelId = intent.getLongExtra(EXTRA_WHEEL_ID, -1)
        extraWheelTitle = intent.getStringExtra(EXTRA_WHEEL_TITLE)
        extraWheelOptions = intent.getStringArrayListExtra(EXTRA_WHEEL_OPTIONS)
        editMode = (extraWheelId != -1L) && (extraWheelTitle != null) && (extraWheelOptions != null)

        // set the title
        binding.toolbar.title = if (editMode) "Edit Wheel" else "Create Wheel"
        binding.toolbar.subtitle = if (editMode) extraWheelTitle else null

        // set listener for views
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        saveMenuItem = binding.toolbar.menu.findItem(R.id.action_save)
        saveMenuItem.setOnMenuItemClickListener {
            if (WheelModel.firstClickCreate) {
                WheelModel.firstClickCreate = false

                saveWheel()
            } else {
                saveWheel()
            }
            true
        }
        deleteMenuItem = binding.toolbar.menu.findItem(R.id.action_delete)
        deleteMenuItem.isVisible = editMode
        deleteMenuItem.setOnMenuItemClickListener {
            showDeleteDialog()
            true
        }

        binding.editTextWheelName.doAfterTextChanged { editable ->
            saveMenuItem.isEnabled = editable.toString().isNotBlank()
        }

        binding.editTextNewOption.doAfterTextChanged {
            binding.buttonAddOption.isEnabled = it.toString().isNotBlank()
        }

        binding.buttonAddOption.setOnClickListener {
            val option = binding.editTextNewOption.text.toString()
            addOption(option)
            binding.editTextNewOption.text?.clear()
        }

        // restore the options
        if (editMode && savedInstanceState == null) {
            extraWheelOptions?.forEach { option ->
                addOption(option)
            }
            binding.editTextWheelName.setText(extraWheelTitle)
        }

        savedInstanceState?.getStringArrayList("options")?.forEach { option ->
            addOption(option)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save the options
        val options = getAllOptions()
        outState.putStringArrayList("options", ArrayList(options))
    }

    private fun addOption(option: String) {
        val itemView = LsItemOptionBinding.inflate(layoutInflater, binding.linearLayoutOptions, true)
        itemView.textViewOption.text = option
        itemView.buttonRemoveOption.setOnClickListener {
            binding.linearLayoutOptions.removeView(itemView.root)
            binding.textViewOptionNumber.text = (binding.linearLayoutOptions.childCount - 1).toString()
        }
        binding.textViewOptionNumber.text = (binding.linearLayoutOptions.childCount - 1).toString()
    }

    private fun getAllOptions(): List<String> =
        binding.linearLayoutOptions.children.map {
            it.findViewById<MaterialTextView>(R.id.textViewOption)
        }
            .filterNotNull()
            .map { it.text.toString() }
            .toList()

    private fun saveWheel() {
        val options = getAllOptions()

        if (options.size < 2) {
            Toast.makeText(this, "The wheel must have at least two option", Toast.LENGTH_SHORT).show()
            return
        }

        val wheelName = binding.editTextWheelName.text.toString()
        if (editMode) {
            viewModel.updateWheel(extraWheelId!!, wheelName, options)
        } else {
            viewModel.createWheel(wheelName, options)
        }
        onBackPressed()
    }

    private fun showDeleteDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this wheel?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteWheel(extraWheelId!!)
                dialog.dismiss()
                onBackPressed()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}