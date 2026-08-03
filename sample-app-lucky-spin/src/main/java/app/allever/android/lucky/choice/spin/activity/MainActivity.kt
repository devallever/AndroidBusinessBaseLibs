package app.allever.android.lucky.choice.spin.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import app.allever.android.lucky.choice.spin.LuckSpinApplication
import app.allever.android.lucky.choice.spin.R
import app.allever.android.lucky.choice.spin.databinding.ActivityMainBinding
import app.allever.android.lucky.choice.spin.fragment.FingerFragment
import app.allever.android.lucky.choice.spin.fragment.NumberFragment
import app.allever.android.lucky.choice.spin.fragment.PasswordFragment
import app.allever.android.lucky.choice.spin.fragment.WheelFragment
import app.allever.android.lucky.choice.spin.log

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val wheelFragment by lazy {
        WheelFragment()
    }
    private val fingerFragment by lazy {
        FingerFragment()
    }
    private val numberFragment by lazy {
        NumberFragment()
    }
    private val pwdFragment by lazy {
        PasswordFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        log("loadOpenAd: onCreate")

        val startTime = System.currentTimeMillis()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            false
        }
        splashScreen.setOnExitAnimationListener { splashScreenProvider ->
            splashScreenProvider.view.animate().alpha(0f).withEndAction {
                splashScreenProvider.remove()
                enableEdgeToEdge()
            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigationView) { v, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.updatePadding(bottom = navBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemSelectedListener {
            val fragment = when(it.itemId) {
                R.id.nav_wheel -> wheelFragment
                R.id.nav_finger -> fingerFragment
                R.id.nav_number -> numberFragment
                R.id.nav_password -> pwdFragment
                else -> null
            }
            fragment?.let {
                supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerView, it).commit();
            }
            true
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}