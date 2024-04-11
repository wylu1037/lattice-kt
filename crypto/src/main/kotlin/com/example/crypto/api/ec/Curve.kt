package com.example.crypto.api.ec

import java.math.BigInteger

interface Curve {
    val n: BigInteger
    val g: CurvePoint

    fun decodePoint(bytes: ByteArray): CurvePoint
    fun createPoint(x: BigInteger, y: BigInteger): CurvePoint
}