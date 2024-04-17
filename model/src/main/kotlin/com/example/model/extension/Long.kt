package com.example.model.extension

import java.math.BigInteger

fun Long.toByteArray() = BigInteger.valueOf(this).toByteArray().stripZeros()
