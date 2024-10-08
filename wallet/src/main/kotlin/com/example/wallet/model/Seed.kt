package com.example.wallet.model

import com.example.crypto.api.CryptoAPI
import com.example.crypto.extension.toECKeyPair
import com.example.crypto.impl.ec.EllipticCurve
import com.example.crypto.impl.ec.getCurveParams
import com.example.crypto.impl.ec.secp256k1
import com.example.crypto.impl.ec.sm2p256v1
import com.example.model.ECKeyPair
import com.example.model.PRIVATE_KEY_SIZE
import com.example.model.PrivateKey
import org.komputing.kbip44.BIP44
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException


/**
 * 种子
 *
 * @param seed
 */
data class Seed(val seed: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Seed

        return seed.contentEquals(other.seed)
    }

    override fun hashCode(): Int {
        return seed.contentHashCode()
    }
}

fun Seed.toExtendedKey(publicKeyOnly: Boolean = false, isGM: Boolean = true, testnet: Boolean = false): ExtendedKey {
    try {
        val lr = CryptoAPI.hmac.init(BITCOIN_SEED).generate(seed)
        val skBytes = lr.copyOfRange(0, PRIVATE_KEY_SIZE)
        val chaincode = lr.copyOfRange(PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)
        val m = BigInteger(1, skBytes)

        val curveParams = getCurveParams(if (isGM) sm2p256v1 else secp256k1)
        val curve = EllipticCurve(curveParams)

        if (m >= curve.n) {
            throw KeyException("Master key creation resulted in a key with higher modulus. Suggest deriving the next increment.")
        }
        val keyPair = PrivateKey(skBytes).toECKeyPair(isGM)
        return if (publicKeyOnly) {
            val pubKeyPair = ECKeyPair(PrivateKey(BigInteger.ZERO), keyPair.publicKey)
            ExtendedKey(pubKeyPair, chaincode, 0, 0, 0, if (testnet) tpub else xpub)
        } else {
            ExtendedKey(keyPair, chaincode, 0, 0, 0, if (testnet) tprv else xprv)
        }
    } catch (e: NoSuchAlgorithmException) {
        throw KeyException(e)
    } catch (e: NoSuchProviderException) {
        throw KeyException(e)
    } catch (e: InvalidKeyException) {
        throw KeyException(e)
    }
}

fun Seed.toKey(path: String, isGM: Boolean = true, testnet: Boolean = false) = BIP44(path).path
    .fold(toExtendedKey(isGM = isGM, testnet = testnet)) { current, bip44Element ->
        current.generateChildKey(bip44Element, isGM)
    }