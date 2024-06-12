package com.example.model

data class EthereumAddress(private val input: String) {
    val cleanHex = input.removePrefix(HEX_PREFIX)

    @Transient
    val hex = "$HEX_PREFIX$cleanHex"

    override fun toString(): String = hex

    override fun equals(other: Any?): Boolean =
        other is EthereumAddress && other.cleanHex.equals(cleanHex, ignoreCase = true)

    override fun hashCode(): Int = cleanHex.uppercase().hashCode()
}

/**
 * 将以太坊地址转为ZLTC地址
 */
fun EthereumAddress.toAddress(prefix: String = "01"): Address {
    return Address("$LATTICE_ADDRESS_PREFIX${Base58.check(hex, prefix)}")
}

data class Address(val input: String) {
    val cleanHex = input.removePrefix(LATTICE_ADDRESS_PREFIX)

    @Transient
    val hex = "$LATTICE_ADDRESS_PREFIX$cleanHex"

    override fun toString(): String = hex

    override fun equals(other: Any?): Boolean = other is Address && other.cleanHex.equals(cleanHex, ignoreCase = true)

    override fun hashCode(): Int = cleanHex.uppercase().hashCode()
}

fun Address.toEthereumAddress(): String {
    val bytes = Base58.decode(input.removePrefix(LATTICE_ADDRESS_PREFIX))
    val hasPrefix = bytes[0] == "01".toByte()
    val ethereumAddressBuilder = StringBuilder(HEX_PREFIX)
    if (hasPrefix) {
        for (i in 1..bytes.size - 5 step 1) {
            val hexStr = "%02x".format(bytes[i])
            ethereumAddressBuilder.append(hexStr)
        }
    }
    return ethereumAddressBuilder.toString()
}