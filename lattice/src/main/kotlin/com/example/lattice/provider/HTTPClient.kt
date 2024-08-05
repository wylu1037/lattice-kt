package com.example.lattice.provider

import com.example.lattice.gson
import com.example.lattice.model.*
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
 * @param chainId 链ID
 * @param token jwt token
 */
data class HttpApiParams(val url: URL, val chainId: Int, val token: String? = null)

fun HttpApiParams.chainIdAsString(): String {
    return chainId.toString()
}

interface HttpApi {
    /**
     * 获取包括pending状态在内的账户余额
     *
     * @param address 账户地址 [Address]
     * @return [Balance] 余额
     */
    fun getBalanceWithPending(address: Address): Balance

    /**
     * 获取创世区块
     *
     * @return 创建区块 [TBlock]
     */
    fun getGenesis(): TBlock

    /**
     * 获取账户最新的区块信息
     *
     * @param address 账户地址 [Address]
     * @return 区块信息 [CurrentTDBlock]
     */
    fun getLatestBlock(address: Address): CurrentTDBlock

    /**
     * 获取账户最新的区块信息，失败则获取创世区块信息
     *
     * @param address 账户地址 [Address]
     * @return [CurrentTDBlock] 区块信息
     */
    fun getLatestTDBlockWithCatch(address: Address): CurrentTDBlock

    /**
     * 发送已签名的交易到链上
     *
     * @param signedTx 已经签名的交易
     * @return 交易哈希
     */
    fun sendRawTBlock(signedTx: Transaction): String

    /**
     * 获取交易的回执信息
     *
     * @param hash 交易哈希
     * @return [Receipt] 交易回执
     */
    fun getReceipt(hash: String): Receipt

    /**
     * 预执行合约
     *
     * @param unsignedTx 未签名的交易 [Transaction]
     * @return 交易回执 [Receipt]
     */
    fun preExecuteContract(unsignedTx: Transaction): Receipt
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
    private inline fun <reified T : Any> sendUseJsonRpc(method: String, params: Array<Any>): T {
        val headers = mutableMapOf<String, String>().apply {
            put("chainId", _params.chainIdAsString())
            _params.token?.let { token ->
                put("Authorization", "Bearer $token")
            }
        }
        val response = _client.send(JsonRpcPayload(method = method, params = params), headers)
        val type = TypeToken.getParameterized(JsonRpcResponse::class.java, T::class.java).type
        val jsonRpcResponse = gson.fromJson<JsonRpcResponse<T>>(response, type)
        return jsonRpcResponse.result?.takeIf {
            jsonRpcResponse.error == null
        } ?: throw Error(jsonRpcResponse.error?.message ?: "Empty result.")
    }

    override fun getBalanceWithPending(address: Address): Balance {
        return sendUseJsonRpc("latc_getBalanceWithPending", arrayOf(address.address))
    }

    override fun getGenesis(): TBlock {
        return sendUseJsonRpc("latc_getGenesis", emptyArray())
    }

    override fun getLatestBlock(address: Address): CurrentTDBlock {
        return sendUseJsonRpc("latc_getCurrentTBDB", arrayOf(address.address))
    }

    override fun getLatestTDBlockWithCatch(address: Address): CurrentTDBlock {
        return try {
            getLatestBlock(address)
        } catch (e: Error) {
            getGenesis().toCurrentTDBlock()
        }
    }

    override fun sendRawTBlock(signedTx: Transaction): String {
        if (signedTx.sign.isNullOrBlank()) {
            throw Error("Property sign must not be null")
        }
        return sendUseJsonRpc("wallet_sendRawTBlock", arrayOf(signedTx.toSendTBlock()))
    }

    override fun getReceipt(hash: String): Receipt {
        return sendUseJsonRpc("latc_getReceipt", arrayOf(hash))
    }

    override fun preExecuteContract(unsignedTx: Transaction): Receipt {
        return sendUseJsonRpc("wallet_preExecuteContract", arrayOf(unsignedTx.toSendTBlock()))
    }
}
