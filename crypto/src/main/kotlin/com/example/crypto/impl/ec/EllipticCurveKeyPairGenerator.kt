package com.example.crypto.impl.ec

import com.example.crypto.api.ec.KeyPairGenerator
import com.example.model.ECKeyPair
import com.example.model.PrivateKey
import com.example.model.PublicKey
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import java.math.BigInteger
import java.util.*

class EllipticCurveKeyPairGenerator(private val curveName: String) : KeyPairGenerator {
    override fun generate(): ECKeyPair  = ECKeyPairGenerator().run {
        init(ECKeyGenerationParameters(getDomainParams(curveName), null))
        generateKeyPair().run {
            // q = dG
            val privateKeyValue = (private as ECPrivateKeyParameters).d
            val publicKeyBytes = (public as ECPublicKeyParameters).q.getEncoded(false)
            val publicKeyValue = BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.size))
            ECKeyPair(PrivateKey(privateKeyValue), PublicKey(publicKeyValue))
        }
    }
}