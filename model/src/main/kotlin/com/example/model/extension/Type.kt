package com.example.model.extension

import kotlin.experimental.or

fun BooleanArray.toByteArray(len: Int = this.size / 8): ByteArray {
    val result = ByteArray(len)
    for (byteIndex in result.indices)
        for (bitIndex in 0..7)
            if (this[byteIndex * 8 + bitIndex]) {
                result[byteIndex] = result[byteIndex] or (1 shl (7 - bitIndex)).toByte()
            }
    return result
}