package com.example.wallet.model

import com.example.crypto.extension.toECKeyPair
import com.example.model.PrivateKey
import com.example.wallet.newWallet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.komputing.khex.model.HexString

@Serializable
data class Wallet(
    val address: String?,
    val cipher: Cipher,
    val uuid: String,
    val isGM: Boolean
) {
    companion object {
        /**
         * 从 FileKey 恢复钱包
         *
         * @param json FileKey 的JSON字符串
         * @return 钱包
         */
        fun fromFileKey(json: String): Wallet {
            return Json.decodeFromString<Wallet>(json)
        }

        /**
         * 从 私钥 恢复钱包
         *
         * @param privateKeyHex 私钥(hex字符串)
         * @param isGM sm2p256v1 or secp256k1
         * @param password FileKey 密码
         * @return 钱包
         */
        fun fromPrivateKey(privateKeyHex: String, isGM: Boolean = true, password: String) =
            PrivateKey(HexString(privateKeyHex)).toECKeyPair(isGM).newWallet(password, isGM)

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