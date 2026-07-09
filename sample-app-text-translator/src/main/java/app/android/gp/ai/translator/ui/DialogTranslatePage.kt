package app.android.gp.ai.translator.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import app.android.gp.ai.translator.R
import app.woejt.wwzdndgl.lib.app.AbsActivity

class DialogTranslatePage : AbsActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.d_translate_activity)

        val srcText = intent?.getStringExtra(DialogTranslationFragmentPage.EXTRA_SRC_TEXT) ?: ""
        val fragment = DialogTranslationFragmentPage()
        val bundle = Bundle()
        bundle.putString(DialogTranslationFragmentPage.EXTRA_SRC_TEXT, srcText)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment).commit()
    }

    companion object {
        fun start(context: Context, srcText: String) {
            val intent = Intent(context, DialogTranslatePage::class.java)
            intent.putExtra(DialogTranslationFragmentPage.EXTRA_SRC_TEXT, srcText)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}