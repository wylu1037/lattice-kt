package com.example.wallet

import com.example.model.extension.toHexString
import com.example.model.toAddress
import com.example.wallet.model.Wallet
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WalletTest {

    @Test
    fun `new wallet from private key`() {
        val privateKey = "0x663e2481bf5645cf80705d5a180d2937f72ba8ba3e1b14b34064a42ec2b1ae74"
        val wallet = Wallet.fromPrivateKey(privateKey, true, "Root1234")
        val json = Json.encodeToString(wallet)
        println(json)
        assertNotNull(json)
    }

    @Test
    fun `decrypt wallet`() {
        val json =
            "{\"uuid\":\"123f1bf5-5599-45c4-8566-9a6440ba359f\",\"address\":\"zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"8f6de52c0be43ae438feddea4c210772da23b9333242b7416446eae889b594e0\",\"iv\":\"1ad693b4d8089da0492b9c8c49bc60d3\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"309210a97fbf705eed7bf3485c16d6922a21591297b52c0c59b4f7495863e300\"}},\"cipherText\":\"8f6de52c0be43ae438feddea4c210772da23b9333242b7416446eae889b594e0\",\"mac\":\"335fab3901f8f5c4408b7d6a310ec29cf5bd3792deb696f1b10282e823241c96\"},\"isGM\":true}"
        val wallet = Wallet.fromFileKey(json)
        val keypair = wallet.decrypt("Root1234")
        val address = keypair.toAddress(true)
        val privateKey = keypair.privateKey.key.toHexString()
        println(keypair.toAddress())
        println(privateKey)
        assertEquals("0x23d5b2a2eb0a9c8b86d62cbc3955cfd1fb26ec576ecc379f402d0f5d2b27a7bb", privateKey)
        assertEquals("zltc_Z1pnS94bP4hQSYLs4aP4UwBP9pH8bEvhi", address.toString())
    }

    @Test
    fun `batch decrypt file key`() {
        val passwordArr = arrayOf(
            "zN9H6VWiwXfeLBa",
            "C4EdacYKVScdol0",
            "enLx9LNhlQlIQEd",
            "us5NrkAhfbvfg3t",
            "zCsvsiUw1LvJtaE",
            "VJmHEbqgkvrOPI9",
            "GTU4TGvJpxlUHwy",
            "B5BWPmtEUFe9l7a",
            "jBJQFhR8EUfiBHk",
            "UsFpa9GR1IOg9TM",
            "XJ9b8CkZkSiAg43",
            "m2GBeyi3imFLdB5",
            "pH6GpfmtDkxh8RQ",
            "rZzrqEBjFMHz9NT",
            "WicxdUAKruqooY4",
            "pP6eGG5kkxFMAsyj"
        )
        val fileKeyArr = arrayOf(
            "{\"uuid\":\"9c05c0cd-8c7c-4210-a1f9-70924a56049e\",\"address\":\"zltc_n9JkiAoXdtouKDtWvSUuDDiBZwa5s2WNq\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"f843e79657e9e62f48dd672bad557813ab797bbb40b1329a25cafc07e381c105\",\"iv\":\"190c33974edf7bdde8d0c0a8ed4207bb\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"f0a742970185a6a2f22cd14514aacbd5a330807af23318e87c82456515c12d86\"}},\"cipherText\":\"f843e79657e9e62f48dd672bad557813ab797bbb40b1329a25cafc07e381c105\",\"mac\":\"9844a70b42683363a1890ff1ef8a2a360a78ce3dfa92f4161fc1dee265134225\"},\"isGM\":true}",
            "{\"uuid\":\"3093f0fe-3d51-4913-a1fd-3e562817c4c1\",\"address\":\"zltc_m8XJ7iTp6PRuK6LJPLYWzusDGYeVaMKer\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"6a53875201234556a4986fa3fc137a48b17dd7a835f908daadd6727f478534dd\",\"iv\":\"6208b5c7216c2728e0e0199f99515e57\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"d3679d432b10733b850ef55cea508f7ab4e22861bbc2936265096b2959cf2e53\"}},\"cipherText\":\"6a53875201234556a4986fa3fc137a48b17dd7a835f908daadd6727f478534dd\",\"mac\":\"5010f6e4acb4531daef6bcd290efe6cd70d96be0081180872c3328eb036b53af\"},\"isGM\":true}",
            "{\"uuid\":\"73de6dff-d0f6-41b5-9f11-1be4e60855ba\",\"address\":\"zltc_Vd7zf3F1NyoHUnh2F1SkUYRpP2kVjPqa5\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"c79c7e052b6da13c27a71881b5b527dd0c65191c22de9f42a0925aba708e68e0\",\"iv\":\"4ecfdd668208fc38d9b0796373168abb\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"e9429e085300430cfbee46cafb16d9be82d6fc2410cce8a9e8e374405eaab8dd\"}},\"cipherText\":\"c79c7e052b6da13c27a71881b5b527dd0c65191c22de9f42a0925aba708e68e0\",\"mac\":\"3b09db8e919c978704b1526b6c4bd7359e30c5aa96fa5535db694960c58ffc36\"},\"isGM\":true}",
            "{\"uuid\":\"f2e6b50d-e69e-41de-b4c2-f30a334d561e\",\"address\":\"zltc_gpB4dBwtsTvB3m7J1FfqWNsbSBrjRAYyR\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"f9c53d3719862fc4641560b002db5d08993eb8246c32ae99efe4bec8224c5c5c\",\"iv\":\"658f68d9ff327e663b395d9ba05f4df1\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"600bf46e47949ac7e4670db1fcfb1b15de15631a11a46b8b1a05a54f700c78bf\"}},\"cipherText\":\"f9c53d3719862fc4641560b002db5d08993eb8246c32ae99efe4bec8224c5c5c\",\"mac\":\"fb97313646d6f6ad6727dfcab1142002d5d56cf94941b594c3424dc7adacb957\"},\"isGM\":true}",
            "{\"uuid\":\"505e9707-f5d4-4a99-be1f-7034d20e5d61\",\"address\":\"zltc_U6rcXhQhzbTwYH4h2Rwoay9qeLDrcWDkj\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"d3e3d144ae74c9ce619657d758ebd187b6791c0ac9f75ffc55a3935b128eccbd\",\"iv\":\"4280a7d8ab324ca68b3f8a1857a7c9bd\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"01f155fc7e0d01aeb03c4aff4b5dccb3dc3d0655d03e04bba81572a4dbc32bc4\"}},\"cipherText\":\"d3e3d144ae74c9ce619657d758ebd187b6791c0ac9f75ffc55a3935b128eccbd\",\"mac\":\"1dfe2358d189d9032202e41065da745785dbdc3fad57a0919290e291cbc6293c\"},\"isGM\":true}",
            "{\"uuid\":\"7143d4a9-e0e4-4ede-84da-38af39f60619\",\"address\":\"zltc_gGuHzSRZQFAq7pksDudNh3WJ5jriGaUuZ\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"b81dd42d371bacd9bbdf75fa5610cac758986bbc49f32f7d57426cbd006a0778\",\"iv\":\"2c3c6251241114c3a2d083aaa206f556\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"2076848ce0db17f38e3f86da78cad5a6b4c57a77c57fd350ffe6a79ce6ac12db\"}},\"cipherText\":\"b81dd42d371bacd9bbdf75fa5610cac758986bbc49f32f7d57426cbd006a0778\",\"mac\":\"000eb018ca88d8e7b0e4b7c2715e86d4a1694ef514df4319df24e93eb2d42d26\"},\"isGM\":true}",
            "{\"uuid\":\"ac808ca1-9606-4af0-a2d9-dae3d110b86f\",\"address\":\"zltc_bux4rfVXxN2QAie6hGe3fTd6DK4Z5avLY\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"d1abd35eb9d74547bf4fb09ac0e6c12264c672ba8e9ce69fc20ba65aaffcae21\",\"iv\":\"932ae82577b844966f0161384a88aa97\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"36b969868b21b1bbd12531e9f3851a6a2254b5d6c9b31167c86b8a61d85a299d\"}},\"cipherText\":\"d1abd35eb9d74547bf4fb09ac0e6c12264c672ba8e9ce69fc20ba65aaffcae21\",\"mac\":\"f7c7c33086bd3af63758ccf1c681224e16afac3dc6998fe66040d55c2816b3a6\"},\"isGM\":true}",
            "{\"uuid\":\"0a72a40d-1941-47ef-8663-b023627d4bdf\",\"address\":\"zltc_eYP7gXWvQywSgyqrSwVJNxTGroq8tg1mF\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"1fc21aa5d3fed6a8648143a538cd163653a3bdcafca731447917e7d62ee01c60\",\"iv\":\"2762c5d112c25dd9a40a13755359b951\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"fce4c394bc2fa7360982f2b84e6427b035c667db1a05ba7addd435d51dbf8cca\"}},\"cipherText\":\"1fc21aa5d3fed6a8648143a538cd163653a3bdcafca731447917e7d62ee01c60\",\"mac\":\"4e17946260ab8d5cc260785388ad5a89eef0d38411e961e03c071696bec4196d\"},\"isGM\":true}",
            "{\"uuid\":\"657828f7-2545-41f7-8ea7-98d304a5702a\",\"address\":\"zltc_cqsVyWn3zhFJMQXrpBETLFguvbTxhgnFb\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"74598f99414a05e95d58c1951d7f4ea7404678de98eabbe44d2648b42fce6404\",\"iv\":\"b24b2d5be43a1f563f2da10733940488\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"968de37bc4b18c8a49bf4380faace365493a8c913b03264ac8590a6cd3d4afcd\"}},\"cipherText\":\"74598f99414a05e95d58c1951d7f4ea7404678de98eabbe44d2648b42fce6404\",\"mac\":\"e933cbcb1356feaa0135bed96f02240ccb9899224e567d77397e2cbdedcf5651\"},\"isGM\":true}",
            "{\"uuid\":\"aa89362e-b12a-4157-a99e-765865746d7d\",\"address\":\"zltc_mdg2qaFL9nE45APA27nP8SbXcWoiVPMpV\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"6035dbadc912ded3677e5b8d6afec27f738c93322bd554d01c238ba015ff5467\",\"iv\":\"c1174d73828ab5af398b6c2468c26f53\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"3051471938707d8c8c68d670bf61593c66c7ba68998bccc2157a23634abe8a17\"}},\"cipherText\":\"6035dbadc912ded3677e5b8d6afec27f738c93322bd554d01c238ba015ff5467\",\"mac\":\"ccdc632cf8839c30e161b869734de767198fe1ea6a25a95ecb7ae350a526484b\"},\"isGM\":true}",
            "{\"uuid\":\"e90a08a2-7a09-4a46-ab24-be8149da70a2\",\"address\":\"zltc_jMpV4uKkKvih215x5jaFazTDVNrNiB4Zf\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"a902fba432b76e47ece5f2435aa9e4953f8698657acbe4b5917abb088386c7fa\",\"iv\":\"e3cd20408d969a9c04aae9142788abf0\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"0b380c9743c0757d804448ab402387621ff56ccd1aa323d0c3b9c596bde57c5d\"}},\"cipherText\":\"a902fba432b76e47ece5f2435aa9e4953f8698657acbe4b5917abb088386c7fa\",\"mac\":\"a4130b6705d24ba53860aafac38cd44afac31b56f72dbab14cb9eba595a238df\"},\"isGM\":true}",
            "{\"uuid\":\"306abdc4-7292-43db-b8df-e49a5fd4c543\",\"address\":\"zltc_YYpNBS4nkHQeThCsnz4RwU52p7pWspaZP\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"48303bde3a2bdf620a5751227523e7e889aeb65c99919c29cff858c0c7b07f1d\",\"iv\":\"c443abe0a696c81006320786ec14d674\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"c322d1ec6c775fd477b250d3331b25fb495b6397af14a73f854650120d7679a4\"}},\"cipherText\":\"48303bde3a2bdf620a5751227523e7e889aeb65c99919c29cff858c0c7b07f1d\",\"mac\":\"411e2334e75642138e6505f5a3d80f7606ecc833bb24f7bcd8848fe2cf90f88b\"},\"isGM\":true}",
            "{\"uuid\":\"aac8599c-f694-4f0d-8b18-6e6c323e4c13\",\"address\":\"zltc_VpkR4raBBDWpotwqa9Q14GP6Jcix6jv3X\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"9083d6f1957ae318744c9da9c7e81cbdc27b2c2e61f2364ffd1c6d4584f77dc0\",\"iv\":\"05d10aca4546278fac071eba9d128777\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"bee73205a29dbc82f341ece44430f1adbccf22c83a37958da167d9a1274c2939\"}},\"cipherText\":\"9083d6f1957ae318744c9da9c7e81cbdc27b2c2e61f2364ffd1c6d4584f77dc0\",\"mac\":\"21783f577c2ca0d2fc01a7ab367a58656474d348e4be3ac9a50e0e58563b346b\"},\"isGM\":true}",
            "{\"uuid\":\"165800ef-f1f3-4cbe-8041-e9a39bc5e3ac\",\"address\":\"zltc_TPuTUFJxFZnnM3Q4cAUcBhyFsdXZ4p4yw\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"189f64219089bf0635cf432388df9bac65e117d401eb4a2674c86a78d54fb3cc\",\"iv\":\"41466b4a6b9c69752244db2693362add\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"a3a1fa6b30979117559ec75dee3acc0c5e3108c627b3fd2578c3faf6ea7381b2\"}},\"cipherText\":\"189f64219089bf0635cf432388df9bac65e117d401eb4a2674c86a78d54fb3cc\",\"mac\":\"96f147fd8cca66d838071e6ab20ce766a1a43f1011e9c99bb44bf1234f883723\"},\"isGM\":true}",
            "{\"uuid\":\"8846c2bc-77f1-4dc7-b3f9-c671994244fd\",\"address\":\"zltc_kjV2YyDiW5JzdUdAqA4tvzyjUqaCmyNes\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"da75cb3d5243ac7d1b7b635674c25a5119c846c9895688f5bf6e9aeb1bb67eec\",\"iv\":\"de9cd75ea1e1550a326d192195b7ce23\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"83ebd4587173ae52634b5c0b99ea2a249a8e33bea9c014d5956bf7d0ce3aeec0\"}},\"cipherText\":\"da75cb3d5243ac7d1b7b635674c25a5119c846c9895688f5bf6e9aeb1bb67eec\",\"mac\":\"086365f052b0544b7723690bb73d49dfaa37949e065ed7ffdbdca0c3c8ba0cc6\"},\"isGM\":true}",
            "{\"uuid\":\"e574c5b2-78da-4fef-a431-55f2f20053fc\",\"address\":\"zltc_eaTBe6eKJ56EFdpzgcuQ2fh6GA7pcxfHF\",\"cipher\":{\"aes\":{\"cipher\":\"aes-128-ctr\",\"cipherText\":\"c59f3de60a6921b21518a27ccc2cd9389578c5cf4bd450b519f882a13ec98353\",\"iv\":\"48e0f53ddb92d6136d8a334eff5adf72\"},\"kdf\":{\"kdf\":\"scrypt\",\"kdfParams\":{\"DKLen\":32,\"n\":32768,\"p\":1,\"r\":8,\"salt\":\"1422604e20b59ea4a6bd56832e3a22befb02940fa5dcef7a2e0fc8c5c9874942\"}},\"cipherText\":\"c59f3de60a6921b21518a27ccc2cd9389578c5cf4bd450b519f882a13ec98353\",\"mac\":\"4629a273a330b9a48dcef414882903019e95ca98a64c905d29f707695e6c3526\"},\"isGM\":true}",
        )

        for ((index, password) in passwordArr.withIndex()) {
            val wallet = Wallet.fromFileKey(fileKeyArr[index])
            val keypair = wallet.decrypt(password)
            val privateKey = keypair.privateKey.key.toHexString()
            println(privateKey)
        }
    }
}