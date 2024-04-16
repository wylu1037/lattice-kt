package com.example.lattice.provider

import com.example.lattice.model.APIPayload

interface Client {
    abstract fun send(body: APIPayload, headers: Map<String, String>): String
}

enum class Protocol {
    UNKNOWN, HTTP, WS, IPC
}

@JvmInline
value class URL(val value: String)

/**
 * 初始化一个客户端
 *
 * @return 客户端
 */
fun URL.newClient(): Client {
    return when (detectProtocol()) {
        Protocol.HTTP -> HttpClient(this, emptyMap())
        Protocol.WS -> throw UnsupportedOperationException("ws is unsupported protocol")
        else -> throw UnsupportedOperationException("unsupported protocol")
    }
}

fun URL.detectProtocol(): Protocol {
    if ("^(http(s)?://)\\w+\\S+(\\.\\S+)+$".toRegex().matches(value)) {
        return Protocol.HTTP
    } else if ("^(ws?://)\\w+\\S+(\\.\\S+)+$".toRegex().matches(value)) {
        return Protocol.WS
    }
    return Protocol.UNKNOWN
}

