package com.example.testcontainers.demo.data

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document
data class User(
    @Id val id: ObjectId = ObjectId.get(),
    @Field("nm") val name: String = "",
    @Field("age") val age: Int = 0,
    @Field("crtAt") val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field("updAt") val updatedAt: LocalDateTime = LocalDateTime.now()
)
