package com.allever.video.editor.function.save


import com.allever.video.editor.utils.SystemUtils.log
import com.android.absbase.helper.log.DLog
import com.android.absbase.utils.DebugUtil
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * 执行shell脚本工具类
 *
 */
object CommandHelper {

    val TAG = CommandHelper::class.java.name
    val DEBUG = DebugUtil.isDebuggable()

    val COMMAND_SU = "su"
    val COMMAND_SH = "sh"
    val COMMAND_EXIT = "exit\n"
    val COMMAND_LINE_END = "\n"

    private var mProcess: Process? = null

    const val RESULT_ID_FALID = -1
    const val RESULT_ID_CANCEL = -2
    const val RESULT_ID_SUCCESS = 0
    /**
     * Command执行结果
     *
     */
    data class Result(var result: Int = RESULT_ID_FALID, var msgError: String? = null, var msgSuccess: String? = null)

    /**
     * 执行命令—单条
     * @param command
     * @param isRoot
     * @return
     */
    fun execCommand(command: String, isRoot: Boolean): Result {
        val commands = arrayOf(command)
        return execCommand(commands, isRoot)
    }

    /**
     * 执行命令-多条
     * @param commands
     * @param isRoot
     * @return
     */
    fun execCommand(commands: Array<String>?, isRoot: Boolean): Result {
        val commandResult = Result()
        if (commands == null || commands.size == 0) {
            commandResult.msgError = "params is null"
            return commandResult
        }

        var os: DataOutputStream? = null
        var successResult: BufferedReader? = null
        var errorResult: BufferedReader? = null
        var successMsg: StringBuilder? = null
        var errorMsg: StringBuilder? = null
        try {
            if (commands.size > 1) {
                mProcess = Runtime.getRuntime().exec(if (isRoot) COMMAND_SU else COMMAND_SH)
                mProcess?.let { os = DataOutputStream(it.outputStream) }
                for (command in commands) {
                    if (command != null) {
                        os?.write(command.toByteArray())
                        os?.writeBytes(COMMAND_LINE_END)
                        os?.flush()
                    }
                }
                os?.writeBytes(COMMAND_EXIT)
                os?.flush()
            } else {
                if (DEBUG) {
                    DLog.i(TAG, "run: ${commands[0]}")
                }
                mProcess = Runtime.getRuntime().exec(commands[0])
            }
            mProcess?.let { commandResult.result = it.waitFor() }
            //获取错误信息
            successMsg = StringBuilder()
            errorMsg = StringBuilder()
            mProcess?.let {
                successResult = BufferedReader(InputStreamReader(it.inputStream))
                errorResult = BufferedReader(InputStreamReader(it.errorStream))
            }
            var s: String = ""
            while ((successResult?.readLine()?.also { s = it }) != null) successMsg.append(s)
            while ((errorResult?.readLine()?.also { s = it }) != null) errorMsg.append(s)
            commandResult.msgSuccess = successMsg.toString()
            commandResult.msgError = errorMsg.toString()

            if (DEBUG) {
                DLog.i(TAG, commandResult.result.toString() + " | " + commandResult.msgSuccess
                        + " | " + commandResult.msgError)
            }
        } catch (e: IOException) {
            DLog.printStackTrace(e)
            commandResult.msgError = e?.message ?: "unknown"
        } catch (e: Exception) {
            DLog.printStackTrace(e)
            commandResult.msgError = e?.message ?: "unknown"
        } finally {
            try {
                os?.close()
                successResult?.close()
                errorResult?.close()
            } catch (e: IOException) {
                DLog.printStackTrace(e)
            }
            mProcess?.destroy()
        }
        return commandResult
    }

    fun stopExecute() {
        try {
            mProcess?.destroy()
            mProcess = null
        }catch (e: Exception){
        }
    }


    class CommandBuilder private constructor(cmd: String?) {
        private var firstCmd: String? = null

        var params = LinkedList<String>()
            private set
        private var root = false

        init {
            if (cmd != null) {
                firstCmd = cmd
                params.add(cmd)
            }
        }

        fun clear() {
            params.clear()
            if (firstCmd != null) {
                params.add(firstCmd!!)
            }
        }

        fun add(param: String): CommandBuilder {
            params.add(param)
            return this
        }

        fun add(param1: String, param2: String): CommandBuilder {
            params.add(param1)
            params.add(param2)
            return this
        }

        fun add(vararg params: String): CommandBuilder {
            for (param in params) {
                this.params.add(param)
            }
            return this
        }

        fun add(vararg params: Any): CommandBuilder {
            for (param in params) {
                this.params.add(param.toString())
            }
            return this
        }

        fun isRoot(root: Boolean = true): CommandBuilder {
            this.root = root
            return this
        }

        fun run(executing: Boolean): Result {
            return if (executing) {
                val command = getCommand()
                log("FFMPEG 命令： $command")
                val result = execCommand(command, this.root)
                result
            } else {
                Result(result =  CommandHelper.RESULT_ID_CANCEL)
            }
        }

        fun getCommand(): String {
            return when {
                params.size > 1 -> params.joinToString(separator = " ")
                params.size == 1 -> params[0]
                else -> ""
            }
        }

        companion object {
            fun create(cmd: String? = null): CommandBuilder {
                return CommandBuilder(cmd)
            }
        }
    }

}
