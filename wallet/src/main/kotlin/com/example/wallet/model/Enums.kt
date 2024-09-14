package com.example.wallet.model

import com.example.mnemonic.wordlist.en.WORDLIST_ENGLISH
import com.example.mnemonic.wordlist.es.WORDLIST_SPANISH
import com.example.mnemonic.wordlist.fr.WORDLIST_FRENCH
import com.example.mnemonic.wordlist.it.WORDLIST_ITALIAN
import com.example.mnemonic.wordlist.ja.WORDLIST_JAPANESE
import com.example.mnemonic.wordlist.ko.WORDLIST_KOREAN
import com.example.mnemonic.wordlist.zhhans.WORDLIST_CHINESE_SIMPLIFIED
import com.example.mnemonic.wordlist.zhhant.WORDLIST_CHINESE_TRADITIONAL

/**
 * 助记词支持的语言
 */
enum class Language {

    /**
     * 英语
     */
    EN,

    /**
     * 西班牙语
     */
    ES,

    /**
     * 法语
     */
    FR,

    /**
     * 意大利语
     */
    IT,

    /**
     * 日语
     */
    JA,

    /**
     * 朝鲜语
     */
    KO,

    /**
     * 中文简体
     */
    ZH_HANS,

    /**
     * 中文繁体
     */
    ZH_HANT
}

/**
 * 返回对应语言的字符集
 *
 * @return 语言的字符集
 */
fun Language.toWordList(): List<String> =
    when (this) {
        Language.EN -> WORDLIST_ENGLISH
        Language.ES -> WORDLIST_SPANISH
        Language.FR -> WORDLIST_FRENCH
        Language.IT -> WORDLIST_ITALIAN
        Language.JA -> WORDLIST_JAPANESE
        Language.KO -> WORDLIST_KOREAN
        Language.ZH_HANS -> WORDLIST_CHINESE_SIMPLIFIED
        Language.ZH_HANT -> WORDLIST_CHINESE_TRADITIONAL
    }
