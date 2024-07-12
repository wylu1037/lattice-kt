package com.example.lattice.proto

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import java.io.InputStream

fun makeFileDescriptor(stream: InputStream): FileDescriptor {
    val fileDescriptorSet: FileDescriptorSet = FileDescriptorSet.parseFrom(stream)
    val fdp = fileDescriptorSet.getFile(0)
    return FileDescriptor.buildFrom(fdp, arrayOf())
}

fun marshallMessage(fileDescriptor: FileDescriptor, json: String): ByteArray {
    val messageDescriptor = fileDescriptor.messageTypes[0]
    val messageBuilder = DynamicMessage.newBuilder(messageDescriptor)
    JsonFormat.parser().merge(json, messageBuilder)
    val message = messageBuilder.build()
    return message.toByteArray()
}

fun unmarshallMessage(fileDescriptor: FileDescriptor, data: ByteArray): String {
    val messageDescriptor = fileDescriptor.messageTypes[0]
    val message = DynamicMessage.parseFrom(messageDescriptor, data)
    val jsonFormat = com.googlecode.protobuf.format.JsonFormat()
    // return JsonFormat.printer().print(message)
    return jsonFormat.printToString(message)
}