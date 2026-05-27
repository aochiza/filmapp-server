package com.filmapp.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Users : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val name = varchar("name", 100)

    val role = enumerationByName(
        name = "role",
        length = 20,
        klass = UserRole::class
    )

    val createdAt = datetime("created_at")
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var email by Users.email
    var passwordHash by Users.passwordHash
    var name by Users.name
    var role by Users.role
    var createdAt by Users.createdAt
}