package com.example.model

object RegExpr {
    // ZLTC地址格式校验
    const val ADDRESS = "^zltc_[a-zA-Z0-9]{33}$"

    // 0x5f2be9a02b43f748ee460bf36eed24fafa109920
    const val ETHEREUM_ADDRESS = "^(0x)?[a-zA-Z0-9]{40}$"

    // 0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb
    const val PRIVATE_KEY_HEX = "^0x[a-zA-Z0-9]{64}$"

    // 压缩公钥
    const val COMPRESSED_PUBLIC_KEY_HEX = "^0x04[a-zA-Z0-9]{128}$"
}