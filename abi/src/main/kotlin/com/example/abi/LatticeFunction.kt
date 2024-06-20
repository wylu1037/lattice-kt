package com.example.abi

import com.example.abi.model.IntNumber
import com.example.abi.model.Types
import com.example.abi.model.UintNumber
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.bouncycastle.util.encoders.Hex
import org.kethereum.abi.model.EthereumABIElement
import org.kethereum.abi.model.EthereumNamedType
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.protocol.core.methods.response.AbiDefinition

object Json {
    val MAPPER: JsonMapper = JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false)
        .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .build()

    /**
     * 将JSON转换为List
     *
     * @param json JSON字符串
     * @param <T> 泛型类型
     * @return 转换后的List实例
     */
    inline fun <reified T> toList(json: String): List<T> {
        val type: JavaType = MAPPER.typeFactory.constructParametricType(List::class.java, T::class.java)
        return try {
            MAPPER.readValue(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun toJsonString(obj: Any): String = MAPPER.writeValueAsString(obj)
}

/**
 * abi function
 *
 * @property abi 合约abi
 * @property methodName 方法名
 * @property method 方法
 */
data class LatticeFunction(val abi: String, val methodName: String? = null, val method: EthereumABIElement)

/**
 * 编码
 * @param args 参数
 * @return code
 */
fun LatticeFunction.encode(args: Array<Any>): String =
    FunctionEncoder.encode(Function(methodName, convertArguments(method.inputs, args), emptyList()))

fun LatticeFunction.decode(rawCode: String): List<Type<*>> {
    val (input, _) = getTypeReferences()
    return FunctionReturnDecoder.decode(rawCode, input)
}

fun LatticeFunction.decodeReturn(rawCode: String): List<Type<*>> {
    val (_, output) = getTypeReferences()
    return FunctionReturnDecoder.decode(rawCode, output)
}


/**
 * 获取 Abi 中方法的输入输出定义
 *
 * @return Pair
 */
fun LatticeFunction.getTypeReferences(): Pair<List<TypeReference<Type<*>>>, List<TypeReference<Type<*>>>> {
    val abiDefinitions = Json.toList<AbiDefinition>(abi)
    val definition = abiDefinitions.filter(methodName)

    return Pair(
        definition.inputs.map { input -> TypeReference.makeTypeReference(input.type) },
        definition.outputs.map { output -> TypeReference.makeTypeReference(output.type) }
    )
}

/**
 * 查找abi中定义的方法
 *
 * @param methodName 方法名
 * @return AbiDefinition
 */
fun List<AbiDefinition>.filter(methodName: String?): AbiDefinition {
    return when {
        methodName == null -> firstOrNull { it.isConstructor() }
        else -> find { it.isFunction() && methodName == it.name }
    } ?: run {
        throw IllegalArgumentException("Can not find ABI definition for $methodName")
    }
}

fun List<EthereumABIElement>.filter(methodName: String? = null): EthereumABIElement {
    return when {
        methodName == null -> firstOrNull { it.isConstructor() }
        else -> find { it.isFunction() && it.name == methodName }
    } ?: run {
        throw IllegalArgumentException("Can not find ABI definition for $methodName")
    }
}

fun EthereumABIElement.isFunction() = type.equals("function", true)
fun EthereumABIElement.isConstructor() = type.equals("constructor", true)

fun AbiDefinition.isFunction() = type.equals("function")
fun AbiDefinition.isConstructor() = type.equals("constructor")

/**
 * 转换参数为zvm的类型
 *
 * @param types 类型定义
 * @param args 合约参数
 * @return 转换后的参数
 */
fun convertArguments(types: List<EthereumNamedType>?, args: Array<Any>): List<Type<*>> {
    return types!!.zip(args).map { (type, arg) ->
        convertArgument(type, arg)
    }
}

fun convertArgument(namedType: EthereumNamedType, arg: Any?): Type<*> {
    val type = namedType.type
    return when {
        type == Types.ADDRESS.value -> {
            val addr =
                arg as? String ?: throw IllegalArgumentException("Invalid argument type, Address needs to be String")
            if (!addr.startsWith("0x")) throw IllegalArgumentException("Invalid argument value, Address needs to be start with 0x")
            return Address(addr)
        }


        type == Types.STRING.value -> Utf8String(
            arg as? String ?: throw IllegalArgumentException("Invalid argument type, String needs to be String")
        )

        SOL_TY_UINT_REGEX.toRegex().matches(type) -> {
            val numStr = arg as? String
                ?: throw IllegalArgumentException("Invalid argument type, Uint needs to be String")

            return UintNumber(numStr.toBigInteger(), uintSize(type))
        }

        SOL_TY_INT_REGEX.toRegex().matches(type) -> {
            val numStr = arg as? String
                ?: throw IllegalArgumentException("Invalid argument type, Uint needs to be String")
            return IntNumber(numStr.toBigInteger(), intSize(type))
        }

        type == Types.BOOL.value -> Bool(
            arg as? Boolean ?: throw IllegalArgumentException("Invalid argument type, Bool needs to be Boolean")
        )

        type == Types.BYTES32.value -> Bytes32(
            Hex.decode(
                (arg as? String
                    ?: throw IllegalArgumentException("Invalid argument type, Bytes32 needs to be Hex String"))
                    .removePrefix("0x")
            )
        )

        type == Types.TUPLE.value -> {
            val array = arg as Array<*>
            val components = namedType.components
                ?: throw IllegalArgumentException("Invalid argument type, Tuple components can not be null")
            val convertedArgs = array.mapIndexed { index, value ->
                convertArgument(components[index], value)
            }.toMutableList()
            DynamicStruct(convertedArgs)
        }

        type.endsWith("[]") -> {
            val array = arg as Array<*>
            when (val childrenType = type.removeSuffix("[]")) {
                Types.TUPLE.value -> {
                    val components = namedType.components
                        ?: throw IllegalArgumentException("Invalid argument type, Tuple components can not be null")
                    val tupleArgs = mutableListOf<DynamicStruct>()
                    for (item in array) {
                        val arr = item as Array<*>
                        val convertedArgs = arr.mapIndexed { index, it ->
                            convertArgument(components[index], it)
                        }.toMutableList()
                        tupleArgs.add(DynamicStruct(convertedArgs))
                    }
                    DynamicArray(tupleArgs)
                }

                else -> DynamicArray(array.map { item ->
                    convertArgument(
                        EthereumNamedType(
                            "",
                            childrenType,
                            childrenType
                        ), item
                    )
                })
            }
        }

        // todo adaptive fixed array
        type.matches(Regex("[a-zA-Z]+[1-9]*\\[\\d+]\$")) -> Utf8String("")

        else -> return Utf8String(arg as String)
    }
}


/// 匹配 solidity 的byte1-byte32类型
const val SOL_TY_BYTES_REGEX = "^(bytes)([1-9]*)$"

/// 匹配 solidity 的uint uint1-uint256类型
const val SOL_TY_UINT_REGEX = "^(uint)([1-9]*)$"

/// 匹配 solidity 的int1-int256类型
const val SOL_TY_INT_REGEX = "^(int)([1-9]*)$"

/// 匹配 solidity 的 array 类型，Example: string[], bool[], bytes32[], uint256[]...
const val SOL_TY_ARRAY_REGEX = "^([a-z1-9]+)(\\[([1-9]*)])$"


fun uintSize(ty: String): Int {
    val pattern = SOL_TY_UINT_REGEX.toRegex()
    return pattern.find(ty)?.groups?.get(2)?.value?.toInt() ?: 0
}

fun intSize(ty: String): Int {
    val pattern = SOL_TY_INT_REGEX.toRegex()
    return pattern.find(ty)?.groups?.get(2)?.value?.toInt() ?: 0
}

