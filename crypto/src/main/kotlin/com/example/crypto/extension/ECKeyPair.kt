package com.example.crypto.extension

import com.example.crypto.api.CryptoAPI
import com.example.model.ECKeyPair
import com.example.model.PrivateKey
import com.example.model.PublicKey

fun PrivateKey.toPublicKey(isGM: Boolean = true): PublicKey =
    if (isGM) {
        PublicKey(CryptoAPI.sm2P256V1Signer.publicFromPrivate(key))
    } else {
        PublicKey(CryptoAPI.secP256K1Signer.publicFromPrivate(key))
    }

fun PrivateKey.toECKeyPair(isGM: Boolean = true) = ECKeyPair(
    this,
    this.toPublicKey(isGM)
)