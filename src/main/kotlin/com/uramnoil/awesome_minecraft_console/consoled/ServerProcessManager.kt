package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class ServerProcessManager(
    private val mutableLineSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val commandFlow: Flow<String>,
) : CoroutineScope, Closeable {
    var shouldLoop = true

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Job()

    private val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
        redirectOutput(ProcessBuilder.Redirect.INHERIT)
    }

    var _process: ServerProcess? = null

    private fun runServer() = launch {
        assert(_process == null)
        _process = ServerProcess(processBuilder, mutableLineSharedFlow, commandFlow)
        _process!!.start()
        _process!!.await()
        _process = null
    }

    fun start() = launch {
        do {
            runServer().join()
            delay(3000)
        } while (shouldLoop)
    }

    suspend fun await() {
        job.join()
    }

    fun startRedirectConsoleInput() = launch {
        val inputBufferedReader = System.`in`.bufferedReader()
        while (true) {
            val message = withContext(Dispatchers.IO) {
                inputBufferedReader.readLine()
            } ?: continue
            _process?.writeLine(message)
        }
    }

    override fun close() {
        _process?.close()
        job.complete()
    }
}