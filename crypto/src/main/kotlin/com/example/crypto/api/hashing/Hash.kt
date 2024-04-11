package com.example.crypto.api.hashing

interface Hash {

    fun hash(message: ByteArray): String
}