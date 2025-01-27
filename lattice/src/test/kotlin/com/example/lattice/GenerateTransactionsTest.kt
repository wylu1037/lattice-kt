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
import com.example.model.block.SendTBlock
import com.example.model.toHex
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class GenerateTransactionsTest {

    private val lattice = LatticeImpl(
        ChainConfig(chainId = Constants.CHAIN_ID.toInt(), curve = Curve.Sm2p256v1, tokenLess = true),
        ConnectingNodeConfig(Constants.HTTP_URL),
        CredentialConfig(
            accountAddress = Constants.ACCOUNT_ADDRESS,
            privateKey = Constants.PRIVATE_KEY,
        ),
    )

    private val httpApi: HttpApi = HttpApiImpl(HttpApiParams(URL(Constants.HTTP_URL)))

    object Constants {
        internal const val IS_GM = true
        internal const val CHAIN_ID = "1"
        internal const val HTTP_URL = "http://192.168.2.40:13000"
        internal const val ACCOUNT_ADDRESS = "zltc_mAsC8VzKGmumYGGAeqn9dz5pTohVTgpTk"
        internal const val PRIVATE_KEY = "0x2cd1ae6e78e8c9b3232477db66559c8b796fdf5419930708ee581ee4a708f826"

        internal const val QUANTITY_PER_ACCOUNT = 1_000 // 每个账户总共生成多少笔交易
        internal const val RESET_DAEMON_BLOCK_HASH_PER = 1000 // 多少笔交易更新守护区块哈希

        internal const val BATCH_SIZE = 10 // 每笔批量包含的交易数量
        internal const val COUNTER_ADDRESS = "zltc_VELeCECodwfYikWXoqabcxoNAzTq2hmFJ" // 计数器合约地址
        internal const val COUNTER_ABI =
            "[{\"inputs\":[],\"name\":\"decrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCount\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"incrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
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
    fun `deploy counter contract`() {
        val bytecode =
            "0x60806040526000805534801561001457600080fd5b50610278806100246000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80635b34b96614610046578063a87d942c14610050578063f5c5ad831461006e575b600080fd5b61004e610078565b005b610058610093565b60405161006591906100d0565b60405180910390f35b61007661009c565b005b600160008082825461008a919061011a565b92505081905550565b60008054905090565b60016000808282546100ae91906101ae565b92505081905550565b6000819050919050565b6100ca816100b7565b82525050565b60006020820190506100e560008301846100c1565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610125826100b7565b9150610130836100b7565b9250817f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0383136000831215161561016b5761016a6100eb565b5b817f80000000000000000000000000000000000000000000000000000000000000000383126000831216156101a3576101a26100eb565b5b828201905092915050565b60006101b9826100b7565b91506101c4836100b7565b9250827f8000000000000000000000000000000000000000000000000000000000000000018212600084121516156101ff576101fe6100eb565b5b827f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff018213600084121615610237576102366100eb565b5b82820390509291505056fea2646970667358221220d841351625356129f6266ada896818d690dbc4b0d176774a97d745dfbe2fe50164736f6c634300080b0033"
        val receipt = lattice.deployContractWaitReceipt(Constants.CHAIN_ID, bytecode)
        println(gson.toJson(receipt))
    }

    @Test
    fun `concurrent generate signed call contract transactions`() {
        val contractAddress = "zltc_hgF4ay6tanS27VupLDEs6yf8aoZx6RuV4"
        val accounts = arrayOf(Constants.ACCOUNT_ADDRESS)
        val privateKeys = arrayOf(Constants.PRIVATE_KEY)
        val function = LatticeAbi(Constants.CONTRACT_ABI).getFunction("save")
        val args = arrayOf<Any>("Hello")
        val code = function.encode(args)

        runBlocking {
            val deferredArray = mutableListOf<Deferred<Unit>>()
            for ((index, account) in accounts.withIndex()) {
                val deferredResult = async {
                    val file = File("./account${index}_tx.txt")
                    if (!file.exists()) file.createNewFile()

                    val latestBlock = httpApi.getLatestTDBlockWithCatch(Constants.CHAIN_ID, Address(account))

                    file.printWriter(Charsets.UTF_8).use { out ->
                        var resetDaemonBlockHashIdx = 0
                        for (i in 1..Constants.QUANTITY_PER_ACCOUNT) {
                            val tx = CallContractTXBuilder.builder()
                                .setBlock(latestBlock)
                                .setOwner(Address(account))
                                .setLinker(Address(contractAddress))
                                .setCode(code)
                                .build()

                            val (_, signature) = tx.sign(
                                privateKeys[index],
                                Constants.IS_GM,
                                Constants.CHAIN_ID.toInt()
                            )
                            tx.sign = signature.toHex()

                            val hash = tx.calculateTransactionHash()

                            // update daemon block hash
                            resetDaemonBlockHashIdx++
                            if (resetDaemonBlockHashIdx == Constants.RESET_DAEMON_BLOCK_HASH_PER) {
                                val block = httpApi.getLatestTDBlockWithCatch(Constants.CHAIN_ID, Address(account))
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

    @Test
    fun `concurrent generate batch transactions`() {
        val accounts = arrayOf(Constants.ACCOUNT_ADDRESS)
        val privateKeys = arrayOf(Constants.PRIVATE_KEY)
        val function = LatticeAbi(Constants.COUNTER_ABI).getFunction("incrementCounter")
        val code = function.encode(emptyArray())

        runBlocking {
            val deferredArray = mutableListOf<Deferred<Unit>>()
            for ((index, account) in accounts.withIndex()) {
                val deferredResult = async {
                    val file = File("./account${index}_tx.txt")
                    if (!file.exists()) file.createNewFile()

                    val latestBlock = httpApi.getLatestTDBlockWithCatch(Constants.CHAIN_ID, Address(account))

                    file.printWriter(Charsets.UTF_8).use { out ->
                        var resetDaemonBlockHashIdx = 0
                        val txs = mutableListOf<SendTBlock>()
                        for (i in 1..Constants.QUANTITY_PER_ACCOUNT) {
                            val tx = CallContractTXBuilder.builder()
                                .setBlock(latestBlock)
                                .setOwner(Address(account))
                                .setLinker(Address(Constants.COUNTER_ADDRESS))
                                .setCode(code)
                                .build()

                            val (_, signature) = tx.sign(
                                privateKeys[index],
                                Constants.IS_GM,
                                Constants.CHAIN_ID.toInt()
                            )
                            tx.sign = signature.toHex()

                            val hash = tx.calculateTransactionHash()

                            // update daemon block hash
                            resetDaemonBlockHashIdx++
                            if (resetDaemonBlockHashIdx == Constants.RESET_DAEMON_BLOCK_HASH_PER) {
                                val block = httpApi.getLatestTDBlockWithCatch(Constants.CHAIN_ID, Address(account))
                                latestBlock.currentDBlockHash = block.currentDBlockHash
                                resetDaemonBlockHashIdx = 0
                            }

                            latestBlock.currentTBlockHash = hash
                            latestBlock.currentTBlockNumber = tx.number

                            txs.add(tx.toSendTBlock())

                            if (txs.size == Constants.BATCH_SIZE) {
                                out.println(gson.toJson(txs))
                                txs.clear()
                            }
                        }
                    }
                }
                deferredArray.add(deferredResult)
            }
            deferredArray.awaitAll()
        }
    }
}