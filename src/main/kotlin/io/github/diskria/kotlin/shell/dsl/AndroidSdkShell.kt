package io.github.diskria.kotlin.shell.dsl

import com.android.build.gradle.AppExtension
import io.github.diskria.kotlin.regex.dsl.primitives.RegexWhitespace
import io.github.diskria.kotlin.utils.extensions.splitToPairOrNull
import java.io.File

class AndroidSdkShell private constructor(
    val appExtension: AppExtension,
    directory: File = appExtension.sdkDirectory
) : Shell(directory) {

    private val commandLineToolsDirectory: File = directory.resolve("cmdline-tools/latest/bin")
    private val platformToolsDirectory: File = directory.resolve("platform-tools")

    fun getApplicationId(): String =
        appExtension.defaultConfig.applicationId ?: error("Application id not found")

    fun getApkApplicationId(apkFile: File): String =
        cd(commandLineToolsDirectory)
            .runAndGetOutput("apkanalyzer manifest application-id ${apkFile.absolutePath}")

    fun getConnectedDevices(): List<String> =
        cd(platformToolsDirectory)
            .runAndGetOutput("adb devices")
            .lines()
            .mapNotNull { outputLine ->
                val separatorRegex = RegexWhitespace.oneOrMore().toRegex()
                val (deviceId, status) = outputLine.trim().splitToPairOrNull(separatorRegex)
                    ?: return@mapNotNull null

                if (status == STABLE_DEVICE_STATUS) deviceId
                else null
            }
            .toList()

    companion object {
        private const val STABLE_DEVICE_STATUS = "device"

        fun open(appExtension: AppExtension): AndroidSdkShell =
            AndroidSdkShell(appExtension)
    }
}
