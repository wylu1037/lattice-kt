package com.example.crypto.api.cipher

interface AESCipher {
    enum class Mode(val code: String) {
        CTR("CTR"),
        CDC("CDC")
    }

    enum class Padding(val value: String) {
        NoPadding("NoPadding"),
        PKCS5Padding("PKCS5Padding")
    }

    enum class Operation {
        ENCRYPTION,
        DECRYPTION
    }

    /**
     * 初始化
     *
     * @param mode 模式
     * @param padding
     * @param key
     * @param iv 初始化向量(Initialization Vector)
     */
    fun init(mode: Mode, padding: Padding, operation: Operation, key: ByteArray, iv: ByteArray): AESCipher

    fun performOperation(data: ByteArray): ByteArray
}