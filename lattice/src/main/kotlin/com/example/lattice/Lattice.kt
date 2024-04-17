package com.example.lattice

import com.example.lattice.model.*
import com.example.lattice.provider.Client
import com.example.lattice.provider.URL
import com.example.lattice.provider.newClient
import com.example.model.Address
import com.example.model.block.CurrentTDBlock
import com.example.model.block.Receipt
import com.example.model.block.TBlock
import com.example.model.block.toCurrentTDBlock
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal val gson by lazy {
    Gson()
}

interface ILattice {
    fun getBalanceWithPending(address: Address): Balance
    fun getGenesis(): TBlock
    fun getCurrentTDBlock(address: Address): CurrentTDBlock
    fun getLatestTDBlockWithCatch(address: Address): CurrentTDBlock
    fun sendRawTBlock(signedTx: Transaction): String
    fun getReceipt(hash: String): Receipt
}

class Lattice(url: URL, private var chainId: Int, jwtToken: String? = null) : ILattice {
    private var client: Client = url.newClient()
    private var _chainId = chainId
    private var _chainIdString = chainId.toString()
    private var _jwtToken = jwtToken

    private inline fun <reified T : Any> sendUseJsonRpc(method: String, params: Array<Any>): T {
        val headers: Map<String, String> = if (_jwtToken != null) {
            mapOf("Authorization" to "Bearer $_jwtToken", "chainId" to _chainIdString)
        } else {
            mapOf("chainId" to _chainIdString)
        }
        val response = client.send(JsonRpcPayload(method = method, params = params), headers)
        val type = TypeToken.getParameterized(JsonRpcResponse::class.java, T::class.java, Any::class.java).type
        val ret = gson.fromJson<JsonRpcResponse<T, Any>>(response, type)
        if (ret.error != null) {
            throw Error(ret.error.message)
        } else if (ret.result == null) {
            throw Error("empty result.")
        }
        return ret.result
    }

    /**
     * get account total balance
     *
     * @param address account address
     * @return Balance
     */
    override fun getBalanceWithPending(address: Address): Balance {
        return sendUseJsonRpc("latc_getBalanceWithPending", arrayOf(address.hex))
    }

    override fun getGenesis(): TBlock {
        return sendUseJsonRpc("latc_getGenesis", emptyArray())
    }

    override fun getCurrentTDBlock(address: Address): CurrentTDBlock {
        return sendUseJsonRpc("latc_getCurrentTBDB", arrayOf(address.hex))
    }

    override fun getLatestTDBlockWithCatch(address: Address): CurrentTDBlock {
        return try {
            getCurrentTDBlock(address)
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
}