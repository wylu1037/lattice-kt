package com.example.wallet.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Aes(
    @SerialName("cipher") val cipher: String,
    @SerialName("cipherText") val ciphertext: String? = null,
    @SerialName("iv") val iv: String
)