package com.truedevelopment.podeferreddeeplinks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PODeepLinkRequest().doRequest("8e5e15546f0396f2751438cd797cd07a") {
            runOnUiThread {
                println("Success: ${it.success}, Message: ${it.message}, Payload: ${it.payload}")
            }
        }
    }
}
