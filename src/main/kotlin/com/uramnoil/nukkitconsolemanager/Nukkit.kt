package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Nukkit(private val builder: ProcessBuilder, private val onReceiveLine: (line: String) -> Unit = {}) : CoroutineScope {
    private val job = SupervisorJob()
    private var childJobs = listOf<Job>()
    override val coroutineContext: CoroutineContext
        get() = job + CoroutineExceptionHandler { coroutineContext, throwable ->
            throwable.printStackTrace()
            close()
        }

    lateinit var process: Process

    private val console: Console by lazy {
        Console(process.inputStream.bufferedReader(), process.outputStream.bufferedWriter())
    }

    fun start() {
        process = builder.start()
        childJobs = listOf(startReceiveEachLines(console), waitForProcess())
    }

    private fun startReceiveEachLines(console: Console) = launch {
        withContext(Dispatchers.IO) {
            while (true) {
                val message = console.readLine()

                if (message == null) {
                    close()
                    break
                }

                onReceiveLine(message)
            }
        }
        println("hoge")
    }

    suspend fun shutdown() {
        console.writeLine("stop")
    }

    fun waitForProcess() = launch {
        withContext(Dispatchers.Default) {
            val hoge = process.waitFor()
            println(hoge)
        }
    }

    private fun close() {
        job.cancel()
        console.close()
    }

    suspend fun join() {
        childJobs.joinAll()
    }
}