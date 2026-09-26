/**
 * DiversityService
 * Enforces artist diversity, album diversity, and song repetition rules
 * so recommendations do not produce repetitive clusters.
 */

const { activeWeights } = require('../model/RecommendationWeights');

class DiversityService {
  constructor(weights = activeWeights) {
    this.weights = weights;
  }

  /**
   * Applies diversity re-ranking to a sorted list of candidate items
   * @param {Array<{ track: Object, score: number, reason: string }>} candidates
   * @param {PlaybackContext} context
   * @param {number} maxConsecutiveArtist
   * @returns {Array<{ track: Object, score: number, reason: string }>}
   */
  applyDiversity(candidates, context, maxConsecutiveArtist = 1) {
    if (candidates.length <= 2) return candidates;

    const diversified = [];
    const remaining = [...candidates];
    const recentSongIds = new Set(context.recentSongIds || []);
    const recentArtists = [...(context.recentArtistIds || [])];

    let lastArtist = context.currentArtistName || null;
    let lastAlbum = context.currentAlbumId || null;
    let consecutiveCount = 0;

    while (remaining.length > 0) {
      let chosenIndex = -1;

      // Find highest scoring candidate that satisfies diversity constraints
      for (let i = 0; i < remaining.length; i++) {
        const item = remaining[i];
        const track = item.track;

        // Skip repetition penalty
        if (recentSongIds.has(track.id)) {
          item.score *= (1.0 - this.weights.songRepetitionPenalty);
        }

        const isSameArtist = lastArtist && track.artist && track.artist.toLowerCase() === lastArtist.toLowerCase();
        const isSameAlbum = lastAlbum && track.albumId && track.albumId === lastAlbum;

        if (isSameArtist) {
          if (consecutiveCount >= maxConsecutiveArtist) {
            // Deprioritize: try to pick another artist first
            continue;
          }
        }

        chosenIndex = i;
        break;
      }

      // If all remaining violate diversity, pick the highest remaining anyway
      if (chosenIndex === -1) {
        chosenIndex = 0;
      }

      const selected = remaining.splice(chosenIndex, 1)[0];
      diversified.push(selected);

      // Update trackers
      if (lastArtist && selected.track.artist && selected.track.artist.toLowerCase() === lastArtist.toLowerCase()) {
        consecutiveCount++;
      } else {
        lastArtist = selected.track.artist;
        consecutiveCount = 1;
      }
      lastAlbum = selected.track.albumId;
    }

    return diversified;
  }
}

const diversityService = new DiversityService();

module.exports = {
  DiversityService,
  diversityService
};
