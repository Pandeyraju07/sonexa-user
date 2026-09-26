/**
 * ExplorationService
 * Interleaves exploitation (high confidence personalized matches)
 * with exploration (fresh releases, trending artists, novel genres)
 * according to a configurable exploration ratio (e.g. 70/30).
 */

const { activeWeights } = require('../model/RecommendationWeights');

class ExplorationService {
  constructor(weights = activeWeights) {
    this.weights = weights;
  }

  /**
   * Blends exploitation and exploration pools into a final queue
   * @param {Array<Object>} exploitationPool - High-confidence candidates
   * @param {Array<Object>} explorationPool - Discovery candidates
   * @param {number} totalLimit - Final desired count
   * @returns {Array<Object>}
   */
  blend(exploitationPool, explorationPool, totalLimit = 20) {
    const explorationRatio = this.weights.explorationRatio; // e.g. 0.30
    const exploreTarget = Math.round(totalLimit * explorationRatio);
    const exploitTarget = totalLimit - exploreTarget;

    const result = [];
    const usedIds = new Set();

    let exploitIdx = 0;
    let exploreIdx = 0;

    // Pattern: 2-3 exploitation, then 1 exploration
    while (result.length < totalLimit && (exploitIdx < exploitationPool.length || exploreIdx < explorationPool.length)) {
      // Pick 2 exploitation items
      for (let k = 0; k < 2 && exploitIdx < exploitationPool.length && result.length < totalLimit; k++) {
        const item = exploitationPool[exploitIdx++];
        if (!usedIds.has(item.track.id)) {
          usedIds.add(item.track.id);
          result.push(item);
        }
      }

      // Pick 1 exploration item
      if (exploreIdx < explorationPool.length && result.length < totalLimit) {
        const item = explorationPool[exploreIdx++];
        if (!usedIds.has(item.track.id)) {
          usedIds.add(item.track.id);
          result.push({
            ...item,
            reason: item.reason || 'Fresh discovery curated for you'
          });
        }
      }
    }

    return result;
  }
}

const explorationService = new ExplorationService();

module.exports = {
  ExplorationService,
  explorationService
};
