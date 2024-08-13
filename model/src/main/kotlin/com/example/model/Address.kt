package com.example.model

/**
 * 以太坊地址类
 *
 * @param addr 以太坊地址，示例：0x5f2be9a02b43f748ee460bf36eed24fafa109920
 */
data class EthereumAddress(private val addr: String) {
    init {
        require(addr.matches(Regex(RegExpr.ETHEREUM_ADDRESS))) {
            "Invalid address: $addr, Example: 0x5f2be9a02b43f748ee460bf36eed24fafa109920"
        }
    }

    val cleanHex = addr.removePrefix(HEX_PREFIX)

    @Transient
    val hex = "$HEX_PREFIX$cleanHex"

    override fun toString(): String = hex

    override fun equals(other: Any?): Boolean =
        other is EthereumAddress && other.cleanHex.equals(cleanHex, ignoreCase = true)

    override fun hashCode(): Int = cleanHex.uppercase().hashCode()
}

/**
 * 将以太坊地址转为ZLTC地址
 *
 * @param prefix 固定为 "01"
 * @return Address
 */
fun EthereumAddress.toAddress(prefix: String = "01"): Address {
    return Address("$LATTICE_ADDRESS_PREFIX${Base58.check(hex, prefix)}")
}

/**
 * ZLTC地址
 *
 * @property addr zltc地址，示例：zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi
 */
data class Address(val addr: String) {
    init {
        require(addr.matches(Regex(RegExpr.ADDRESS))) {
            "Invalid address: $addr, Example: zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi"
        }
    }

    // 移除前缀 zltc_
    val cleanAddress = addr.removePrefix(LATTICE_ADDRESS_PREFIX)

    @Transient
    val address = "$LATTICE_ADDRESS_PREFIX$cleanAddress"

    override fun toString(): String = address

    override fun equals(other: Any?): Boolean =
        other is Address && other.cleanAddress.equals(cleanAddress, ignoreCase = true)

    override fun hashCode(): Int = cleanAddress.uppercase().hashCode()
}

/**
 * 将以太坊地址转为ZLTC地址
 *
 * @return zltc地址
 */
fun Address.toEthereumAddress(): String {
    val bytes = Base58.decode(addr.removePrefix(LATTICE_ADDRESS_PREFIX))
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