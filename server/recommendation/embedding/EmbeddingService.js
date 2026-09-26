/**
 * Embedding Service Abstraction
 * Allows switching between LocalEmbeddingService, OpenAI, HuggingFace, etc.
 */

class EmbeddingService {
  /**
   * Generates a dense embedding vector for a song
   * @param {Object} track - Song metadata & acoustic features
   * @returns {Promise<Array<number>>|Array<number>} - Embedding vector
   */
  generateSongEmbedding(track) {
    throw new Error('generateSongEmbedding() must be implemented');
  }

  /**
   * Generates an embedding vector for arbitrary text
   * @param {string} text - Query or description
   * @returns {Promise<Array<number>>|Array<number>} - Embedding vector
   */
  generateTextEmbedding(text) {
    throw new Error('generateTextEmbedding() must be implemented');
  }

  /**
   * Computes cosine similarity between two embedding vectors
   * @param {Array<number>} vecA
   * @param {Array<number>} vecB
   * @returns {number} similarity in [0, 1]
   */
  computeCosineSimilarity(vecA, vecB) {
    if (!vecA || !vecB || vecA.length !== vecB.length) return 0;
    let dot = 0;
    let normA = 0;
    let normB = 0;
    for (let i = 0; i < vecA.length; i++) {
      dot += vecA[i] * vecB[i];
      normA += vecA[i] * vecA[i];
      normB += vecB[i] * vecB[i];
    }
    if (normA === 0 || normB === 0) return 0;
    const sim = dot / (Math.sqrt(normA) * Math.sqrt(normB));
    return Math.max(0, Math.min(1, (sim + 1) / 2)); // normalized to [0, 1]
  }
}

module.exports = EmbeddingService;
