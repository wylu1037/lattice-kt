package com.example.abi

import com.example.abi.model.FixedArray
import com.example.abi.model.Types
import com.example.model.RegExpr.ADDRESS
import com.example.model.RegExpr.ETHEREUM_ADDRESS
import com.example.model.toEthereumAddress
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
            return when {
                ADDRESS.toRegex().matches(addr) -> Address(com.example.model.Address(addr).toEthereumAddress())
                ETHEREUM_ADDRESS.toRegex().matches(addr) -> Address(addr)
                else -> throw IllegalArgumentException("Invalid argument value, invalid Address format")
            }
        }


        type == Types.STRING.value -> Utf8String(
            arg as? String ?: throw IllegalArgumentException("Invalid argument type, String needs to be String")
        )

        type.matches(Regex(SOL_TY_UINT_REGEX)) -> {
            val num: BigInteger = when (arg) {
                is String -> BigInteger(arg)
                is Byte, is Short, is Int, is Long -> BigInteger(arg.toString())
                is BigInteger -> arg
                else -> throw IllegalArgumentException("Invalid argument type, Uint only supports String, Byte, Short, Int, Long and BigInteger")
            }

            return when (val bitSize = uintSize(type)) {
                8 -> Uint8(num)
                16 -> Uint16(num)
                24 -> Uint24(num)
                32 -> Uint32(num)
                40 -> Uint40(num)
                48 -> Uint48(num)
                56 -> Uint56(num)
                64 -> Uint64(num)
                72 -> Uint72(num)
                80 -> Uint80(num)
                88 -> Uint88(num)
                96 -> Uint96(num)
                104 -> Uint104(num)
                112 -> Uint112(num)
                120 -> Uint120(num)
                128 -> Uint128(num)
                136 -> Uint136(num)
                144 -> Uint144(num)
                152 -> Uint152(num)
                160 -> Uint160(num)
                168 -> Uint168(num)
                176 -> Uint176(num)
                184 -> Uint184(num)
                192 -> Uint192(num)
                200 -> Uint200(num)
                208 -> Uint208(num)
                216 -> Uint216(num)
                224 -> Uint224(num)
                232 -> Uint232(num)
                240 -> Uint240(num)
                248 -> Uint248(num)
                256 -> Uint256(num)
                else -> throw IllegalArgumentException("Invalid argument type, Uint bit size not support $bitSize")
            }
            // return UintNumber(numStr.toBigInteger(), uintSize(type))
        }

        type.matches(Regex(SOL_TY_INT_REGEX)) -> {
            val num: BigInteger = when (arg) {
                is String -> BigInteger(arg)
                is Byte, is Short, is Int, is Long -> BigInteger(arg.toString())
                is BigInteger -> arg
                else -> throw IllegalArgumentException("Invalid argument type, Int only supports String, Byte, Short, Int, Long and BigInteger")
            }

            return when (val bitSize = intSize(type)) {
                8 -> Int8(num)
                16 -> Int16(num)
                24 -> Int24(num)
                32 -> Int32(num)
                40 -> Int40(num)
                48 -> Int48(num)
                56 -> Int56(num)
                64 -> Int64(num)
                72 -> Int72(num)
                80 -> Int80(num)
                88 -> Int88(num)
                96 -> Int96(num)
                104 -> Int104(num)
                112 -> Int112(num)
                120 -> Int120(num)
                128 -> Int128(num)
                136 -> Int136(num)
                144 -> Int144(num)
                152 -> Int152(num)
                160 -> Int160(num)
                168 -> Int168(num)
                176 -> Int176(num)
                184 -> Int184(num)
                192 -> Int192(num)
                200 -> Int200(num)
                208 -> Int208(num)
                216 -> Int216(num)
                224 -> Int224(num)
                232 -> Int232(num)
                240 -> Int240(num)
                248 -> Int248(num)
                256 -> Int256(num)
                else -> throw IllegalArgumentException("Invalid argument type, Int bit size not support $bitSize")
            }
            // return IntNumber(numStr.toBigInteger(), intSize(type))
        }

        type == Types.BOOL.value -> {
            return when (arg) {
                is Boolean -> Bool(arg)
                is String -> Bool(arg.toBoolean())
                is Byte -> Bool(arg == 1.toByte())
                else -> throw IllegalArgumentException("Invalid argument type, Bool only supports Boolean, String and Byte")
            }
        }

        // bytes1 ~ bytes32
        type.matches(Regex(SOL_TY_BYTES_REGEX)) -> {
            val bytes = Hex.decode(
                (arg as? String
                    ?: throw IllegalArgumentException("Invalid argument type, $type needs to be Hex String"))
                    .removePrefix("0x")
            )
            val size = bytesSize(type)
            if (bytes.size != size) {
                throw IllegalArgumentException("Invalid argument type, $type expect $size byte, but $arg is ${bytes.size}")
            }
            return when (size) {
                1 -> Bytes1(bytes)
                2 -> Bytes2(bytes)
                3 -> Bytes3(bytes)
                4 -> Bytes4(bytes)
                5 -> Bytes5(bytes)
                6 -> Bytes6(bytes)
                7 -> Bytes7(bytes)
                8 -> Bytes8(bytes)
                9 -> Bytes9(bytes)
                10 -> Bytes10(bytes)
                11 -> Bytes11(bytes)
                12 -> Bytes12(bytes)
                13 -> Bytes13(bytes)
                14 -> Bytes14(bytes)
                15 -> Bytes15(bytes)
                16 -> Bytes16(bytes)
                17 -> Bytes17(bytes)
                18 -> Bytes18(bytes)
                19 -> Bytes19(bytes)
                20 -> Bytes20(bytes)
                21 -> Bytes21(bytes)
                22 -> Bytes22(bytes)
                23 -> Bytes23(bytes)
                24 -> Bytes24(bytes)
                25 -> Bytes25(bytes)
                26 -> Bytes26(bytes)
                27 -> Bytes27(bytes)
                28 -> Bytes28(bytes)
                29 -> Bytes29(bytes)
                30 -> Bytes30(bytes)
                31 -> Bytes31(bytes)
                32 -> Bytes32(bytes)
                else -> throw IllegalArgumentException("Invalid argument type, Bytes${bytes.size} not support")
            }
        }

        type == Types.TUPLE.value -> {
            val array = arg as Array<*>
            val components = namedType.components
                ?: throw IllegalArgumentException("Invalid argument type, Tuple components can not be null")
            val convertedArgs = array.mapIndexed { index, value ->
                convertArgument(components[index], value)
            }.toMutableList()
            DynamicStruct(convertedArgs)
        }

        // 动态数组：dynamic array
        type.endsWith("[]") -> {
            val array = arg as Array<*>
            return when (val childrenType = type.removeSuffix("[]")) {
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
                        EthereumNamedType("", childrenType, childrenType), item
                    )
                })
            }
        }

        // fixme web3j have a bug with static array
        // 静态数组：fixed array
        type.matches(Regex(SOL_FIXED_ARRAY_REGEX)) -> {
            val array = arg as Array<*>
            val ty = fixedArrayType(type)
            val size = fixedArraySize(type)
            val convertedArgs = FixedArray(
                size,
                array.map { item ->
                    convertArgument(EthereumNamedType("", ty, ty), item)
                }
            )
            return convertedArgs
        }

        else -> return Utf8String(arg as String)
    }
}

