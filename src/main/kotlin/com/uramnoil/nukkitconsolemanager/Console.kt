package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter

class Console(private val bufferedReader: BufferedReader, private val bufferedWriter: BufferedWriter) {
    suspend fun readLine(): String? = withContext(Dispatchers.IO) {
        bufferedReader.readLine()
    }

    suspend fun writeLine(line: String) = withContext(Dispatchers.IO) {
        bufferedWriter.write(line)
        bufferedWriter.flush()
    }

    fun close() {
        bufferedReader.close()
        bufferedWriter.close()
    }
}