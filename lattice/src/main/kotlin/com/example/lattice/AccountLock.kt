package com.example.lattice

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

fun newAccountLock(): AccountLock {
    return AccountLockImpl()
}

interface AccountLock {
    /**
     * 获取账户锁
     *
     * @param chainId 区块链ID
     * @param address 账户地址
     */
    fun obtain(chainId: String, address: String)

    /**
     * 释放账户锁
     *
     * @param chainId 区块链ID
     * @param address 账户地址
     */
    fun unlock(chainId: String, address: String)

    fun <T> withLock(chainId: String, address: String, block: () -> T): T
}

class AccountLockImpl : AccountLock {

    private val locks = ConcurrentHashMap<String, ReentrantLock>()

    override fun obtain(chainId: String, address: String) {
        logger.debug("obtain account lock, chainId: {}, address: {}", chainId, address)
        val key = "${chainId}_${address}"
        val lock = locks.computeIfAbsent(key) { ReentrantLock() }
        if (lock.isHeldByCurrentThread) {
            logger.warn("account lock is already held by current thread, chainId: {}, address: {}", chainId, address)
            return
        }
        lock.lock()
    }

    override fun unlock(chainId: String, address: String) {
        logger.debug("unlock account lock, chainId: {}, address: {}", chainId, address)
        val key = "${chainId}_${address}"
        // fixme lock leakage problem
        locks[key]?.unlock()
    }

    override fun <T> withLock(chainId: String, address: String, block: () -> T): T {
        obtain(chainId, address)
        try {
            return block()
        } finally {
            unlock(chainId, address)
        }
    }
}