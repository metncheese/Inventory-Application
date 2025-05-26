package com.example.inventoryapplication.data.api

import com.google.gson.*
import java.lang.reflect.Type

class UserListDeserializer : JsonDeserializer<List<User>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): List<User> {
        val jsonArray = json.asJsonArray
        return jsonArray.map { UserDeserializer().deserialize(it, User::class.java, context) }
    }
}
