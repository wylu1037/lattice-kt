package com.example.lattice.model

import com.example.lattice.provider.URL

data class BaseRequest(
    val url: URL,
    val body: Map<String, Any> = emptyMap(),
    val headers: Map<String, String>? = null
)