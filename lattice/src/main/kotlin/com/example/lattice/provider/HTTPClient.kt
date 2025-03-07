package com.example.lattice.provider

import com.example.lattice.gson
import com.example.lattice.logger
import com.example.lattice.model.APPLICATION_JSON
import com.example.lattice.model.Balance
import com.example.lattice.model.BaseRequest
import com.example.lattice.model.JsonRpcPayload
import com.example.lattice.model.JsonRpcResponse
import com.example.lattice.model.Transaction
import com.example.lattice.model.toBody
import com.example.lattice.model.toSendTBlock
import com.example.model.Address
import com.example.model.block.CurrentTDBlock
import com.example.model.block.Receipt
import com.example.model.block.TBlock
import com.example.model.block.toCurrentTDBlock
import com.google.gson.reflect.TypeToken
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.concurrent.TimeUnit

class HTTPClient(private val url: URL, private val options: Map<String, String> = emptyMap()) : Client {

    override fun send(payload: JsonRpcPayload, headers: Map<String, String>): String {
        val response = post(BaseRequest(url, payload.toBody(), headers))
        if (!response.isSuccessful) {
            throw Error(response.body.string())
        }
        return response.body.string()
    }
}

object HttpClientFactory {

    private const val MAX_IDLE_CONNECTIONS = 50 // 最大空闲连接数
    private const val KEEP_ALIVE_DURATION = 30L // 连接的保持时间
    private const val CONNECT_TIMEOUT = 90L
    private const val READ_TIMEOUT = 90L

    private val connectionPool = ConnectionPool(
        MAX_IDLE_CONNECTIONS,
        KEEP_ALIVE_DURATION,
        TimeUnit.SECONDS
    )

    val client = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}

fun post(request: BaseRequest): Response {
    val body = gson.toJson(request.body).toRequestBody(APPLICATION_JSON)
    val builder = Request.Builder().post(body).url(request.url.value)
    if (request.headers?.isNotEmpty() == true) {
        request.headers.forEach { (t, u) ->
            builder.addHeader(t, u)
        }
    }
    val call = HttpClientFactory.client.newCall(builder.build())
    return call.execute()
}

/**
 * @param url url
 * @param token jwt token
 */
data class HttpApiParams(val url: URL, var token: String? = null)

interface HttpApi {
    /**
     * 刷新token
     * @param newToken 刷新的token
     */
    fun refreshToken(newToken: String)

    /**
     * 获取包括pending状态在内的账户余额
     *
     * @param address 账户地址 [Address]
     * @return [Balance] 余额
     */
    fun getBalanceWithPending(chainId: String, address: Address): Balance

    /**
     * 获取创世区块
     *
     * @return 创建区块 [TBlock]
     */
    fun getGenesis(chainId: String): TBlock

    /**
     * 获取账户最新的区块信息
     *
     * @param address 账户地址 [Address]
     * @return 区块信息 [CurrentTDBlock]
     */
    fun getLatestBlock(chainId: String, address: Address): CurrentTDBlock

    /**
     * 获取账户最新的区块信息，失败则获取创世区块信息
     *
     * @param address 账户地址 [Address]
     * @return [CurrentTDBlock] 区块信息
     */
    fun getLatestTDBlockWithCatch(chainId: String, address: Address): CurrentTDBlock

    /**
     * 发送已签名的交易到链上
     *
     * @param signedTx 已经签名的交易
     * @return 交易哈希
     */
    fun sendRawTBlock(chainId: String, signedTx: Transaction): String

    /**
     * 获取交易的回执信息
     *
     * @param hash 交易哈希
     * @return [Receipt] 交易回执
     */
    fun getReceipt(chainId: String, hash: String): Receipt

    /**
     * 预执行合约
     *
     * @param unsignedTx 未签名的交易 [Transaction]
     * @return 交易回执 [Receipt]
     */
    fun preCallContract(chainId: String, unsignedTx: Transaction): Receipt
}

data class ChainId(val id: String)

fun ChainId.isValid(): Boolean {
    if (id.isBlank()) {
        return false
    }
    val regex = Regex("^[1-9]\\d*$")
    return regex.matches(id)
}

fun emptyChainId(): String {
    return ""
}

class HttpApiImpl(params: HttpApiParams) : HttpApi {

    private val _params: HttpApiParams = params
    private val _client: Client = params.url.newClient()

    /**
     * 发送json rpc请求
     *
     * @param method 方法名
     * @param params 方法参数
     * @return T generic
     */
    private inline fun <reified T : Any> sendUseJsonRpc(chainId: ChainId, method: String, params: Array<Any>): T {
        logger.debug("开始发起json-rpc请求，chainId: ${chainId.id}, method: $method, params: ${params.contentToString()}")
        val headers = mutableMapOf<String, String>().apply {
            put("chainId", if (chainId.isValid()) chainId.id else emptyChainId())
            _params.token?.let { token ->
                put("Authorization", "Bearer $token")
            }
        }
        val response = _client.send(JsonRpcPayload(method = method, params = params), headers)
        val type = TypeToken.getParameterized(JsonRpcResponse::class.java, T::class.java).type
        val jsonRpcResponse = gson.fromJson<JsonRpcResponse<T>>(response, type)
        val result = jsonRpcResponse.result?.takeIf {
            jsonRpcResponse.error == null
        } ?: throw Error(jsonRpcResponse.error?.message ?: "Empty result.")
        logger.debug("结束发起json-rpc请求，method: $method, result: ${gson.toJson(result)}")
        return result
    }

    override fun refreshToken(newToken: String) {
        _params.token = newToken
    }

    override fun getBalanceWithPending(chainId: String, address: Address): Balance {
        return sendUseJsonRpc(ChainId(chainId), "latc_getBalanceWithPending", arrayOf(address.address))
    }

    override fun getGenesis(chainId: String): TBlock {
        return sendUseJsonRpc(ChainId(chainId), "latc_getGenesis", emptyArray())
    }

    override fun getLatestBlock(chainId: String, address: Address): CurrentTDBlock {
        return sendUseJsonRpc(ChainId(chainId), "latc_getCurrentTBDB", arrayOf(address.address))
    }

    override fun getLatestTDBlockWithCatch(chainId: String, address: Address): CurrentTDBlock {
        return try {
            getLatestBlock(chainId, address)
        } catch (e: Error) {
            getGenesis(chainId).toCurrentTDBlock()
        }
    }

    override fun sendRawTBlock(chainId: String, signedTx: Transaction): String {
        if (signedTx.sign.isNullOrBlank()) {
            throw Error("Property sign must not be null")
        }
        return sendUseJsonRpc(ChainId(chainId), "wallet_sendRawTBlock", arrayOf(signedTx.toSendTBlock()))
    }

    override fun getReceipt(chainId: String, hash: String): Receipt {
        return sendUseJsonRpc(ChainId(chainId), "latc_getReceipt", arrayOf(hash))
    }

    override fun preCallContract(chainId: String, unsignedTx: Transaction): Receipt {
        return sendUseJsonRpc(ChainId(chainId), "wallet_preExecuteContract", arrayOf(unsignedTx.toSendTBlock()))
    }
}
