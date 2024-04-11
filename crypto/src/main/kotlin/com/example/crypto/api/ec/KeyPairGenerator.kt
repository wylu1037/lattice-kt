package com.example.crypto.api.ec

import com.example.model.ECKeyPair
import java.security.KeyPair

interface KeyPairGenerator {
    fun generate(): ECKeyPair
}