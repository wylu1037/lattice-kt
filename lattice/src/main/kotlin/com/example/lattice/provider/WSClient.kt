package com.example.lattice.provider

import com.example.lattice.model.JsonRpcPayload

class WSClient(private val url: URL, private val options: Map<String, String>) : Client {
    override fun send(payload: JsonRpcPayload, headers: Map<String, String>): String {
        TODO("Not yet implemented")
    }
}