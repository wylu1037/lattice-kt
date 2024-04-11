package com.example.crypto

import com.example.crypto.api.CryptoAPI
import com.example.model.ECKeyPair
import com.example.model.PrivateKey
import com.example.model.PublicKey
import com.example.model.SignatureData
import java.math.BigInteger
import java.security.SignatureException

fun ECKeyPair.signMessage(message: ByteArray, gm: Boolean = true) = sign(message, privateKey, gm)

fun PublicKey.verifySignature(message: ByteArray, signature: SignatureData, gm: Boolean) = verify(message, signature, this, gm)

private fun sign(message: ByteArray, privateKey: PrivateKey, gm: Boolean = true): SignatureData =
    if (gm) {
        CryptoAPI.sm2P256V1Signer.sign(message, privateKey.key)
    } else {
        CryptoAPI.secP256K1Signer.sign(message, privateKey.key)
    }

private fun verify(message: ByteArray, signature: SignatureData, publicKey: PublicKey, gm: Boolean = true): Boolean =
    if (gm) {
        CryptoAPI.sm2P256V1Signer.verify(message, signature, publicKey.key)
    } else {
        CryptoAPI.secP256K1Signer.verify(message, signature, publicKey.key)
    }

@Throws(SignatureException::class)
fun signedMessageToKey(message: ByteArray, signature: SignatureData): PublicKey {
    if (signature.e != BigInteger.ZERO) {
        for (i in 0..3) {
            val publicKey = CryptoAPI.sm2P256V1Signer.recover(i, message, signature)
            if (publicKey != null) {
                return PublicKey(publicKey)
            }
        }
    }
    val header = signature.v.toByteArray().last()
    // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
    //                  0x1D = second key with even y, 0x1E = second key with odd y
    if (header < 27 || header > 34) {
        throw SignatureException("Header byte out of range: $header")
    }

    val recId = header - 27
    return PublicKey(
        CryptoAPI.secP256K1Signer.recover(recId, message, signature)
            ?: throw SignatureException("Could not recover public key from signature")
    )
}

fun publicKeyFromPrivate(privateKey: PrivateKey, gm: Boolean): PublicKey =
    if (gm) {
        PublicKey(CryptoAPI.sm2P256V1Signer.publicFromPrivate(privateKey.key))
    } else {
        PublicKey(CryptoAPI.secP256K1Signer.publicFromPrivate(privateKey.key))
    }