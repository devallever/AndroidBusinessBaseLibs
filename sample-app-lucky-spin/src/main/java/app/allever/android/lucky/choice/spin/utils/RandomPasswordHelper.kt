package app.allever.android.lucky.choice.spin.utils

import kotlin.random.Random

private const val UPPER_CASE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ"
private const val LOWER_CASE_CHARS = "abcdefghjkmnpqrstuvwxyz"
private const val NUMBER_CHARS = "23456789"
private const val SPECIAL_CHARS = "!@#$%^&*()_+-=[]{};:,.<>?/~"

private const val AMBIGUOUS_CHARS_UPPER = "ILO"
private const val AMBIGUOUS_CHARS_LOWER = "ilo"
private const val AMBIGUOUS_CHARS_NUMBER = "01"
private const val AMBIGUOUS_CHARS_SPECIAL = "|"

object RandomPasswordHelper {
    fun generate(
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSpecial: Boolean = true,
        includeAmbiguous: Boolean = false,
        length: Int = 12
    ): String {
        require(
            includeUppercase || includeLowercase || includeNumbers || includeSpecial
        ) { "At least one character type must be included" }

        val allowedChars = StringBuilder().apply {
            if (includeUppercase) append(UPPER_CASE_CHARS)
            if (includeLowercase) append(LOWER_CASE_CHARS)
            if (includeNumbers) append(NUMBER_CHARS)
            if (includeSpecial) append(SPECIAL_CHARS)

            if (includeAmbiguous) {
                if (includeUppercase) append(AMBIGUOUS_CHARS_UPPER)
                if (includeLowercase) append(AMBIGUOUS_CHARS_LOWER)
                if (includeNumbers) append(AMBIGUOUS_CHARS_NUMBER)
                if (includeSpecial) append(AMBIGUOUS_CHARS_SPECIAL)
            }
        }

        val random = Random(System.currentTimeMillis())
        val password = StringBuilder(length)

        repeat(length) {
            val randomIndex = random.nextInt(allowedChars.length)
            password.append(allowedChars[randomIndex])
        }
        return password.toString()
    }
}