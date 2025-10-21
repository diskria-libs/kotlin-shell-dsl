package io.github.diskria.kotlin.shell.dsl.git

import io.github.diskria.kotlin.shell.dsl.common.Shell
import io.github.diskria.kotlin.shell.dsl.git.commits.CommitMessage
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.asDirectory
import io.github.diskria.kotlin.utils.extensions.generics.joinBySpace
import io.github.diskria.kotlin.utils.extensions.toFile
import io.github.diskria.kotlin.utils.extensions.wrapWithDoubleQuote
import java.io.File

class GitShell private constructor(projectDirectory: File) : Shell(projectDirectory) {

    fun configureUser(name: String, email: String, isGlobal: Boolean = false) {
        configure("user.name", name, isGlobal)
        configure("user.email", email, isGlobal)
    }

    fun clone(remoteUrl: String, targetDirectory: File) {
        execute("clone $remoteUrl $targetDirectory")
    }

    fun isInvalidRepository(): Boolean =
        execute("fsck --no-progress --no-dangling").not()

    fun fetch(remoteName: String = ORIGIN_REMOTE_NAME) {
        execute("fetch $remoteName")
    }

    fun checkout(branchName: String) {
        execute("checkout $branchName")
    }

    fun pull(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String) {
        execute("pull $remoteName $branchName")
    }

    fun stage(vararg paths: String): Boolean =
        execute("add ${paths.asIterable().joinBySpace()}")

    fun commit(message: CommitMessage, isAmend: Boolean = false): Boolean =
        execute(
            buildString {
                append("commit ")
                if (isAmend) {
                    append("--amend ")
                }
                append("-m ${message.build().wrapWithDoubleQuote()}")
            }
        )

    fun push(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String = HEAD): Boolean =
        execute("push $remoteName $branchName")

    fun getRemoteNames(): List<String> =
        executeAndGetOutput("remote").lines().filter { it.isNotBlank() }

    fun getRemoteUrl(remoteName: String = ORIGIN_REMOTE_NAME): String =
        executeAndGetOutput("remote get-url $remoteName")

    fun setRemoteUrl(remoteName: String = ORIGIN_REMOTE_NAME, remoteUrl: String) {
        execute("remote set-url $remoteName $remoteUrl")
    }

    fun addRemote(remoteName: String = ORIGIN_REMOTE_NAME, remoteUrl: String) {
        execute("remote add $remoteName $remoteUrl")
    }

    fun removeRemote(remoteName: String = ORIGIN_REMOTE_NAME) {
        execute("remote remove $remoteName")
    }

    fun getAheadCount(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String): Int =
        executeAndGetOutput("rev-list HEAD..$remoteName/$branchName --count").toInt()

    fun hasUncommitedChanges(): Boolean =
        execute("diff --quiet").not() || execute("diff --cached --quiet").not()

    fun merge(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String): Boolean =
        execute("merge $remoteName $branchName")

    fun getTotalCommitsCount(): Int =
        executeAndGetOutput("rev-list --count HEAD").toInt()

    fun getHooksDirectory(): File =
        executeAndGetOutput("rev-parse --git-path hooks").toFile()

    fun updateSubmodules(shouldReinit: Boolean = false, allowRecursive: Boolean = false) {
        if (shouldReinit) {
            execute("submodule deinit --all --force")
        }
        execute(
            buildString {
                append("submodule update --init")
                if (allowRecursive) {
                    append(" --recursive")
                }
            }
        )
    }

    fun addSubmodule(path: String, url: String) {
        execute("submodule add -f $url $path")
    }

    fun hardResetToRemoteCommit(remoteName: String = ORIGIN_REMOTE_NAME, commitSha: String) {
        execute("reset --hard $commitSha")
        execute("clean -fd")
    }

    fun hardResetToRemoteBranch(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String) {
        execute("reset --hard $remoteName/$branchName")
        execute("clean -fd")
    }

    private fun configure(key: String, value: String, isGlobal: Boolean = false) {
        execute(
            buildString {
                append("config ")
                if (isGlobal) {
                    append("--global ")
                }
                append(key)
                append(Constants.Char.SPACE)
                append(value.wrapWithDoubleQuote())
            }
        )
    }

    private fun executeAndGetOutput(command: String): String =
        runAndGetOutput(GIT_NAME + Constants.Char.SPACE + command)

    private fun execute(command: String): Boolean =
        run(GIT_NAME + Constants.Char.SPACE + command)

    companion object {
        private const val GIT_NAME = "git"
        val DOT_GIT: String = Constants.Char.DOT + GIT_NAME

        const val ORIGIN_REMOTE_NAME = "origin"
        const val UPSTREAM_REMOTE_NAME = "upstream"
        const val HEAD = "HEAD"

        fun open(gitProjectDirectory: File): GitShell =
            GitShell(gitProjectDirectory.asDirectory())
    }
}
