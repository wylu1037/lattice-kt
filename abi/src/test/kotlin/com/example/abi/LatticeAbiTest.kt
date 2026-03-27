package com.example.abi

import kotlin.test.assertEquals
import kotlin.test.Test


class LatticeAbiTest {

    companion object {
        const val LEDGER_ABI_FILENAME = "ledgerContract.json"
    }

    object ResourceUtils {
        fun readFileContent(fileName: String): String {
            return ResourceUtils::class.java.classLoader.getResourceAsStream(fileName)?.use {
                it.bufferedReader().readText()
            } ?: throw IllegalArgumentException("File not found: $fileName")
        }
    }

    @Test
    fun `encode writeTraceability`() {
        val args = arrayOf<Any>(
            "10001",
            "0x516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432",
            arrayOf("0x0900000000000000000000000000000000000000000000000000000000000000"),
            "0x561717f7922a233720ae38acaa4174cda0bf1766"
        )
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("writeTraceability").encode(args)
        val expected =
            "0x4131ff53000000000000000000000000000000000000000000000000000000000000271100000000000000000000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000000000000000000100000000000000000000000000561717f7922a233720ae38acaa4174cda0bf1766000000000000000000000000000000000000000000000000000000000000004230783531363438326232383830373231313439663735633961656133623661366137303030323263373835363166366532326662643064346637336535653734333200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010900000000000000000000000000000000000000000000000000000000000000"
        println(code)
        assertEquals(expected, code)
    }

    @Test
    fun `decode writeTraceability`() {
        val rawCode =
            "0x000000000000000000000000000000000000000000000000000000000000271100000000000000000000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000000000000000000100000000000000000000000000561717f7922a233720ae38acaa4174cda0bf1766000000000000000000000000000000000000000000000000000000000000004230783531363438326232383830373231313439663735633961656133623661366137303030323263373835363166366532326662643064346637336535653734333200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010900000000000000000000000000000000000000000000000000000000000000"
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("writeTraceability").decode(rawCode)
        println(code)
    }

    @Test
    fun `encode setDataSecret`() {
        val args = arrayOf<Any>(
            "0x516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432",
            "0x561717f7922a233720ae38acaa4174cda0bf1766",
        )
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("setDataSecret").encode(args)
        val expected =
            "0xa2ec96570000000000000000000000000000000000000000000000000000000000000040000000000000000000000000561717f7922a233720ae38acaa4174cda0bf17660000000000000000000000000000000000000000000000000000000000000042307835313634383262323838303732313134396637356339616561336236613661373030303232633738353631663665323266626430643466373365356537343332000000000000000000000000000000000000000000000000000000000000"
        assertEquals(expected, code)
    }

    @Test
    fun `decode setDataSecret`() {
        val expected =
            "0x0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000561717f7922a233720ae38acaa4174cda0bf17660000000000000000000000000000000000000000000000000000000000000042307835313634383262323838303732313134396637356339616561336236613661373030303232633738353631663665323266626430643466373365356537343332000000000000000000000000000000000000000000000000000000000000"
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("setDataSecret").decode(expected)
        assertEquals<Any>(expected, code as Any)
    }

    @Test
    fun `encode getTraceability`() {
        val args = arrayOf<Any>(
            "1234",
            "561717f7922a233720ae38acaa4174cda0bf1766"
        )
        val expected =
            "0x295adafb0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000561717f7922a233720ae38acaa4174cda0bf176600000000000000000000000000000000000000000000000000000000000000043132333400000000000000000000000000000000000000000000000000000000"
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("getTraceability").encode(args)
        assertEquals(expected, code)
    }

    @Test
    fun `encode writeTraceabilityBatch`() {
        val args = arrayOf<Any>(
            arrayOf<Any>(
                arrayOf<Any>(
                    111222333L,
                    "str0",
                    arrayOf("0x516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432"),
                    "0x561717f7922a233720ae38acaa4174cda0bf1766"
                )
            )
        )
        val expected =
            "0x77b34b730000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000000100000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000006a11e3d000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000561717f7922a233720ae38acaa4174cda0bf1766000000000000000000000000000000000000000000000000000000000000000473747230000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432"
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("writeTraceabilityBatch").encode(args)
        assertEquals(expected, code)
    }

    @Test
    fun `encode writeTraceabilityBatch version2`() {
        val args = arrayOf<Any>(
            arrayOf<Any>(
                arrayOf<Any>(
                    111222333L,
                    "str0",
                    arrayOf("0x516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432"),
                    "0x561717f7922a233720ae38acaa4174cda0bf1766"
                ),
                arrayOf<Any>(
                    "111222333",
                    "str0",
                    arrayOf("0x516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432"),
                    "0x561717f7922a233720ae38acaa4174cda0bf1766"
                )
            )
        )
        val expected =
            "0x77b34b7300000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000001400000000000000000000000000000000000000000000000000000000006a11e3d000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000561717f7922a233720ae38acaa4174cda0bf1766000000000000000000000000000000000000000000000000000000000000000473747230000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e74320000000000000000000000000000000000000000000000000000000006a11e3d000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000000c0000000000000000000000000561717f7922a233720ae38acaa4174cda0bf1766000000000000000000000000000000000000000000000000000000000000000473747230000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001516482b2880721149f75c9aea3b6a6a700022c78561f6e22fbd0d4f73e5e7432"
        val abi = ResourceUtils.readFileContent(LEDGER_ABI_FILENAME)
        val code = LatticeAbi(abi).getFunction("writeTraceabilityBatch").encode(args)
        println(code)
        assertEquals(expected, code)
    }

