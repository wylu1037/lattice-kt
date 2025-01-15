package com.example.crypto.impl.hashing

sealed class DigestParams(val keySize: Int) {
    data object Sha256 : DigestParams(256)
    data object Sha512 : DigestParams(512)
}