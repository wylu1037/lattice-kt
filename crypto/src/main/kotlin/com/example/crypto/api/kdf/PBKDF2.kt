package com.example.crypto.api.kdf

import com.example.crypto.impl.hashing.DigestParams

/**
 * 密钥派生函数：Password-Based Key Derivation Function 2，通常用于从用户提供的密码派生出加密密钥，以保护用户的私钥或其他敏感信息
 */
interface PBKDF2 {
    fun derive(pass: ByteArray, salt: ByteArray?, iterations: Int = 2048, digestParams: DigestParams = DigestParams.Sha512): ByteArray

    fun derive(pass: CharArray, salt: ByteArray?, iterations: Int = 2048, digestParams: DigestParams = DigestParams.Sha512): ByteArray
}