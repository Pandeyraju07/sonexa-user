/**
 * Configurable Recommendation Weights
 * Supports runtime dynamic adjustment via backend config or A/B experimentation.
 */

class RecommendationWeights {
  constructor(customWeights = {}) {
    this.contentSimilarity = customWeights.contentSimilarity ?? 0.25;
    this.userPreference = customWeights.userPreference ?? 0.20;
    this.context = customWeights.context ?? 0.20;
    this.artistAffinity = customWeights.artistAffinity ?? 0.10;
    this.genreAffinity = customWeights.genreAffinity ?? 0.08;
    this.moodAffinity = customWeights.moodAffinity ?? 0.07;
    this.popularity = customWeights.popularity ?? 0.05;
    this.freshness = customWeights.freshness ?? 0.05;

    // Penalties & diversity ratios
    this.artistDiversityPenalty = customWeights.artistDiversityPenalty ?? 0.25;
    this.albumDiversityPenalty = customWeights.albumDiversityPenalty ?? 0.20;
    this.songRepetitionPenalty = customWeights.songRepetitionPenalty ?? 0.50;
    this.skipPenaltyWeight = customWeights.skipPenaltyWeight ?? 0.30;
    this.earlySkipPenaltyWeight = customWeights.earlySkipPenaltyWeight ?? 0.45;
    this.explorationRatio = customWeights.explorationRatio ?? 0.30; // 70% exploitation / 30% exploration
  }

  normalize() {
    const sum = this.contentSimilarity +
      this.userPreference +
      this.context +
      this.artistAffinity +
      this.genreAffinity +
      this.moodAffinity +
      this.popularity +
      this.freshness;

    if (sum > 0) {
      this.contentSimilarity /= sum;
      this.userPreference /= sum;
      this.context /= sum;
      this.artistAffinity /= sum;
      this.genreAffinity /= sum;
      this.moodAffinity /= sum;
      this.popularity /= sum;
      this.freshness /= sum;
    }
    return this;
  }
}

// Global active recommendation weights configuration
const activeWeights = new RecommendationWeights();

module.exports = {
  RecommendationWeights,
  activeWeights
};
