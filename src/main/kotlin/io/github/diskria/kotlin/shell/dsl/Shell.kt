package io.github.diskria.kotlin.shell.dsl

import io.github.diskria.regex.dsl.combinators.RegexBetween
import io.github.diskria.regex.dsl.combinators.RegexOr
import io.github.diskria.regex.dsl.extensions.findAll
import io.github.diskria.regex.dsl.primitives.RegexCharacterClass
import io.github.diskria.regex.dsl.primitives.RegexWhitespace
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.*
import io.github.diskria.kotlin.utils.extensions.common.failWithInvalidValue
import io.github.diskria.kotlin.utils.extensions.generics.modifyFirst
import java.io.File

open class Shell protected constructor(private var workingDirectory: File) {

    private var previousDirectory: File? = null

    fun cd(directory: File): Shell {
        previousDirectory = workingDirectory
        workingDirectory = directory
        return this
    }

    fun undoCd(): Shell {
        previousDirectory?.let {
            workingDirectory = it
            previousDirectory = null
        }
        return this
    }

    fun pwd(): File =
        workingDirectory

    fun run(command: String): Boolean =
        runAndGetExitCode(command) == SUCCESS_EXIT_CODE

    fun runAndGetExitCode(command: String): Int =
        startProcess(command).waitFor()

    fun runAndGetOutput(command: String): String =
        startProcess(command).inputStream.readText().trim()

    private fun startProcess(command: String): Process {
        val arguments = splitToArguments(command).toMutableList()
        val executable = arguments.firstOrNull() ?: failWithInvalidValue(command)

        if (workingDirectory.resolve(executable).asFileOrNull()?.canExecute() == true) {
            arguments.modifyFirst { Constants.File.Path.CURRENT_DIRECTORY + it }
        }

        return ProcessBuilder(arguments)
            .directory(workingDirectory)
            .redirectErrorStream(true)
            .start()
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
