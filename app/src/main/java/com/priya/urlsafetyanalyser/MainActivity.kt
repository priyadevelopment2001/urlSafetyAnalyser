package com.priya.urlsafetyanalyser

import android.os.Bundle
import android.os.Message
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.priya.urlsafetyanalyser.utils.WebViewScreenshotService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var submitButton : Button
    private lateinit var resultText : TextView
    private lateinit var urlEditText : EditText
    private lateinit var urlPreview : ImageView
    private lateinit var errorText : TextView
    private lateinit var urlPreviewText : TextView
    private lateinit var mainForBackground : LinearLayout
    private lateinit var clearButton : ImageView


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
        mainForBackground=findViewById<LinearLayout>(R.id.main)
        clearButton=findViewById<ImageView>(R.id.clearImageButton)

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
        clearButton.setOnClickListener{
            clearEverything()
        }

    }



    private fun analyseURl(url: String) {
        val thread = Thread{
            try {

                val customFilterResult = checkWithCustomFilter(url)
                val phishTankResult = checkWithPhishTank(url)

                runOnUiThread{
                    val finalResult = "$customFilterResult \n $phishTankResult"
                    val isUnsafe = finalResult.contains("Unsafe", true) || finalResult.contains("malware", true)
                    updateUI(isUnsafe, finalResult)
                    resultText.text = finalResult
                }

            }catch (e : Exception){
                e.printStackTrace()
            }

        }
        thread.start()
    }

    private fun checkWithPhishTank(url: String) : String {

        val requestUrl = "https://checkurl.phishtank.com/checkurl/"
        val jsonPayload = "format=json&url=$url"
         return makePostRequest(requestUrl,jsonPayload, "PhishTank")
    }

    private fun makePostRequest(
        url: String,
        jsonPayload: String,
        source: String
    ): String {
        val client = OkHttpClient()
        val request = Request.Builder().url(url)
            .post(RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonPayload))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful){
                val responseBody = response.body()?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    if (jsonResponse.has("matches") || jsonResponse.toString().contains("malicious")) {
                        val threats = jsonResponse.optJSONArray("matches")?.length()?: 1
                        return "Unsafe ($threats threats found) - $source"
                    }
                }
            }
            "Safe - $source"
        }catch (e : IOException){
            e.printStackTrace()
            "Error occurred while checking URL - $source"
        }
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
    private fun updateUI(isUnsafe: Boolean, message: String) {
        runOnUiThread {
            resultText.text = message
            if (isUnsafe) {
                mainForBackground.setBackgroundColor(getColor(android.R.color.holo_red_light))
            } else {
                mainForBackground.setBackgroundColor(getColor(android.R.color.white))
            }
        }
    }

    private fun clearEverything() {
        urlEditText.text.clear()
        resultText.text = ""
        urlPreview.visibility = View.GONE
        urlPreviewText.visibility = View.GONE
        mainForBackground.setBackgroundColor(getColor(android.R.color.white))
    }
}