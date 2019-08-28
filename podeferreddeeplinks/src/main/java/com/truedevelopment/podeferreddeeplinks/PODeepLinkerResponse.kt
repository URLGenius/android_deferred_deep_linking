package com.truedevelopment.podeferreddeeplinks

import org.json.JSONObject

class PODeepLinkerResponse(json: String): JSONObject(json) {
    val data: DeepLinkerData = DeepLinkerData(this.getBoolean("success"), this.optString("message"), this.optString("payload"))
}

data class DeepLinkerData(
    val success: Boolean,
    val message: String?,
    val payload: String?
)
