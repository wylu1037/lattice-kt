package com.example.crypto.api.mac

import com.example.crypto.impl.hashing.DigestParams

interface Hmac {

    fun init(key: ByteArray, digestParams: DigestParams = DigestParams.Sha512): Hmac

    fun generate(data: ByteArray): ByteArray
}