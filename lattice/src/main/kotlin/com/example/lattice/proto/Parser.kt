package com.example.lattice.proto

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import java.io.InputStream

/**
 * 获取消息的文件描述
 *
 * @param stream proto编译成desc文件后的输入流
 * @return FileDescriptor
 */
fun makeFileDescriptor(stream: InputStream): FileDescriptor {
    val fileDescriptorSet: FileDescriptorSet = FileDescriptorSet.parseFrom(stream)
    val fdp = fileDescriptorSet.getFile(0)
    return FileDescriptor.buildFrom(fdp, arrayOf())
}

/**
 * 序列化消息
 *
 * @param fileDescriptor 文件描述
 * @param json json字符串
 * @return ByteArray, 字节数组
 */
fun marshallMessage(fileDescriptor: FileDescriptor, json: String): ByteArray {
    val messageDescriptor = fileDescriptor.messageTypes[0]
    val messageBuilder = DynamicMessage.newBuilder(messageDescriptor)
    JsonFormat.parser().merge(json, messageBuilder)
    val message = messageBuilder.build()
    return message.toByteArray()
}

/**
 * 反序列化消息
 *
 * @param fileDescriptor 文件描述
 * @param data 字节数组
 * @return String, json字符串
 */
fun unmarshallMessage(fileDescriptor: FileDescriptor, data: ByteArray): String {
    val messageDescriptor = fileDescriptor.messageTypes[0]
    val message = DynamicMessage.parseFrom(messageDescriptor, data)
    val jsonFormat = com.googlecode.protobuf.format.JsonFormat()
    // return JsonFormat.printer().print(message)
    return jsonFormat.printToString(message)
}