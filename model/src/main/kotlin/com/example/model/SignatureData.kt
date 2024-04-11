package com.example.model

import org.komputing.khex.extensions.clean0xPrefix
import org.komputing.khex.extensions.isValidHex
import org.komputing.khex.model.HexString
import java.math.BigInteger
import java.security.SignatureException

data class SignatureData constructor(
    var r: BigInteger = BigInteger.ZERO,
    var s: BigInteger = BigInteger.ZERO,
    var v: BigInteger = BigInteger.ZERO,
    var e: BigInteger = BigInteger.ZERO,
) {
    companion object {
        fun fromHexString(signature: String): SignatureData {
            if (HexString(signature).isValidHex()) {
                throw SignatureException("Invalid signature $signature")
            }
            val cleanedHex = HexString(signature).clean0xPrefix().string
            if (cleanedHex.length <= 128){
                throw SignatureException("Signature hex too short, expected more than 128 bytes")
            }

            val r = BigInteger(signature.substring(0, 64), 16)
            val s = BigInteger(signature.substring(64, 128), 16)
            val v = BigInteger(signature.substring(128, 130 ), 16)

            if (signature.length > 130) {
                val e = BigInteger(signature.substring(130), 16)
                return SignatureData(r, s, v, e)
            }
            return SignatureData(r, s, v)
        }
    }
}