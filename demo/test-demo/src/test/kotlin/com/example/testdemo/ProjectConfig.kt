package com.example.testdemo

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringExtension
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy
import org.testcontainers.containers.wait.strategy.WaitStrategy

internal class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension)

    override suspend fun beforeProject() {
        MongoDBContainer("mongo:latest").also {
            it.start()
            it.waitingFor(HostPortWaitStrategy())

            System.setProperty("spring.data.mongodb.uri", it.replicaSetUrl)
        }
    }

}