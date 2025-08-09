package io.github.diskria.dsl.shell

import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.asDirectoryOrThrow
import io.github.diskria.utils.kotlin.extensions.common.modifyIf
import io.github.diskria.utils.kotlin.extensions.generics.joinBySpace
import io.github.diskria.utils.kotlin.extensions.toFile
import io.github.diskria.utils.kotlin.extensions.wrapWithDoubleQuote
import java.io.File

class GitShell private constructor(projectDirectory: File) : VCSShell(GIT_NAME, projectDirectory) {

    fun isInvalidRepository(): Boolean =
        execute("fsck --no-progress --no-dangling").not()

    fun clone(remoteUrl: String, targetDirectory: File) {
        execute("clone $remoteUrl $targetDirectory")
    }

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

    fun commit(message: String): Boolean =
        execute("commit -m ${message.wrapWithDoubleQuote()}")

    fun push(remoteName: String = ORIGIN_REMOTE_NAME, branchName: String = HEAD): Boolean =
        execute("push $remoteName $branchName")

    fun getRemoteNames(): List<String> =
        executeAndGetOutput("remote").lines().filter { it.isNotBlank() }

    fun getRemoteUrl(remoteName: String): String =
        executeAndGetOutput("remote get-url $remoteName")

    fun setRemoteUrl(remoteName: String, remoteUrl: String) {
        execute("remote set-url $remoteName $remoteUrl")
    }

    fun addRemote(remoteName: String, remoteUrl: String) {
        execute("remote add $remoteName $remoteUrl")
    }

    fun removeRemote(remoteName: String) {
        execute("remote remove $remoteName")
    }

    fun getAheadCount(remoteName: String, branchName: String): Int =
        executeAndGetOutput("rev-list HEAD..$remoteName/$branchName --count").toInt()

    fun hasUncommitedChanges(): Boolean =
        execute("diff --quiet").not() || execute("diff --cached --quiet").not()

    fun merge(remoteName: String, branchName: String): Boolean =
        execute("merge $remoteName $branchName")

    fun getTotalCommitsCount(): Int =
        executeAndGetOutput("rev-list --count HEAD").toInt()

    fun getHooksDirectory(): File =
        executeAndGetOutput("rev-parse --git-path hooks").toFile()

    fun updateSubmodules(shouldReinit: Boolean = false, allowRecursive: Boolean = false) {
        if (shouldReinit) {
            execute("submodule deinit --all --force")
        }
        execute("submodule update --init".modifyIf(allowRecursive) { "$this --recursive" })
    }

    fun addSubmodule(path: String, url: String) {
        execute("submodule add -f $url $path")
    }

    fun hardResetToRemoteCommit(remoteName: String, commitSha: String) {
        execute("reset --hard $commitSha")
        execute("clean -fd")
    }

    fun hardResetToRemoteBranch(remoteName: String, branchName: String) {
        execute("reset --hard $remoteName/$branchName")
        execute("clean -fd")
    }

    companion object {
        private const val GIT_NAME = "git"
        val DOT_GIT: String by lazy { Constants.Char.DOT + GIT_NAME }

        const val UPSTREAM_REMOTE_NAME = "upstream"
        const val ORIGIN_REMOTE_NAME = "origin"
        const val HEAD = "HEAD"

        fun open(gitProjectDirectory: File): GitShell =
            GitShell(gitProjectDirectory.asDirectoryOrThrow())
    }
}
