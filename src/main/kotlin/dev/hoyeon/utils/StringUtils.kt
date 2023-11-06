package dev.hoyeon.utils

import java.util.UUID

fun String.toUUID(): UUID =
    UUID.fromString(this)