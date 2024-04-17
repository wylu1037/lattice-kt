package com.example.lattice.provider

import com.example.lattice.gson
import com.example.lattice.model.APPLICATION_JSON
import com.example.lattice.model.BaseRequest
import com.example.lattice.model.JsonRpcPayload
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class HttpClient(private val url: URL, private val options: Map<String, String>) : Client {

    override fun send(payload: JsonRpcPayload, headers: Map<String, String>): String {
        val parameters = payload.let {
            mapOf(
                "id" to it.id,
                "jsonRpc" to it.jsonRpc,
                "method" to it.method,
                "params" to it.params
            )
        }
        val response = post(BaseRequest(url, parameters, headers))
        if (!response.isSuccessful) {
            throw Error(response.body.string())
        }
        return response.body.string()
    }
}

object HttpClientFactory {

    private const val MAX_IDLE_CONNECTIONS = 50 // 最大空闲连接数
    private const val KEEP_ALIVE_DURATION = 30L // 连接的保持时间
    private const val CONNECT_TIMEOUT = 90L
    private const val READ_TIMEOUT = 90L

    private val connectionPool = ConnectionPool(
        MAX_IDLE_CONNECTIONS,
        KEEP_ALIVE_DURATION,
        TimeUnit.SECONDS
    )

    val client = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}

fun post(request: BaseRequest): Response {
    val body = gson.toJson(request.params).toRequestBody(APPLICATION_JSON)
    val builder = Request.Builder().post(body).url(request.url.value)
    if (request.headers?.isNotEmpty() == true) {
        request.headers.forEach { (t, u) ->
            builder.addHeader(t, u)
        }
    }
    val call = HttpClientFactory.client.newCall(builder.build())
    return call.execute()
}