package com.example.rlp

import com.example.model.extension.removeLeadingZero
import com.example.model.extension.toMinimalByteArray
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.model.HexString
import java.math.BigInteger
import java.math.BigInteger.ZERO

/**
 * RLP as of Appendix B. Recursive Length Prefix at https://github.com/ethereum/yellowpaper
 */
fun String.toRLP() = if (this.startsWith("0x")) {
    val str = this.substring(2)
    if (str.isEmpty()) {
        byteArrayOf().toRLP()
    } else {
        HexString(str).hexToByteArray().toRLP()
    }
} else if (this.isBlank()) {
    byteArrayOf().toRLP()
} else {
    RLPElement(toByteArray())
}

fun Int.toRLP() = RLPElement(toMinimalByteArray())
fun BigInteger.toRLP() = RLPElement(toByteArray().removeLeadingZero())
fun ByteArray.toRLP() = RLPElement(this)
fun Byte.toRLP() = RLPElement(ByteArray(1) { this })

fun Array<*>.toRLP(): RLPList {
    return if (this.isArrayOf<String>()) {
        RLPList(this.map { (it as String).toRLP() })
    } else if (this.isArrayOf<Int>()) {
        RLPList(this.map { (it as Int).toRLP() })
    } else if (this.isArrayOf<BigInteger>()) {
        RLPList(this.map { (it as BigInteger).toRLP() })
    } else if (this.isArrayOf<Byte>()) {
        RLPList(this.map { (it as Byte).toRLP() })
    } else {
        RLPList(this.map { it.toString().toRLP() })
    }
}

// from RLP
fun RLPElement.toIntFromRLP() = if (bytes.isEmpty()) {
    0
} else {
    bytes.mapIndexed { index, byte -> (byte.toInt() and 0xff).shl((bytes.size - 1 - index) * 8) }
        .reduce { acc, i -> acc + i }
}

fun RLPElement.toUnsignedBigIntegerFromRLP(): BigInteger = if (bytes.isEmpty()) ZERO else BigInteger(1, bytes)
fun RLPElement.toByteFromRLP(): Byte {
    require(bytes.size == 1) { "trying to convert RLP with != 1 byte to Byte" }
    return bytes.first()
}

fun RLPElement.toStringFromRLP() = String(bytes)