package com.example.wallet

import com.example.model.extension.toHexString
import com.example.model.toAddress
import com.example.wallet.model.Wallet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WalletTest {

    @Test
    fun `new wallet from private key`() {
        val privateKey = "0x663e2481bf5645cf80705d5a180d2937f72ba8ba3e1b14b34064a42ec2b1ae74"
        val wallet = Wallet.fromPrivateKey(privateKey, true, "Root1234")
        val json = Json.encodeToString(wallet)
        println(json)
        assertNotNull(json)
    }

    @Test
    fun `decrypt wallet`() {
        val json =
            "{\"uuid\":\"123f1bf5-5599-45c4-8566-9a6440ba359f\",\"address\":\"zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"8f6de52c0be43ae438feddea4c210772da23b9333242b7416446eae889b594e0\",\"iv\":\"1ad693b4d8089da0492b9c8c49bc60d3\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"309210a97fbf705eed7bf3485c16d6922a21591297b52c0c59b4f7495863e300\"}},\"cipherText\":\"8f6de52c0be43ae438feddea4c210772da23b9333242b7416446eae889b594e0\",\"mac\":\"335fab3901f8f5c4408b7d6a310ec29cf5bd3792deb696f1b10282e823241c96\"},\"isGM\":true}"
        val wallet = Wallet.fromFileKey(json)
        val keypair = wallet.decrypt("Root1234")
        val address = keypair.toAddress(true)
        val privateKey = keypair.privateKey.key.toHexString()
        println(keypair.toAddress())
        println(privateKey)
        assertEquals("0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb", privateKey)
        assertEquals("zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi", address.toString())
    }
}