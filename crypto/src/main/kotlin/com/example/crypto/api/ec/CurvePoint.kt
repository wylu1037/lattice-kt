package com.example.crypto.api.ec

import com.example.model.PublicKey
import java.math.BigInteger

/**
 * @property x 椭圆曲线上点的横坐标
 * @property y 椭圆曲线上点的纵坐标
 */
interface CurvePoint {
    val x: BigInteger
    val y: BigInteger

    /**
     * 点的倍乘运算
     */
    fun mul(n: BigInteger): CurvePoint

    /**
     * 椭圆曲线点加法操作
     */
    fun add(p: CurvePoint): CurvePoint

    /**
     * 此方法用于规范化椭圆曲线上的点，确保它是以最简形式表示，例如消除多余的因式或者处理无穷远点等特殊情况。
     */
    fun normalize(): CurvePoint

    /**
     * 判断当前点是否是无穷远点（O），即是否是椭圆曲线群的单位元。
     */
    fun isInfinity(): Boolean

    /**
     * 将椭圆曲线上的点编码为大整数，可选地采用压缩格式。在压缩格式下，利用椭圆曲线的特性可以仅用一个大整数来表示一个点，
     * 从而节省存储空间。非压缩格式则通常包含 x 和 y 坐标两个大整数的信息。
     */
    fun encoded(compressed: Boolean = false): ByteArray
}

fun CurvePoint.toPublicKey() = encoded().let {
    PublicKey(BigInteger(1, it.copyOfRange(1, it.size)))
}