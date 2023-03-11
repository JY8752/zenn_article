package com.example.testdemo.data

import com.example.testdemo.service.User
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(collection = "users")
data class UserDocument(
    @Id val id: ObjectId = ObjectId.get(),
    @Field("nm") val name: String = "",
    @Field("age") val age: Int = 0,
    @Field("crtAt") val createdAt: LocalDateTime = LocalDateTime.now(),
    @Field("updAt") val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(model: User) : this(
        name = model.name,
        age = model.age,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt
    )
}
