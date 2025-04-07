package com.example.model.block

import com.example.model.EMPTY_PAYLOAD

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
    val hub: List<String>? = emptyList(),
    val code: String? = null,
    val codeHash: String? = null,
    val payload: String? = EMPTY_PAYLOAD,
    val amount: Long = 0,
    val income: Long? = null,
    val joule: Long = 0,
    val sign: String? = null,
    val proofOfWork: String? = null,
    val version: Int? = null,
    val difficulty: Int? = 0
)