package com.example.lattice

import com.example.abi.LatticeAbi
import com.example.abi.encode
import com.example.abi.getFunction
import com.example.lattice.model.Transaction
import com.example.lattice.model.TxTypeEnum
import com.example.lattice.model.sign
import com.example.lattice.provider.URL
import com.example.model.Address
import com.example.model.toHex
import org.junit.Test
import java.time.Instant

internal const val ACCOUNT_ADDRESS_STR = "zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi"
internal const val LINKER_ADDRESS_STR = "zltc_nbrZcx1AzBXC361nWSwry8JgSJNEzrNiD"
internal const val PRIVATE_KEY_HEX = "0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb"
internal const val IS_GM = true
internal const val CHAIN_ID = 1
internal const val HTTP_URL = "http://192.168.1.115:13000"

class LatticeTest {

    private val lattice: ILattice = Lattice(URL(HTTP_URL), CHAIN_ID)

    @Test
    fun `get balance`() {
        val balance = lattice.getBalanceWithPending(Address(ACCOUNT_ADDRESS_STR))
        println(gson.toJson(balance))
    }

    @Test
    fun `send transaction`() {
        val latestTBlock = lattice.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            payload = "0x01",
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address(LINKER_ADDRESS_STR),
            type = TxTypeEnum.SEND
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = lattice.sendRawTBlock(tx)
        val receipt = lattice.getReceipt(hash)
        println(gson.toJson(receipt))
    }

    @Test
    fun `send deploy contract transaction`() {
        val bytecode =
            "0x60806040526000805534801561001457600080fd5b50610278806100246000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80635b34b96614610046578063a87d942c14610050578063f5c5ad831461006e575b600080fd5b61004e610078565b005b610058610093565b60405161006591906100d0565b60405180910390f35b61007661009c565b005b600160008082825461008a919061011a565b92505081905550565b60008054905090565b60016000808282546100ae91906101ae565b92505081905550565b6000819050919050565b6100ca816100b7565b82525050565b60006020820190506100e560008301846100c1565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610125826100b7565b9150610130836100b7565b9250817f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0383136000831215161561016b5761016a6100eb565b5b817f80000000000000000000000000000000000000000000000000000000000000000383126000831216156101a3576101a26100eb565b5b828201905092915050565b60006101b9826100b7565b91506101c4836100b7565b9250827f8000000000000000000000000000000000000000000000000000000000000000018212600084121516156101ff576101fe6100eb565b5b827f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff018213600084121615610237576102366100eb565b5b82820390509291505056fea2646970667358221220d841351625356129f6266ada896818d690dbc4b0d176774a97d745dfbe2fe50164736f6c634300080b0033"

        val latestTBlock = lattice.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address(LINKER_ADDRESS_STR),
            type = TxTypeEnum.CONTRACT,
            code = bytecode
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = lattice.sendRawTBlock(tx)
        val receipt = lattice.getReceipt(hash)
        println(gson.toJson(receipt))
    }

    @Test
    fun `execute contract increment counter`() {
        val abi =
            "[{\"inputs\":[],\"name\":\"decrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCount\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"incrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val latestTBlock = lattice.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val method = "incrementCounter"
        val code = LatticeAbi(abi).getFunction(method).encode(arrayOf())
        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address("zltc_RvRUFNUYCg2vsjHii713Gc9Y3VNauM46J"),
            type = TxTypeEnum.EXECUTE,
            code = code
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = lattice.sendRawTBlock(tx)
        val receipt = lattice.getReceipt(hash)
        println(gson.toJson(receipt))
    }

    @Test
    fun `execute contract get counter`() {
        val abi =
            "[{\"inputs\":[],\"name\":\"decrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCount\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"incrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val latestTBlock = lattice.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val method = "getCount"
        val code = LatticeAbi(abi).getFunction(method).encode(arrayOf())
        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address("zltc_RvRUFNUYCg2vsjHii713Gc9Y3VNauM46J"),
            type = TxTypeEnum.EXECUTE,
            code = code
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = lattice.sendRawTBlock(tx)
        val receipt = lattice.getReceipt(hash)
        println(gson.toJson(receipt))
    }
}