package com.example.lattice.model

import com.example.lattice.model.TxTypeEnum.CONTRACT
import com.example.lattice.model.TxTypeEnum.CREATE
import com.example.lattice.model.TxTypeEnum.EXECUTE
import com.example.lattice.model.TxTypeEnum.GENESIS
import com.example.lattice.model.TxTypeEnum.RECEIVE
import com.example.lattice.model.TxTypeEnum.SEND
import com.example.lattice.model.TxTypeEnum.UPDATE
import com.example.lattice.model.TxVersionEnum.CHAOS
import com.example.lattice.model.TxVersionEnum.LATEST
import com.example.lattice.model.TxVersionEnum.NUWA
import com.example.lattice.model.TxVersionEnum.PANGU
import okhttp3.MediaType.Companion.toMediaTypeOrNull

internal val APPLICATION_JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

/**
 * 交易类型枚举
 *
 * @property GENESIS 创世交易
 * @property CREATE 铸造交易
 * @property SEND 发送交易
 * @property RECEIVE 接收交易
 * @property CONTRACT 部署合约
 * @property EXECUTE 执行合约
 * @property UPDATE 升级合约
 */
enum class TxTypeEnum(val hex: String) {
    GENESIS("0x00"),
    CREATE("0x01"),
    SEND("0x02"),
    RECEIVE("0x03"),
    CONTRACT("0x04"),
    EXECUTE("0x05"),
    UPDATE("0x06");
}

fun TxTypeEnum.type() = name.lowercase()

/**
 * 链交易的版本号
 *
 * @property CHAOS 混沌
 * @property PANGU 盘古
 * @property NUWA 女娲
 * @property LATEST 最新版本
 */
enum class TxVersionEnum {
    CHAOS,
    PANGU,
    NUWA,
    LATEST
}

fun TxVersionEnum.version() = ordinal

internal const val DIFFICULTY = 12
const val ZERO_HASH = "0x0000000000000000000000000000000000000000000000000000000000000000"
const val ZERO_LTC_ADDR = "zltc_QLbz7JHiBTspS962RLKV8GndWFwjA5K66"

internal val DIFFICULTY_BYTE_ARRAY = byteArrayOf()
internal val POW_BYTE_ARRAY = byteArrayOf()

