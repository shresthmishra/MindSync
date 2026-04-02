package com.sevenlabs.mindsync

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeepReflectionHelper(private val context: Context, private val modelPath: String) {
    private var llmInference: LlmInference? = null

    init {
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(512)
            .setTemperature(0.7f)
            .setTopK(40)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)
    }

    suspend fun generateDeepReflection(journalContent: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            <start_of_turn>user
            Act as a professional mindfulness coach. Talk to me naturally like a graceful mentor. 
            Focus on emotional growth and one or two or as many as required gentle piece of advice.
            At the very end give an AI Disclaimer.
            Entry: $journalContent<end_of_turn>
            <start_of_turn>model
            Reflection:
        """.trimIndent()

        try {
            llmInference?.generateResponse(prompt) ?: "I'm unable to reflect right now."
        } catch (e: Exception) {
            "Reflection error: ${e.message}"
        }
    }

    fun close() {
        llmInference?.close()
    }
}