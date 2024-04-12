package com.example.wallet

import com.example.crypto.SecureRandomUtils.secureRandom
import com.example.crypto.api.CryptoAPI
import com.example.crypto.api.cipher.AESCipher
import com.example.crypto.extension.toECKeyPair
import com.example.model.ECKeyPair
import com.example.model.PRIVATE_KEY_SIZE
import com.example.model.PrivateKey
import com.example.model.extension.hash
import com.example.model.extension.toBytesPadded
import com.example.model.toAddress
import com.example.wallet.model.*
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.extensions.toNoPrefixHexString
import org.komputing.khex.model.HexString
import java.util.*

private const val CIPHER = "aes-128-ctr"
private const val SCRYPT = "scrypt"

fun ECKeyPair.newWallet(password: String, isGM: Boolean = true): Wallet {
    val uuid = UUID.randomUUID().toString()
    val salt = generateRandomBytes(32)
    val kdfParams = KdfParams(
        dkLen = 32,
        n = 262144,
        r = 8,
        p = 1,
        salt = salt.toNoPrefixHexString()
    )

    val derivedKey = generateDerivedSCryptKey(
        password.toByteArray(Charsets.UTF_8),
        kdfParams
    )

    val encryptKey = derivedKey.copyOfRange(0, 16)
    val iv = generateRandomBytes(16)
    val privateKeyBytes = privateKey.key.toBytesPadded(PRIVATE_KEY_SIZE)
    val ciphertext = performCipherOperation(AESCipher.Operation.ENCRYPTION, iv, encryptKey, privateKeyBytes)
    val mac = generateMac(derivedKey, ciphertext, isGM)

    return Wallet(
        address = toAddress(isGM).toString(),
        cipher = Cipher(
            aes = Aes(
                cipher = CIPHER,
                ciphertext = ciphertext.toNoPrefixHexString(),
                iv = iv.toNoPrefixHexString(),
            ),
            ciphertext = ciphertext.toNoPrefixHexString(),
            kdf = Kdf(
                kdf = SCRYPT,
                kdfParams = kdfParams
            ),
            mac = mac.toNoPrefixHexString()
        ),
        uuid = uuid,
        isGM = isGM,
    )
}

private fun generateDerivedSCryptKey(password: ByteArray, kdfParams: KdfParams) =
    CryptoAPI.scrypt.derive(
        password,
        HexString(kdfParams.salt).hexToByteArray(),
        kdfParams.n,
        kdfParams.r,
        kdfParams.p,
        kdfParams.dkLen
    )

private fun performCipherOperation(
    operation: AESCipher.Operation,
    iv: ByteArray,
    encryptKey: ByteArray,
    text: ByteArray
) =
    CryptoAPI.aesCipher.init(
        AESCipher.Mode.CTR,
        AESCipher.Padding.NoPadding,
        operation,
        encryptKey,
        iv
    ).performOperation(text)

private fun generateMac(derivedKey: ByteArray, ciphertext: ByteArray, isGM: Boolean): ByteArray {
    val result = ByteArray(16 + ciphertext.size)

    derivedKey.copyInto(result, startIndex = 16)
    ciphertext.copyInto(result, destinationOffset = 16)

    return result.hash(isGM)
}

/**
 * 生成指定长度的随机的字节数组
 *
 * @param size 随机数长度
 * @return 字节数组
 */
internal fun generateRandomBytes(size: Int) = ByteArray(size).apply {
    secureRandom().nextBytes(this)
}

fun Wallet.decrypt(password: String): ECKeyPair {
    this.validate()

    val mac = HexString(cipher.mac).hexToByteArray()
    val iv = HexString(cipher.aes.iv).hexToByteArray()
    val ciphertext = HexString(cipher.ciphertext).hexToByteArray()

    val derivedKey = generateDerivedSCryptKey(
        password.toByteArray(Charsets.UTF_8),
        cipher.kdf.kdfParams
    )

    val derivedMac = generateMac(derivedKey, ciphertext, isGM)
    if (!derivedMac.contentEquals(mac)) {
        throw InvalidPasswordException()
    }

    val encryptKey = derivedKey.copyOfRange(0, 16)
    val privateKey = PrivateKey(performCipherOperation(AESCipher.Operation.DECRYPTION, iv, encryptKey, ciphertext))
    return privateKey.toECKeyPair(isGM)
}

internal fun Wallet.validate() {
    when {
        cipher.aes.cipher != CIPHER -> throw IllegalArgumentException("Wallet cipher is not supported")
        cipher.kdf.kdf != "scrypt" -> throw IllegalArgumentException("Wallet kdf is not supported")
    }
}