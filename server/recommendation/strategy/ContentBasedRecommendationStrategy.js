/**
 * ContentBasedRecommendationStrategy
 * Calculates similarity based on acoustic features, genre, tempo, energy, and language.
 */

const RecommendationStrategy = require('./RecommendationStrategy');

class ContentBasedRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('ContentBased');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];

    const targetTempo = context.currentTempo || 100;
    const targetEnergy = context.currentEnergy || 0.6;
    const targetValence = context.currentValence || 0.6;
    const targetLanguage = context.currentLanguage || 'Hindi';

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const af = track.acousticFeatures || {};
      const tempoDiff = Math.abs((af.tempo || 100) - targetTempo) / 100;
      const energyDiff = Math.abs((af.energy || 0.6) - targetEnergy);
      const valenceDiff = Math.abs((af.valence || af.happiness || 0.6) - targetValence);

      const acousticSim = 1.0 - (tempoDiff * 0.3 + energyDiff * 0.4 + valenceDiff * 0.3);
      const langMatch = track.language === targetLanguage ? 1.0 : (track.language === 'English' ? 0.4 : 0.2);

      const score = Math.max(0, Math.min(1, acousticSim * 0.7 + langMatch * 0.3));

      results.push({
        track,
        score,
        strategy: this.name,
        reason: 'Harmonic tempo, energy, and acoustic alignment'
      });
    }

    return results;
  }
}

module.exports = ContentBasedRecommendationStrategy;
