package com.uramnoil.nukkitconsolemanager

interface ApiServer {
    suspend fun sendConsoleLine(line: String)
}