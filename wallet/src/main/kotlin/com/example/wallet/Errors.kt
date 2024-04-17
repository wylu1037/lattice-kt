package com.example.wallet

class InvalidPasswordException : Exception() {
    override val message: String
        get() = "Invalid Password"
}