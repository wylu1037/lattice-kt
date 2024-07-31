package com.example.lattice.provider

import com.example.lattice.model.JsonRpcPayload
import com.example.lattice.provider.Protocol.*

interface Client {
    /**
     * 发送请求
     *
     * @param payload json-rpc payload
     * @param headers 请求头
     * @return response
     */
    fun send(payload: JsonRpcPayload, headers: Map<String, String>): String
}

/**
 * 协议枚举
 *
 * @property HTTP
 * @property WS
 * @property UNKNOWN
 */
enum class Protocol {
    HTTP, WS, UNKNOWN
}

data class URL(val value: String)

/**
 * 初始化一个客户端
 *
 * @return 客户端
 */
fun URL.newClient(): Client {
    return when (detectProtocol()) {
        HTTP -> HTTPClient(this)
        WS -> throw UnsupportedOperationException("ws is unsupported protocol")
        else -> throw UnsupportedOperationException("unsupported protocol")
    }
}

// fixme
fun URL.detectProtocol(): Protocol {
    if ("^(http(s)?://)\\w+\\S+(\\.\\S+)+$".toRegex().matches(value)) {
        return HTTP
    } else if ("^(ws?://)\\w+\\S+(\\.\\S+)+$".toRegex().matches(value)) {
        return WS
    }
    return UNKNOWN
}

