package com.example.lattice.provider

import com.example.lattice.model.JsonRpcPayload

class WsClient(private val url: URL, private val options: Map<String, String>) : Client {
    override fun send(payload: JsonRpcPayload, headers: Map<String, String>): String {
        TODO("Not yet implemented")
    }
}