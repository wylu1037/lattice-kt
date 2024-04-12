package com.example.model

data class EthereumAddress(private val input: String) {
    val cleanHex = input.removePrefix("0x")

    @Transient
    val hex = "0x$cleanHex"

    override fun toString(): String = hex

    override fun equals(other: Any?): Boolean =
        other is EthereumAddress && other.cleanHex.equals(cleanHex, ignoreCase = true)

    override fun hashCode(): Int = cleanHex.uppercase().hashCode()
}

data class Address(private val input: String) {
    val cleanHex = input.removePrefix("zltc_")

    @Transient
    val hex = "zltc_$cleanHex"

    override fun toString(): String = hex

    override fun equals(other: Any?): Boolean = other is Address && other.cleanHex.equals(cleanHex, ignoreCase = true)

    override fun hashCode(): Int = cleanHex.uppercase().hashCode()
}
