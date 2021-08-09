package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.*
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class Server(private val builder: ProcessBuilder, private val onReceiveLine: suspend (line: String) -> Unit = {}) : CoroutineScope, Closeable {
    private val job = SupervisorJob()

    private var _joinableChildren = listOf<Job>()

    val joinableChild: List<Job>
        get() = _joinableChildren

    override val coroutineContext: CoroutineContext
        get() = job + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
            close()
        }

    lateinit var process: Process

    private val console: Console by lazy {
        val write = process.outputStream.bufferedWriter()
        write.appendLine()
        Console(process.inputStream.bufferedReader(), process.outputStream.bufferedWriter())
    }

    fun start() {
        process = builder.start()
        _joinableChildren = listOf(startReceiveEachLines(console), waitForProcess())
        startRedirectConsoleInput()
    }

    private fun startReceiveEachLines(console: Console) = launch {
        while (true) {
            val message = console.readLine() ?: break
            onReceiveLine(message)
        }
    }

    private fun startRedirectConsoleInput() = launch {
        val inputBufferedReader = System.`in`.bufferedReader()
        while (true) {
            val message = withContext(Dispatchers.IO) {
                inputBufferedReader.readLine()
            } ?: continue
            writeLine(message)
        }
    }

    fun writeLine(line: String) {
        console.writeLine(line)
    }

    fun shutdown() {
        writeLine("stop")
    }

    fun waitForProcess() = launch {
        withContext(Dispatchers.Default) {
            process.waitFor()
        }
    }

    private fun stop() {
        job.cancel()
    }

    override fun close() {
        console.close()
        process.apply {
            inputStream.close()
            outputStream.close()
            errorStream.close()
        }
        process.destroy()
    }

    suspend fun join() {
        _joinableChildren.joinAll()
    }
}