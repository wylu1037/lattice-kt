package com.example.wallet.model

import kotlinx.serialization.Serializable

/**
 * @property kdf 密钥派生函数，PBKDF2、Scrypt
 */
@Serializable
data class Kdf(
    val kdf: String,
    val kdfParams: KdfParams
)