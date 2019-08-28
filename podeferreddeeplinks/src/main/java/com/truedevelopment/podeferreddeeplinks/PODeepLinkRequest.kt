package com.truedevelopment.podeferreddeeplinks

import android.os.AsyncTask
import android.os.Build
import java.io.*
import java.net.*

class PODeepLinkRequest {

    companion object {
        private const val TEST_MODE = false
        private const val FULL_URL = "https://apistaging.urlgeni.us/api/v1/deep_links"
        private const val TIMEOUT = 10 * 1000

        private const val UNAUTHORIZED_ERROR = "You are not authorized to access this endpoint."
        private const val TIMEOUT_ERROR = "It appears you have poor network signal. Please try again later."
        private const val GENERIC_ERROR_MESSAGE = "An error occurred. Please try again later."
    }

    private val deviceIdentifier: String
        get() {
            if (TEST_MODE) {
                return "-Android-"
            }

            return "${Build.MODEL}-Android-${Build.VERSION.RELEASE}"
        }

    fun doRequest(apiKey: String, callback: (DeepLinkerData) -> Unit) {
        if (apiKey == "") {
            callback(DeepLinkerData(false, "API key not set", null))
            return
        }

        val params = "${URLEncoder.encode("api_key", "UTF-8")}=${URLEncoder.encode(apiKey, "UTF-8")}" +
                "&${URLEncoder.encode("device_type", "UTF-8")}=${URLEncoder.encode(deviceIdentifier, "UTF-8")}"

        HttpTask {
            if (it == null) {
                callback(DeepLinkerData(false, GENERIC_ERROR_MESSAGE, null))
                return@HttpTask
            }

            when (it) {
                UNAUTHORIZED_ERROR, TIMEOUT_ERROR, GENERIC_ERROR_MESSAGE -> {
                    callback(DeepLinkerData(false, it, null))
                    return@HttpTask
                }
            }

            callback(PODeepLinkerResponse(it).data)
        }.execute(params)
    }

    class HttpTask(var callback: (String?) -> Unit): AsyncTask<String, Unit, String>() {

        override fun doInBackground(vararg params: String): String? {
            val url = URL(FULL_URL)

            val httpClient = (url.openConnection() as HttpURLConnection).also {
                it.readTimeout = TIMEOUT
                it.connectTimeout = TIMEOUT
                it.requestMethod = "POST"
                it.instanceFollowRedirects = false
                it.doOutput = true
                it.doInput = true
                it.useCaches = false
                it.setRequestProperty("X-PO-Device-Platform", "Google/Android")
            }

            try {
                httpClient.connect()

                val os = httpClient.outputStream
                BufferedWriter(OutputStreamWriter(os, "UTF-8") as Writer).also {
                    it.write(params[0])
                    it.flush()
                    it.close()
                }
                os.close()

                when (httpClient.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val stream = BufferedInputStream(httpClient.inputStream)
                        return readStream(stream)
                    }
                    HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        return UNAUTHORIZED_ERROR
                    }
                    HttpURLConnection.HTTP_CLIENT_TIMEOUT, HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> {
                        return TIMEOUT_ERROR
                    }
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        return GENERIC_ERROR_MESSAGE
                    }
                    else -> {
                        println("ERROR: ${httpClient.responseCode}")
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                httpClient.disconnect()
            }

            return null
        }

        private fun readStream(inputStream: BufferedInputStream): String {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            bufferedReader.forEachLine { stringBuilder.append(it) }
            return stringBuilder.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            callback(result)
        }
    }
}