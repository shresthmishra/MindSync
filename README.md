# MindSync: An AI-Enhanced Mindfulness Journal

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)

## Description

MindSync is a comprehensive, privacy-first Android journaling application that leverages advanced on-device generative AI to provide emotional clarity. By combining local sentiment classification with large language model (LLM) processing, MindSync transforms simple daily logs into deep, actionable mindfulness insights—all without your data ever leaving your phone.

## Key Features

  * **Two-Tier AI Insight System:**
      * **Tier 1 (Sentiment Mapping):** Uses a TensorFlow Lite model trained on the GoEmotions dataset to categorize entries into 28 emotional states and provide immediate, context-aware mindfulness tips.
      * **Tier 2 (Deep Reflection):** Powered by **Gemma 2b** via MediaPipe GenAI. This tier generates unique, empathetic, and deep coaching reflections based on the raw text of your entries.
  * **Local-First Privacy:** Every byte of your journal, from the raw text to the generative AI reflections, is stored and processed locally using an encrypted Room Database.
  * **Premium UX Details:** \* Subtle copy-to-clipboard buttons for entries and AI reflections.
      * Dynamic "Jump to Today" navigation for easy access.
      * Calming, gradient-based UI built entirely with Jetpack Compose.

## Tech Stack

  * **Language:** Kotlin
  * **UI Toolkit:** Jetpack Compose
  * **Generative AI:** Gemma 2b (Quantized 4-bit)
  * **Inference Engine:** MediaPipe LLM Inference API & TensorFlow Lite (LiteRT)
  * **Architecture:** MVVM (Model-View-ViewModel)
  * **Database:** Room Persistence Library
  * **Background Tasks:** WorkManager for daily mindfulness reminders


---
<i>Thanks for reading, have a good one!</i>
