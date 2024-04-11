package com.example.crypto.impl.kdf

import com.example.crypto.api.kdf.SCrypt

class SCryptImpl : SCrypt {
    override fun derive(password: ByteArray, salt: ByteArray?, n: Int, r: Int, p: Int, dkLen: Int) =
        org.bouncycastle.crypto.generators.SCrypt.generate(password, salt, n, r, p, dkLen)
}