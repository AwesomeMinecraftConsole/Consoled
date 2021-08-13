package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable

class Console(private val bufferedReader: BufferedReader, private val bufferedWriter: BufferedWriter) : Closeable {
    suspend fun readLine(): String? = withContext(Dispatchers.IO) {
        bufferedReader.readLine()
    }

    fun writeLine(line: String) {
        bufferedWriter.write(line)
        bufferedWriter.appendLine()
        bufferedWriter.flush()
    }

    override fun close() {
        bufferedReader.close()
        bufferedWriter.close()
    }
}