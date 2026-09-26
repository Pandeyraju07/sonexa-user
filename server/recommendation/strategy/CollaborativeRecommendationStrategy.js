/**
 * CollaborativeRecommendationStrategy
 * Identifies songs co-occurring across curated playlists and community listening sessions.
 */

const RecommendationStrategy = require('./RecommendationStrategy');
const { catalogStore } = require('../../catalog/catalogStore');

class CollaborativeRecommendationStrategy extends RecommendationStrategy {
  constructor() {
    super('Collaborative');
    this.coOccurrenceMap = this.buildCoOccurrenceMatrix();
  }

  buildCoOccurrenceMatrix() {
    const matrix = new Map(); // Map<songId, Map<otherSongId, count>>
    const playlists = catalogStore.playlists;

    for (const pl of playlists) {
      const trackIds = pl.tracks || [];
      for (let i = 0; i < trackIds.length; i++) {
        const idA = trackIds[i];
        if (!matrix.has(idA)) matrix.set(idA, new Map());

        for (let j = 0; j < trackIds.length; j++) {
          if (i === j) continue;
          const idB = trackIds[j];
          const curCount = matrix.get(idA).get(idB) || 0;
          matrix.get(idA).set(idB, curCount + 1);
        }
      }
    }
    return matrix;
  }

  generateCandidates(context, candidatePool, weights) {
    const results = [];
    const currentId = context.currentSongId;
    const coMap = currentId ? this.coOccurrenceMap.get(currentId) : null;

    for (const track of candidatePool) {
      if (currentId && track.id === currentId) continue;

      let score = 0.5; // baseline
      if (coMap && coMap.has(track.id)) {
        const count = coMap.get(track.id);
        score = Math.min(1.0, 0.6 + count * 0.2);
      }

      results.push({
        track,
        score,
        strategy: this.name,
        reason: 'Frequently enjoyed together by listeners with similar taste'
      });
    }

    return results;
  }
}

module.exports = CollaborativeRecommendationStrategy;
