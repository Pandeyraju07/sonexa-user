/**
 * RecommendationScoreCalculator
 * Computes composite ranking scores from strategy signals, user affinity, and context weights.
 */

const { activeWeights } = require('../model/RecommendationWeights');
const { affinityEngine } = require('../../events/AffinityEngine');

class RecommendationScoreCalculator {
  constructor(weights = activeWeights) {
    this.weights = weights;
  }

  /**
   * Calculates composite score for a track given strategy signals and context
   */
  calculateScore(track, strategyScores, context) {
    const w = this.weights;
    const userId = context.userId || 'usr_anonymous';

    const contentSim = strategyScores['ContentBased'] ?? 0.5;
    const userPref = strategyScores['Personalized'] ?? 0.5;
    const movieScore = strategyScores['MovieSoundtrack'] ?? 0.0;
    const moodScore = strategyScores['Mood'] ?? 0.5;
    const genreScore = strategyScores['Genre'] ?? 0.5;
    const artistScore = strategyScores['Artist'] ?? 0.5;
    const trendingScore = strategyScores['Trending'] ?? 0.5;
    const collabScore = strategyScores['Collaborative'] ?? 0.5;

    // Context score combines movie match and session vibe
    const contextScore = movieScore > 0.8 ? movieScore : (moodScore * 0.6 + genreScore * 0.4);

    const artistAffinity = affinityEngine.getArtistScore(userId, track.artist);
    const genreAffinity = track.genre ? affinityEngine.getGenreScore(userId, track.genre) : 0.5;
    const moodAffinity = track.moods
      ? (Object.keys(track.moods).reduce((sum, m) => sum + affinityEngine.getMoodScore(userId, m), 0) / Math.max(1, Object.keys(track.moods).length))
      : 0.5;

    const popularityScore = track.acousticFeatures?.popularity ?? 0.8;
    const freshnessScore = Math.max(0, 1.0 - ((new Date().getFullYear() - (track.year || 2024)) * 0.05));

    let composite =
      (contentSim * w.contentSimilarity) +
      (userPref * w.userPreference) +
      (contextScore * w.context) +
      (artistAffinity * w.artistAffinity) +
      (genreAffinity * w.genreAffinity) +
      (moodAffinity * w.moodAffinity) +
      (popularityScore * w.popularity) +
      (freshnessScore * w.freshness);

    // Apply skip penalty if this track or genre has high skips
    const songAff = affinityEngine.getNormalizedAffinity(userId, track.id);
    if (songAff < 0.4) {
      composite -= (0.4 - songAff) * w.skipPenaltyWeight;
    }

    return Math.max(0.01, Math.min(1.0, composite));
  }
}

const recommendationScoreCalculator = new RecommendationScoreCalculator();

module.exports = {
  RecommendationScoreCalculator,
  recommendationScoreCalculator
};
