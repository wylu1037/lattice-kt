package com.example.crypto.api.ec

import java.math.BigInteger

/**
 * ECDSA（Elliptic Curve Digital Signature Algorithm，椭圆曲线数字签名算法)，
 * 签名结果由两个大整数组成，通常记作 (r, s)
 *
 * @param r R = k * G，k为随机数，G为椭圆曲线上的基点，R 的 x 坐标作为 r。r 是一个在 [1, n-1] 区间内的整数，其中 n 是椭圆曲线群的阶。
 * @param s 是基于消息摘要（经过哈希处理后的原始信息）、私钥 d、随机数 k 以及前面计算出的 r 值通过某种数学运算得出的，目的是使得验证者可以通过公钥、消息摘要以及 (r, s) 能够验证签名的有效性。
 *
 */
data class ECDSASignature(val r: BigInteger, val s: BigInteger)
