package com.example.model.block

data class TBlock(
    val number: Long,
    val parentHash: String,
    val daemonHash: String,
)

fun TBlock.toCurrentTDBlock() = CurrentTDBlock(
    daemonHash,
    parentHash,
    number
)

data class SendTBlock(
    val number: Long,
    val parentHash: String,
    val daemonHash: String,
    val timestamp: Long,
    val owner: String,
    val linker: String,
    val type: String,
    val hub: Array<String>? = emptyArray(),
    val code: String? = null,
    val codeHash: String? = null,
    val payload: String? = "0x00",
    val amount: Long = 0,
    val income: Long? = null,
    val joule: Long = 0,
    val sign: String? = null,
    val proofOfWork: String? = null,
    val version: Int? = null,
    val difficulty: Int? = 0
)