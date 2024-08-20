package com.example.lattice

import com.example.abi.LatticeAbi
import com.example.abi.decodeReturn
import com.example.abi.encode
import com.example.abi.getFunction
import com.example.lattice.model.*
import com.example.lattice.provider.HttpApi
import com.example.lattice.provider.HttpApiImpl
import com.example.lattice.provider.HttpApiParams
import com.example.lattice.provider.URL
import com.example.model.*
import com.example.model.extension.toBytes32Array
import com.example.model.extension.toHexString
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.io.File
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal const val ACCOUNT_ADDRESS_STR = "zltc_dS73XWcJqu2uEk4cfWsX8DDhpb9xsaH9s"
internal const val LINKER_ADDRESS_STR = "zltc_nbrZcx1AzBXC361nWSwry8JgSJNEzrNiD"
internal const val PRIVATE_KEY_HEX = "0xdbd91293f324e5e49f040188720c6c9ae7e6cc2b4c5274120ee25808e8f4b6a7"
internal const val IS_GM = true
internal const val CHAIN_ID = 1
internal const val HTTP_URL = "http://192.168.1.185:13000"

internal val lattice = LatticeImpl(
    ChainConfig(chainId = 1, curve = Curve.Sm2p256v1, tokenLess = true),
    ConnectingNodeConfig("http://192.168.1.185:13000"),
    CredentialConfig(
        accountAddress = "zltc_dS73XWcJqu2uEk4cfWsX8DDhpb9xsaH9s",
        privateKey = "0xdbd91293f324e5e49f040188720c6c9ae7e6cc2b4c5274120ee25808e8f4b6a7"
    ),
)

internal const val LEDGER_ABI =
    "[{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolSuite\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"addProtocol\",\"outputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"name\":\"getAddress\",\"outputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Protocol[]\",\"name\":\"protocol\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"updateProtocol\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"getTraceability\",\"outputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"number\",\"type\":\"uint64\"},{\"internalType\":\"uint64\",\"name\":\"protocol\",\"type\":\"uint64\"},{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Evidence[]\",\"name\":\"evi\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"writeTraceability\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"internalType\":\"struct Business.batch[]\",\"name\":\"bt\",\"type\":\"tuple[]\"}],\"name\":\"writeTraceabilityBatch\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"

class LatticeTest {

    private val httpApi: HttpApi = HttpApiImpl(HttpApiParams(URL(HTTP_URL), CHAIN_ID))

    @Test
    fun `get balance`() {
        val balance = httpApi.getBalanceWithPending(Address(ACCOUNT_ADDRESS_STR))
        println(gson.toJson(balance))
    }

    @Test
    fun `transfer wait receipt`() {
        val receipt = lattice.transferWaitReceipt("zltc_nbrZcx1AzBXC361nWSwry8JgSJNEzrNiD", "0x01")
        assertNotNull(receipt)
    }

    @Test
    fun `deploy counter contract`() {
        val bytecode =
            "0x60806040526000805534801561001457600080fd5b50610278806100246000396000f3fe608060405234801561001057600080fd5b50600436106100415760003560e01c80635b34b96614610046578063a87d942c14610050578063f5c5ad831461006e575b600080fd5b61004e610078565b005b610058610093565b60405161006591906100d0565b60405180910390f35b61007661009c565b005b600160008082825461008a919061011a565b92505081905550565b60008054905090565b60016000808282546100ae91906101ae565b92505081905550565b6000819050919050565b6100ca816100b7565b82525050565b60006020820190506100e560008301846100c1565b92915050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000610125826100b7565b9150610130836100b7565b9250817f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0383136000831215161561016b5761016a6100eb565b5b817f80000000000000000000000000000000000000000000000000000000000000000383126000831216156101a3576101a26100eb565b5b828201905092915050565b60006101b9826100b7565b91506101c4836100b7565b9250827f8000000000000000000000000000000000000000000000000000000000000000018212600084121516156101ff576101fe6100eb565b5b827f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff018213600084121615610237576102366100eb565b5b82820390509291505056fea2646970667358221220d841351625356129f6266ada896818d690dbc4b0d176774a97d745dfbe2fe50164736f6c634300080b0033"
        val receipt = lattice.deployContractWaitReceipt(bytecode)
        println(gson.toJson(receipt))
    }

