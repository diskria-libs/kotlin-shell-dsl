package io.github.diskria.kotlin.shell.dsl

import io.github.diskria.kotlin.utils.Constants
import java.io.File

open class VCSShell protected constructor(
    private val name: String,
    projectDirectory: File
) : Shell(projectDirectory) {

    protected fun executeAndGetOutput(command: String): String =
        runAndGetOutput(name + Constants.Char.SPACE + command)

    protected fun execute(command: String): Boolean =
        run(name + Constants.Char.SPACE + command)
}
