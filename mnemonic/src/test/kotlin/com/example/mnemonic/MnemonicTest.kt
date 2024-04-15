package com.example.mnemonic

import Mnemonic
import com.example.crypto.getCompressedPublicKey
import com.example.mnemonic.model.Language
import com.example.model.extension.toHexString
import com.example.model.toAddress
import org.junit.Test
import toExtendedKey
import toMnemonicWords
import kotlin.test.assertEquals

class MnemonicTest {

    @Test
    fun `generate root keypair for sm2p256v1`() {
        val isGM = true
        val mnemonic = Mnemonic.generate(128, Language.ZH_HANS)
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/0", "Root1234", isGM)

        // 笔 余 罩 老 配 速 历 在 联 烧 拨 郎
        println(mnemonic.phrase)
        // 0xac0bce22aa31b2482491380f00432c5f42a71616bbe0e3a7c3d4e9f054173e9d
        println(key.keyPair.privateKey.key.toHexString())
        // 0x036e3a5f8a258fddce2f2c27db5d7806cbe8b37bf212f91800c19c3c03404c7a41
        println(key.keyPair.getCompressedPublicKey(isGM).toHexString())
        // zltc_dbD561ryjFwz31crQvNrbm3GUhBijz93H
        println(key.keyPair.toAddress(isGM))

        assertEquals(12, mnemonic.toMnemonicWords().words.size)
    }

    @Test
    fun `derive child keypair for sm2p256v1`() {
        val isGM = true;
        val mnemonic = Mnemonic("笔 余 罩 老 配 速 历 在 联 烧 拨 郎")
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/1", "Root1234", isGM)
        val privateKey = key.keyPair.privateKey.key.toHexString()
        val publicKey = key.keyPair.getCompressedPublicKey(isGM).toHexString()
        val address = key.keyPair.toAddress(isGM).hex

        assertEquals("0xcd2f6988a577d47be601acb8eaa172352fadbefcf9b08601a31f7fc1aee09f99", privateKey)
        assertEquals("0x03144e73944db8ff60b8632f16f9c95f501785729eab79ba851db61260f3d94141", publicKey)
        assertEquals("zltc_gSQarL4igrxT3mNfk4J6UW2dp8WYwKwyC", address)
    }

    @Test
    fun `mnemonic to keypair for sm2p256v1`() {
        val isGM = true;
        val mnemonic = Mnemonic("笔 余 罩 老 配 速 历 在 联 烧 拨 郎")
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/0", "Root1234", isGM)
        val privateKey = key.keyPair.privateKey.key.toHexString()
        val publicKey = key.keyPair.getCompressedPublicKey(isGM).toHexString()
        val address = key.keyPair.toAddress(isGM).hex

        assertEquals("0xac0bce22aa31b2482491380f00432c5f42a71616bbe0e3a7c3d4e9f054173e9d", privateKey)
        assertEquals("0x036e3a5f8a258fddce2f2c27db5d7806cbe8b37bf212f91800c19c3c03404c7a41", publicKey)
        assertEquals("zltc_dbD561ryjFwz31crQvNrbm3GUhBijz93H", address)
    }
}