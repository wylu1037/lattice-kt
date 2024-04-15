package com.example.abi

import junit.framework.TestCase.assertEquals
import org.junit.Test

class FuncTest {
    private val abi =
        "[{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolSuite\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"addProtocol\",\"outputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"name\":\"getAddress\",\"outputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Protocol[]\",\"name\":\"protocol\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"updateProtocol\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"getTraceability\",\"outputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"number\",\"type\":\"uint64\"},{\"internalType\":\"uint64\",\"name\":\"protocol\",\"type\":\"uint64\"},{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Evidence[]\",\"name\":\"evi\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"setDataSecret\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"writeTraceability\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"internalType\":\"struct Business.batch[]\",\"name\":\"bt\",\"type\":\"tuple[]\"}],\"name\":\"writeTraceabilityBatch\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"

    @Test
    fun encode() {
        val func = Func(abi, "setDataSecret")
        val args = arrayOf<Any>("10", "561717f7922a233720ae38acaa4174cda0bf1766")

        val code = func.encode(args)
        val expected =
            "0xa2ec96570000000000000000000000000000000000000000000000000000000000000040000000000000000000000000561717f7922a233720ae38acaa4174cda0bf176600000000000000000000000000000000000000000000000000000000000000023130000000000000000000000000000000000000000000000000000000000000"
        assertEquals(expected, code)
    }
}