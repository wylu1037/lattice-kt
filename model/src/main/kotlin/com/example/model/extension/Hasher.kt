package com.example.model.extension

import org.bouncycastle.crypto.digests.SM3Digest
import org.komputing.khash.sha256.extensions.sha256
import org.komputing.khex.extensions.hexToByteArray
import org.komputing.khex.model.HexString

fun ByteArray.hash(gm: Boolean = true): ByteArray {
    return if (gm) {
        val digest = SM3Digest()
        digest.update(this, 0, this.size)
        val out = ByteArray(digest.digestSize)
        digest.doFinal(out, 0)
        out
    } else{
        this.sha256()
    }
}

fun HexString.hash(gm: Boolean = true): ByteArray {
    return this.hexToByteArray().hash(gm)
}