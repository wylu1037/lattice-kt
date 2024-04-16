package com.example.lattice

import com.example.lattice.model.APIPayload
import com.example.lattice.model.Balance
import com.example.lattice.model.JsonRpcResponse
import com.example.lattice.provider.Client
import com.example.lattice.provider.URL
import com.example.lattice.provider.newClient
import com.example.model.Address
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal val gson by lazy {
    Gson()
}

class Lattice(url: URL, private var chainId: Int, jwtToken: String? = null) {
    private var client: Client = url.newClient()
    private var _chainId = chainId
    private var _chainIdString = chainId.toString()
    private var _url = url
    private var _jwtToken = jwtToken

    private inline fun <reified T : Any> sendUseJsonRpc(method: String, params: Array<Any>): T {
        val headers: Map<String, String> = if (_jwtToken != null) {
            mapOf("Authorization" to "Bearer $_jwtToken", "chainId" to _chainIdString)
        } else {
            mapOf("chainId" to _chainIdString)
        }
        val response = this.client.send(APIPayload(method = method, params = params), headers)
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
    fun getBalanceWithPending(address: Address): Balance {
        return this.sendUseJsonRpc("latc_getBalanceWithPending", arrayOf(address.hex))
    }
}