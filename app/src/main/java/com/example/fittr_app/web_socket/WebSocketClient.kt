package com.example.fittr_app.web_socket

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import android.util.Log
import com.example.fittr_app.media_pipe.BackendResponse
import com.example.fittr_app.media_pipe.CameraFragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class WebSocketClient(private val cameraFragment: CameraFragment) : WebSocketListener() {
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
    }
    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "Message received from backend: $text")

        // Parse the JSON response
        try {
            val backendResponse = Gson().fromJson(text, BackendResponse::class.java)

            // Pass the response to the CameraFragment
            (cameraFragment as? CameraFragment)?.handleBackendResponse(backendResponse)
        } catch (e: JsonSyntaxException) {
            Log.e("WebSocket", "Error parsing backend response: ${e.message}")
        }
    }


    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connected")
        super.onOpen(webSocket, response)
    }
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        Log.d("WebSocket", "Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        Log.e("WebSocket", "Error: ${t.message}")
    }

}