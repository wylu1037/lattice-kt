package com.example.lattice

import com.example.lattice.model.Transaction
import com.example.lattice.model.TxTypeEnum
import com.example.model.Address
import com.example.model.block.CurrentTDBlock
import java.time.Instant

/**
 * 交易的建造者模式
 * Design: See https://refactoringguru.cn/design-patterns/builder
 */
interface TransactionBuilder<T : TransactionBuilder<T>> {
    fun build(): Transaction

    fun setBlock(latest: CurrentTDBlock): T

    fun setCode(data: String): T

    fun setPayload(payload: String): T

    fun setOwner(owner: Address): T

    fun setLinker(linker: Address): T

    fun setAmount(amount: Long): T

    fun setJoule(joule: Long): T

    fun refreshTimestamp(): T
}

/**
 * 交易的建造者抽象类
 *
 * @param type[TxTypeEnum] 交易的类型枚举
 */
abstract class BaseTransactionBuilder<T : BaseTransactionBuilder<T>>(type: TxTypeEnum) :
    TransactionBuilder<T> {

    private val tx = Transaction(0, "", "", owner = Address(ZERO_ADDRESS), type = type)

    @Suppress("UNCHECKED_CAST")
    private fun self(): T = this as T

    override fun build(): Transaction = tx

    override fun setBlock(latest: CurrentTDBlock): T {
        tx.number = latest.currentTBlockNumber + 1
        tx.parentHash = latest.currentTBlockHash
        tx.daemonHash = latest.currentDBlockHash
        return self()
    }

    override fun setCode(data: String): T {
        tx.code = data
        return self()
    }

    override fun setPayload(payload: String): T {
        tx.payload = payload
        return self()
    }

    override fun setOwner(owner: Address): T {
        tx.owner = owner
        return self()
    }

    override fun setLinker(linker: Address): T {
        tx.linker = linker
        return self()
    }

    override fun setAmount(amount: Long): T {
        tx.amount = amount
        return self()
    }

    override fun setJoule(joule: Long): T {
        tx.joule = joule
        return self()
    }

    override fun refreshTimestamp(): T {
        tx.timestamp = Instant.now().epochSecond
        return self()
    }
}

/**
 * 转账交易建造者
 */
class TransferTXBuilder : BaseTransactionBuilder<TransferTXBuilder>(TxTypeEnum.SEND) {
    companion object {
        fun builder(): TransferTXBuilder = TransferTXBuilder()
    }
}

/**
 * 部署合约交易建造者
 */
class DeployContractTXBuilder : BaseTransactionBuilder<DeployContractTXBuilder>(TxTypeEnum.CONTRACT) {
    companion object {
        fun builder(): DeployContractTXBuilder = DeployContractTXBuilder()
    }
}

/**
 * 调用合约交易的建造者
 */
class CallContractTXBuilder : BaseTransactionBuilder<CallContractTXBuilder>(TxTypeEnum.EXECUTE) {
    companion object {
        fun builder(): CallContractTXBuilder = CallContractTXBuilder()
    }
}
