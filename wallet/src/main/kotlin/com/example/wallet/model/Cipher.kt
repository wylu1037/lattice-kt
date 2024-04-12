package com.example.wallet.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cipher(
    val aes: Aes,
    @SerialName("cipherText") val ciphertext: String,
    val kdf: Kdf,
    val mac: String
)