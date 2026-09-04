package com.vignesh.uniquepersoncollage.ml

import android.content.Context
import android.graphics.Bitmap
import com.vignesh.uniquepersoncollage.data.model.FaceEmbedding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Extracts normalized deep feature embeddings from face crop bitmaps.
 */
class FaceEmbedder(context: Context) {

    private val embeddingModel = EmbeddingModel(context)

    /**
     * Extracts embedding vector asynchronously.
     */
    suspend fun getEmbedding(faceCrop: Bitmap): FaceEmbedding = withContext(Dispatchers.Default) {
        val inputBuffer = embeddingModel.preprocessBitmap(faceCrop)
        val vector = embeddingModel.runInference(inputBuffer, faceCrop)
        FaceEmbedding(vector = vector)
    }

    fun close() {
        embeddingModel.close()
    }
}
