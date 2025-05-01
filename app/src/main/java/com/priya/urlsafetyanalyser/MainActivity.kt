

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var info: Info

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        info = Info(this)

        binding.analyseButton.setOnClickListener {
            clearEverything()
            val url = binding.receivedURL.text.toString().trim()
            if (url.isNotEmpty()) {
                updateProgressAndErrors("Analysing URL...")
                analyseURL(url)
                updateProgressAndErrors("Loading Preview...")
                loadPreview(url)
            } else {
                updateProgressAndErrors("Please enter a valid URL")
                clearEverything()
            }
        }
        binding.clearbutton.setOnClickListener {
            binding.receivedURL.text?.clear()
            binding.progressAndErrorsText.text= ""
            clearEverything()
        }
        binding.privacyPolicy.setOnClickListener {
            info.privacyPolicy()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun analyseURL(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val checksafety = SafeBrowsingService.checkUrlSafety(url)
                val threats = checksafety.threats
                val metadata = URLMetadataFetcher.fetchURLMetadata(url)

                withContext(Dispatchers.Main) {
                    binding.threatAnalyseResult.text = "URL is ${if (checksafety.isSafe) "safe" else "unsafe"} \n\n" +
                            "Number of Threats : ${threats.size} threats \n\nThreat types : ${threats.map { it.type }}"
                    binding.threatAnalyseContainer.visibility = View.VISIBLE
                }

                withContext(Dispatchers.Main) {
                    if (metadata != null) {
                        binding.threatAnalyseContainer.visibility = View.VISIBLE
                        binding.ContentResult.visibility = View.VISIBLE
                        binding.ContentResult.text = "Title: ${metadata.title}\n\nDescription: ${metadata.description}"
                        metadata.image?.let {
                            binding.urlPreview.setImageBitmap(it)
                            binding.urlPreview.visibility = View.VISIBLE
                        }
                    } else {
                        binding.ContentResult.text = "Metadata could not be fetched for the URL."
                    }

                    binding.ContentResult.visibility = View.VISIBLE
                    updateProgressAndErrors("")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.threatAnalyseContainer.visibility = View.VISIBLE
                   binding.threatAnalyseResult.text="Error analysing URL: ${e.message}"
                    updateProgressAndErrors("")
                }
            }
        }
    }
    private fun loadPreview(url: String) {
        WebViewScreenshotService.loadUrlInBackground(this, url) { screenshot ->
            runOnUiThread {
                if (screenshot != null) {
                    binding.urlScreenshot.setImageBitmap(screenshot)
                    binding.urlScreenshotContainer.visibility = View.VISIBLE
                    updateProgressAndErrors("")
                } else {
                    updateProgressAndErrors("Website preview failed.")
                }
            }
        }
    }

    private fun updateProgressAndErrors(message: String) {
        binding.progressAndErrorsText.text = message
    }
    private fun clearEverything() {
        binding.threatAnalyseContainer.visibility = View.GONE
        binding.threatAnalyseResult.text = ""
        binding.AIResult.text = ""
        binding.ContentResult.text = ""
        binding.AIResultContainer.visibility = View.GONE
        binding.ContentResult.visibility = View.GONE
        binding.urlPreview.visibility = View.GONE
        binding.urlScreenshotContainer.visibility = View.GONE
    }

}
