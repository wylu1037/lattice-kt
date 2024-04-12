package com.example.wallet.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * KDF结构体
 *
 * @property dkLen 衍生密钥（Derived Key）的长度（字节）
 * @property n 迭代次数
 * @property p 并行度，即同时进行多少次哈希运算
 * @property r 代表内部使用的伪随机函数的内存难度，即需要使用的 RAM 大小
 * @property salt 表示在 KDF 过程中使用的盐值，盐值可以增加密钥派生函数的安全性，防止彩虹表攻击
 */
@Serializable
data class KdfParams(
    @SerialName("DKLen") val dkLen: Int,
    val n: Int,
    val p: Int,
    val r: Int,
    val salt: String
)