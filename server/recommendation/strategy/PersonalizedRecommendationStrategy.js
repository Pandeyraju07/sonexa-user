/**
 * PersonalizedRecommendationStrategy
 * Evaluates individual user affinity, like history, completion rates, and skip history.
 */

const RecommendationStrategy = require('./RecommendationStrategy');
const { affinityEngine } = require('../../events/AffinityEngine');

class PersonalizedRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Personalized');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const userId = context.userId || 'usr_anonymous';

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const songAff = affinityEngine.getNormalizedAffinity(userId, track.id);
      const artAff = affinityEngine.getArtistScore(userId, track.artist);
      const genreAff = track.genre ? affinityEngine.getGenreScore(userId, track.genre) : 0.5;

      let moodAff = 0.5;
      if (track.moods) {
        const moodScores = Object.keys(track.moods).map(m => affinityEngine.getMoodScore(userId, m));
        if (moodScores.length > 0) {
          moodAff = moodScores.reduce((a, b) => a + b, 0) / moodScores.length;
        }
      }

      // If user has skipped this genre frequently, reduce score
      if (context.skippedGenres && context.skippedGenres.includes(track.genre)) {
        genreAff *= 0.6;
      }

      const score = Math.min(1.0, songAff * 0.4 + artAff * 0.3 + genreAff * 0.15 + moodAff * 0.15);

      results.push({
        track,
        score,
        strategy: this.name,
        reason: 'Curated based on your personal listening history'
      });
    }

    return results;
  }
}

module.exports = PersonalizedRecommendationStrategy;
