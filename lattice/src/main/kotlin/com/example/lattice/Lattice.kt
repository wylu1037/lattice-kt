package com.example.lattice

import com.example.lattice.Curve.Secp256k1
import com.example.lattice.Curve.Sm2p256v1
import com.example.lattice.Strategy.BACK_OFF
import com.example.lattice.Strategy.FIXED_INTERVAL
import com.example.lattice.Strategy.RANDOM_INTERVAL
import com.example.lattice.model.sign
import com.example.lattice.provider.HttpApiImpl
import com.example.lattice.provider.HttpApiParams
import com.example.lattice.provider.URL
import com.example.model.Address
import com.example.model.RegExpr
import com.example.model.block.CurrentTDBlock
import com.example.model.block.Receipt
import com.example.model.toHex
import com.github.michaelbull.retry.policy.RetryPolicy
import com.github.michaelbull.retry.policy.binaryExponentialBackoff
import com.github.michaelbull.retry.policy.constantDelay
import com.github.michaelbull.retry.policy.decorrelatedJitterBackoff
import com.github.michaelbull.retry.retry
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

var logger = LoggerFactory.getLogger("Lattice")

internal val gson by lazy {
    Gson()
}

// 零账户地址
const val ZERO_ADDRESS = "zltc_QLbz7JHiBTspS962RLKV8GndWFwjA5K66"

/**
 * 椭圆曲线枚举
 *
 * @property Sm2p256v1 国密曲线
 * @property Secp256k1 国际曲线
 */
enum class Curve {
    Sm2p256v1,
    Secp256k1
}

/**
 * 链配置信息
 *
 * @property chainId 链ID
 * @property curve [Curve] 链签名使用的椭圆曲线
 * @property tokenLess 是否使用通证，默认值true:不使用通证
 */
data class ChainConfig(val chainId: Int, val curve: Curve, val tokenLess: Boolean = true) {
    init {
        require(chainId > 0) { "Chain ID must be greater than zero." }
    }
}

fun ChainConfig.isGM(): Boolean {
    return curve == Sm2p256v1
}

/**
 * 连接节点配置
 *
 * @property url 节点的http连接信息
 */
data class ConnectingNodeConfig(val url: String)

/**
 * 交易的凭证信息
 *
 * @property accountAddress 账户地址
 * @property privateKey 私钥（16进制字符串）
 * @property passphrase 身份密码，用来解密FileKey，默认null
 */
data class CredentialConfig(val accountAddress: String, val privateKey: String, val passphrase: String? = null) {
    init {
        require(accountAddress.matches(Regex(RegExpr.ADDRESS))) { "Invalid account address." }
        require(privateKey.matches(Regex(RegExpr.PRIVATE_KEY_HEX))) { "Invalid private key." }
    }
}

/**
 * 可选配置
 *
 * @property interval 间隔
 */
data class Options(val interval: Int)

/**
 * 重试策略
 *
 * @property BACK_OFF 退避算法
 * @property FIXED_INTERVAL 固定间隔
 * @property RANDOM_INTERVAL 随机间隔
 */
enum class Strategy {
    BACK_OFF,
    FIXED_INTERVAL,
    RANDOM_INTERVAL,
}

/**
 * 获取回执的重试策略
 */
class RetryStrategy() {
    companion object {
        @JvmStatic
        fun defaultBackOffRetry(): RetryPolicy<Throwable> {
            return binaryExponentialBackoff(min = 10L, max = 500L)
        }

        @JvmStatic
        fun defaultFixedIntervalRetry(): RetryPolicy<Throwable> {
            return constantDelay(100)
        }

        @JvmStatic
        fun defaultRandomIntervalRetry(): RetryPolicy<Throwable> {
            return decorrelatedJitterBackoff(min = 10L, max = 500L)
        }

    }
}

interface Lattice {

    /**
     * 发起转账
     *
     * @param linker 转账的地址
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @return 哈希
     */
    fun transfer(chainId: String, linker: String, payload: String = "0x", amount: Long = 0, joule: Long = 0): String

    /**
     * 发起转账并等待回执
     *
     * @param linker 转账的地址
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @param retryPolicy 重试策略，默认为固定间隔（100ms）重试
     * @return 回执[Receipt]
     */
    fun transferWaitReceipt(
        chainId: String,
        linker: String,
        payload: String = "0x",
        amount: Long = 0,
        joule: Long = 0,
        retryPolicy: RetryPolicy<Throwable> = RetryStrategy.defaultFixedIntervalRetry()
    ): Receipt

    /**
     * 部署合约
     *
     * @param data
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @return 哈希
     */
    fun deployContract(chainId: String, data: String, payload: String = "0x", amount: Long = 0, joule: Long = 0): String

    /**
     * 部署合约并等待回执
     *
     * @param data 部署的合约的bytecode
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @param retryPolicy 重试策略，默认为固定间隔（100ms）重试
     * @return 回执[Receipt]
     */
    fun deployContractWaitReceipt(
        chainId: String,
        data: String,
        payload: String = "0x",
        amount: Long = 0,
        joule: Long = 0,
        retryPolicy: RetryPolicy<Throwable> = RetryStrategy.defaultFixedIntervalRetry()
    ): Receipt

    /**
     * 调用合约
     *
     * @param contractAddress 合约地址
     * @param data 合约data
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @return 哈希
     */
    fun callContract(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String = "0x",
        amount: Long = 0,
        joule: Long = 0
    ): String

