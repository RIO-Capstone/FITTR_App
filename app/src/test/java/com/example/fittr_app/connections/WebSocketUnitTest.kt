package com.example.fittr_app.connections

import com.example.fittr_app.media_pipe.BackendResponse
import com.example.fittr_app.media_pipe.BackendResponseHandler
import com.example.fittr_app.media_pipe.CameraFragment
import com.google.gson.Gson
import okhttp3.WebSocket
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.capture

class WebSocketUnitTest {

    @Mock
    private lateinit var mockWebSocket: WebSocket

    @Mock
    private lateinit var mockResponseHandler: BackendResponseHandler

    @Captor
    private lateinit var backendCaptor: ArgumentCaptor<BackendResponse>

    private lateinit var webSocketClient: WebSocketClient

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        webSocketClient = WebSocketClient(mockResponseHandler)
    }

    @Test
    fun `test onMessage handles valid backend response`() {
        // test backend data
        val testJsonResponse = """{"rep_count":1,"message":"Test"}"""
        webSocketClient.onMessage(mockWebSocket, testJsonResponse)
        verify(mockResponseHandler).handleBackendResponse(capture(backendCaptor))
        assert(1 == backendCaptor.value.rep_count)
        assert("Test" == backendCaptor.value.message)
    }

    @Test
    fun `test onMessage handles invalid JSON gracefully`() {
        val invalidJson = """{"status": "success", "message": "Test""" // Missing closing }
        webSocketClient.onMessage(mockWebSocket, invalidJson)
        verify(mockResponseHandler, never()).handleBackendResponse(anyOrNull())
    }
}