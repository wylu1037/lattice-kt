package com.example.crypto

import com.example.crypto.extension.toECKeyPair
import com.example.model.PrivateKey
import com.example.model.SignatureData
import com.example.model.extension.hash
import com.example.model.extension.toHexString
import com.example.model.extension.toHexStringZeroPadded
import com.example.model.toAddress
import com.example.model.toEthereumAddress
import com.example.model.toHex
import org.junit.Test
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SignTest {

    @Test
    fun `sign and verify for secp256k1`() {
        val isGM = false
        val priKeyHex = "0x011b1861bfa89d5fb1d71c64a5c435cdee93f69d5e32d2b7ecae7663c3f5d810"
        val message = HexString("0102030405060708010203040506070801020304050607080102030405060708").hexToByteArray()
        val keypair = PrivateKey(HexString(priKeyHex)).toECKeyPair(isGM)
        val signature = keypair.signMessage(message, isGM)
        println("signature: ${signature.toHex()}")
        val result = keypair.publicKey.verifySignature(message, signature, isGM)
        println(result)
    }

    @Test
    fun `verify for secp256k1`() {
        val isGM = false
        val privateKey = "0xc842e1ef9ece7e992a4021423a58d6e89c751881e43fd7dbebe70f932ad493e2"
        val message = HexString("0102030405060708010203040506070801020304050607080102030405060708").hexToByteArray()
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        val signature =
            "0xa4e6cdd177a9f108604b2f5ab849937ad5376db01e72367081e29a27895367243b59ef831909ce154151daf1fe564bb2bd39de3a703222e5bdf54f88e37fa2931b"
        val pass = keypair.publicKey.verifySignature(message, SignatureData.fromHexString(signature), isGM)
        assertTrue(pass)
    }

    @Test
    fun `sign and verify for sm2p256v1`() {
        val isGM = true
        val priKeyHex = "0xa6de98be23f726db5345ebdc9fe1096193c84ad4750203c8826181a8d8f76c56"
        val message =
            HexString("0x7148648be39da5466cadbcf93c8d99a6385122e698164f1a8733d5c164eb5734").hexToByteArray().hash(isGM)
        val keypair = PrivateKey(HexString(priKeyHex)).toECKeyPair(isGM)

        val signature = keypair.signMessage(message, isGM)
        val publicKey = keypair.getCompressedPublicKey(isGM).toNoPrefixHexString()
        println("publicKey: $publicKey")
        println(
            "signature: 0x${signature.r.toHexStringZeroPadded(64, false)}${
                signature.s.toHexStringZeroPadded(
                    64,
                    false
                )
            }$publicKey"
        )
        val result = keypair.publicKey.verifySignature(message, signature, isGM)
        assertTrue(result)
    }

    @Test
    fun `verify for sm2p256v1`() {
        val isGM = true
        val privateKey = "0xa6de98be23f726db5345ebdc9fe1096193c84ad4750203c8826181a8d8f76c56"
        val message =
            HexString("0x7148648be39da5466cadbcf93c8d99a6385122e698164f1a8733d5c164eb5734").hexToByteArray().hash(isGM)
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        val signature =
            "0x01ab74136da660e4f8c9fc071da81ceaa528429b6c416f66e28a108ab1a277a241495dbe14aa9a9033fd58a6c700d67340a2698720dacc5475d62f162d9dfcfb00"
        val pass = keypair.publicKey.verifySignature(message, SignatureData.fromHexString(signature), isGM)
        assertTrue(pass)
    }

    @Test
    fun `generate address for sm2p256v1`() {
        val isGM = true
        val privateKey = "0x9860956de90cc61a05447ea067197be1fa08d712c4a5088c9cb62182bdca0f92"
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        println(keypair.publicKey.key.toHexString())
        val actual = keypair.publicKey.toAddress(isGM).address
        val expected = "zltc_oJCrxCx6X23m5xVZFLjexi8GGaib6Zwff"
        assertEquals(expected, actual)
    }

    @Test
    fun `generate address for secp256k1`() {
        val isGM = false
        val privateKey = "0xd2c784688ab85d689e358a7b030c9f26b8ee45e66e89d8842fa88da3b9637955"
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        assertEquals(
            keypair.publicKey.key.toHexString(),
            "0x31dc027c63ccb1229cae4a8f138b53c14f7989323e8cded430b54cf3ef9ddf5e348458706a05ab6c7597fc2b190adb2479e0cb635d92c9e5e92c396fae998bd6"
        )
        val actual = keypair.publicKey.toAddress(isGM)
        val expected = "zltc_cWAvRSgCKgfyp5Rz5TH8srmrZsH5fVYpg"
        assertEquals(expected, actual.address)
    }

    @Test
    fun `recovery keypair from private key`() {
        val isGM = true
        val privateKey = "0x72ffdd7245e0ad7cffd533ad99f54048bf3fa6358e071fba8c2d7783d992d997"
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        println(keypair.publicKey.key.toHexString())
        val address = keypair.publicKey.toAddress(isGM)
        println(address.toEthereumAddress())
        println(address)
    }
}