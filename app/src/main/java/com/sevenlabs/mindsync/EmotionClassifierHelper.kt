package com.sevenlabs.mindsync

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Accelerator
import org.json.JSONObject

class EmotionClassifierHelper(private val context: Context) {

    private var model: CompiledModel? = null
    private var vocab: Map<String, Int> = emptyMap()

    private val labels = listOf(
        "admiration", "amusement", "anger", "annoyance", "approval", "caring",
        "confusion", "curiosity", "desire", "disappointment", "disapproval",
        "disgust", "embarrassment", "excitement", "fear", "gratitude", "grief",
        "joy", "love", "nervousness", "optimism", "pride", "realization",
        "relief", "remorse", "sadness", "surprise", "neutral"
    )

    init {
        try {
            val options = CompiledModel.Options(Accelerator.CPU)
            model = CompiledModel.create(context.assets, "emotion_classifier.tflite", options)
            vocab = loadVocab()
            Log.d("MindSyncAI", "LiteRT System Online - No Compilation Errors")
        } catch (e: Exception) {
            Log.e("MindSyncAI", "Init Error: ${e.message}")
        }
    }

    private fun loadVocab(): Map<String, Int> {
        return try {
            val jsonString = context.assets.open("vocab.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            val map = mutableMapOf<String, Int>()
            jsonObject.keys().forEach { map[it] = jsonObject.getInt(it) }
            map
        } catch (e: Exception) { emptyMap() }
    }

    fun classify(text: String): String {
        val activeModel = model ?: return "Neutral"

        try {
            val tokens = tokenize(text)
            val inputBuffers = activeModel.createInputBuffers()
            val outputBuffers = activeModel.createOutputBuffers()

            // writeInt is the correct method for LiteRT 2.1.3 TensorBuffers
            inputBuffers[0].writeInt(tokens)

            activeModel.run(inputBuffers, outputBuffers)

            // readFloat gives the entire array of 28 probabilities
            val results = outputBuffers[0].readFloat()

            // LOGGING THE TRUTH: If all scores are 0.0, the model is 'Dead'
            results.indices.sortedByDescending { results[it] }.take(3).forEach { i ->
                Log.d("MindSyncAI", "Ranked: ${labels[i]} | Score: ${results[i]}")
            }

            val maxIndex = results.indices.maxByOrNull { results[it] } ?: 27
            return labels[maxIndex].replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            Log.e("MindSyncAI", "Inference error: ${e.message}")
            return "Neutral"
        }
    }

    private fun tokenize(text: String): IntArray {
        val tokens = IntArray(50) { 0 }
        val cleanText = text.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim()
        val words = cleanText.split(Regex("\\s+"))

        // Let's try Post-Padding one last time (words first)
        for (i in 0 until minOf(words.size, 50)) {
            tokens[i] = vocab[words[i]] ?: 1
        }

        return tokens
    }

    fun close() {
        model?.close()
    }
}