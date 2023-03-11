package com.example.testdemo.grpc

import com.example.testdemo.proto.user.*
import com.example.testdemo.service.User
import com.example.testdemo.service.UserService
import com.google.protobuf.Empty
import org.lognet.springboot.grpc.GRpcService

@GRpcService
class UserGrpcService(
    private val userService: UserService
) : UserGrpcKt.UserCoroutineImplBase() {
    override suspend fun register(request: CreateUserRequest): UserResponse {
        return this.userService.create(User(name = request.name, age = request.age)).toResponse()
    }

    override suspend fun getUser(request: GetUserRequest): UserResponse {
        return this.userService.getUser(request.id)?.toResponse() ?: UserResponse.getDefaultInstance()
    }

    override suspend fun getUsers(request: Empty): UserListResponse {
        val users = this.userService.getUsers().map { it.toResponse() }
        return UserListResponse.newBuilder().addAllUserList(users).build()
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse.newBuilder()
            .setId(this.id.toString())
            .setName(this.name)
            .setAge(this.age)
            .build()
    }
}