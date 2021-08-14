package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.Closeable
import kotlin.coroutines.CoroutineContext

class Consoled : Closeable, CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + serverProcessManager.coroutineContext

    private val mutableSharedCommandFlow = MutableSharedFlow<String>()
    private val mutableSharedLineFlow = MutableSharedFlow<String>()
    private val mutableOperationFlow = MutableSharedFlow<Operation>()
    private val mutableNotificationFlow = MutableSharedFlow<String>()

    private val serverProcessManager: ServerProcessManager =
        ServerProcessManager(mutableSharedLineFlow, mutableSharedCommandFlow)

    private val octopassClient = OctopassClientImpl(
        ManagedChannelBuilder.forAddress("127.0.0.1", 50052).usePlaintext().build(),
        mutableSharedLineFlow,
        mutableSharedCommandFlow,
        mutableNotificationFlow,
        mutableOperationFlow
    )

    fun start() {
        octopassClient.start()
        serverProcessManager.start()
        launch {
            mutableSharedLineFlow.collect {
                println(it)
            }
        }
    }

    suspend fun await() {
        serverProcessManager.await()
    }

    override fun close() {
        serverProcessManager.close()
        octopassClient.close()
    }
}