    /**
     * 调用合约并且等待回执
     *
     * @param contractAddress 合约地址
     * @param data 合约data
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @param retryPolicy 重试策略，默认为固定间隔（100ms）重试
     * @return 回执[Receipt]
     */
    fun callContractWaitReceipt(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String = "0x",
        amount: Long = 0,
        joule: Long = 0,
        retryPolicy: RetryPolicy<Throwable> = RetryStrategy.defaultFixedIntervalRetry()
    ): Receipt

    /**
     * 预调用合约
     *
     * @param contractAddress 合约地址
     * @param data 合约data
     * @param payload 转账的交易备注
     * @param amount 转账的通证额度
     * @param joule 交易的手续费
     * @return 回执[Receipt]
     */
    fun preCallContract(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String = "0x",
        amount: Long = 0,
        joule: Long = 0
    ): Receipt
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
    private val _httpApi = HttpApiImpl(HttpApiParams(URL(_connectingNodeConfig.url)))

    override fun transfer(chainId: String, linker: String, payload: String, amount: Long, joule: Long): String {
        logger.debug(
            "开始发起转账交易，chainId:{}, linker: {}, payload: {}, amount: {}, joule: {}",
            chainId,
            linker,
            payload,
            amount,
            joule
        )
        val block = _httpApi.getLatestBlock(chainId, Address(_credentialConfig.accountAddress))
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

        val hash = _httpApi.sendRawTBlock(chainId, transaction)
        logger.debug("结束转账交易，交易哈希为：{}", hash)
        return hash
    }

    override fun transferWaitReceipt(
        chainId: String,
        linker: String,
        payload: String,
        amount: Long,
        joule: Long,
        retryPolicy: RetryPolicy<Throwable>
    ): Receipt {
        val hash = transfer(chainId, linker, payload, amount, joule)

        logger.debug("获取交易【{}】的回执", hash)
        val receipt = runBlocking {
            retry(retryPolicy) {
                _httpApi.getReceipt(chainId, hash)
            }
        }
        logger.debug("获取到交易【{}】的回执为：{}", hash, gson.toJson(receipt))
        return receipt
    }

    override fun deployContract(chainId: String, data: String, payload: String, amount: Long, joule: Long): String {
        logger.debug(
            "开始发起部署合约交易，chainId:{}, data: {}, payload: {}, amount: {}, joule: {}",
            chainId,
            data,
            payload,
            amount,
            joule
        )
        val block = _httpApi.getLatestBlock(chainId, Address(_credentialConfig.accountAddress))
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

        val hash = _httpApi.sendRawTBlock(chainId, transaction)
        logger.debug("结束部署合约，交易哈希为：{}", hash)
        return hash
    }

    override fun deployContractWaitReceipt(
        chainId: String,
        data: String,
        payload: String,
        amount: Long,
        joule: Long,
        retryPolicy: RetryPolicy<Throwable>
    ): Receipt {
        val hash = deployContract(chainId, data, payload, amount, joule)

        val receipt = runBlocking {
            retry(retryPolicy) {
                _httpApi.getReceipt(chainId, hash)
            }
        }
        return receipt
    }

    override fun callContract(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String,
        amount: Long,
        joule: Long
    ): String {
        logger.debug(
            "开始发起调用合约交易，chainId:{}, contractAddress: {}, data: {}, payload: {}, amount: {}, joule: {}",
            chainId, contractAddress, data, payload, amount, joule
        )
        val block = _httpApi.getLatestBlock(chainId, Address(_credentialConfig.accountAddress))
        val transaction = CallContractTXBuilder.builder()
            .setBlock(block)
            .setOwner(Address(_credentialConfig.accountAddress))
            .setLinker(Address(contractAddress))
            .setCode(data)
            .setPayload(payload)
            .setAmount(amount)
            .setJoule(joule)
            .build()

        val (_, signature) = transaction.sign(_credentialConfig.privateKey, _chainConfig.isGM(), _chainConfig.chainId)
        transaction.sign = signature.toHex()

        val hash = _httpApi.sendRawTBlock(chainId, transaction)
        logger.debug("结束调用合约，交易哈希为：{}", hash)
        return hash
    }

    override fun callContractWaitReceipt(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String,
        amount: Long,
        joule: Long,
        retryPolicy: RetryPolicy<Throwable>
    ): Receipt {
        val hash = callContract(chainId, contractAddress, data, payload, amount, joule)

        val receipt = runBlocking {
            retry(retryPolicy) {
                _httpApi.getReceipt(chainId, hash)
            }
        }
        return receipt
    }

    override fun preCallContract(
        chainId: String,
        contractAddress: String,
        data: String,
        payload: String,
        amount: Long,
        joule: Long
    ): Receipt {
        logger.debug(
            "开始发起预执行合约交易，chainId: {}, contractAddress: {}, data: {}, payload: {}, amount: {}, joule: {}",
            chainId, contractAddress, data, payload, amount, joule
        )
        val transaction = CallContractTXBuilder.builder()
            .setBlock(CurrentTDBlock.zeroBlock())
            .setOwner(Address(_credentialConfig.accountAddress))
            .setLinker(Address(contractAddress))
            .setCode(data)
            .setPayload(payload)
            .setAmount(amount)
            .setJoule(joule)
            .build()

        val receipt = _httpApi.preCallContract(chainId, transaction)
        logger.debug("结束预调用合约，回执为：{}", gson.toJson(receipt))
        return receipt
    }
}
