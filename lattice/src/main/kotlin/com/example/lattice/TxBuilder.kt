package com.example.lattice

import com.example.lattice.model.Transaction
import com.example.lattice.model.TxTypeEnum
import com.example.model.Address
import com.example.model.block.CurrentTDBlock
import java.time.Instant

// 建造者模式
// Design: See https://refactoringguru.cn/design-patterns/builder
interface TransactionBuilder {
    fun build(): Transaction

    fun setBlock(latest: CurrentTDBlock): TransactionBuilder

    fun setCode(data: String): TransactionBuilder

    fun setPayload(payload: String): TransactionBuilder

    fun setOwner(owner: Address): TransactionBuilder

    fun setLinker(linker: Address): TransactionBuilder

    fun setAmount(amount: Long): TransactionBuilder

    fun setJoule(joule: Long): TransactionBuilder

    fun refreshTimestamp(): TransactionBuilder
}

// 转账交易建造者
class TransferTXBuilder : TransactionBuilder {

    private val tx = Transaction(0, "", "", owner = Address(""), type = TxTypeEnum.SEND)

    companion object {
        fun builder(): TransferTXBuilder = TransferTXBuilder()
    }

    override fun build(): Transaction {
        return tx
    }

    override fun setBlock(latest: CurrentTDBlock): TransferTXBuilder {
        tx.number = latest.currentTBlockNumber + 1
        tx.parentHash = latest.currentTBlockHash
        tx.daemonHash = latest.currentDBlockHash
        return this
    }

    override fun setCode(data: String): TransferTXBuilder {
        tx.code = data
        return this
    }

    override fun setPayload(payload: String): TransferTXBuilder {
        tx.payload = payload
        return this
    }

    override fun setOwner(owner: Address): TransferTXBuilder {
        tx.owner = owner
        return this
    }

    override fun setLinker(linker: Address): TransferTXBuilder {
        tx.linker = linker
        return this
    }

    override fun setAmount(amount: Long): TransactionBuilder {
        tx.amount = amount
        return this
    }

    override fun setJoule(joule: Long): TransactionBuilder {
        tx.joule = joule
        return this
    }

    override fun refreshTimestamp(): TransferTXBuilder {
        tx.timestamp = Instant.now().epochSecond
        return this
    }
}
