package com.example.inventoryapplication.data.api

import com.google.gson.*
import java.lang.reflect.Type

class UserDeserializer : JsonDeserializer<User> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): User {
        val obj = json.asJsonObject

        val id = obj["id"]?.asInt ?: -1
        val username = obj["username"]?.asString ?: ""
        val email = obj["email"]?.asString ?: ""
        val role = if (obj["role"]?.isJsonNull == false) obj["role"].asString else "user"
        val createdAt = if (obj["created_at"]?.isJsonNull == false) obj["created_at"].asString else null
        val updatedAt = if (obj["updated_at"]?.isJsonNull == false) obj["updated_at"].asString else null

        val photoElement = obj["photo_profile"]
        val photoProfile = when {
            photoElement == null || photoElement.isJsonNull -> null
            photoElement.isJsonPrimitive -> photoElement.asString
            photoElement.isJsonObject -> photoElement.asJsonObject["url"]?.asString
            else -> null
        }

        return User(id, username, email, role, photoProfile, createdAt, updatedAt)
    }
}