    @Test
    fun `call counter contract`() {
        val abi =
            "[{\"inputs\":[],\"name\":\"decrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCount\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"incrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val data = LatticeAbi(abi).getFunction("incrementCounter").encode(arrayOf())
        val receipt = lattice.callContractWaitReceipt("zltc_d1pTRCCH2F6McFCmXYCB743L7spuNtw31", data)
        println(gson.toJson(receipt))
    }

    @Test
    fun `pre call counter contract`() {
        val abi =
            "[{\"inputs\":[],\"name\":\"decrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"getCount\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"incrementCounter\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val data = LatticeAbi(abi).getFunction("getCount").encode(arrayOf())
        val receipt = lattice.preCallContract("zltc_d1pTRCCH2F6McFCmXYCB743L7spuNtw31", data)
        println(gson.toJson(receipt))
    }

    @Test
    fun `build payload tx`() {
        val latestTBlock = httpApi.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val filePath = "./data.txt"
        val file = File(filePath)
        if (!file.exists()) file.createNewFile()

        file.printWriter().use { out ->
            for (i in 1..10000) {
                val tx = TransferTXBuilder.builder()
                    .setBlock(latestTBlock)
                    .setPayload("0x01")
                    .setOwner(Address(ACCOUNT_ADDRESS_STR))
                    .setLinker(Address(LINKER_ADDRESS_STR))
                    .build()
                val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
                tx.sign = signature.toHex()

                val hash = tx.calculateTransactionHash()

                latestTBlock.currentTBlockHash = hash
                latestTBlock.currentTBlockNumber = tx.number

                out.println(gson.toJson(tx.toSendTBlock()))
            }
        }
    }

    @Test
    fun `multiple build payload`() {
        val accounts = arrayOf("zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi", "zltc_RvmBhKxeLojxYCCoMM4iaxpkeJ1FjLBHQ")
        val privateKeys = arrayOf(
            "0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb",
            "0xa83691e0d2241fa9a8bc90f8d27a5aac4d4619f964963a80481e174b773f356a"
        )
        // 每个账户生成多少笔交易
        val count = 10000

        runBlocking {
            val deferredArray = mutableListOf<Deferred<Unit>>()
            for ((index, account) in accounts.withIndex()) {

                val deferredRes = async {
                    val file = File("./data$index.txt")
                    if (!file.exists()) file.createNewFile()

                    val latestTBlock = httpApi.getLatestTDBlockWithCatch(Address(account))

                    file.printWriter().use { out ->
                        for (i in 1..count) {
                            val tx = TransferTXBuilder.builder()
                                .setBlock(latestTBlock)
                                .setPayload("0x01")
                                .setOwner(Address(ACCOUNT_ADDRESS_STR))
                                .setLinker(Address(LINKER_ADDRESS_STR))
                                .build()

                            val (_, signature) = tx.sign(privateKeys[index], IS_GM, CHAIN_ID)
                            tx.sign = signature.toHex()

                            val hash = tx.calculateTransactionHash()

                            latestTBlock.currentTBlockHash = hash
                            latestTBlock.currentTBlockNumber = tx.number

                            out.println(gson.toJson(tx.toSendTBlock()))
                        }
                    }
                }

                deferredArray.add(deferredRes)
            }
            deferredArray.awaitAll()
        }
    }


