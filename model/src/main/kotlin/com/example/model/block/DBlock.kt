package com.example.model.block

/**
 * @property currentDBlockHash 当前链最新的守护区块哈希
 * @property currentTBlockHash 当前账户最新一笔的交易哈希
 * @property currentTBlockNumber 当前账户最新的账户高度
 */
data class CurrentTDBlock(
    var currentDBlockHash: String,
    var currentTBlockHash: String,
    var currentTBlockNumber: Long
)