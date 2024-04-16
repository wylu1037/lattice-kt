package com.example.mnemonic

import com.example.crypto.api.CryptoAPI
import com.example.mnemonic.model.*
import com.example.model.extension.toBitArray
import com.example.model.extension.toByteArray
import org.komputing.khash.sha256.extensions.sha256
import java.security.SecureRandom

/**
 * 助记词
 */
@JvmInline
value class Mnemonic(val phrase: String) {
    companion object {
        /**
         * Generates a mnemonic phrase, given a desired [strength]
         * The [strength] represents the number of entropy bits this phrase encodes and needs to be a multiple of 32
         *
         * @param strength 强度（长度），128 - 12 个助记词，160 - 15，192 - 18，256 - 24
         * @param lang 语言
         * @return 助记词
         */
        fun generate(strength: Int = 128, lang: Language = Language.ZH_HANS): Mnemonic {
            require(strength % 32 == 0) { "The entropy strength needs to be a multiple of 32" }

            val entropyBytes = ByteArray(strength / 8)
            SecureRandom().nextBytes(entropyBytes)

            Entropy(entropyBytes)

            return Entropy(entropyBytes).toMnemonic(lang)
        }
    }
}

/**
 * 熵
 */
@JvmInline
value class Entropy(val entropy: ByteArray)

fun Mnemonic.toMnemonicWords() = MnemonicWords(
    phrase.trim().lowercase().split(" ")
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toList()
)

/**
 * Generates a seed buffer from a mnemonic phrase according to the BIP39 spec.
 * The mnemonic phrase is given as a list of words and the seed can be salted using a password
 *
 * @param password 密码
 * @return 种子
 */
fun MnemonicWords.toSeed(password: String? = ""): Seed {
    val pass = words.joinToString(" ")
    val salt = "mnemonic$password"

    return Seed(CryptoAPI.pbkdf2.derive(pass.toCharArray(), salt.toByteArray()))
}

fun Mnemonic.toEntropy(lang: Language) = MnemonicWords(phrase).toEntropy(lang)

/**
 * Converts a list of words into a [ByteArray] entropy buffer according to the BIP39 spec
 *
 * @param lang 语言
 * @return 熵
 */
fun MnemonicWords.toEntropy(lang: Language): Entropy {
    require(words.size % 3 <= 0) { "Word list size must be multiple of three words." }
    require(words.isNotEmpty()) { "Word list is empty." }

    val numTotalBits = words.size * 11
    val bitArray = BooleanArray(numTotalBits)

    val wordList = lang.toWordList()
    for ((phraseIndex, word) in words.withIndex()) {
        val dictIndex = wordList.indexOf(word)
        require(dictIndex >= 0) { "word($word) not in known word list" }

        // Set the next 11 bits to the value of the index.
        for (bit in 0..10)
            bitArray[phraseIndex * 11 + bit] = dictIndex and (1 shl (10 - bit)) != 0
    }

    val numChecksumBits = numTotalBits / 33
    val numEntropyBits = numTotalBits - numChecksumBits

    val entropy = bitArray.toByteArray(numEntropyBits / 8)

    // Take the digest of the entropy.
    val hash = entropy.sha256()
    val hashBits = hash.toBitArray()

    // Check all the checksum bits.
    for (i in 0 until numChecksumBits)
        require(bitArray[numEntropyBits + i] == hashBits[i]) { "mnemonic checksum does not match" }

    return Entropy(entropy)
}

/**
 * 助记词生成扩展密钥
 *
 * @param path 默认路径为 m/44'/60'/0'/0/0
 * @param saltPhrase 盐值(密码)
 * @param isGM sm2p256v1 or secp256k1
 * @return 扩展密钥
 */
fun MnemonicWords.toExtendedKey(path: String = DEFAULT_PATH, saltPhrase: String = "", isGM: Boolean = true) =
    toSeed(saltPhrase).toKey(path, isGM)

/**
 * 熵转为助记词
 *
 * @param lang 助记词的语言
 * @return 助记词
 */
fun Entropy.toMnemonic(lang: Language = Language.ZH_HANS): Mnemonic {
    if (entropy.size % 4 > 0) {
        throw RuntimeException("Entropy not multiple of 32 bits.")
    }
    if (entropy.isEmpty()) {
        throw RuntimeException("Entropy is empty.")
    }

    val hash = entropy.sha256()
    val hashBits = hash.toBitArray()

    val entropyBits = entropy.toBitArray()
    val checksumLengthBits = entropyBits.size / 32

    val concatBits = BooleanArray(entropyBits.size + checksumLengthBits)
    entropyBits.copyInto(concatBits)
    hashBits.copyInto(concatBits, destinationOffset = entropyBits.size, endIndex = checksumLengthBits)

    val wordList = lang.toWordList()
    val words = ArrayList<String>().toMutableList()
    val numWords = concatBits.size / 11
    for (i in 0 until numWords) {
        var index = 0
        for (j in 0..10) {
            index = index shl 1
            if (concatBits[i * 11 + j]) {
                index = index or 0x01
            }
        }
        words.add(wordList[index])
    }
    return Mnemonic(words.joinToString(" "))
}