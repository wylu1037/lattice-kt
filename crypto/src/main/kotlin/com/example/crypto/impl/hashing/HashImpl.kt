package com.example.crypto.impl.hashing

import com.example.crypto.api.hashing.Hash
import org.bouncycastle.crypto.digests.SM3Digest
import org.komputing.khash.sha256.extensions.sha256
import org.komputing.khex.extensions.toHexString

class HashImpl(private val gm: Boolean): Hash {
    override fun hash(message: ByteArray): String {
        return if (gm) {
            val digest = SM3Digest()
            digest.update(message, 0, message.size)
            val out = ByteArray(digest.digestSize)
            digest.doFinal(out, 0)
            out.toHexString()
        } else {
            message.sha256().toHexString()
        }
    }
}