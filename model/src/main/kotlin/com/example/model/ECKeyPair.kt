package com.example.model

import com.example.model.extension.toBigInteger
import java.math.BigInteger

/**
 * 私钥
 *
 * @param key 私钥的大整数
 */
data class PrivateKey(val key: BigInteger) {
    constructor(privateKey: ByteArray): this(privateKey.toBigInteger())
    constructor(privateKeyHexString: String): this(privateKeyHexString.toBigInteger())
}

data class PublicKey(val key: BigInteger) {
    constructor(publicKey: ByteArray): this(publicKey.toBigInteger())
    constructor(publicKeyHexString: String): this(publicKeyHexString.toBigInteger())

    override fun toString() = key.toString()
}

data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey)