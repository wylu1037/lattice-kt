package com.example.model

import com.example.model.extension.hash
import com.example.model.extension.hexToBigInteger
import com.example.model.extension.toBigInteger
import com.example.model.extension.toHexStringZeroPadded
import org.komputing.khash.sha256.extensions.sha256
import org.komputing.khex.decode
import org.komputing.khex.model.HexString
import java.math.BigInteger

/**
 * 私钥
 *
 * @param key 私钥的大整数
 */
data class PrivateKey(val key: BigInteger) {
    constructor(privateKey: ByteArray) : this(privateKey.toBigInteger())
    constructor(privateKeyHex: HexString) : this(privateKeyHex.hexToBigInteger())
}

data class PublicKey(val key: BigInteger) {
    constructor(publicKey: ByteArray) : this(publicKey.toBigInteger())
    constructor(publicKeyHexString: String) : this(publicKeyHexString.toBigInteger())

    override fun toString() = key.toString()
}

@OptIn(ExperimentalStdlibApi::class)
fun PublicKey.toEthereumAddress(isGM: Boolean = true): EthereumAddress {
    val publicKeyHexString = HexString(key.toHexStringZeroPadded(PUBLIC_KEY_LENGTH_IN_HEX, false))
    val hash = publicKeyHexString.hash(isGM).toHexString()

    return EthereumAddress(hash.substring(hash.length - ADDRESS_LENGTH_IN_HEX))
}

fun PublicKey.toAddress(isGM: Boolean = true): Address {
    val ethereumAddress = toEthereumAddress(isGM)
    val prefix = decode("01")
    val hashArr = prefix.plus(decode(ethereumAddress.cleanHex))
    val d1 = hashArr.sha256()
    val d2 = d1.sha256()
    val d3 = hashArr.plus(d2.sliceArray(0..3))
    return Address("zltc_" + Base58.encode(d3))
}

data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey)

fun ECKeyPair.toAddress(isGM: Boolean = true) = publicKey.toAddress(isGM)