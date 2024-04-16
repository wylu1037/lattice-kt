package com.example.lattice.provider

import com.example.abi.Json
import com.example.lattice.model.APIPayload
import com.example.lattice.model.BaseRequest
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class HttpClient(private val url: URL, private val options: Map<String, String>) : Client {

    override fun send(body: APIPayload, headers: Map<String, String>): String {
        val parameters = body.let {
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

    // http连接池
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
    val params = Json.toJsonString(request.params!!)
    val body = params.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    val builder = Request.Builder().post(body).url(request.url.value)
    if (request.headers?.isNotEmpty() == true) {
        request.headers.forEach { (t, u) ->
            builder.addHeader(t, u)
        }
    }
    val call = HttpClientFactory.client.newCall(builder.build())
    return call.execute()
}