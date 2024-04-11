package com.example.crypto.impl.ec

import com.example.crypto.api.ec.Curve
import com.example.crypto.api.ec.CurvePoint
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import java.math.BigInteger

const val sm2p256v1 = "sm2p256v1"
const val secp256k1 = "secp256k1"

/**
 * @param name 椭圆曲线名称
 * @return X9ECParameters，包含了特定椭圆曲线的所有参数，例如曲线方程、基点 G、阶 n 以及曲线上点的共轭次数 h 等。
 */
fun getCurveParams(name: String): X9ECParameters =
    CustomNamedCurves.getByName(name)

/**
 * @param name 椭圆曲线名称
 * @return ECDomainParameters，包含了完整的椭圆曲线域参数，包括椭圆曲线本身、基点 G、阶 n 以及曲线上点的共轭次数 h。
 */
fun getDomainParams(name: String): ECDomainParameters =
    getCurveParams(name).run {
        ECDomainParameters(curve, g, n, h)
    }

class EllipticCurve(private val curveParams: X9ECParameters) : Curve {

    override val n: BigInteger
        get() = curveParams.n
    override val g: CurvePoint
        get() = curveParams.g.toCurvePoint()

    override fun decodePoint(bytes: ByteArray): CurvePoint =
        curveParams.curve.decodePoint(bytes).toCurvePoint()


    override fun createPoint(x: BigInteger, y: BigInteger): CurvePoint =
        curveParams.curve.createPoint(x, y).toCurvePoint()
}