    @Test
    fun `encode fixed array`() {
        val abi =
            "[{\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string[2]\",\"name\":\"_name\",\"type\":\"string[2]\"}],\"name\":\"set\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
        val code = LatticeAbi(abi).getFunction("set").encode(arrayOf(arrayOf("1", "2")))
        println(code)
        val expected =
            "0x74d379540000000000000000000000000000000000000000000000000000000000000020000000000000000000000000000000000000000000000000000000000000004000000000000000000000000000000000000000000000000000000000000000800000000000000000000000000000000000000000000000000000000000000001310000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000013200000000000000000000000000000000000000000000000000000000000000"
        assertEquals(expected, code)
        // 0x74d379540000000000000000000000000000000000000000000000000000000000000001310000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000013200000000000000000000000000000000000000000000000000000000000000
    }

    @Test
    fun `encode createContract`() {
        val abi =
            """[{"name":"createContract","type":"function","stateMutability":"view","inputs":[{"name":"args","internalType":"struct Resource.ContractParams","type":"tuple","components":[{"name":"contractId","type":"string","internalType":"string"},{"name":"contractName","type":"string","internalType":"string"},{"name":"contractAbstract","type":"string","internalType":"string"},{"name":"signMode","type":"string","internalType":"string"},{"name":"hasPrivacyCompute","type":"bool","internalType":"bool"},{"name":"activationTime","type":"uint64","internalType":"uint64"},{"name":"endTime","type":"uint64","internalType":"uint64"},{"name":"strategies","internalType":"struct Resource.Strategy[]","type":"tuple[]","components":[{"name":"resourceId","type":"string","internalType":"string"},{"name":"connects","internalType":"struct Resource.ConnectInfo[]","type":"tuple[]","components":[{"name":"connectId","type":"string","internalType":"string"},{"name":"accessType","type":"string","internalType":"string"},{"name":"accessConfig","type":"string","internalType":"string"},{"name":"entity","type":"string","internalType":"string"}]},{"name":"resourceName","type":"string","internalType":"string"},{"name":"Abstract","type":"string","internalType":"string"},{"name":"operation","type":"string","internalType":"string"},{"name":"strategyNodes","internalType":"struct Resource.StrategyNode[]","type":"tuple[]","components":[{"name":"nodeId","type":"string","internalType":"string"},{"name":"nodeName","type":"string","internalType":"string"},{"name":"nodePeerId","type":"string","internalType":"string"},{"name":"strategyType","type":"string","internalType":"string"}]},{"name":"rules","internalType":"struct Resource.Rule[]","type":"tuple[]","components":[{"name":"name","type":"string","internalType":"string"},{"name":"grule","type":"string","internalType":"string"},{"name":"Type","type":"uint8","internalType":"uint8"},{"name":"factJsonString","type":"string","internalType":"string"}]}]},{"name":"code","type":"bytes","internalType":"bytes"}]}],"outputs":[]}]"""
        val args = arrayOf<Any>(
            arrayOf<Any>(                       // tuple: ContractParams
                "contract001",                  // contractId
                "TestContract",                 // contractName
                "A test contract",              // contractAbstract
                "single",                       // signMode
                true,                           // hasPrivacyCompute
                1700000000L,                    // activationTime
                1800000000L,                    // endTime
                arrayOf<Any>(                   // strategies: Strategy[]
                    arrayOf<Any>(               // Strategy tuple
                        "resource001",          // resourceId
                        arrayOf<Any>(           // connects: ConnectInfo[]
                            arrayOf<Any>(       // ConnectInfo tuple
                                "connect001",   // connectId
                                "http",         // accessType
                                "{}",           // accessConfig
                                "entity001"     // entity
                            )
                        ),
                        "ResourceA",            // resourceName
                        "Resource abstract",    // Abstract
                        "read",                 // operation
                        arrayOf<Any>(           // strategyNodes: StrategyNode[]
                            arrayOf<Any>(       // StrategyNode tuple
                                "node001",      // nodeId
                                "NodeA",        // nodeName
                                "peer001",      // nodePeerId
                                "default"       // strategyType
                            )
                        ),
                        arrayOf<Any>(           // rules: Rule[]
                            arrayOf<Any>(       // Rule tuple
                                "rule001",      // name
                                "when x > 0 then y = 1", // grule
                                1,              // Type (uint8)
                                "{\"key\":\"value\"}" // factJsonString
                            )
                        )
                    )
                ),
                "0x6080604052"                  // code (bytes)
            )
        )
        val code = LatticeAbi(abi).getFunction("createContract").encode(args)
        println(code)
    }
}
