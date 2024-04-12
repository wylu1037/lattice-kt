package com.example.wallet

import com.example.crypto.extension.toECKeyPair
import com.example.model.PrivateKey
import com.example.model.extension.toHexString
import com.example.model.toAddress
import com.example.wallet.model.Wallet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import org.komputing.khex.model.HexString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WalletTest {

    @Test
    fun `new wallet`() {
        val privateKey = "0x663e2481bf5645cf80705d5a180d2937f72ba8ba3e1b14b34064a42ec2b1ae74"
        val priKey = PrivateKey(HexString(privateKey))
        val ecKeyPair = priKey.toECKeyPair(true)
        val wallet = ecKeyPair.newWallet("Root1234", true)
        val json = Json.encodeToString(wallet)
        println(json)
        assertNotNull(json)
    }

    @Test
    fun `decrypt wallet`() {
        val json =
            "{\"address\":\"zltc_T3MMEH9S2bSzo5EDiAt6H8KiXZvchGyw2\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"e1edddff6ab96ebf6e81e744cb7beb3f344d77216f21e9e55c862b97343fdefc\",\"iv\":\"f5f1f9b46c8df1aa04e60b9a66a97c0d\"},\"cipherText\":\"e1edddff6ab96ebf6e81e744cb7beb3f344d77216f21e9e55c862b97343fdefc\",\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"7afb36e93d5ce21be90dab3a565e303dcc67d41b88eddff987df13e3120e2a36\"}},\"mac\":\"24877efbd19f0b5abfb1a80aaeb62ab60fd4f8355d77d199189dd7f98b4bf8e0\"},\"uuid\":\"ff13b6f6-5150-455e-9cfa-177c88f744c1\",\"isGM\":true}"
        val wallet = Wallet.fromJson(json)
        val keypair = wallet.decrypt("Root1234")
        val address = keypair.toAddress(true)
        val privateKey = keypair.privateKey.key.toHexString()
        assertEquals("0x663e2481bf5645cf80705d5a180d2937f72ba8ba3e1b14b34064a42ec2b1ae74", privateKey)
        assertEquals("zltc_T3MMEH9S2bSzo5EDiAt6H8KiXZvchGyw2", address.toString())
    }
}