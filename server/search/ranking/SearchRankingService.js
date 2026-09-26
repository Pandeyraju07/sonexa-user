/**
 * SearchRankingService
 * Multi-signal scoring engine combining exact match, prefix match, fuzzy match,
 * entity relevance, popularity, user affinity, and recency.
 */

const { normalizeString } = require('../../catalog/catalogStore');
const { levenshteinDistance } = require('../entity/EntityResolutionService');
const { affinityEngine } = require('../../events/AffinityEngine');

class SearchRankingService {
  constructor(weights = {}) {
    this.weights = {
      exactMatch: weights.exactMatch ?? 0.35,
      prefixMatch: weights.prefixMatch ?? 0.15,
      fuzzyMatch: weights.fuzzyMatch ?? 0.10,
      entityRelevance: weights.entityRelevance ?? 0.20,
      popularity: weights.popularity ?? 0.10,
      userAffinity: weights.userAffinity ?? 0.05,
      recency: weights.recency ?? 0.05
    };
  }

  rankTracks(tracks, query, intent, userId = 'usr_anonymous') {
    const qNorm = normalizeString(query);

    const scored = tracks.map(track => {
      const tNorm = normalizeString(track.title);
      const aNorm = normalizeString(track.artist);
      const mNorm = normalizeString(track.movie || '');
      const gNorm = normalizeString(track.genre || '');

      // 1. Exact Match
      const exactMatch = (tNorm === qNorm || aNorm === qNorm || mNorm === qNorm) ? 1.0 : 0.0;

      // 2. Prefix Match
      const prefixMatch = (tNorm.startsWith(qNorm) || aNorm.startsWith(qNorm) || mNorm.startsWith(qNorm)) ? 1.0 : 0.0;

      // 3. Fuzzy Match
      const dist = Math.min(
        levenshteinDistance(qNorm, tNorm.slice(0, qNorm.length + 3)),
        levenshteinDistance(qNorm, aNorm.slice(0, qNorm.length + 3))
      );
      const fuzzyMatch = Math.max(0, 1.0 - (dist / Math.max(1, qNorm.length)));

      // 4. Entity Relevance based on intent
      let entityRelevance = 0.5;
      if (intent.type === 'MOVIE_SONGS' && intent.movie && (track.movieId === intent.movie.id || (track.movie && normalizeString(track.movie) === normalizeString(intent.movie.title)))) {
        entityRelevance = 1.0;
      } else if (intent.type === 'ARTIST' && intent.artist && (track.artist.toLowerCase().includes(intent.artist.name.toLowerCase()))) {
        entityRelevance = 1.0;
      } else if (intent.type === 'ARTIST_MOOD' && intent.artist && intent.mood) {
        const isArt = track.artist.toLowerCase().includes(intent.artist.name.toLowerCase());
        const isMood = track.moods && track.moods[intent.mood] >= 0.7;
        entityRelevance = (isArt ? 0.6 : 0) + (isMood ? 0.4 : 0);
      } else if (intent.type === 'MOOD' && intent.mood && track.moods && track.moods[intent.mood] >= 0.7) {
        entityRelevance = 1.0;
      }

      // 5. Popularity
      const popularity = track.acousticFeatures?.popularity ?? 0.8;

      // 6. User Affinity
      const userAffinity = affinityEngine.getNormalizedAffinity(userId, track.id);

      // 7. Recency
      const recency = Math.max(0, 1.0 - ((new Date().getFullYear() - (track.year || 2024)) * 0.05));

      const w = this.weights;
      const finalScore =
        (exactMatch * w.exactMatch) +
        (prefixMatch * w.prefixMatch) +
        (fuzzyMatch * w.fuzzyMatch) +
        (entityRelevance * w.entityRelevance) +
        (popularity * w.popularity) +
        (userAffinity * w.userAffinity) +
        (recency * w.recency);

      return {
        track,
        score: finalScore
      };
    });

    scored.sort((a, b) => b.score - a.score);
    return scored.map(s => s.track);
  }
}

const searchRankingService = new SearchRankingService();

module.exports = {
  SearchRankingService,
  searchRankingService
};
