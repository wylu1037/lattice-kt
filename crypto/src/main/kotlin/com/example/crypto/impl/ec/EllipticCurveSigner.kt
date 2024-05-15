package com.example.crypto.impl.ec

import com.example.crypto.api.ec.Signer
import com.example.model.PUBLIC_KEY_SIZE
import com.example.model.SignatureData
import com.example.model.extension.*
import org.bouncycastle.asn1.x9.X9IntegerConverter
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.crypto.signers.SM2Signer
import org.bouncycastle.crypto.signers.StandardDSAEncoding
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.math.ec.custom.gm.SM2P256V1Curve
import org.bouncycastle.math.ec.custom.sec.SecP256K1Curve
import org.bouncycastle.util.Arrays
import org.komputing.khex.decode
import java.math.BigInteger

class EllipticCurveSigner(private val curveName: String) : Signer {
    private val curveParams = getCurveParams(curveName)
    private val domainParams = getDomainParams(curveName)
    private val sm2P = "FFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF00000000FFFFFFFFFFFFFFFF".toBigInteger(16)

    /**
     * r、s: r 和 s 是两个大整数，它们是通过对消息哈希值应用椭圆曲线密码学运算得出的。在签名生成过程中，通过私钥对哈希值进行操作后会产生两个随机数，
     *       分别是 r 和 s。这两个值共同构成签名的核心部分，能够证明签名者拥有对应公钥的私钥，并且同意了签名的消息内容。
     * v: 在某些签名实现中（例如以太坊的ECDSA签名），v 是一个额外的值，用来表示签名过程中使用的椭圆曲线点的奇偶性，或者在某些情况下用于标识签名算法
     *    的版本或恢复标识符。v 的取值范围通常会根据具体实现有所不同，但它同样是签名验证过程中的重要组成部分，用于确定签名的有效性和恢复原始签名者的公钥。
     * e: 在 SM2 签名的情况下，e 是计算过程中需要的一个中间值，代表了消息经过特定哈希处理后的结果。
     *    在 ECDSA 签名中通常称为“被签名的消息摘要”（hashed message），在这里可能是针对 SM2 标准进行了相应的计算调整。
     *
     * @param message 待签名消息
     * @param privateKey 私钥
     * @return 签名结果
     */
    override fun sign(message: ByteArray, privateKey: BigInteger): SignatureData {
        if (curveName == sm2p256v1) {
            val signer = SM2Signer()
            val e = calculateE(message, publicFromPrivate(privateKey))
            val privateKeyParameters = ECPrivateKeyParameters(privateKey, domainParams)
            signer.init(true, privateKeyParameters)
            signer.update(message, 0, message.size)
            val sig = signer.generateSignature()
            val components = StandardDSAEncoding.INSTANCE.decode(curveParams.n, sig)
            val r = components[0]
            val s = components[1]

            return SignatureData(r, s, "01".toBigInteger(), e.toBigInteger())
        } else {
            val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))

            val privateKeyParameters = ECPrivateKeyParameters(privateKey, domainParams)
            signer.init(true, privateKeyParameters)
            val components = signer.generateSignature(message)
            val r = components[0]
            val s = components[1]

            //recovery id取值范围为[0..3]
            val recoveryId = calculateRecId(message, publicFromPrivate(privateKey), SignatureData(r, s))

