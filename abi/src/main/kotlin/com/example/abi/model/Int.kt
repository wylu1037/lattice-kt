package com.example.abi.model

import org.web3j.abi.datatypes.IntType
import java.math.BigInteger

class UintNumber(private val value: BigInteger, size: Int) : IntType("uint", size, value)

class IntNumber(private val value: BigInteger, size: Int) : IntType("uint", size, value)

