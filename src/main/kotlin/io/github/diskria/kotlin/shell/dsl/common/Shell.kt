package io.github.diskria.kotlin.shell.dsl.common

import io.github.diskria.kotlin.regex.dsl.combinators.RegexBetween
import io.github.diskria.kotlin.regex.dsl.combinators.RegexOr
import io.github.diskria.kotlin.regex.dsl.extensions.findAll
import io.github.diskria.kotlin.regex.dsl.primitives.RegexCharacterClass
import io.github.diskria.kotlin.regex.dsl.primitives.RegexWhitespace
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.generics.modifyFirst
import java.io.File
import java.util.logging.Logger

open class Shell protected constructor(private var workingDirectory: File) {

    private val logger: Logger = Logger.getLogger(Shell::class.java.name)
    private var previousDirectory: File? = null

    fun cd(directory: File): Shell {
        logger.info("Changing directory: ${workingDirectory.absolutePath} -> ${directory.absolutePath}")
        previousDirectory = workingDirectory
        workingDirectory = directory
        return this
    }

    fun undoCd(): Shell {
        previousDirectory?.let {
            logger.info("Reverting directory: ${workingDirectory.absolutePath} -> ${it.absolutePath}")
            workingDirectory = it
            previousDirectory = null
        }
        return this
    }

    fun pwd(): File =
        workingDirectory

    fun run(command: String): Boolean =
        runAndGetExitCode(command) == SUCCESS_EXIT_CODE

    fun runAndGetExitCode(command: String): Int {
        val process = startProcess(command)
        val exitCode = process.waitFor()
        val output = process.inputStream.bufferedReader().readText().trim()
        logger.info("Command finished with exitCode=$exitCode, output:\n$output")
        return exitCode
    }

    fun runAndGetOutput(command: String): String {
        val process = startProcess(command)
        val output = process.inputStream.bufferedReader().readText().trim()
        val exitCode = process.waitFor()
        logger.info("Command output captured (exitCode=$exitCode):\n$output")
        return output
    }

    private fun startProcess(command: String): Process {
        val arguments = splitToArguments(command).toMutableList()
        val executable = arguments.firstOrNull() ?: failWithInvalidValue(command)

        logger.info("Executing command: ${command.wrapWithDoubleQuote()}")
        logger.info("Parsed arguments: $arguments")

        if (workingDirectory.resolve(executable).asFileOrNull()?.canExecute() == true) {
            arguments.modifyFirst { Constants.File.Path.CURRENT_DIRECTORY + it }
        }

        val builder = ProcessBuilder(arguments).directory(workingDirectory).redirectErrorStream(true)
        try {
            val process = builder.start()
            logger.info("Process started in directory: ${workingDirectory.absolutePath}")
            return process
        } catch (exception: Exception) {
            logger.severe("Failed to start process: ${exception.message}")
            throw exception
        }
    }

    private fun splitToArguments(command: String): List<String> =
        command.findAll(argumentRegex).map { (argument) ->
            argument.trim(Constants.Char.DOUBLE_QUOTE, Constants.Char.SINGLE_QUOTE)
        }.toList()

    companion object {
        private const val SUCCESS_EXIT_CODE: Int = 0
        private const val ERROR_EXIT_CODE: Int = 1

        private val argumentRegex: Regex by lazy {
            RegexOr.of(
                RegexCharacterClass.ofNegated(
                    listOf(RegexWhitespace),
                    Constants.Char.DOUBLE_QUOTE, Constants.Char.SINGLE_QUOTE
                ).oneOrMore(),
                RegexBetween.of(Constants.Char.DOUBLE_QUOTE),
                RegexBetween.of(Constants.Char.SINGLE_QUOTE),
            ).oneOrMore().toRegex()
        }

        fun open(workingDirectoryPath: String = Constants.File.Path.CURRENT_DIRECTORY): Shell =
            Shell(workingDirectoryPath.toFile().asFile())

        fun open(workingDirectory: File): Shell =
            Shell(workingDirectory.asDirectory())
    }
}
