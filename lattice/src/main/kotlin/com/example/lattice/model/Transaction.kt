package com.example.lattice.model

import com.example.crypto.extension.toECKeyPair
import com.example.crypto.signMessage
import com.example.model.*
import com.example.model.block.SendTBlock
import com.example.model.extension.hash
import com.example.model.extension.toByteArray
import com.example.rlp.RLPList
import com.example.rlp.encode
import com.example.rlp.toRLP
import org.komputing.khex.extensions.toHexString
import org.komputing.khex.model.HexString
import java.math.BigInteger

/**
 * 交易数据类
 */
data class Transaction(
    var number: Long,
    var parentHash: String,
    var daemonHash: String,
    var timestamp: Long,
    var owner: Address,
    var linker: Address? = Address(ZERO_LTC_ADDR),
    val type: TxTypeEnum,
    var hub: Array<String> = emptyArray(),
    var code: String? = null,
    val codeHash: String? = null,
    var payload: String? = "0x",
    var amount: Long = 0,
    var income: Long? = null,
    var joule: Long = 0,
    var sign: String? = null,
    var proofOfWork: String? = null,
    var version: Int = VersionEnum.LATEST.version(),
    var difficulty: Int? = 0
)

/**
 * 签名交易
 *
 * @param privateKey 私钥
 * @param isGM sm2p256v1 or secp256k1
 * @param chainId 区块链ID
 * @param useProofOfWork 默认false
 */
fun Transaction.sign(
    privateKey: String,
    isGM: Boolean = true,
    chainId: Int = 1,
    useProofOfWork: Boolean = false
): Pair<String, SignatureData> {
    val (pow, hash) = hash(isGM, chainId, useProofOfWork)
    val signature = PrivateKey(HexString(privateKey)).toECKeyPair(isGM).signMessage(hash, isGM)
    return Pair(pow, signature)
}

/**
 * 对交易进行哈希运算
 *
 * @param isGM sm2p256v1 or secp256k1
 * @param chainId 区块链ID
 * @param useProofOfWork 默认false
 * @return Pair<pow,hash>
 */
@OptIn(ExperimentalStdlibApi::class)
fun Transaction.hash(isGM: Boolean = true, chainId: Int = 1, useProofOfWork: Boolean = false): Pair<String, ByteArray> {
    val codeHash = if (!code.isNullOrBlank()) {
        if (codeHash.isNullOrBlank()) {
            HexString(code!!).hash(isGM).toHexString()
        } else {
            codeHash
        }
    } else {
        ZERO_HASH
    }
    if (payload.isNullOrBlank()) payload = "0x"
    linker = linker ?: Address(ZERO_LTC_ADDR)

    val pow = if (useProofOfWork) getPow(codeHash, chainId, isGM) else "0x00"
    val hash = hashSeal(codeHash, pow, chainId, isGM, useProofOfWork, isSign = true)

    return Pair(pow, hash)
}

fun Transaction.getPow(codeHash: String, chainId: Int, isGM: Boolean = true): String {
    val min = BigInteger.ONE.shiftLeft(256 - DIFFICULTY)
    var i: Long = 0
    while (true) {
        i++
        val pow = BigInteger.valueOf(i).toString(16)
        val hash = hashSeal(codeHash, pow, chainId, isGM, useProofOfWork = true, isSign = false)
        if (BigInteger(1, hash) <= min) {
            return pow
        }
    }
}

fun Transaction.hashSeal(
    codeHash: String,
    pow: String,
    chainId: Int,
    isGM: Boolean = true,
    useProofOfWork: Boolean = false,
    isSign: Boolean = false
): ByteArray {
    val raw = mutableListOf<Any>()
    raw.add(number.toByteArray())
    raw.add(type.hex)
    raw.add(parentHash)
    raw.add(hub)
    raw.add(daemonHash)
    raw.add(codeHash)
    raw.add(owner.toEthereumAddress())
    raw.add(linker!!.toEthereumAddress())
    raw.add(amount.toByteArray())
    raw.add(joule.toByteArray())
    if (useProofOfWork) {
        raw.add(pow)
    } else {
        raw.add(DIFFICULTY_BYTE_ARRAY)
        raw.add(POW_BYTE_ARRAY)
    }
    raw.add(payload ?: "0x")
    raw.add(timestamp.toByteArray())
    raw.add((chainId.toLong()).toByteArray())
    if (isSign) {
        raw.add(HEX_PREFIX)
        raw.add(HEX_PREFIX)
    }
    val rlp = RLPList(
        raw.map {
            when (it) {
                is String -> it.toRLP()
                is ByteArray -> it.toRLP()
                is Array<*> -> it.toRLP()
                else -> it.toString().toRLP()
            }
        }
    ).encode()
    return rlp.hash(isGM)
}

fun Transaction.toSendTBlock() = SendTBlock(
    number,
    parentHash,
    daemonHash,
    timestamp,
    owner.input,
    linker!!.input,
    type.type(),
    hub,
    code,
    codeHash,
    payload,
    amount,
    income,
    joule,
    sign,
    proofOfWork,
    version,
    difficulty
)