/// 匹配 solidity 的byte1-byte32类型
const val SOL_TY_BYTES_REGEX = "^(bytes)([1-9]*)$"

/// 匹配 solidity 的uint uint1-uint256类型
const val SOL_TY_UINT_REGEX = "^(uint)([1-9]*)$"

/// 匹配 solidity 的int1-int256类型
const val SOL_TY_INT_REGEX = "^(int)([1-9]*)$"

/// 匹配 solidity 的固定数组
const val SOL_FIXED_ARRAY_REGEX = "([a-zA-Z]+[1-9]*)\\[(\\d+)]$"

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

// 获取bytes的长度
fun bytesSize(ty: String): Int {
    val pattern = SOL_TY_BYTES_REGEX.toRegex()
    return pattern.find(ty)?.groups?.get(2)?.value?.toInt() ?: 0
}

fun fixedArrayType(ty: String): String {
    val pattern = SOL_FIXED_ARRAY_REGEX.toRegex()
    val groups = pattern.find(ty)?.groups
    return pattern.find(ty)?.groups?.get(1)?.value ?: ""
}

fun fixedArraySize(ty: String): Int {
    val pattern = SOL_FIXED_ARRAY_REGEX.toRegex()
    val groups = pattern.find(ty)?.groups
    return pattern.find(ty)?.groups?.get(2)?.value?.toInt() ?: 0
}