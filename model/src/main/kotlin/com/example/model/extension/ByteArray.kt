package com.example.model.extension

fun ByteArray.toFixedLengthByteArray(fixedSize: Int, fillByte: Byte = 0) = if (size == fixedSize) {
    this
} else {
    require(size < fixedSize) { "ByteArray too big - max size is $fixedSize but got $size" }
    ByteArray(fixedSize) { getOrNull(size - fixedSize + it) ?: fillByte }
}

// fun ByteArray.removeLeadingZero() = if (first() == 0.toByte()) copyOfRange(1, size) else this
fun ByteArray.removeLeadingZero(): ByteArray {
    if (isEmpty()) return this
    var start = 0
    while (start < size && this[start] == 0.toByte()) start++
    return this.copyOfRange(start, size)
}