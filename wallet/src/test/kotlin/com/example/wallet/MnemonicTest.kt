package com.example.wallet

import com.example.crypto.createKeyPair
import com.example.crypto.getCompressedPublicKey
import com.example.model.extension.toHexString
import com.example.model.toAddress
import com.example.wallet.model.Language
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MnemonicTest {

    @Test
    fun `generate key pair`() {
        val keypair = createKeyPair()
        println(keypair.privateKey.key.toHexString())
        println(keypair.publicKey.toAddress())
        assertNotNull(keypair)
    }

    @Test
    fun `new mnemonic`() {
        val mnemonic = Mnemonic.generate(128, Language.EN)
        println(mnemonic.toMnemonicWords())
        assertEquals(12, mnemonic.toMnemonicWords().words.size)
    }

    @Test
    fun derive() {
        val mnemonic = Mnemonic("刚 灵 柱 仅 庄 基 画 龄 累 析 飞 阅 海 们 德 否 央 群 毁 渔 康 老 咨 鼻")
        for (i in 0..100) {
            val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/$i", "Root1234")
            println(key.keyPair.privateKey.key.toHexString())
        }
    }

    @Test
    fun `derive for sm2p256v1`() {
        val words = "potato front rug inquiry old author dose little still apart below develop"
        val passphrase = "Root1234"
        val mnemonic = Mnemonic(words)
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/0", passphrase, true)
        val expected = "0x24f5d48f3804af48d7d0f3f02b25bdf7b3f936d8c2c7b04eca415fa83cc02758"
        assertEquals(expected, key.keyPair.privateKey.key.toHexString())
    }

    @Test
    fun `derive for sm2p256v1 version2`() {
        val words = "medal shed task apart range accident ride matrix fire citizen motion ridge"
        val passphrase = "123"
        val mnemonic = Mnemonic(words)
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/2'/3'/4/5", passphrase, true)
        val expected = "0xcd2e0330c22f7d8d38e22ad8df4d15824a7ba0ef7150f4dd777bf036fde64eed"
        assertEquals(key.keyPair.privateKey.key.toHexString(), expected)
    }

    @Test
    fun `generate keypair for sm2p256v1`() {
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
        val isGM = true
        val mnemonic = Mnemonic("笔 余 罩 老 配 速 历 在 联 烧 拨 郎")
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/1", "Root1234", isGM)
        val privateKey = key.keyPair.privateKey.key.toHexString()
        val publicKey = key.keyPair.getCompressedPublicKey(isGM).toHexString()
        val address = key.keyPair.toAddress(isGM).address

        assertEquals("0x93e19bbaf5263b996d946bf3a82f72a221ccf8c2809653b74b4bf7e9802e62e0", privateKey)
        assertEquals("0x032177edf9019a1688439547f6e41efb0bbef34adb11278d8e4c55869798d24cb2", publicKey)
        assertEquals("zltc_ifDn1ssVpTVnhvXQ4KaSQbwnCA91RuL8x", address)
    }

    @Test
    fun `mnemonic to keypair for sm2p256v1`() {
        val isGM = true
        val mnemonic = Mnemonic("笔 余 罩 老 配 速 历 在 联 烧 拨 郎")
        val key = mnemonic.toMnemonicWords().toExtendedKey("m/44'/60'/0'/0/0", "Root1234", isGM)
        val privateKey = key.keyPair.privateKey.key.toHexString()
        val publicKey = key.keyPair.getCompressedPublicKey(isGM).toHexString()
        val address = key.keyPair.toAddress(isGM).address

        assertEquals("0x3aa1180b5036f8f965159a8528454da137f2ae844695ae3e5e1b983a9acc3dcb", privateKey)
        assertEquals("0x03ca7d3d6edee763c83375e596617600c477961aecf8d0533aeaa744fb176a8237", publicKey)
        assertEquals("zltc_XuHuDthDs7KWDaHihg8NGt6ShU8XnFyPW", address)
    }
}