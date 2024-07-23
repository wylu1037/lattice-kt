package com.example.lattice.proto

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.writeText

object Compiler {

    private const val PROTO_SUFFIX = ".proto"
    private const val DESC_SUFFIX = ".desc"

    private fun getCommand(out: String, source: String, filename: String): String {
        val path = "protoc --descriptor_set_out=$out --proto_path=$source $source${File.separator}$filename"
        return path
    }

    private fun createProtoSource(filename: String, proto: String): String {
        val dir = Files.createTempDirectory("")
        val file = Files.createFile(Paths.get(dir.toAbsolutePath().toString(), "$filename$PROTO_SUFFIX"))
        file.writeText(proto)
        return dir.toAbsolutePath().toString()
    }

    private fun createDescOut(filename: String): String {
        val dir = Files.createTempDirectory("")
        return "${dir.toAbsolutePath()}${File.separator}$filename$DESC_SUFFIX"
    }

    /**
     * 编译proto文件为desc文件
     *
     * @param filename 文件名，示例：Student
     * @param proto proto文件内容，示例："syntax = \"proto3\";\n\nmessage Student {\n\tstring id = 1;\n\tstring name = 2;\n}"
     * @return String, desc文件的路径
     */
    fun compile(filename: String, proto: String): String {
        val out = createDescOut(filename)
        val source = createProtoSource(filename, proto)
        try {
            val runtime = Runtime.getRuntime()
            val process = runtime.exec(getCommand(out, source, "$filename$PROTO_SUFFIX"))
            val code = process.waitFor()
            process.destroy()
            if (code == 0) {
                return out
            } else {
                Files.deleteIfExists(Path.of(out))
                throw RuntimeException("Error compiling $filename")
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            Files.deleteIfExists(Path.of("$source${File.separator}$filename$PROTO_SUFFIX"))
        }
    }
}