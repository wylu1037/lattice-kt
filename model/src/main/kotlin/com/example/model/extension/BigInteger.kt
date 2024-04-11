package com.example.model.extension

import java.math.BigInteger


/**
 * The sign is represented as an integer signum value: -1 for negative, 0 for zero, or 1 for positive.
 */
fun ByteArray.toBigInteger() =  BigInteger(1, this)