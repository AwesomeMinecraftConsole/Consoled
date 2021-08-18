package com.uramnoil.awesome_minecraft_console.consoled

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class ServerProcessManager(
    private val mutableLineSharedFlow: MutableSharedFlow<String> = MutableSharedFlow(),
    private val commandFlow: Flow<String>,
    var shouldLoop: Boolean = true
) : CoroutineScope, Closeable {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Job()

    private val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        redirectErrorStream(true)
    }

    private var process: ServerProcess? = null

    private fun runServer() = launch {
        assert(process == null)
        process = ServerProcess(processBuilder, mutableLineSharedFlow, commandFlow)
        process!!.start()
        process!!.await()
        process = null
    }

    fun start() = launch {
        do {
            runServer().join()
        } while (shouldLoop)
        job.cancel()
    }

    suspend fun await() {
        job.join()
    }

    override fun close() {
        process?.close()
        job.complete()
    }
}
