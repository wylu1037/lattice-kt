package com.example.model

import org.junit.Test

class AddressTest {

    @Test
    fun `zltc address to eth address`() {
        val addr = "zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi"
        println(Address(addr).toEthereumAddress())
    }

    @Test
    fun `new zltc address`() {
        val addr = Address("zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi")
    }
}