            return SignatureData(r, s, recoveryId.toBigInteger())
        }
    }

    /**
     * 根据给定的消息(message)、公钥(publicKey)以及签名数据(signature)计算恢复标识符(recId)，
     * 恢复标识符用于从签名数据中恢复原始签名密钥。
     *
     * @param message 消息
     * @param publicKey 公钥
     * @param signature 签名
     * @return 恢复标识符
     */
    private fun calculateRecId(message: ByteArray, publicKey: BigInteger, signature: SignatureData): Int {
        for (i in 0..3) {
            val k = recover(i, message, signature)
            if (k != null && k == publicKey) {
                return i
            }
        }
        throw RuntimeException("Could not construct a recoverable key. This should never happen.")
    }

    private fun calculateE(
        message: ByteArray,
        publicKey: BigInteger,
        userId: String = "31323334353637383132333435363738"
    ): ByteArray {
        val z = getZ(publicKey, userId)
        return z.plus(message).hash()
    }

    private fun getZ(publicKey: BigInteger, userID: String): ByteArray {
        val ecParams = curveParams
        val pub = publicKey.toHexStringZeroPadded(130, false)
        val a = ecParams.curve.a.toBigInteger().toHexStringZeroPadded(64, false)
        val b = ecParams.curve.b.toBigInteger().toHexStringZeroPadded(64, false)
        val gx = ecParams.g.affineXCoord.toBigInteger().toHexStringZeroPadded(64, false)
        val gy = ecParams.g.affineYCoord.toBigInteger().toHexStringZeroPadded(64, false)
        val px = pub.substring(2, 66)
        val py = pub.substring(66, 130)
        val data = byteArrayOf(0, 128.toByte()).plus(decode(userID + a + b + gx + gy + px + py))

        return data.hash(true)
    }

    override fun recover(recId: Int, message: ByteArray, signature: SignatureData): BigInteger? {
        require(recId >= 0) { "recId must be positive" }
        require(signature.r.signum() >= 0) { "r must be positive" }
        require(signature.s.signum() >= 0) { "s must be positive" }

        val n = curveParams.n
        val i = BigInteger.valueOf(recId.toLong() / 2)
        val x = signature.r.add(i.multiply(n))

        val prime = if (curveName == sm2p256v1) SM2P256V1Curve.q else SecP256K1Curve.q
        if (x >= prime) {
            return null
        }

        val r = decompressKey(x, (recId and 1) == 1)

        if (!r.multiply(n).isInfinity) {
            return null
        }

        val e = BigInteger(1, message)
        val eInv = BigInteger.ZERO.subtract(e).mod(n)
        val rInv = signature.r.modInverse(n)
        val srInv = rInv.multiply(signature.s).mod(n)
        val eInvrInv = rInv.multiply(eInv).mod(n)
        val q = ECAlgorithms.sumOfTwoMultiplies(curveParams.g, eInvrInv, r, srInv)

        val qBytes = q.getEncoded(false)
        return BigInteger(1, Arrays.copyOfRange(qBytes, 1, qBytes.size))
    }

    override fun verify(message: ByteArray, signatureData: SignatureData, publicKey: BigInteger): Boolean {
        // val publicKeyArray = publicKey.toByteArray()
        val publicKeyArray = publicKey.toBytesPadded(PUBLIC_KEY_SIZE + 1)

        return if (curveName == sm2p256v1) {
            val signer = SM2Signer()
            publicKeyArray[0] = 4
            val publicKeyWithPrefix = BigInteger(1, Arrays.copyOfRange(publicKeyArray, 0, publicKeyArray.size))
            val publicKeyParameters =
                ECPublicKeyParameters(domainParams.curve.decodePoint(publicKeyWithPrefix.toByteArray()), domainParams)
            signer.init(false, publicKeyParameters)
            signer.update(message, 0, message.size)
            val sig = StandardDSAEncoding.INSTANCE.encode(curveParams.n, signatureData.r, signatureData.s)
            signer.verifySignature(sig)
        } else {
            val signer = ECDSASigner(HMacDSAKCalculator(SHA256Digest()))
            publicKeyArray[0] = 4
            publicKeyArray.let {
                val publicKeyParameters =
                    ECPublicKeyParameters(domainParams.curve.decodePoint(it), domainParams)
                signer.init(false, publicKeyParameters)
                signer.verifySignature(message, signatureData.r, signatureData.s)
            }
        }
    }

    private fun decompressSm2Key(xBN: BigInteger, yBit: Boolean): ECPoint {
        val x3 = xBN.multiply(xBN).multiply(xBN).add(curveParams.curve.b.toBigInteger())
        val a = SM2P256V1Curve().fromBigInteger(curveParams.curve.a.toBigInteger())
        val x = SM2P256V1Curve().fromBigInteger(xBN)
        val ax = a.multiply(x)

        val y = x3.add(ax.toBigInteger())
            .modPow(sm2P.add(BigInteger.ONE).divide(BigInteger.valueOf(4)), sm2P)

        if (yBit != y.isOdd()) {
            y.subtract(sm2P)
        }
        println(y.toHexStringNoPrefix())
        return domainParams.curve.createPoint(xBN, y)
    }

    /** Decompress a compressed public key (x co-ord and low-bit of y-coord).  */
    private fun decompressKey(xBN: BigInteger, yBit: Boolean): ECPoint {
        val x9 = X9IntegerConverter()
        val compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(curveParams.curve))
        compEnc[0] = (if (yBit) 0x03 else 0x02).toByte()
        return domainParams.curve.decodePoint(compEnc)
    }

    private fun BigInteger.isOdd(): Boolean {
        return abs().toByteArray()[0].toInt() == 1
    }

    private fun getSM2Scalar(a: ByteArray): ByteArray {
        val scalarBytes: ByteArray
        val result = ByteArray(32)

        val n = a.toBigInteger()
        scalarBytes = if (n >= curveParams.n) {
            n.mod(curveParams.n)
            n.toByteArray()
        } else {
            a
        }

        scalarBytes.forEachIndexed { index, byte ->
            result[scalarBytes.size - index - 1] = byte
        }
        return result
    }

    override fun publicFromPrivate(privateKey: BigInteger): BigInteger {
        val point = publicPointFromPrivate(privateKey)

        val encoded = point.getEncoded(false)
        return BigInteger(1, Arrays.copyOfRange(encoded, 1, encoded.size))
    }

    /**
     * Returns public key point from the given private key.
     */
    private fun publicPointFromPrivate(privateKey: BigInteger): ECPoint {
        val postProcessedPrivateKey = if (privateKey.bitLength() > curveParams.n.bitLength()) {
            privateKey.mod(domainParams.n)
        } else {
            privateKey
        }
        return FixedPointCombMultiplier().multiply(domainParams.g, postProcessedPrivateKey)
    }
}
