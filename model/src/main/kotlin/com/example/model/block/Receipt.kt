package com.example.model.block

/**
 * 交易回执
 *
 * @property contractAddress 合约地址
 * @property contractRet 合约返回值
 * @property dblockHash 守护区块哈希
 * @property dblockNumber 守护区块高度
 * @property events 事件列表
 * @property jouleUsed 消耗的手续费
 * @property receiptIndex 回执索引
 * @property success 是否成功
 * @property tblockHash 交易区块哈希
 * @property confirmTime 确认时间
 */
data class Receipt(
    val contractAddress: String,
    var contractRet: String,
    val dblockHash: String,
    val dblockNumber: Int,
    val events: List<Event>,
    val jouleUsed: Long,
    val receiptIndex: Int,
    val success: Boolean,
    var tblockHash: String,
    var confirmTime: Long
)

/**
 * 事件
 *
 * @property address 地址
 * @property topics 事件主题
 * @property data 事件数据
 * @property logIndex 日志索引
 * @property tblockHash 交易区块哈希
 * @property dblockNumber 守护区块高度
 * @property removed 是否移除
 * @property dataHex 事件数据的16进制字符串
 */
data class Event(
    val address: String,
    val topics: List<String>,
    val data: ByteArray,
    val logIndex: UInt,
    val tblockHash: String,
    val dblockNumber: ULong,
    val removed: Boolean,
    val dataHex: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (removed != other.removed) return false
        if (address != other.address) return false
        if (topics != other.topics) return false
        if (!data.contentEquals(other.data)) return false
        if (logIndex != other.logIndex) return false
        if (tblockHash != other.tblockHash) return false
        if (dblockNumber != other.dblockNumber) return false
        if (dataHex != other.dataHex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = removed.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + topics.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + logIndex.hashCode()
        result = 31 * result + tblockHash.hashCode()
        result = 31 * result + dblockNumber.hashCode()
        result = 31 * result + dataHex.hashCode()
        return result
    }
}
