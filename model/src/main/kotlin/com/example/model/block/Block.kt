package com.example.model.block

data class Anchor(
    val number: Long,
    val hash: String,
    val owner: String
)

/**
 * 守护区块
 *
 * @property number 区块高度
 * @property hash 区块哈希
 * @property parentHash 父区块哈希
 * @property ledgerHash 账本哈希
 * @property receiptsHash 回执哈希
 * @property coinbase 矿工地址
 * @property signer 签名者地址
 * @property contracts 合约地址列表
 * @property difficulty 难度
 * @property lastedDBNumber 最后一个守护区块高度
 * @property extra 额外信息
 * @property reward 奖励
 * @property pow 工作量证明
 * @property timestamp 时间戳
 * @property size 区块大小
 * @property td 总难度
 * @property ttd 总交易难度
 * @property version 版本
 * @property txHashList 交易哈希列表
 * @property receipts 回执列表
 * @property anchors 锚点列表
 */
data class DBlock(
    val number: Long,
    val hash: String,
    val parentHash: String,
    val ledgerHash: String,
    val receiptsHash: String,
    val coinbase: String,
    val signer: String,
    val contracts: List<String>,
    val difficulty: Long,
    val lastedDBNumber: Long,
    val extra: String,
    val reward: Long,
    val pow: Long,
    val timestamp: Long,
    val size: Int,
    val td: Int,
    val ttd: Int,
    val version: Int,
    val txHashList: List<String>,
    val receipts: List<Receipt>,
    val anchors: List<Anchor>
)
