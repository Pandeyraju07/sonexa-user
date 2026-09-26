/**
 * MovieRecommendationStrategy
 * Provides strong boosts to tracks from the same movie soundtrack or related film universe.
 */

const RecommendationStrategy = require('./RecommendationStrategy');
const { normalizeString } = require('../../catalog/catalogStore');

class MovieRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('MovieSoundtrack');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const currentMovieId = context.currentMovieId;
    const currentMovieNorm = normalizeString(context.currentMovieName || '');

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      let score = 0.2;
      let reason = 'Soundtrack discovery';

      const isSameMovieId = currentMovieId && track.movieId === currentMovieId;
      const isSameMovieName = currentMovieNorm && track.movie && normalizeString(track.movie) === currentMovieNorm;

      if (isSameMovieId || isSameMovieName) {
        score = 0.98;
        reason = `From the same movie soundtrack: ${track.movie}`;
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

module.exports = MovieRecommendationStrategy;
