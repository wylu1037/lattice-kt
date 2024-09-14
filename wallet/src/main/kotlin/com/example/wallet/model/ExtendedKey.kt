package com.example.wallet.model

import com.example.crypto.api.CryptoAPI
import com.example.crypto.api.ec.toPublicKey
import com.example.crypto.extension.toECKeyPair
import com.example.crypto.getCompressedPublicKey
import com.example.crypto.impl.ec.EllipticCurve
import com.example.crypto.impl.ec.getCurveParams
import com.example.crypto.impl.ec.secp256k1
import com.example.crypto.impl.ec.sm2p256v1
import com.example.model.Base58
import com.example.model.ECKeyPair
import com.example.model.PRIVATE_KEY_SIZE
import com.example.model.PrivateKey
import com.example.model.extension.toBytesPadded
import org.komputing.kbip44.BIP44Element
import org.komputing.khash.ripemd160.extensions.digestRipemd160
import org.komputing.khash.sha256.extensions.sha256
import java.io.IOException
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.InvalidKeyException
import java.security.KeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

/**
 * HD Wallet中的扩展密钥
 *
 * @property keyPair 密钥对
 * @property chainCode 代表链码。链码与私钥一起用于派生子密钥，确保了密钥层次结构的确定性
 * @property depth 字节形式的深度值，表示该密钥在密钥树中的层级位置。根密钥的深度通常为0，其下一级为1，依此类推
 * @property parentFingerprint 父密钥的指纹，这是一个 4 字节的标识符，用于唯一识别上一层级的密钥
 * @property sequence 序列号，又称为索引或子索引，用于区分同一层级内派生出的不同子密钥
 * @property versionBytes 字节数组，表示密钥版本字节，用于区分主私钥、普通密钥以及针对不同网络（例如比特币主网与测试网）的密钥类型
 */
data class ExtendedKey(
    val keyPair: ECKeyPair,
    internal val chainCode: ByteArray,
    internal val depth: Byte,
    private val parentFingerprint: Int,
    private val sequence: Int,
    internal val versionBytes: ByteArray
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExtendedKey

        if (keyPair != other.keyPair) return false
        if (!versionBytes.contentEquals(other.versionBytes)) return false
        if (!chainCode.contentEquals(other.chainCode)) return false
        if (depth != other.depth) return false
        if (parentFingerprint != other.parentFingerprint) return false
        if (sequence != other.sequence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = keyPair.hashCode()
        result = 31 * result + versionBytes.contentHashCode()
        result = 31 * result + chainCode.contentHashCode()
        result = 31 * result + depth
        result = 31 * result + parentFingerprint
        result = 31 * result + sequence
        return result
    }

    /**
     * 序列化
     */
    fun serialize(publicKeyOnly: Boolean = false): String {
        val out = ByteBuffer.allocate(EXTENDED_KEY_SIZE)
        try {
            if (!publicKeyOnly && !(versionBytes contentEquals xprv) && !(versionBytes contentEquals tprv)) {
                throw KeyException("The extended version bytes dedicated to public keys. Suggest using publicKeyOnly mode")
            }

            if (!publicKeyOnly && keyPair.privateKey.key == BigInteger.ZERO) {
                throw KeyException("The extended key doesn't provide any private key. Suggest using publicKeyOnly mode")
            }

            out.put(if (publicKeyOnly && versionBytes contentEquals xprv) xpub else if (publicKeyOnly && versionBytes contentEquals tprv) tpub else versionBytes)
            out.put(depth)
            out.putInt(parentFingerprint)
            out.putInt(sequence)
            out.put(chainCode)
            if (publicKeyOnly) {
                out.put(keyPair.getCompressedPublicKey())
            } else {
                out.put(0x00)
                out.put(keyPair.privateKey.key.toBytesPadded(PRIVATE_KEY_SIZE))
            }
        } catch (e: IOException) {
            throw IOException()
        }

        return Base58.encode(out.array())
    }
}

/**
 * 根据给定的父级扩展密钥（ExtendedKey）和BIP44路径元素（BIP44Element）来生成子级扩展密钥的。
 * 该函数遵循比特币和其他基于secp256k1或sm2p256v1曲线的加密货币所使用的BIP32标准进行派生。
 *
 * @param element bip44路径
 * @param isGM sm2p256v1 or secp256k1
 * @return 扩展密钥
 */
