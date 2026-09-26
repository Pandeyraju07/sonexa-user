/**
 * MoodRecommendationStrategy
 * Computes multi-mood alignment between playback context and candidate tracks.
 */

const RecommendationStrategy = require('./RecommendationStrategy');

class MoodRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Mood');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const targetMood = (context.currentMood || 'ROMANTIC').toUpperCase();

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const trackMoods = track.moods || {};
      const directMoodWeight = trackMoods[targetMood] ?? 0;

      // Also compute dot product if current song has full mood vector
      let dotScore = directMoodWeight;
      let reason = directMoodWeight >= 0.7
        ? `Fits the ${targetMood.toLowerCase()} mood perfectly`
        : 'Harmonic mood transition';

      results.push({
        track,
        score: Math.max(0.1, Math.min(1.0, dotScore)),
        strategy: this.name,
        reason
      });
    }

    return results;
  }
}

module.exports = MoodRecommendationStrategy;
