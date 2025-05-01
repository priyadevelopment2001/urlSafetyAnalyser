package shivam.sycodes.securepeek.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException

object SafeBrowsingService {

    private val client = OkHttpClient()
    private const val apiKey = "" // replace with your api key

    suspend fun checkUrlSafety(url: String): ThreatResult {
        val requestBody = """
        {
            "client": {
                "clientId": "url-analyzer",
                "clientVersion": "1.0"
            },
            "threatInfo": {
                "threatTypes": ["MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"],
                "platformTypes": ["ANY_PLATFORM"],
                "threatEntryTypes": ["URL"],
                "threatEntries": [{"url": "$url"}]
            }
        }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://safebrowsing.googleapis.com/v4/threatMatches:find?key=$apiKey")
            .post(RequestBody.create("application/json".toMediaType(), requestBody))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Error: Received HTTP ${response.code}. Please check the request format.")
                }

                val responseJson = response.body?.string()
                if (responseJson?.contains("matches") == true) {
                    val threatResponse = Gson().fromJson(responseJson, ThreatResponse::class.java)
                    return ThreatResult(
                        isSafe = false,
                        threats = threatResponse.matches?.map { match ->
                            Threat(
                                type = match.threatType,
                                platform = match.platformType,
                                url = match.threat?.url ?: "Unknown"
                            )
                        } ?: emptyList()
                    )
                }
                return ThreatResult(isSafe = true, threats = emptyList())
            }
        } catch (e: Exception) {
            Log.e("SafeBrowsingService", "Error checking URL safety: ${e.message}", e)

            throw Exception("Failed to check URL safety. Please try again.")
        }
    }

    data class ThreatResult(
        val isSafe: Boolean,
        val threats: List<Threat>
    )

    data class Threat(
        val type: String,
        val platform: String,
        val url: String
    )

    data class ThreatResponse(
        @SerializedName("matches") val matches: List<Match>?
    )

    data class Match(
        @SerializedName("threatType") val threatType: String,
        @SerializedName("platformType") val platformType: String,
        @SerializedName("threat") val threat: ThreatDetail?
    )

    data class ThreatDetail(
        @SerializedName("url") val url: String
    )
}

