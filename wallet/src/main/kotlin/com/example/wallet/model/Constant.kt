package com.example.wallet.model

import com.example.model.PRIVATE_KEY_SIZE

internal val BITCOIN_SEED = "Bitcoin seed".toByteArray()
internal const val CHAINCODE_SIZE = PRIVATE_KEY_SIZE
internal const val COMPRESSED_PUBLIC_KEY_SIZE = PRIVATE_KEY_SIZE + 1 // 压缩后的公钥大小
internal const val EXTENDED_KEY_SIZE = 78
internal val xprv = byteArrayOf(0x04, 0x88.toByte(), 0xAD.toByte(), 0xE4.toByte())
internal val xpub = byteArrayOf(0x04, 0x88.toByte(), 0xB2.toByte(), 0x1E.toByte())
internal val tprv = byteArrayOf(0x04, 0x35.toByte(), 0x83.toByte(), 0x94.toByte())
internal val tpub = byteArrayOf(0x04, 0x35.toByte(), 0x87.toByte(), 0xCF.toByte())

internal const val DEFAULT_PATH = "m/44'/60'/0'/0/0"