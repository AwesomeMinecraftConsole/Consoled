package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class ServerProcess(
    private val builder: ProcessBuilder,
    private val mutableLineSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val commandFlow: Flow<String>,
) : CoroutineScope, Closeable {
    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext
        get() = job + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
        }

    lateinit var process: Process

    private val console: Console by lazy {
        val write = process.outputStream.bufferedWriter()
        write.appendLine()
        Console(process.inputStream.bufferedReader(), process.outputStream.bufferedWriter())
    }

    fun start() {
        process = builder.start()
        launch {
            commandFlow.collect {
                writeLine(it)
            }
        }
        startReceiveEachLines(console)
    }

    private fun startReceiveEachLines(console: Console) = launch {
        while (true) {
            val message = console.readLine() ?: break
            mutableLineSharedFlow.emit(message)
        }
    }

    fun writeLine(line: String) {
        console.writeLine(line)
    }

    private suspend fun Process.await() = withContext(Dispatchers.Default) {
        waitFor()
    }

    private fun stop() {
        job.cancel()
    }

    override fun close() {
        console.close()
        job.complete()
        process.apply {
            inputStream.close()
            outputStream.close()
            errorStream.close()
        }
        process.destroy()
    }

    suspend fun await() {
        process.await()
        job.join()
    }
}