package com.example.crypto

import com.example.crypto.api.CryptoAPI
import com.example.crypto.impl.ec.getCurveParams
import com.example.crypto.impl.ec.secp256k1
import com.example.crypto.impl.ec.sm2p256v1
import com.example.model.ECKeyPair
import com.example.model.PUBLIC_KEY_SIZE
import com.example.model.PrivateKey
import com.example.model.extension.toBytesPadded
import org.komputing.khex.model.HexString

/**
 * 生成秘钥对
 *
 * @param gm 是否支持国密算法
 * @return ECKeyPair
 */
fun createKeyPair(gm: Boolean = true): ECKeyPair =
    if (gm) {
        CryptoAPI.sm2P256V1KeyPairGenerator.generate()
    } else {
        CryptoAPI.secP256K1KeyPairGenerator.generate()
    }

/**
 * 获取压缩公钥，压缩公钥只包含X坐标和一个额外的标志位(用来标识Y坐标的奇偶性)
 *
 * @param gm 是否是sm2p256v1
 * @return 压缩公钥(257bit)
 */
fun ECKeyPair.getCompressedPublicKey(gm: Boolean = true): ByteArray {
    val ret = publicKey.key.toBytesPadded(PUBLIC_KEY_SIZE + 1)
    ret[0] = 4
    val point = getCurveParams(if (gm) sm2p256v1 else secp256k1).curve.decodePoint(ret)
    return point.getEncoded(true)
}

fun decompressKey(publicBytes: ByteArray, gm: Boolean = true): ByteArray {
    val point = getCurveParams(if (gm) sm2p256v1 else secp256k1).curve.decodePoint(publicBytes)
    val encoded = point.getEncoded(false)
    return encoded.copyOfRange(1, encoded.size)
}

/**
 * convert hex string to private key
 *
 * @param privateKeyString 私钥的16进制字符串
 * @return PrivateKey
 */
fun convertStringToPrivateKey(privateKeyString: String): PrivateKey {
    val hex = HexString(privateKeyString)
    return PrivateKey(hex)
}