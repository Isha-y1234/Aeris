package com.runanywhere.kotlin_starter_example.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object ConversationRepository {
    private const val FILE_NAME = "conversations.json"
    private val _messages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val messages: StateFlow<List<ConversationMessage>> = _messages

    fun init(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (file.exists()) {
            try {
                val jsonString = file.readText()
                val jsonArray = JSONArray(jsonString)
                val loadedMessages = mutableListOf<ConversationMessage>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    loadedMessages.add(
                        ConversationMessage(
                            text = obj.getString("text"),
                            isFromOther = obj.getBoolean("isFromOther"),
                            timestamp = obj.getLong("timestamp")
                        )
                    )
                }
                _messages.value = loadedMessages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addMessage(context: Context, message: ConversationMessage) {
        val currentList = _messages.value.toMutableList()
        currentList.add(message)
        _messages.value = currentList
        saveMessages(context)
    }

    fun clear(context: Context) {
        _messages.value = emptyList()
        saveMessages(context)
    }

    private fun saveMessages(context: Context) {
        try {
            val jsonArray = JSONArray()
            _messages.value.forEach { msg ->
                val obj = JSONObject()
                obj.put("text", msg.text)
                obj.put("isFromOther", msg.isFromOther)
                obj.put("timestamp", msg.timestamp)
                jsonArray.put(obj)
            }
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class ConversationMessage(
    val text: String,
    val isFromOther: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
