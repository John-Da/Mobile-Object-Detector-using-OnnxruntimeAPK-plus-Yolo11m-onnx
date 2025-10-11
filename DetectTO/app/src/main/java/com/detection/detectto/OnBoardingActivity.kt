package com.detection.detectto

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat


class OnBoardingActivity : AppCompatActivity() {

    private val PREFS_NAME = "app_prefs"
    private val ONBOARDING_DONE_KEY = "onboarding_done"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean(ONBOARDING_DONE_KEY, false)

        if (onboardingDone) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.on_boarding)

        val textDesc: TextView = findViewById(R.id.textView5)

        // --- START: Direct Text Coloring Logic ---

        // 1. Get the string with the HTML <b> tag and convert it to Spanned
        val styledText: Spanned = HtmlCompat.fromHtml(
            getString(R.string.on_boarding),
            HtmlCompat.FROM_HTML_MODE_LEGACY // Mode for parsing HTML
        ) as Spanned

        // 2. Create a new SpannableString from the result
        val spannable = SpannableString(styledText)

        // 3. Define the target string to color (ensure this matches the text in your strings.xml)
        val targetString = "AI-powered tools"
        val fullString = styledText.toString()

        // 4. Find the start and end indices of the target string
        val targetStart = fullString.indexOf(targetString)
        val targetEnd = targetStart + targetString.length

        if (targetStart >= 0) {
            // 5. Define the color
            val accentColor = ContextCompat.getColor(this, R.color.accent)

            // 6. Apply the ForegroundColorSpan
            spannable.setSpan(
                ForegroundColorSpan(accentColor),
                targetStart,
                targetEnd,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // 7. Set the final styled text
        textDesc.text = spannable

        // --- END: Direct Text Coloring Logic ---

        val getStartedButton: Button = findViewById(R.id.button6)
        getStartedButton.setOnClickListener {
            prefs.edit().putBoolean(ONBOARDING_DONE_KEY, true).apply()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}