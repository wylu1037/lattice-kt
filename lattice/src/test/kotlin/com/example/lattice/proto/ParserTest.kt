package com.example.lattice.proto

import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ParserTest {

    @Test
    fun `marshall and unmarshall`() {
        val desc = Compiler.compile(
            "student",
            "syntax = \"proto3\";\n\nmessage Student {\n\tint64 id = 1;\n\tstring name = 2;\n" +
                    "\tint32 grade = 3;\n}"
        )
        val fileDescriptor = makeFileDescriptor(Files.newInputStream(Path.of(desc)))
        val json = "{\"id\": 1,\"name\": \"雷军\",\"grade\": 1}"
        val data = marshallMessage(fileDescriptor, json)
        assertContentEquals(byteArrayOf(8, 1, 18, 6, -23, -101, -73, -27, -122, -101, 24, 1), data)
        val rawJson = unmarshallMessage(fileDescriptor, data)
        assertEquals(rawJson, json)
        Files.deleteIfExists(Path.of(desc))
    }
}

fun main() {
    val proto = "syntax = \"proto3\";\n\nmessage Student {\n\tint64 id = 1;\n\tstring name = 2;\n" +
            "\tint32 grade = 3;\n}"
    val protoName = proto.substring(proto.indexOf("message ") + 8, proto.indexOf(" {"))
    println("protoName: {$protoName}_1")
}