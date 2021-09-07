package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class ServerProcessManager(
    private val mutableLineSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val commandFlow: Flow<String>,
    var shouldLoop: Boolean = true,
    parentCoroutineContext: CoroutineContext = Dispatchers.Default,
) : CoroutineScope by CoroutineScope(parentCoroutineContext), Closeable {
    private val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
    }

    private var process: ServerProcess? = null

    private var _isRunning = false
    val isRunning: Boolean
        get() = _isRunning

    private fun runServer() = launch {
        assert(process == null)
        process = ServerProcess(processBuilder, mutableLineSharedFlow, commandFlow)
        process!!.start()
        process!!.await()
        process = null
    }

    fun start() {
        if (isRunning) return
        launch {
            _isRunning = true
            do {
                runServer().join()
            } while (shouldLoop)
            _isRunning = false
        }
    }

    suspend fun await() {
        coroutineContext.job.join()
    }

    override fun close() {
        process?.close()
        coroutineContext.cancel()
    }
}