fun ExtendedKey.generateChildKey(element: BIP44Element, isGM: Boolean = true): ExtendedKey {
    try {
        require(!(element.hardened && keyPair.privateKey.key == BigInteger.ZERO)) {
            "need private key for private generation using hardened paths"
        }
        val mac = CryptoAPI.hmac.init(chainCode)

        val extended: ByteArray
        val pub = keyPair.getCompressedPublicKey(isGM)
        if (element.hardened) {
            val privateKeyPaddedBytes = keyPair.privateKey.key.toBytesPadded(PRIVATE_KEY_SIZE)

            extended = ByteBuffer
                .allocate(privateKeyPaddedBytes.size + 5)
                .order(ByteOrder.BIG_ENDIAN)
                .put(0)
                .put(privateKeyPaddedBytes)
                .putInt(element.numberWithHardeningFlag)
                .array()
        } else {
            // non-hardened
            extended = ByteBuffer
                .allocate(pub.size + 4)
                .order(ByteOrder.BIG_ENDIAN)
                .put(pub)
                .putInt(element.numberWithHardeningFlag)
                .array()
            //if (isGM) extended[32] = 0
        }
        val lr = mac.generate(extended)
        val skBytes = lr.copyOfRange(0, PRIVATE_KEY_SIZE)
        val chaincode = lr.copyOfRange(PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)

        val m = BigInteger(1, skBytes)

        // 获取椭圆曲线参数
        val curveParams = getCurveParams(if (isGM) sm2p256v1 else secp256k1)
        val curve = EllipticCurve(curveParams)

        if (m >= curve.n) {
            throw KeyException(
                "Child key derivation resulted in a key with higher modulus. Suggest deriving the next increment."
            )
        }

        return if (keyPair.privateKey.key != BigInteger.ZERO) {
            val k = m.add(keyPair.privateKey.key).mod(curve.n)
            if (k == BigInteger.ZERO) {
                throw KeyException("Child key derivation resulted in zeros. Suggest deriving the next increment.")
            }
            ExtendedKey(
                PrivateKey(k).toECKeyPair(isGM),
                chaincode,
                (depth + 1).toByte(),
                keyPair.computeFingerPrint(isGM),
                element.numberWithHardeningFlag,
                versionBytes
            )
        } else {
            val q = curve.g.mul(m).add(curve.decodePoint(pub)).normalize()
            if (q.isInfinity()) {
                throw KeyException("Child key derivation resulted in zeros. Suggest deriving the next increment.")
            }
            val curvePoint = curve.createPoint(q.x, q.y)
            ExtendedKey(
                ECKeyPair(PrivateKey(BigInteger.ZERO), curvePoint.toPublicKey()),
                chaincode,
                (depth + 1).toByte(),
                keyPair.computeFingerPrint(isGM),
                element.numberWithHardeningFlag,
                versionBytes
            )
        }
    } catch (e: NoSuchAlgorithmException) {
        throw KeyException(e)
    } catch (e: NoSuchProviderException) {
        throw KeyException(e)
    } catch (e: InvalidKeyException) {
        throw KeyException(e)
    }
}

/**
 * 计算密钥对的指纹，该指纹是基于其压缩形式的公钥生成的。
 *
 * 1.获取压缩公钥；
 * 2.对压缩公钥进行SHA-256哈希运算，对哈希执行RIPEMD-160运算，生成160位的哈希值（publicKeyHash）；
 * 3.迭代publicKeyHash的前四个字节，将这四个字节转换成一个32位的整数作为指纹。
 *
 * @param isGM sm2p256v1 or secp256k1
 * @return 指纹
 */
fun ECKeyPair.computeFingerPrint(isGM: Boolean = true): Int {
    val publicKeyHash = getCompressedPublicKey(isGM)
        .sha256()
        .digestRipemd160()

    var fingerprint = 0
    for (i in 0..3) {
        fingerprint = fingerprint shl 8
        fingerprint = fingerprint or (publicKeyHash[i].toInt() and 0xff)
    }
    return fingerprint
}