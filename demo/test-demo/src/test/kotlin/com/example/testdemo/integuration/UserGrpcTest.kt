package com.example.testdemo.integuration

import com.example.testdemo.data.UserDocument
import com.example.testdemo.data.UserRepository
import com.example.testdemo.getChannel
import com.example.testdemo.proto.user.CreateUserRequest
import com.example.testdemo.proto.user.GetUserRequest
import com.example.testdemo.proto.user.UserGrpcKt
import com.example.testdemo.proto.user.UserResponse
import com.google.protobuf.Empty
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
internal class UserGrpcTest(
    private val userRepository: UserRepository
) : StringSpec({
    val stub: UserGrpcKt.UserCoroutineStub by lazy { UserGrpcKt.UserCoroutineStub(getChannel()) }

    fun buildCreateUserRequest(name: String = "user", age: Int = 32) = CreateUserRequest.newBuilder()
        .setName(name)
        .setAge(age)
        .build()
    fun buildGetUserRequest(id: String) = GetUserRequest.newBuilder()
        .setId(id)
        .build()
    fun buildUserResponse(id: String, name: String = "user", age: Int = 32) = UserResponse.newBuilder()
        .setId(id)
        .setName(name)
        .setAge(age)
        .build()

    afterTest { userRepository.deleteAll() }

    "User.Register" {
        val response = withContext(Dispatchers.Default) {
            stub.register(buildCreateUserRequest())
        }

        response shouldBe buildUserResponse(id = response.id)
    }
    "User.GetUser" {
        //given
        val savedDocument = userRepository.save(UserDocument(name = "user", age = 32))
        val savedId = savedDocument.id.toString()

        //when
        val response = async { stub.getUser(buildGetUserRequest(savedId)) }

        //then
        response.await() shouldBe buildUserResponse(id = savedId)
    }
    "User.GetUsers" {
        //given
        userRepository.save(UserDocument(name = "user1"))
        userRepository.save(UserDocument(name = "user2"))

        //when
        val response = stub.getUsers(Empty.getDefaultInstance())

        //then
        response.userListCount shouldBe 2
    }
})