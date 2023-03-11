package com.example.testcontainers.demo.data

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.MongoDBContainer
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest
internal class UserRepositoryTest(
    private val userRepository: UserRepository
) : StringSpec({
    "save and find" {
        val testTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0))
        val saved = userRepository.save(User(name = "user", age = 32, createdAt = testTime, updatedAt = testTime)).block()!!
        val found = userRepository.findById(saved.id)

        StepVerifier.create(found)
            .expectNext(saved)
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        val mongo = MongoDBContainer("mongo:latest").apply {
            start()
        }
    }
}