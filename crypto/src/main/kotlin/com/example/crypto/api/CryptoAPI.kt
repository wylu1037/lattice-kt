package com.example.crypto.api

import com.example.crypto.api.ec.KeyPairGenerator
import com.example.crypto.api.ec.Signer
import com.example.crypto.impl.cipher.AESCipherImpl
import com.example.crypto.impl.ec.EllipticCurveKeyPairGenerator
import com.example.crypto.impl.ec.EllipticCurveSigner
import com.example.crypto.impl.ec.secp256k1
import com.example.crypto.impl.ec.sm2p256v1
import com.example.crypto.impl.hmac.HmacImpl
import com.example.crypto.impl.kdf.PBKDF2Impl
import com.example.crypto.impl.kdf.SCryptImpl


object CryptoAPI {

    val scrypt by lazy { SCryptImpl() }
    val aesCipher by lazy { AESCipherImpl() }

    val secP256K1KeyPairGenerator by lazy { EllipticCurveKeyPairGenerator(secp256k1) as KeyPairGenerator }
    val secP256K1Signer by lazy { EllipticCurveSigner(secp256k1) as Signer }

    val sm2P256V1KeyPairGenerator by lazy { EllipticCurveKeyPairGenerator(sm2p256v1) as KeyPairGenerator }
    val sm2P256V1Signer by lazy { EllipticCurveSigner(sm2p256v1) as Signer }

    val hmac by lazy { HmacImpl() }

    val pbkdf2 by lazy { PBKDF2Impl() }
}