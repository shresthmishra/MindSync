package com.sevenlabs.mindsync

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Accelerator
import org.json.JSONObject

class EmotionClassifierHelper(private val context: Context) {
    private var model: CompiledModel? = null
    private var vocab: Map<String, Int> = emptyMap()
    private val confidenceThreshold = 0.15f

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
        } catch (e: Exception) {
            Log.e("MindSyncAI", e.message ?: "Initialization Error")
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
            inputBuffers[0].writeInt(tokens)
            activeModel.run(inputBuffers, outputBuffers)
            val results = outputBuffers[0].readFloat()

            val maxIndex = results.indices.maxByOrNull { results[it] } ?: 27
            val topScore = results[maxIndex]

            return if (topScore < confidenceThreshold) {
                "Neutral"
            } else {
                labels[maxIndex].replaceFirstChar { it.uppercase() }
            }
        } catch (e: Exception) {
            return "Neutral"
        }
    }

    private fun tokenize(text: String): IntArray {
        val tokens = IntArray(50) { 0 }
        val words = text.lowercase().replace(Regex("[^a-z0-9\\s]"), "").trim().split(Regex("\\s+"))
        for (i in 0 until minOf(words.size, 50)) {
            tokens[i] = vocab[words[i]] ?: 1
        }
        return tokens
    }

    fun close() {
        model?.close()
    }
}