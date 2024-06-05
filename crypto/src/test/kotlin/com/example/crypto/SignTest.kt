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

class SignTest {

    @Test
    fun `sign and verify for secp256k1`() {
        val isGM = false
        val priKeyHex = "0xc842e1ef9ece7e992a4021423a58d6e89c751881e43fd7dbebe70f932ad493e2"
        val message = HexString("790dcb1e43ac151998f8c2e59e0959072f9d476d19fb6f98d7a4e59ea5f8e59e").hexToByteArray()
        val keypair = PrivateKey(HexString(priKeyHex)).toECKeyPair(isGM)
        val signature = keypair.signMessage(message, isGM)
        println("signature: ${signature.toHex()}")
        val result = keypair.publicKey.verifySignature(message, signature, isGM)
        println(result)
    }

    @Test
    fun `sign and verify for sm2p256k1`() {
        val isGM = true
        val priKeyHex = "0x29d63245990076b0bbb33f7482beef21855a8d2197c8d076c2356c49e2a06322"
        val message = HexString("790dcb1e43ac151998f8c2e59e0959072f9d476d19fb6f98d7a4e59ea5f8e59e").hexToByteArray()
        val keypair = PrivateKey(HexString(priKeyHex)).toECKeyPair(isGM)
        var signature = keypair.signMessage(message, isGM)
        println("signature: ${signature.toHex()}")
        signature =
            SignatureData.fromHexString("0x4a5d753c8746143ace65ee925f9ce49bae755a9dfa29f06c1a3b593365363f46ec32fb799769ff01337cb89fb98c88999885a2d52b3d73d56c83f8acb8a8d7c0010931ab5708c28403560b471e30e7f5c404bdeabea2e8e2d5d6cc4f1ca96ba4aa")
        println("signature: ${signature.toHex()}")
        // 0x203611c32f0cebacf218183a5f381b1023412376bf5ad5f478342c19ad9321790ea307113fb7e38b9e4c8de13c748b971aa82972134277bba3045c5e09cd457a011bab3d01ceb5c070d2291bd15fa2087205cbce2cc68df51561d915956ed83ed5
        val result = keypair.publicKey.verifySignature(message, signature, isGM)
        println(result)
    }

    @Test
    fun `new keypair`() {
        val keypair = createKeyPair(true)
        println(keypair.privateKey.key.toHexString())
    }

    @Test
    fun `recovery keypair from private key`() {
        val privateKey = "23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb"
        val keypair = PrivateKey(HexString(privateKey)).toECKeyPair(true)
        println(keypair.publicKey.key.toHexString())
        val address = keypair.publicKey.toAddress(true)
        println(address)
    }
}