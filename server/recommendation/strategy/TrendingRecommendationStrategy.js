/**
 * TrendingRecommendationStrategy
 * Evaluates track popularity, total play volume, and release freshness (ideal for cold-start).
 */

const RecommendationStrategy = require('./RecommendationStrategy');

class TrendingRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Trending');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];

    // Max plays in catalog for normalization
    let maxPlays = 1;
    for (const t of candidatePool) {
      if ((t.playsCount || 0) > maxPlays) maxPlays = t.playsCount;
    }

    const currentYear = new Date().getFullYear();

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const normPlays = (track.playsCount || 0) / maxPlays;
      const recencyBoost = Math.max(0, 1.0 - ((currentYear - (track.year || 2024)) * 0.08));
      const popularityScore = (track.acousticFeatures?.popularity ?? 0.8) * 0.6 + normPlays * 0.4;

      const score = Math.min(1.0, popularityScore * 0.7 + recencyBoost * 0.3);

      results.push({
        track,
        score,
        strategy: this.name,
        reason: 'Trending now across all listeners'
      });
    }

    return results;
  }
}

module.exports = TrendingRecommendationStrategy;
