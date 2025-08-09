package io.github.diskria.dsl.shell

import io.github.diskria.utils.kotlin.Constants
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
