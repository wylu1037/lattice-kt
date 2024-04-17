package com.example.lattice

import com.example.lattice.model.Transaction
import com.example.lattice.model.TxTypeEnum
import com.example.lattice.model.sign
import com.example.lattice.provider.URL
import com.example.model.Address
import com.example.model.toHex
import org.junit.Test
import java.time.Instant

class LatticeTest {

    @Test
    fun `get balance`() {
        val lattice = Lattice(URL("http://192.168.1.115:13000"), 1)
        val balance = lattice.getBalanceWithPending(Address("zltc_SWPBMR765rAZPAth1ni6c36s4jxGBdaWr"))
        println(gson.toJson(balance))
    }

    @Test
    fun `send transaction`() {
        val lattice = Lattice(URL("http://192.168.1.115:13000"), 1)
        val latestTBlock = lattice.getLatestTDBlockWithCatch(Address("zltc_T3MMEH9S2bSzo5EDiAt6H8KiXZvchGyw2"))
        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            payload = "0x01",
            timestamp = Instant.now().epochSecond,
            owner = Address("zltc_T3MMEH9S2bSzo5EDiAt6H8KiXZvchGyw2"),
            linker = Address("zltc_nbrZcx1AzBXC361nWSwry8JgSJNEzrNiD"),
            type = TxTypeEnum.SEND
        )
        val (_, signature) = tx.sign("0x663e2481bf5645cf80705d5a180d2937f72ba8ba3e1b14b34064a42ec2b1ae74", true, 1)
        tx.sign = signature.toHex()

        val hash = lattice.sendRawTBlock(tx)
        val receipt = lattice.getReceipt(hash)
        println(gson.toJson(receipt))
    }
}