package com.example.abi

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
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
}

/**
 * abi function
 *
 * @property abi 合约 abi
 * @property methodName 方法名
 */
data class Func(val abi: String, val methodName: String? = null) {
    companion object {
        fun convertArgs(args: Array<Any>, typeReferences: List<TypeReference<*>>): List<Type<*>> {
            return args.zip(typeReferences).map { (arg, typeReference) ->
                TypeDecoder.instantiateType(typeReference, arg)
            }
        }
    }
}

/**
 * 编码
 * @param args 参数
 * @return code
 */
fun Func.encode(args: Array<Any>): String {
    val (input, output) = getTypeReferences()
    //val convertedArgs = convertArgs(args, input)
    return FunctionEncoder.encode(Function(methodName, convertArguments(input, args), output))
}

/**
 * 获取 Abi 中方法的输入输出定义
 *
 * @return Pair
 */
fun Func.getTypeReferences(): Pair<List<TypeReference<*>>, List<TypeReference<*>>> {
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

fun AbiDefinition.isFunction() = type.equals("function")
fun AbiDefinition.isConstructor() = type.equals("constructor")


fun convertArguments(typeReferences: List<TypeReference<*>>, args: Array<Any>): List<Type<*>> {
    return typeReferences.zip(args).map { (type, arg) ->
        covertArgument(type, arg)
    }
}

fun covertArgument(typeReference: TypeReference<*>, arg: Any): Type<*> {
    when (typeReference.type) {
        Address::class.java -> return Address(arg as String)
        Utf8String::class.java -> return Utf8String(arg as String)
        Uint::class.java -> return Uint(arg as BigInteger)
        Bool::class.java -> return Bool(arg as Boolean)
    }
    return Utf8String(arg.toString())
}

fun main() {
    val abi =
        "[{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolSuite\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"addProtocol\",\"outputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"}],\"name\":\"getAddress\",\"outputs\":[{\"components\":[{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Protocol[]\",\"name\":\"protocol\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"name\":\"updateProtocol\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"getTraceability\",\"outputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"number\",\"type\":\"uint64\"},{\"internalType\":\"uint64\",\"name\":\"protocol\",\"type\":\"uint64\"},{\"internalType\":\"address\",\"name\":\"updater\",\"type\":\"address\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"}],\"internalType\":\"struct credibilidity.Evidence[]\",\"name\":\"evi\",\"type\":\"tuple[]\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"setDataSecret\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"name\":\"writeTraceability\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"components\":[{\"internalType\":\"uint64\",\"name\":\"protocolUri\",\"type\":\"uint64\"},{\"internalType\":\"string\",\"name\":\"hash\",\"type\":\"string\"},{\"internalType\":\"bytes32[]\",\"name\":\"data\",\"type\":\"bytes32[]\"},{\"internalType\":\"address\",\"name\":\"address\",\"type\":\"address\"}],\"internalType\":\"struct Business.batch[]\",\"name\":\"bt\",\"type\":\"tuple[]\"}],\"name\":\"writeTraceabilityBatch\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    val func = Func(abi, "setDataSecret")
    val args = arrayOf<Any>("10", "561717f7922a233720ae38acaa4174cda0bf1766")


}