package com.example.testdemo.service

import com.example.testdemo.data.UserDocument
import com.example.testdemo.data.UserRepository
import com.example.testdemo.getTestTime
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

internal class UserServiceTest : StringSpec({
    lateinit var userRepository: UserRepository
    lateinit var userService: UserService

    beforeTest {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    "create" {
        //given
        val testTime = getTestTime()
        val user = User(name = "user", age = 32, createdAt = testTime, updatedAt = testTime)
        val document = UserDocument(user)

        every { userRepository.save(any()) } returns document

        //when
        val result = userService.create(user)

        //then
        result.shouldBeUser(document)

        verify { userRepository.save(any()) }
        confirmVerified(userRepository)
    }

}) {
    companion object {
        fun User.shouldBeUser(document: UserDocument): User {
            this.id shouldBe document.id
            this.name shouldBe document.name
            this.age shouldBe document.age
            this.createdAt shouldBe document.createdAt
            this.updatedAt shouldBe document.updatedAt
            return this
        }
    }

}