    @Test
    fun `execute contract`() {
        val abi =
            "[{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolSuite\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"addProtocol\",\"outputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"name\":\"getAddress\",\"outputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Protocol[]\",\"name\":\"protocol\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"updateProtocol\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"getTraceability\",\"outputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"number\",\"type\":\"uint64\"},{\"internalType\":\"uint64\",\"name\":\"protocol\",\"type\":\"uint64\"},{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Evidence[]\",\"name\":\"evi\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"writeTraceability\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"internalType\":\"struct Business.batch[]\",\"name\":\"bt\",\"type\":\"tuple[]\"}],\"name\":\"writeTraceabilityBatch\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val latestTBlock = httpApi.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))
        val method = "writeTraceability"

        val times = 1000
        for (i in 1..times) {
            val code = LatticeAbi(abi).getFunction(method).encode(
                arrayOf(
                    64424509441,
                    generateDataId(),
                    generateStringArr(20),
                    Address("zltc_YBomBNykwMqxm719giBL3VtYV4ABT9a8D").toEthereumAddress()
                )
            )

            val tx = Transaction(
                number = latestTBlock.currentTBlockNumber + 1,
                parentHash = latestTBlock.currentTBlockHash,
                daemonHash = latestTBlock.currentDBlockHash,
                timestamp = Instant.now().epochSecond,
                owner = Address(ACCOUNT_ADDRESS_STR),
                linker = Address("zltc_QLbz7JHiBTspUvTPzLHy5biDS9mu53mmv"),
                type = TxTypeEnum.EXECUTE,
                code = code
            )
            val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
            tx.sign = signature.toHex()

            val hash = httpApi.sendRawTBlock(tx)
            println("交易哈希：$hash")
            val receipt = httpApi.getReceipt(hash)
            println("交易回执：${gson.toJson(receipt)}")
        }
    }

    @Test
    fun `create protocol`() {
        val latestTBlock = httpApi.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))

        val protocolSuite = 10L
        val protocol = "syntax = \"proto3\";\n\nmessage Student {\n\tfloat id = 1;\n}"

        val fn = LatticeAbi(LEDGER_ABI).getFunction("addProtocol")
        val code = fn.encode(
            arrayOf(
                protocolSuite,
                protocol.toByteArray().toBytes32Array()
            )
        )

        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address("zltc_QLbz7JHiBTspUvTPzLHy5biDS9mu53mmv"),
            type = TxTypeEnum.EXECUTE,
            code = code
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = httpApi.sendRawTBlock(tx)
        println("创建协议哈希：$hash")

        Thread.sleep(1_000)

        val receipt = httpApi.getReceipt(hash)
        assertNotNull(receipt)
        assertEquals(receipt.success, true)

        val outputs = fn.decodeReturn(receipt.contractRet)
        println("协议号：${outputs[0].value}")
    }

    @Test
    fun `creat business`() {
        val latestTBlock = httpApi.getLatestTDBlockWithCatch(Address(ACCOUNT_ADDRESS_STR))

        val tx = Transaction(
            number = latestTBlock.currentTBlockNumber + 1,
            parentHash = latestTBlock.currentTBlockHash,
            daemonHash = latestTBlock.currentDBlockHash,
            timestamp = Instant.now().epochSecond,
            owner = Address(ACCOUNT_ADDRESS_STR),
            linker = Address("zltc_QLbz7JHiBTspS9WTWJUrbNsB5wbENMweQ"),
            type = TxTypeEnum.EXECUTE,
            code = byteArrayOf(49).toHexString()
        )
        val (_, signature) = tx.sign(PRIVATE_KEY_HEX, IS_GM, CHAIN_ID)
        tx.sign = signature.toHex()

        val hash = httpApi.sendRawTBlock(tx)
        println("创建业务地址哈希：$hash")

        Thread.sleep(1_000)

        val receipt = httpApi.getReceipt(hash)
        assertNotNull(receipt)
        assertEquals(receipt.success, true)

        println("业务合约地址：${EthereumAddress(receipt.contractRet).toAddress().address}")
    }
}

fun generateStringArr(len: Int): Array<String> {
    return Array(len) { _ ->
        "0x0c3505786be40e9cf488bce6b574346d72ddc58306092228f789b99ae04cfaa2"
    }
}

fun generateDataId(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    return "0x" + (1..64)
        .map { chars.random() }
        .joinToString("")
}

fun main() {
    val addr = Address("zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi")
    println(addr.toEthereumAddress())
}
