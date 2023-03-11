package com.example.testdemo.service

import com.example.testdemo.data.UserDocument
import com.example.testdemo.data.UserRepository
import org.bson.types.ObjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {
    fun create(user: User): User {
        val document = this.userRepository.save(UserDocument(user))
        return User(document)
    }
    fun getUser(id: String): User? = userRepository.findByIdOrNull(ObjectId(id))?.let { User(it) }

    fun getUsers() = userRepository.findAll().map { User(it) }
}