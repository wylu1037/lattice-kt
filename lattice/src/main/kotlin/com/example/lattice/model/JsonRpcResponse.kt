package com.example.lattice.model

data class JsonRpcResponse<T, E>(
    val id: JsonRpcId,
    val jsonRpc: JsonRpcIdentifier,
    val result: T?,
    val error: JsonRpcError<E>?
)

data class JsonRpcError<T>(
    val code: Int,
    val message: String,
    val data: T
)