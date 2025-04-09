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
@Suppress("ArrayInDataClass")
data class Receipt(
    val contractAddress: String,
    var contractRet: String,
    val dblockHash: String,
    val dblockNumber: Int,
    val events: Array<Event>? = emptyArray(),
    val jouleUsed: Long,
    val receiptIndex: Int,
    val success: Boolean,
    var tblockHash: String,
    var confirmTime: Long
) {

    fun shallowClone(): Receipt {
        return Receipt(
            contractAddress = contractAddress,
            contractRet = contractRet,
            dblockHash = dblockHash,
            dblockNumber = dblockNumber,
            events = events, // Reference copy (shallow)
            jouleUsed = jouleUsed,
            receiptIndex = receiptIndex,
            success = success,
            tblockHash = tblockHash,
            confirmTime = confirmTime
        )
    }

    fun deepClone(): Receipt {
        return Receipt(
            contractAddress = contractAddress,
            contractRet = contractRet,
            dblockHash = dblockHash,
            dblockNumber = dblockNumber,
            events = events?.map { it.deepClone() }?.toTypedArray(), // Deep copy each Event
            jouleUsed = jouleUsed,
            receiptIndex = receiptIndex,
            success = success,
            tblockHash = tblockHash,
            confirmTime = confirmTime
        )
    }
}

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
@Suppress("ArrayInDataClass")
data class Event(
    val address: String,
    val topics: Array<String>,
    val data: String,
    val logIndex: UInt,
    val tblockHash: String,
    val dblockNumber: ULong,
    val removed: Boolean,
    val dataHex: String
) {
    fun deepClone(): Event {
        return Event(
            address = address,
            topics = topics.copyOf(), // Deep copy Array<String>
            data = data,
            logIndex = logIndex,
            tblockHash = tblockHash,
            dblockNumber = dblockNumber,
            removed = removed,
            dataHex = dataHex
        )
    }
}
