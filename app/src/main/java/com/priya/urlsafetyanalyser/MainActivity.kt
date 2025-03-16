package com.priya.urlsafetyanalyser

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.priya.urlsafetyanalyser.utils.WebViewScreenshotService
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var submitButton : Button
    private lateinit var resultText : TextView
    private lateinit var urlEditText : EditText
    private lateinit var urlPreview : ImageView
    private lateinit var errorText : TextView
    private lateinit var urlPreviewText : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        submitButton =findViewById<Button>(R.id.submitButton)
        resultText=findViewById<TextView>(R.id.resultTextView)
        urlEditText=findViewById<EditText>(R.id.urlEditTextBox)
        urlPreview=findViewById<ImageView>(R.id.URLPreview)
        errorText=findViewById<TextView>(R.id.errorTextView)
        urlPreviewText=findViewById<TextView>(R.id.urlPreviewTextView)

        submitButton.setOnClickListener{
            val url =urlEditText.text.toString().trim()

            if (url.isNotEmpty()){
                errorText.visibility = View.GONE
                analyseURl(url)
                loadPreview(url)
            }else{
               errorText.visibility = View.VISIBLE
                errorText.text = "Please enter a URL"
            }
        }

    }

    private fun analyseURl(url: String) {
        val thread = Thread{
            try {

                val customFilterResult = checkWithCustomFilter(url)
                val phishTankResult = checkWithPhishTank(url)

                runOnUiThread{
                    val finalResult = customFilterResult
                    resultText.text = finalResult
                }

            }catch (e : Exception){
                e.printStackTrace()
            }

        }
        thread.start()
    }

    private fun checkWithPhishTank(url: String) {

    }

    private fun checkWithCustomFilter(url: String) : String {
        val maliciousWords = listOf("malware","Malware","phishing","attack", "hacked","danger","virus")
        return if (maliciousWords.any { url.contains(it, ignoreCase = true) }){
            "Unsafe URL detected by custom filter"
        }else{
            "NO suspicious word found in URL."
        }
    }

    private fun loadPreview(url: String) {
        WebViewScreenshotService.loadUrlInBackground(this, url) { screenshot ->
            runOnUiThread {
                if (screenshot != null) {
                    urlPreview.setImageBitmap(screenshot)
                    urlPreviewText.visibility = View.VISIBLE
                    urlPreview.visibility = View.VISIBLE

                } else {
                    urlPreview.visibility = View.GONE
                    urlPreviewText.visibility = View.GONE
                }
            }
        }
    }
}