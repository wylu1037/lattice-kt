package com.example.wallet.model

import com.example.wallet.toEntropy

data class MnemonicWords(val words: Collection<String>) {
    /**
     * 构造函数
     *
     * @param phrase 词语/短语
     */
    constructor(phrase: String) : this(phrase.split(" "))

    /**
     * 构造函数
     *
     * @param phrases 词语/短语 集
     */
    constructor(phrases: Array<String>) : this(phrases.toList())

    override fun toString() = words.joinToString(" ")
}

fun MnemonicWords.validate(lang: Language) = try {
    toEntropy(lang)
    true
} catch (e: IllegalArgumentException) {
    e.printStackTrace()
    false
}