package com.balajitechlabs.quickdash.features.ai

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Wraps MediaPipe's LlmInference API for on-device text generation.
 * Supports .bin and .task model formats (Gemma, Phi-2 from Google/Microsoft).
 *
 * NOTE: Model initialization is slow the first time (~5–20s depending on model size).
 * The instance is cached and reused across calls for the same model path.
 */
@Suppress("DEPRECATION")
class LocalInferenceEngine {

    private var inference: LlmInference? = null
    private var loadedModelPath: String? = null

    /**
     * Initializes the inference engine with the given model file.
     * Returns true on success, false if model failed to load.
     *
     * This is a blocking call — run on Dispatchers.IO.
     */
    suspend fun initialize(context: Context, modelPath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            // Skip re-init if same model is already loaded
            if (loadedModelPath == modelPath && inference != null) return@withContext true

            // Pre-flight checks
            val file = java.io.File(modelPath)
            if (!file.exists()) return@withContext false
            if (modelPath.endsWith(".gguf", ignoreCase = true)) {
                // GGUF is llama.cpp format — not supported by MediaPipe
                throw UnsupportedOperationException("GGUF format is not supported by the on-device engine. Please choose a Gemma or Phi-2 model (.bin / .task).")
            }

            // Close previous instance
            inference?.close()
            inference = null

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .build()

            inference = LlmInference.createFromOptions(context, options)
            loadedModelPath = modelPath
            true
        } catch (e: Exception) {
            e.printStackTrace()
            inference = null
            loadedModelPath = null
            false
        }
    }

    /**
     * Generates a response for the given prompt using the loaded model.
     * Returns the generated text, or an error message on failure.
     *
     * Runs on IO dispatcher — safe to call from a coroutine.
     */
    suspend fun generateResponse(systemPrompt: String, userPrompt: String): String = withContext(Dispatchers.IO) {
        val engine = inference ?: return@withContext "⚠️ Model not loaded. Please initialize first."

        return@withContext try {
            // Format in instruction-tuned chat template (works for Gemma IT, Phi, TinyLlama)
            val fullPrompt = buildInstructPrompt(systemPrompt, userPrompt)
            engine.generateResponse(fullPrompt) ?: "No response generated."
        } catch (e: Exception) {
            "⚠️ Inference error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    /**
     * Generates a response with streaming updates via callback.
     * The [onToken] lambda is called with each token as it's generated.
     * Returns the complete final response.
     */
    suspend fun generateResponseStreaming(
        systemPrompt: String,
        userPrompt: String,
        onToken: (partial: String, done: Boolean) -> Unit
    ): String = withContext(Dispatchers.IO) {
        val engine = inference ?: run {
            onToken("⚠️ Model not loaded.", true)
            return@withContext ""
        }

        val fullPrompt = buildInstructPrompt(systemPrompt, userPrompt)
        val result = StringBuilder()

        return@withContext try {
            engine.generateResponseAsync(fullPrompt) { partialResult, done ->
                val chunk = partialResult ?: ""
                result.append(chunk)
                onToken(result.toString(), done)
            }
            result.toString()
        } catch (e: Exception) {
            val err = "⚠️ Inference error: ${e.localizedMessage}"
            onToken(err, true)
            err
        }
    }

    val isLoaded: Boolean get() = inference != null

    fun close() {
        inference?.close()
        inference = null
        loadedModelPath = null
    }
}

/**
 * Formats a system + user prompt in the standard instruction-tuned chat template.
 * Compatible with Gemma IT, Phi-2 instruct, and TinyLlama chat formats.
 */
private fun buildInstructPrompt(systemPrompt: String, userInput: String): String {
    return buildString {
        // Gemma / instruction-tuned format
        if (systemPrompt.isNotBlank()) {
            append("<bos><start_of_turn>system\n")
            append(systemPrompt.trim())
            append("<end_of_turn>\n")
        }
        append("<start_of_turn>user\n")
        append(userInput.trim())
        append("<end_of_turn>\n")
        append("<start_of_turn>model\n")
    }
}

/**
 * Builds the system prompt for a given AI task (used in QuickTranslatorScreen).
 */
fun buildSystemPromptForTask(task: String): String = when (task) {
    "Summarize" -> "You are a helpful assistant. Summarize the text below in 2-4 concise bullet points. Be brief and accurate."
    "Fix Grammar" -> "You are a grammar and writing expert. Fix all grammar, spelling, punctuation, and style issues in the text below. Return only the corrected text without explanations."
    "Rephrase" -> "You are a skilled writer. Rephrase the following text naturally while preserving the original meaning. Vary sentence structure and word choice."
    "Explain Code" -> "You are an expert software engineer. Explain what the following code does step-by-step in simple, clear terms. Mention the key logic, inputs, and outputs."
    "Bullet Points" -> "You are a concise writer. Convert the following text into a clean, well-organized bullet-point list."
    "Simplify" -> "You are a plain-language writer. Rewrite the following text so it is easy for anyone to understand. Use short sentences and simple words."
    "Professional Tone" -> "You are a business writing coach. Rewrite the following text in a formal, professional tone suitable for business emails or reports."
    "Translate & Explain" -> "You are a multilingual language expert. First identify the language of the input text, translate it to English if needed, then briefly explain the meaning and any cultural context."
    else -> "You are a helpful assistant. Process the following text."
}
