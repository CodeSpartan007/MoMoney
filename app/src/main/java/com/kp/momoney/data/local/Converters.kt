package com.kp.momoney.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }
    
    // Date <-> Long converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Long? {
        return value
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Long?): Long? {
        return date
    }
    
    // List<String> <-> String converters (using JSON)
    @TypeConverter
    fun fromTagsString(value: String?): List<String>? {
        if (value == null || value.isEmpty()) {
            return emptyList()
        }
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            // Fallback to comma-separated values if JSON parsing fails
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
    
    @TypeConverter
    fun tagsListToString(tags: List<String>?): String? {
        return if (tags.isNullOrEmpty()) {
            null
        } else {
            try {
                json.encodeToString(tags)
            } catch (e: Exception) {
                tags.joinToString(",")
            }
        }
    }
}

