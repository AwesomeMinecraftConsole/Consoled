package com.uramnoil.awesome_minecraft_console.consoled

import io.grpc.ManagedChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import java.io.Closeable

class Consoled(channel: ManagedChannel) : Closeable {
    val mutableSharedCommandFlow = MutableSharedFlow<String>()
    val mutableSharedLineFlow = MutableSharedFlow<String>()
    val mutableOperationFlow = MutableSharedFlow<Operation>()
    val mutableNotificationFlow = MutableSharedFlow<String>()

    private val serverProcessManager: ServerProcessManager =
        ServerProcessManager(mutableSharedLineFlow, mutableSharedCommandFlow)

    private val octopassClient = OctopassClientImpl(
        channel,
        mutableSharedLineFlow,
        mutableSharedCommandFlow,
        mutableNotificationFlow,
        mutableOperationFlow
    )

    fun start() {
        octopassClient.start()
        serverProcessManager.start()
    }

    suspend fun await() {
        serverProcessManager.await()
    }

    override fun close() {
        serverProcessManager.close()
        octopassClient.close()
    }
}