package com.example.testdemo.data

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.cdi.MongoRepositoryExtension
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<UserDocument, ObjectId> {
    fun findFirstByName(name: String): UserDocument?
}