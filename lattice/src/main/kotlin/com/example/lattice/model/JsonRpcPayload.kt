package com.example.lattice.model

private const val version = "2.0"

typealias JsonRpcIdentifier = String
typealias JsonRpcId = Int

data class JsonRpcPayload(
    val jsonRpc: JsonRpcIdentifier = version,
    val id: JsonRpcId = 1,
    val method: String,
    val params: Array<Any>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsonRpcPayload

        if (jsonRpc != other.jsonRpc) return false
        if (id != other.id) return false
        if (method != other.method) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = jsonRpc.hashCode()
        result = 31 * result + id
        result = 31 * result + method.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}

fun JsonRpcPayload.toBody(): Map<String, Any> {
    return mapOf(
        "id" to id,
        "jsonRpc" to jsonRpc,
        "method" to method,
        "params" to params
    )
}