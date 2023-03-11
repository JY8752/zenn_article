package com.example.testdemo.service

import com.example.testdemo.data.UserDocument
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class User(
    val id: ObjectId? = null,
    val name: String,
    val age: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(document: UserDocument) : this(
        id = document.id,
        name = document.name,
        age = document.age,
        createdAt = document.createdAt,
        updatedAt = document.updatedAt
    )
}
