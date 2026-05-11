package com.autodevops.verifier.utils

import io.github.oshai.kotlinlogging.KotlinLogging

object Logger {
    fun getLogger(name: String) = KotlinLogging.logger(name)
}
