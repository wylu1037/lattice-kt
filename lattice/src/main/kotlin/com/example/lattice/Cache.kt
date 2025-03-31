package com.example.lattice

import com.example.model.Address
import com.example.model.block.CurrentTDBlock
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface Cache<K, V> {
    /**
     * 根据键获取值，如果不存在返回 null
     *
     * @param key 键
     * @return 值
     */
    fun getIfPresent(key: K): V?

    /**
     * 根据键获取值，如果不存在则调用 loader 函数加载值
     *
     * @param key 键
     * @param loader 加载函数
     * @return 值
     */
    fun get(key: K, loader: (K) -> V): V

    /**
     * 存入键值对
     *
     * @param key 键
     * @param value 值
     */
    fun put(key: K, value: V)

    /**
     * 移除键值对
     *
     * @param key 键
     */
    fun invalidate(key: K)

    /**
     * 移除所有键值对
     */
    fun invalidateAll()

    /**
     * 获取缓存的大小
     *
     * @return 缓存的大小
     */
    fun size(): Long

    /**
     * 判断缓存中是否存在指定的键
     *
     * @param key 键
     * @return 是否存在
     */
    fun containsKey(key: K): Boolean
}

class CacheImpl<K : Any, V : Any> : Cache<K, V> {
    private val cache = Caffeine.newBuilder()
        .maximumSize(5000)
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build<K, V>()

    override fun getIfPresent(key: K): V? = cache.getIfPresent(key)

    override fun get(key: K, loader: (K) -> V): V =
        cache.get(key, loader) ?: throw IllegalStateException("Cache missed key: $key")

    override fun put(key: K, value: V) = cache.put(key, value)

    override fun invalidate(key: K) = cache.invalidate(key)

    override fun invalidateAll() = cache.invalidateAll()

    override fun size(): Long = cache.estimatedSize()

    override fun containsKey(key: K): Boolean = cache.getIfPresent(key) != null
}

interface BlockCache {
    /**
     * 存入区块
     *
     * @param chainId 区块链ID
     * @param address 账户地址
     * @param block 区块
     */
    fun put(chainId: String, address: String, block: CurrentTDBlock)

    /**
     * 获取区块
     *
     * @param chainId 区块链ID
     * @param address 账户地址
     * @param fallback 如果缓存中不存在，则调用 fallback 函数获取区块
     * @return 区块
     */
    fun get(chainId: String, address: String, fallback: (String, Address) -> CurrentTDBlock): CurrentTDBlock
}

class BlockCacheImpl(
    private val daemonHashExpiration: Duration = 10.seconds,
) : BlockCache {

    private val daemonHashExpireAtMap = ConcurrentHashMap<String, Date>()

    private val cache: Cache<String, CurrentTDBlock> = CacheImpl()

    override fun put(chainId: String, address: String, block: CurrentTDBlock) {
        cache.put("${chainId}_${address}", block)

        if (!daemonHashExpireAtMap.containsKey(chainId)) {
            daemonHashExpireAtMap[chainId] =
                Date(System.currentTimeMillis() + daemonHashExpiration.inWholeMilliseconds)
        }
    }

    override fun get(chainId: String, address: String, fallback: (String, Address) -> CurrentTDBlock): CurrentTDBlock {
        logger.debug("get block from cache, chainId: $chainId, address: $address")
        val block = cache.getIfPresent("${chainId}_${address}") ?: return fallback(chainId, Address(address))

        daemonHashExpireAtMap[chainId]?.let {
            if (it.before(Date())) {
                logger.warn("daemon hash expired, chainId: $chainId")
                val latestBlock = fallback(chainId, Address(address))
                daemonHashExpireAtMap[chainId] =
                    Date(System.currentTimeMillis() + daemonHashExpiration.inWholeMilliseconds)
                block.currentDBlockHash = latestBlock.currentDBlockHash
            }
        }

        return block
    }
}