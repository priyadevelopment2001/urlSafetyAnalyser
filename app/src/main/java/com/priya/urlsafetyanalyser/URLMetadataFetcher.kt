package shivam.sycodes.securepeek.metadata

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import shivam.sycodes.securepeek.model.Metadata

object URLMetadataFetcher {
    private val client = OkHttpClient()

    suspend fun fetchURLMetadata(url: String): Metadata {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val html = response.body?.string() ?: ""

            val title = Regex("<title>(.*?)</title>", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)

            val description = Regex("<meta (name|property)=\"description\" content=\"(.*?)\".*?>", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(2)

            val imageUrl = Regex("<meta property=\"og:image\" content=\"(.*?)\".*?>", RegexOption.IGNORE_CASE)
                .find(html)?.groupValues?.get(1)

            val bitmap = if (imageUrl != null) fetchImage(imageUrl) else null

            return Metadata(title, description, bitmap)
        }
    }

    private fun fetchImage(imageUrl: String): Bitmap? {
        val request = Request.Builder().url(imageUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val inputStream = response.body?.byteStream()
            return BitmapFactory.decodeStream(inputStream)
        }
    }
}
