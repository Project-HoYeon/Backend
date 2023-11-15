package dev.hoyeon.utils

import io.github.cdimascio.dotenv.Dotenv

private val dotenv: Dotenv = getKoinInstance()

val isDevelopment: Boolean
    get() = dotenv["DEVELOPMENT", "false"].toBoolean()