package com.example.lattice.model

data class JsonRpcResponse<T>(
    val id: JsonRpcId,
    val jsonRpc: JsonRpcIdentifier,
    val result: T?,
    val error: JsonRpcError?
)

data class JsonRpcError(
    val code: Int,
    val message: String,
)