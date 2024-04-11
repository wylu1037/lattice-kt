package com.example.crypto.api.kdf

/**
 * 密钥派生函数：它的设计目标是抵御ASIC等专用硬件攻击，从而增加攻击者破解密码的难度。与PBKDF2不同，SCrypt使用大量的内存来增加派生密钥的复杂度，
 * 这使得攻击者需要更多的内存来破解密码。在区块链中，SCrypt通常用于生成加密货币钱包的密钥，例如比特币。
 */
interface SCrypt {
    fun derive(password: ByteArray, salt: ByteArray?, n: Int, r: Int, p: Int, dkLen: Int): ByteArray
}