package com.example.wallet.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Wallet(
    val address: String?,
    val cipher: Cipher,
    val uuid: String,
    val isGM: Boolean
) {
    companion object {
        fun fromJson(json: String): Wallet {
            return Json.decodeFromString<Wallet>(json)
        }
    }
}

/**
 * 仅模块内可见
 */
internal data class WalletForImport(

    var address: String? = null,

    var cipher: Cipher? = null,

    @SerialName("Cipher") var cipherFromMEW: Cipher? = null,

    var uuid: String? = null,
    var isGM: Boolean
)