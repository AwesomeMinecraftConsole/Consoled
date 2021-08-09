package com.uramnoil.nukkitconsolemanager

import kotlinx.coroutines.coroutineScope
import java.io.File

suspend fun main(): Unit = coroutineScope {
    val processBuilder = ProcessBuilder().apply {
        command("sh", "start.sh")
        directory(File("/Users/uramnoil/IdeaProjects/NukkitConsoleManager/nukkit/"))
        redirectOutput(File("/Users/uramnoil/IdeaProjects/NukkitConsoleManager/nukkit/log.txt"))
        // HACK inheritIO()を設定すると"Disabling terminal, you're running in an unsupported environment"が出てプロセスが終了する
        // inheritIO()
        redirectInput(ProcessBuilder.Redirect.INHERIT)
    }
    val server = Nukkit(processBuilder) {
        // HACK 上同 コンソール表示用
        println(it)
    }
    server.start()
    server.join()
}