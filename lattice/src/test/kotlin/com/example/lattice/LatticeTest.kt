package com.example.lattice

import com.example.lattice.provider.URL
import com.example.model.Address
import org.junit.Test

class LatticeTest {

    @Test
    fun `get balance`() {
        val lattice = Lattice(URL("http://192.168.1.115:13000"), 1)
        val balance = lattice.getBalanceWithPending(Address("zltc_SWPBMR765rAZPAth1ni6c36s4jxGBdaWr"))
        println(gson.toJson(balance))
    }
}