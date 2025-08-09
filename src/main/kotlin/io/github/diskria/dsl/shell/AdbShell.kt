package io.github.diskria.dsl.shell

import io.github.diskria.utils.kotlin.Constants
import io.github.diskria.utils.kotlin.extensions.generics.joinBySpace
import io.github.diskria.utils.kotlin.extensions.skipUntilAfter
import io.github.diskria.utils.kotlin.extensions.toFile

class AdbShell private constructor(
    deviceId: String
) : Shell(Constants.File.ROOT_DIRECTORY.toFile()) {

    val activityManager: ActivityManager by lazy { ActivityManager() }
    val packageManager: PackageManager by lazy { PackageManager() }
    val systemDumpManager: SystemDumpManager by lazy { SystemDumpManager() }

    private val targetDevicePrefix: String = "adb -s $deviceId shell"

    fun executeAndGetOutput(command: String): String =
        runAndGetOutput(listOf(targetDevicePrefix, command).joinBySpace())

    inner class ActivityManager() : Command("am") {
        fun startActivity(applicationId: String, activityName: String) {
            execute("start -n $applicationId/$activityName")
        }

        fun forceStop(packageName: String) {
            execute("force-stop $packageName")
        }
    }

    inner class PackageManager() : Command("pm") {
        fun getApkPath(applicationId: String): String =
            execute("path $applicationId").removePrefix("package:")
    }

    inner class SystemDumpManager() : Command("dumpsys") {
        fun findEnabledActivityAlias(
            applicationId: String,
            prefix: String,
            defaultIcon: String,
        ): String =
            execute("package $applicationId")
                .lineSequence()
                .skipUntilAfter("enabledComponents")
                .firstOrNull { it.trim().startsWith(prefix) }
                ?: (prefix + defaultIcon)
    }

    open inner class Command(private val prefix: String) {
        fun execute(command: String): String =
            executeAndGetOutput(listOf(targetDevicePrefix, prefix, command).joinBySpace())
    }

    companion object {
        fun open(deviceId: String): AdbShell =
            AdbShell(deviceId)
    }
}
