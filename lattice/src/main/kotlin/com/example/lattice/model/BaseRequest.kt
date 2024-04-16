package com.example.lattice.model

import com.example.lattice.provider.URL

data class BaseRequest(val url: URL, val params: Map<String, Any>? = null, val headers: Map<String, String>? = null)