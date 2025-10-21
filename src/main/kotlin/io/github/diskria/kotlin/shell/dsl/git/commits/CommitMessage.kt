package io.github.diskria.kotlin.shell.dsl.git.commits

import io.github.diskria.kotlin.utils.BracketsType
import io.github.diskria.kotlin.utils.Constants
import io.github.diskria.kotlin.utils.extensions.generics.joinByNewLine
import io.github.diskria.kotlin.utils.extensions.wrapWithBrackets

data class CommitMessage(
    val type: CommitType,
    val subject: String,
    val scope: String? = null,
    val emoji: String? = null,
    val body: String? = null,
    val breakingChangeDescription: String? = null,
    val footer: Pair<String, String>? = null,
) {
    fun build(): String =
        buildString {
            val header = buildString {
                append(type.name.lowercase())
                scope?.let { append(it.wrapWithBrackets(BracketsType.ROUND)) }
                append(Constants.Char.COLON)
                emoji?.let { append(Constants.Char.SPACE + it) }
                append(Constants.Char.SPACE)
                append(subject)
            }
            val footers = listOfNotNull(
                breakingChangeDescription?.let { BREAKING_CHANGE_FOOTER to it },
                footer,
            )
            val footerSection = footers.joinByNewLine { (type, value) ->
                buildString {
                    append(type.trim())
                    append(Constants.Char.COLON)
                    append(Constants.Char.SPACE)
                    append(value.trim())
                }
            }
            val sections = listOfNotNull(body, footerSection.takeIf { it.isNotEmpty() })

            append(header)
            if (sections.isNotEmpty()) {
                appendLine()
                appendLine()
                append(sections.joinByNewLine(linesCount = 2))
            }
        }

    companion object {
        private const val BREAKING_CHANGE_FOOTER: String = "BREAKING CHANGE"
    }
}
