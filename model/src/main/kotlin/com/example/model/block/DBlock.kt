package com.example.model.block

data class CurrentTDBlock(
    val currentDBlockHash: String,
    val currentTBlockHash: String,
    val currentTBlockNumber: Long
)