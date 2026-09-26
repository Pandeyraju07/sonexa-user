/**
 * GenreRecommendationStrategy
 * Evaluates hierarchical genre match and user genre preference.
 */

const RecommendationStrategy = require('./RecommendationStrategy');
const { normalizeString } = require('../../catalog/catalogStore');

class GenreRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Genre');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const targetGenre = normalizeString(context.currentGenre || 'bollywood');

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const trackGenre = normalizeString(track.genre || '');
      const subgenre = normalizeString(track.subgenre || '');
      const allGenres = (track.genres || []).map(g => normalizeString(g));

      let score = 0.2;
      let reason = 'Genre exploration';

      if (trackGenre === targetGenre || allGenres.includes(targetGenre)) {
        score = 0.90;
        reason = `Matches your preference for ${track.genre}`;
      } else if (trackGenre.includes(targetGenre) || targetGenre.includes(trackGenre) || subgenre.includes(targetGenre)) {
        score = 0.75;
        reason = `Subgenre connection with ${track.subgenre || track.genre}`;
      }

      results.push({
        track,
        score,
        strategy: this.name,
        reason
      });
    }

    return results;
  }
}

module.exports = GenreRecommendationStrategy;
