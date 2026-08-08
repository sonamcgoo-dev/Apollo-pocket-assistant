package com.appolopocket.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class ScriptExecutor(private val context: Context) {

    data class ExecutionResult(
        val exitCode: Int,
        val stdout: String,
        val stderr: String,
        val duration: Long
    )

    suspend fun executePython(script: String, timeout: Long = 30000): ExecutionResult {
        val file = File(context.cacheDir, "temp_script_${System.currentTimeMillis()}.py")
        try {
            file.writeText(script)
            return execute("python3 ${file.absolutePath}", timeout)
        } finally {
            file.delete()
        }
    }

    suspend fun executeShell(command: String, timeout: Long = 30000): ExecutionResult {
        return execute("/bin/sh -c '$command'", timeout)
    }

    suspend fun execute(command: String, timeout: Long = 30000): ExecutionResult {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val process = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command))
            
            val stdout = StringBuilder()
            val stderr = StringBuilder()
            
            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))
            
            // Read stdout
            Thread {
                stdoutReader.useLines { lines ->
                    lines.forEach { stdout.appendLine(it) }
                }
            }.start()
            
            // Read stderr
            Thread {
                stderrReader.useLines { lines ->
                    lines.forEach { stderr.appendLine(it) }
                }
            }.start()
            
            val exitCode = process.waitFor(timeout, TimeUnit.MILLISECONDS)
            val duration = System.currentTimeMillis() - startTime
            
            ExecutionResult(
                exitCode = if (exitCode) 0 else -1,
                stdout = stdout.toString().trim(),
                stderr = stderr.toString().trim(),
                duration = duration
            )
        }
    }

    suspend fun executeWithRoot(command: String, timeout: Long = 30000): ExecutionResult {
        return execute("su -c '$command'", timeout)
    }

    suspend fun getSuAvailable(): Boolean {
        return try {
            val result = execute("which su", 5000)
            result.exitCode == 0
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isRooted(): Boolean {
        return try {
            val result = execute("su -c id", 5000)
            result.stdout.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }
}
