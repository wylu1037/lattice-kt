package com.example.crypto.api.ec

import com.example.model.SignatureData
import java.math.BigInteger

interface Signer {
    fun sign(message: ByteArray, privateKey: BigInteger): SignatureData
    fun recover(recId: Int, message: ByteArray, signature: SignatureData): BigInteger?
    fun verify(message: ByteArray, signatureData: SignatureData, publicKey: BigInteger): Boolean
    fun publicFromPrivate(privateKey: BigInteger): BigInteger
}