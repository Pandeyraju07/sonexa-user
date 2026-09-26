/**
 * RecommendationStrategy Base Class
 */

class RecommendationStrategy {
  constructor(name) {
    this.name = name;
  }

  /**
   * Generates scored candidates for a given playback context
   * @param {PlaybackContext} context
   * @param {Array<Object>} candidatePool
   * @param {RecommendationWeights} weights
   * @returns {Array<{ track: Object, score: number, strategy: string, reason: string }>}
   */
  generateCandidates(context, candidatePool, weights) {
    throw new Error(`generateCandidates() must be implemented by ${this.name}`);
  }
}

module.exports = RecommendationStrategy;
