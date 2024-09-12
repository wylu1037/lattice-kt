package com.example.lattice

import com.example.abi.LatticeAbi
import com.example.abi.encode
import com.example.abi.getFunction
import com.example.lattice.model.calculateTransactionHash
import com.example.lattice.model.sign
import com.example.lattice.model.toSendTBlock
import com.example.lattice.provider.HttpApi
import com.example.lattice.provider.HttpApiImpl
import com.example.lattice.provider.HttpApiParams
import com.example.lattice.provider.URL
import com.example.model.Address
import com.example.model.toHex
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class GenerateTransactionsTest {

    private val httpApi: HttpApi = HttpApiImpl(HttpApiParams(URL(Constants.HTTP_URL), Constants.CHAIN_ID))

    object Constants {
        internal const val IS_GM = true
        internal const val CHAIN_ID = 490
        internal const val HTTP_URL = "http://192.168.3.16:45001"

        internal const val CONTRACT_ABI =
            "[{\"inputs\":[{\"internalType\":\"string\",\"name\":\"str\",\"type\":\"string\"}],\"name\":\"save\",\"outputs\":[{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"hash\",\"type\":\"bytes32\"}],\"name\":\"read\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"bytes32\",\"name\":\"\",\"type\":\"bytes32\"}],\"name\":\"strMap\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"}]"
    }

    @Test
    fun `test encode save function`() {
        val args = arrayOf<Any>("Hello")
        val actual = LatticeAbi(Constants.CONTRACT_ABI).getFunction("save").encode(args)
        val expected =
            "0x38e48f060000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000548656c6c6f000000000000000000000000000000000000000000000000000000"
        assertEquals(expected, actual)
    }

    @Test
    fun `concurrent generate signed call contract transactions`() {
        val contractAddress = "zltc_hgF4ay6tanS27VupLDEs6yf8aoZx6RuV4"
        val accounts = arrayOf("zltc_mAsC8VzKGmumYGGAeqn9dz5pTohVTgpTk")
        val privateKeys = arrayOf(
            "0x2cd1ae6e78e8c9b3232477db66559c8b796fdf5419930708ee581ee4a708f826"
        )
        val generatedCountPerAccount = 100_0000
        val resetDaemonBlockHashPer = 1000
        val function = LatticeAbi(Constants.CONTRACT_ABI).getFunction("save")
        val args = arrayOf<Any>("Hello")
        val code = function.encode(args)

        runBlocking {
            val deferredArray = mutableListOf<Deferred<Unit>>()
            for ((index, account) in accounts.withIndex()) {
                val deferredResult = async {
                    val file = File("./account${index}_tx.txt")
                    if (!file.exists()) file.createNewFile()

                    val latestBlock = httpApi.getLatestTDBlockWithCatch(Address(account))

                    file.printWriter(Charsets.UTF_8).use { out ->
                        var resetDaemonBlockHashIdx = 0
                        for (i in 1..generatedCountPerAccount) {
                            val tx = CallContractTXBuilder.builder()
                                .setBlock(latestBlock)
                                .setOwner(Address(account))
                                .setLinker(Address(contractAddress))
                                .setCode(code)
                                .build()

                            val (_, signature) = tx.sign(privateKeys[index], Constants.IS_GM, Constants.CHAIN_ID)
                            tx.sign = signature.toHex()

                            val hash = tx.calculateTransactionHash()

                            // update daemon block hash
                            resetDaemonBlockHashIdx++
                            if (resetDaemonBlockHashIdx == resetDaemonBlockHashPer) {
                                val block = httpApi.getLatestTDBlockWithCatch(Address(account))
                                latestBlock.currentDBlockHash = block.currentDBlockHash
                                resetDaemonBlockHashIdx = 0
                            }

                            latestBlock.currentTBlockHash = hash
                            latestBlock.currentTBlockNumber = tx.number

                            out.println(gson.toJson(tx.toSendTBlock()))
                        }
                    }
                }

                deferredArray.add(deferredResult)
            }
            deferredArray.awaitAll()
        }
    }
}