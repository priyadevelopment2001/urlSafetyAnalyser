package com.priya.urlsafetyanalyser

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var submitButton : Button
    private lateinit var resultText : TextView
    private lateinit var urlEditText : TextInputEditText


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
        urlEditText=findViewById<TextInputEditText>(R.id.urlEditTextBox)

        submitButton.setOnClickListener{
            val url =urlEditText.text.toString().trim()

            if (url.isNotEmpty()){
                analyseURl(url)
            }else{
                resultText.text = "Please enter a valid URl"
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

            }
        }
    }

    private fun checkWithPhishTank(url: String) {}

    private fun checkWithCustomFilter(url: String) : String {
        val maliciousWords = listOf("malware","Malware","phishing","attack", "hacked","danger","virus")
        return if (maliciousWords.any { url.contains(it, ignoreCase = true) }){
            "Unsafe URL detected by custom filter"
        }else{
            "NO suspicious word found in URl."
        }
    }
}