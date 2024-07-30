package com.example.crypto

import com.example.crypto.extension.toECKeyPair
import com.example.model.PrivateKey
import com.example.model.SignatureData
import com.example.model.extension.toHexString
import com.example.model.toAddress
import com.example.model.toHex
import org.junit.Test
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.model.HexString
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
        val priKeyHex = "0xb58ee7d18f8ea223e8f4ca11cd813d3122990a354355f7b25f4891aa1be0ff2b"
        val message = HexString("0102030405060708010203040506070801020304050607080102030405060708").hexToByteArray()
        val keypair = PrivateKey(HexString(priKeyHex)).toECKeyPair(isGM)
        val signature = keypair.signMessage(message, isGM)
        val result = keypair.publicKey.verifySignature(message, signature, isGM)
        assertTrue(result)
    }

    @Test
    fun `verify for sm2p256v1`() {
        val isGM = true
        val privateKey = "0x29d63245990076b0bbb33f7482beef21855a8d2197c8d076c2356c49e2a06322"
        val message = HexString("0102030405060708010203040506070801020304050607080102030405060708").hexToByteArray()
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        val signature =
            "0xa812ac845156ad2eab7756db7fbe61c49a6d719ee7e60ea60397af5895f0d569480be6f9b7b2ef6e40a8bee3c543f3118d71b02dcebec2893c1527cd7ada4849016d6fccf8d952706dcc2c2b5df560466dcd65a43c47965fca67be5121bc1b16ab"
        val pass = keypair.publicKey.verifySignature(message, SignatureData.fromHexString(signature), isGM)
        assertTrue(pass)
    }

    @Test
    fun `new keypair`() {
        val keypair = createKeyPair(true)
        println(keypair.privateKey.key.toHexString())
    }

    @Test
    fun `recovery keypair from private key`() {
        val isGM = true
        val privateKey = "0xdbd91293f324e5e49f040188720c6c9ae7e6cc2b4c5274120ee25808e8f4b6a7"
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(isGM)
        println(keypair.publicKey.key.toHexString())
        val address = keypair.publicKey.toAddress(isGM)
        println(address)
        // 0x6e8db685761cc07094b8bbc9b3a5e84da03de973f98de0c063bb0f1f4af9d8542cfc685e94b9da87c2466b765a7d657a700a3bef96e63bd0a89195d6e6086a2f
        // zltc_jJdpKvG19bYMinz1hEjDpQ4rPryPj1Eos
        // 0xe5dd7b589a5ed21447c9da86efb89ed9cbab99106e08d5645a52bab4caaed2b83393594cc1c404ba0df1b1cbb41401ee9652d66f806fce48e08db01c08a43af6
        // zltc_dS73XWcJqu2uEk4cfWsX8DDhpb9xsaH9s
    }
}