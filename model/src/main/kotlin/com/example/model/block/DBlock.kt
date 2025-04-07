package com.example.model.block

import com.example.model.ZERO_HEX_STRING

/**
 * 当前账户的交易高度、交易哈希和守护区块高度
 *
 * @property currentDBlockHash 当前链最新的守护区块哈希
 * @property currentTBlockHash 当前账户最新一笔的交易哈希
 * @property currentTBlockNumber 当前账户最新的账户高度
 */
data class CurrentTDBlock(
    var currentDBlockHash: String,
    var currentTBlockHash: String,
    var currentTBlockNumber: Long
) {
    companion object {
        /**
         * 创建一个空的区块
         *
         * @return [CurrentTDBlock]
         */
        @JvmStatic
        fun newZeroBlock(): CurrentTDBlock {
            return CurrentTDBlock(
                currentDBlockHash = ZERO_HEX_STRING,
                currentTBlockHash = ZERO_HEX_STRING,
                currentTBlockNumber = 0L
            )
        }
    }

    /**
     * 更新交易哈希 并 自增账户高度
     *
     * @param hash 交易哈希
     */
    fun update(hash: String) {
        currentTBlockHash = hash
        currentTBlockNumber++
    }
}