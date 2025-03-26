package com.example.abi

import org.kethereum.abi.EthereumABI

data class LatticeAbi(val abi: String) {
    private var ethereumAbi: EthereumABI = EthereumABI(abi)

    fun getMethods() = ethereumAbi.methodList
}

fun LatticeAbi.getFunction(methodName: String) = LatticeFunction(abi, methodName, getMethods().filter(methodName))

fun LatticeAbi.getConstructor() = LatticeFunction(abi, null, getMethods().first { it.isConstructor() })
