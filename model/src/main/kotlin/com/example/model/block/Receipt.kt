package com.example.model.block

data class Receipt(
    val contractAddress: String,
    var contractRet: String,
    val dblockHash: String,
    val dblockNumber: Int,
    val events: Array<Any>,
    val jouleUsed: Long,
    val receiptIndex: Int,
    val success: Boolean,
    var tblockHash: String,
    var confirmTime: Long
)