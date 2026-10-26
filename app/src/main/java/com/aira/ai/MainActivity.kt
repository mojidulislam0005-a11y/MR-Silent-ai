package com.aira.ai

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var status: TextView
    private var ttsReady = false
    private var isBangla = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#12081F"))
            setPadding(48, 48, 48, 48)
        }
        val title = TextView(this).apply {
            text = "AIRA"
            textSize = 44f
            setTextColor(Color.parseColor("#B794F6"))
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            text = "ভাষা বেছে নিয়ে কথা বলো\nBhasha chuno aur bolo"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 48, 0, 48)
        }
        val btnBn = Button(this).apply {
            text = "🎙️ বাংলা"
            setOnClickListener { listen(true) }
        }
        val btnHi = Button(this).apply {
            text = "🎙️ Hinglish"
            setOnClickListener { listen(false) }
        }
        root.addView(title)
        root.addView(status)
        root.addView(btnBn)
        root.addView(btnHi)
        setContentView(root)
    }

    private fun listen(bangla: Boolean) {
        isBangla = bangla
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (bangla) "bn-BD" else "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (bangla) "বলো..." else "Boliye...")
        }
        try {
            startActivityForResult(intent, 100)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Google voice app পাওয়া যায়নি", Toast.LENGTH_LONG).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val heard = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: return
            val reply = makeReply(heard)
            status.text = "তুমি / Tum: $heard\n\nAira: $reply"
            speak(reply)
        }
    }

    private fun makeReply(heard: String): String {
        val t = heard.lowercase()
        val greetings = listOf(
            "hello", "namaste", "নমস্কার", "হ্যালো", "হাই", "नमस्ते", "हैलो"
        )
        return if (greetings.any { t.contains(it) }) {
            if (isBangla) "হ্যালো! আমি আইরা। কী করতে পারি?"
            else "Namaste! Main Aira hoon. Kya kar sakti hoon?"
        } else {
            if (isBangla) "তুমি বলেছো: $heard" else "Tumne kaha: $heard"
        }
    }

    private fun speak(text: String) {
        if (!ttsReady) return
        val locale = if (isBangla) Locale("bn", "BD") else Locale("en", "IN")
        val r = tts.setLanguage(locale)
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale("hi", "IN"))
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "aira")
    }

    override fun onInit(status: Int) {
        ttsReady = status == TextToSpeech.SUCCESS
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
