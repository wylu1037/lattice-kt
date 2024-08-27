package com.example.abi

import com.example.abi.model.Types
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
import org.web3j.abi.datatypes.generated.*
import org.web3j.protocol.core.methods.response.AbiDefinition
import java.math.BigInteger

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
            val num: BigInteger = when (arg) {
                is String -> BigInteger(arg)
                is Byte, is Short, is Int, is Long -> BigInteger(arg.toString())
                is BigInteger -> arg
                else -> throw IllegalArgumentException("Invalid argument type, Uint only supports String, Byte, Short, Int, Long and BigInteger")
            }

            when (val bitSize = uintSize(type)) {
                8 -> return Uint8(num)
                16 -> return Uint16(num)
                24 -> return Uint24(num)
                32 -> return Uint32(num)
                40 -> return Uint40(num)
                48 -> return Uint48(num)
                56 -> return Uint56(num)
                64 -> return Uint64(num)
                72 -> return Uint72(num)
                80 -> return Uint80(num)
                88 -> return Uint88(num)
                96 -> return Uint96(num)
                104 -> return Uint104(num)
                112 -> return Uint112(num)
                120 -> return Uint120(num)
                128 -> return Uint128(num)
                136 -> return Uint136(num)
                144 -> return Uint144(num)
                152 -> return Uint152(num)
                160 -> return Uint160(num)
                168 -> return Uint168(num)
                176 -> return Uint176(num)
                184 -> return Uint184(num)
                192 -> return Uint192(num)
                200 -> return Uint200(num)
                208 -> return Uint208(num)
                216 -> return Uint216(num)
                224 -> return Uint224(num)
                232 -> return Uint232(num)
                240 -> return Uint240(num)
                248 -> return Uint248(num)
                256 -> return Uint256(num)
                else -> throw IllegalArgumentException("Invalid argument type, Uint bit size not support $bitSize")
            }
            // return UintNumber(numStr.toBigInteger(), uintSize(type))
        }

        SOL_TY_INT_REGEX.toRegex().matches(type) -> {
            val num: BigInteger = when (arg) {
                is String -> BigInteger(arg)
                is Byte, is Short, is Int, is Long -> BigInteger(arg.toString())
                is BigInteger -> arg
                else -> throw IllegalArgumentException("Invalid argument type, Int only supports String, Byte, Short, Int, Long and BigInteger")
            }

            when (val bitSize = intSize(type)) {
                8 -> return Int8(num)
                16 -> return Int16(num)
                24 -> return Int24(num)
                32 -> return Int32(num)
                40 -> return Int40(num)
                48 -> return Int48(num)
                56 -> return Int56(num)
                64 -> return Int64(num)
                72 -> return Int72(num)
                80 -> return Int80(num)
                88 -> return Int88(num)
                96 -> return Int96(num)
                104 -> return Int104(num)
                112 -> return Int112(num)
                120 -> return Int120(num)
                128 -> return Int128(num)
                136 -> return Int136(num)
                144 -> return Int144(num)
                152 -> return Int152(num)
                160 -> return Int160(num)
                168 -> return Int168(num)
                176 -> return Int176(num)
                184 -> return Int184(num)
                192 -> return Int192(num)
                200 -> return Int200(num)
                208 -> return Int208(num)
                216 -> return Int216(num)
                224 -> return Int224(num)
                232 -> return Int232(num)
                240 -> return Int240(num)
                248 -> return Int248(num)
                256 -> return Int256(num)
                else -> throw IllegalArgumentException("Invalid argument type, Int bit size not support $bitSize")
            }
            // return IntNumber(numStr.toBigInteger(), intSize(type))
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

