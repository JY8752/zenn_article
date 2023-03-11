package com.example.testdemo.data

import com.example.testdemo.getTestTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
internal class UserRepositoryTest(
    private val userRepository: UserRepository
) : StringSpec({
    "save and find" {
        //given
        val testTime = getTestTime()
        val document = UserDocument(name = "user", age = 32, createdAt = testTime, updatedAt = testTime)
        val saved = userRepository.save(document)

        //when
        val result = userRepository.findByIdOrNull(document.id)

        //then
        result shouldBe saved
    }
    "findByName" {
        userRepository.save(UserDocument(name = "user"))
        userRepository.findFirstByName("user") shouldNotBe null
    }
    "count" {
        userRepository.count() shouldBe 0
    }
})