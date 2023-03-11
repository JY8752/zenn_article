package com.example.testcontainers.demo

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension

internal class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)
}