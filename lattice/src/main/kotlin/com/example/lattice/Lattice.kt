package com.example.lattice

import com.example.lattice.Strategy.*
import com.example.lattice.model.sign
import com.example.lattice.provider.HttpApiImpl
import com.example.lattice.provider.HttpApiParams
import com.example.lattice.provider.URL
import com.example.model.Address
import com.example.model.RegExpr
import com.example.model.block.Receipt
import com.example.model.toHex
import com.google.gson.Gson

internal val gson by lazy {
    Gson()
}


const val ZERO_ADDRESS = "zltc_QLbz7JHiBTspS962RLKV8GndWFwjA5K66"

enum class Curve {
    Sm2p256v1,
    Secp256k1
}

data class ChainConfig(val chainId: Int, val curve: Curve, val tokenLess: Boolean = true) {
    init {
        require(chainId > 0) { "Chain ID must be greater than zero." }
    }
}

fun ChainConfig.isGM(): Boolean {
    return curve == Curve.Sm2p256v1
}

data class ConnectingNodeConfig(val url: String)

/**
 * @property accountAddress 账户地址
 * @property privateKey 私钥（16进制字符串）
 * @property passphrase 身份密码，用来解密FileKey
 */
data class CredentialConfig(val accountAddress: String, val privateKey: String, val passphrase: String? = null) {
    init {
        require(accountAddress.matches(Regex(RegExpr.ADDRESS))) { "Invalid account address." }
        require(privateKey.matches(Regex(RegExpr.PRIVATE_KEY_HEX))) { "Invalid private key." }
    }
}

data class Options(val interval: Int)

/**
 * 重试策略
 *
 * @property BACK_OFF
 * @property FIXED_INTERVAL
 * @property RANDOM_INTERVAL
 */
enum class Strategy {
    BACK_OFF,
    FIXED_INTERVAL,
    RANDOM_INTERVAL,
}

class WaitStrategy()

fun WaitStrategy.backOffOpts() {

}

fun WaitStrategy.fixedIntervalOpts() {

}

fun WaitStrategy.randomIntervalOpts() {

}

interface Lattice {

    /**
     * 发起转账
     *
     * @param linker 转账的地址
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     */
    fun transfer(linker: String, payload: String, amount: Long = 0, joule: Long = 0): String

    fun transferWaitReceipt(linker: String, payload: String, amount: Long = 0, joule: Long = 0): Receipt

    fun deployContract(data: String, payload: String, amount: Long = 0, joule: Long = 0): String

    fun callContract(contractAddress: String, data: String, payload: String, amount: Long = 0, joule: Long = 0): String
}

class LatticeImpl(
    chainConfig: ChainConfig,
    connectingNodeConfig: ConnectingNodeConfig,
    credentialConfig: CredentialConfig,
    options: Options? = null
) : Lattice {

    private val _chainConfig = chainConfig
    private val _connectingNodeConfig = connectingNodeConfig
    private val _credentialConfig = credentialConfig
    private val _options = options

    // initialize a private http api
    private val _httpApi = HttpApiImpl(HttpApiParams(URL(_connectingNodeConfig.url), chainConfig.chainId))

    override fun transfer(linker: String, payload: String, amount: Long, joule: Long): String {
        val block = _httpApi.getLatestBlock(Address(_credentialConfig.accountAddress))
        val transaction = TransferTXBuilder.builder()
            .setBlock(block)
            .setOwner(Address(_credentialConfig.accountAddress))
            .setLinker(Address(linker))
            .setPayload(payload)
            .setAmount(amount)
            .setJoule(joule)
            .build()

        val (_, signature) = transaction.sign(_credentialConfig.privateKey, _chainConfig.isGM(), _chainConfig.chainId)
        transaction.sign = signature.toHex()

        return _httpApi.sendRawTBlock(transaction)
    }

    override fun transferWaitReceipt(linker: String, payload: String, amount: Long, joule: Long): Receipt {
        TODO("Not yet implemented")
    }

    override fun deployContract(data: String, payload: String, amount: Long, joule: Long): String {
        val block = _httpApi.getLatestBlock(Address(_credentialConfig.accountAddress))
        val transaction = DeployContractTXBuilder.builder()
            .setBlock(block)
            .setOwner(Address(_credentialConfig.accountAddress))
            .setLinker(Address(ZERO_ADDRESS))
            .setCode(data)
            .setPayload(payload)
            .setAmount(amount)
            .setJoule(joule)
            .build()

        val (_, signature) = transaction.sign(_credentialConfig.privateKey, _chainConfig.isGM(), _chainConfig.chainId)
        transaction.sign = signature.toHex()

        return _httpApi.sendRawTBlock(transaction)
    }

    override fun callContract(
        contractAddress: String,
        data: String,
        payload: String,
        amount: Long,
        joule: Long
    ): String {
        TODO("Not yet implemented")
    }
}
