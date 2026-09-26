/**
 * ArtistRecommendationStrategy
 * Ranks tracks by current artist or stylistically affiliated peer artists.
 */

const RecommendationStrategy = require('./RecommendationStrategy');
const { catalogStore, normalizeString } = require('../../catalog/catalogStore');

class ArtistRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Artist');
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const currentArtist = normalizeString(context.currentArtistName || '');

    for (const track of candidatePool) {
      if (context.currentSongId && track.id === context.currentSongId) continue;

      const trackArtistNorm = normalizeString(track.artist || '');
      const trackArtists = (track.artists || []).map(a => normalizeString(a));

      let score = 0.2;
      let reason = 'Trending artist discovery';

      if (currentArtist && (trackArtistNorm.includes(currentArtist) || trackArtists.includes(currentArtist))) {
        score = 0.95;
        reason = `Because you listened to ${context.currentArtistName}`;
      } else if (currentArtist && track.genre && track.genre.includes('Bollywood') && currentArtist.includes('arijit')) {
        // High affinity between Arijit Singh and peer composers/singers like Pritam, Shreya Ghoshal
        if (trackArtistNorm.includes('pritam') || trackArtistNorm.includes('shreya')) {
          score = 0.85;
          reason = `Frequently co-created with ${context.currentArtistName}`;
        }
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

module.exports = ArtistRecommendationStrategy;
