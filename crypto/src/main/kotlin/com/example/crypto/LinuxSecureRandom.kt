package com.example.crypto

import java.io.*
import java.security.Provider
import java.security.SecureRandomSpi
import java.security.Security


/**
 * Implementation from
 * [BitcoinJ implementation](https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/LinuxSecureRandom.java)
 *
 *
 * A SecureRandom implementation that is able to override the standard JVM provided
 * implementation, and which simply serves random numbers by reading /dev/urandom. That is, it
 * delegates to the kernel on UNIX systems and is unusable on other platforms. Attempts to manually
 * set the seed are ignored. There is no difference between seed bytes and non-seed bytes, they are
 * all from the same source.
 */
class LinuxSecureRandom : SecureRandomSpi() {

    // DataInputStream is not thread safe, so each random object has its own.
    private val dis: DataInputStream = DataInputStream(urandom)

    private class LinuxSecureRandomProvider : Provider("LinuxSecureRandom", 1.0, "A Linux specific random number provider that uses /dev/urandom") {
        init {
            put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom::class.java.name)
        }
    }

    override fun engineSetSeed(bytes: ByteArray) {
        // Ignore.
    }

    override fun engineNextBytes(bytes: ByteArray) {
        try {
            dis.readFully(bytes) // This will block until all the bytes can be read.
        } catch (e: IOException) {
            throw RuntimeException(e) // Fatal error. Do not attempt to recover from this.
        }
    }

    override fun engineGenerateSeed(i: Int) = ByteArray(i).apply {
        engineNextBytes(this)
    }

    companion object {
        private val urandom: FileInputStream

        init {
            try {
                val file = File("/dev/urandom")
                // This stream is deliberately leaked.
                urandom = FileInputStream(file)
                if (urandom.read() == -1) {
                    throw RuntimeException("/dev/urandom not readable?")
                }
                // Now override the default SecureRandom implementation with this one.
                val position = Security.insertProviderAt(LinuxSecureRandomProvider(), 1)

                if (position != -1) {
                    println("Secure randomness will be read from {} only.$file")
                } else {
                    println("Randomness is already secure.")
                }
            } catch (e: FileNotFoundException) {
                // Should never happen.
                println("/dev/urandom does not appear to exist or is not openable")
                throw RuntimeException(e)
            } catch (e: IOException) {
                println("/dev/urandom does not appear to be readable")
                throw RuntimeException(e)
            }
        }
    }
}