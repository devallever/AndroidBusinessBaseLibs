package app.allever.android.lucky.choice.spin.activity

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import app.allever.android.lib.core.base.AbstractActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import app.allever.android.lucky.choice.spin.R
import app.allever.android.lucky.choice.spin.databinding.LsActivityWheelBinding
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WheelActivity : AbstractActivity() {

    private lateinit var soundPool: SoundPool
    private var spinCompleteSoundId: Int = -1

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_DATA = "extra_data"

        fun start(context: Context, title: String, data: List<String>) {
            val intent = Intent(context, WheelActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putStringArrayListExtra(EXTRA_DATA, ArrayList(data))
            }
            context.startActivity(intent)
        }
    }

    private lateinit var binding: LsActivityWheelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = LsActivityWheelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.appBarLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(top = systemBars.top)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.title = intent.getStringExtra(EXTRA_TITLE)


        binding.spinWheelView.data = intent.getStringArrayListExtra(EXTRA_DATA) ?: emptyList()

        binding.spinWheelView.onSpinEndListener = { index, value ->
            soundPool.play(spinCompleteSoundId, 1f, 1f, 0, 0, 1f)
            showResult(value)

        }

        binding.spinWheelView.setOnClickListener {
            binding.spinWheelView.spin()
        }

        binding.btnSpin.setOnClickListener {
            binding.spinWheelView.spin()
        }

        initSoundPool()
    }

    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(1)
            .build()

        spinCompleteSoundId = soundPool.load(this, R.raw.ls_wheel_ok , 1)
    }

    private fun showResult(result : String) {
        lifecycleScope.launch {
            if (!isActive) {
                return@launch
            }

            binding.tvResult.text = result

            MaterialAlertDialogBuilder(this@WheelActivity)
                .setTitle("Ta-da!")
                .setMessage("The wheel says: $result")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Again") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}