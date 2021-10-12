package com.kunalapps.vaccineFinder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kunalapps.vaccineFinder.locale.LocaleHelper
import kotlinx.android.synthetic.main.activity_language.*

class LanguageActivity : AppCompatActivity() {

    private var languageCheckedItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val appSelectedLanguage = App.getAppSelectedLanguage()
        languageSpinner.setOnClickListener {
            val list = arrayOf<CharSequence>(getString(R.string.english), getString(R.string.hindi))
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle(getString(R.string.choose_language))
            builder.setSingleChoiceItems(list, languageCheckedItem) { dialog, which ->
                dialog.dismiss()
                if (which != languageCheckedItem) {
                    if (which == 0) {
                        val context = LocaleHelper.setLocale(this,"en")
                        languageSpinner.text =context.getString(R.string.english)
                        proceed.text=context.getString(R.string.proceed)
                        welcomeText.text=context.getString(R.string.welcome)
                    } else if (which == 1) {
                        val context = LocaleHelper.setLocale(this, "hi")
                        languageSpinner.text =context.getString(R.string.hindi)
                        proceed.text=context.getString(R.string.proceed)
                        welcomeText.text=context.getString(R.string.welcome)
                    }
                }
                languageCheckedItem = which
            }
            val dialog = builder.create()
            dialog.show()
        }

        proceed.setOnClickListener {
            startActivity(Intent(this,SplashActivity::class.java))
            overridePendingTransition(R.anim.slide_from_right, R.anim.slideout_from_left);
            finish()
        }
    }
}