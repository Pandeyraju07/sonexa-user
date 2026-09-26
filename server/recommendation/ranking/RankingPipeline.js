/**
 * RankingPipeline
 * Coordinates the full multi-stage recommendation workflow:
 * Candidate Generation -> Filtering -> Scoring -> Business Rules -> Diversity -> Exploration -> Final Ranking
 */

const { recommendationScoreCalculator } = require('./RecommendationScoreCalculator');
const { diversityService } = require('./DiversityService');
const { explorationService } = require('./ExplorationService');
const { userAffinityStore } = require('../../events/UserAffinityStore');

class RankingPipeline {
  constructor(strategies = []) {
    this.strategies = strategies;
  }

  /**
   * Runs the complete ranking pipeline
   * @param {PlaybackContext} context
   * @param {Array<Object>} catalogTracks
   * @param {RecommendationWeights} weights
   * @returns {Array<{ track: Object, score: number, reason: string, strategy: string }>}
   */
  execute(context, catalogTracks, weights) {
    // 1. Candidate Generation across all strategies
    const strategyOutputs = new Map(); // Map<trackId, { track, scoresByStrategy: {}, reasons: [] }>

    for (const strategy of this.strategies) {
      const candidates = strategy.generateCandidates(context, catalogTracks, weights);
      for (const c of candidates) {
        const id = c.track.id;
        if (!strategyOutputs.has(id)) {
          strategyOutputs.set(id, {
            track: c.track,
            scoresByStrategy: {},
            reasons: []
          });
        }
        const entry = strategyOutputs.get(id);
        entry.scoresByStrategy[strategy.name] = c.score;
        if (c.reason && c.score >= 0.7) {
          entry.reasons.push(c.reason);
        }
      }
    }

    // 2. Candidate Filtering & Business Rules
    const validCandidates = [];
    const recentSongIds = new Set(context.recentSongIds || []);

    for (const [id, entry] of strategyOutputs.entries()) {
      const track = entry.track;

      // Filter: Cannot be current song
      if (context.currentSongId && id === context.currentSongId) continue;

      // Filter: Deleted or unplayable
      if (track.isDeleted || track.unavailable) continue;

      // Business Rule: Respect explicit setting
      if (context.filterExplicit && track.explicit) continue;

      // Business Rule: Skipped genres penalty
      if (context.skippedGenres && context.skippedGenres.includes(track.genre)) {
        // Significantly lower or filter if user repeatedly skipped this genre
        entry.scoresByStrategy['Genre'] = (entry.scoresByStrategy['Genre'] || 0.5) * 0.3;
      }

      validCandidates.push(entry);
    }

    // 3. Feature Extraction & Scoring
    const scoredList = [];
    for (const item of validCandidates) {
      const compositeScore = recommendationScoreCalculator.calculateScore(
        item.track,
        item.scoresByStrategy,
        context
      );

      const topReason = item.reasons[0] || 'Matches your musical preferences';

      scoredList.push({
        track: item.track,
        score: compositeScore,
        strategy: Object.keys(item.scoresByStrategy)[0] || 'Hybrid',
        reason: topReason,
        scoresByStrategy: item.scoresByStrategy
      });
    }

    // Sort by composite score
    scoredList.sort((a, b) => b.score - a.score);

    // 4. Split into Exploitation and Exploration Pools
    const exploitPool = scoredList.filter(item => item.score >= 0.6);
    const explorePool = scoredList.filter(item => item.score < 0.6 || item.track.year >= 2024);

    // 5. Exploration vs Exploitation Blending
    const blended = explorationService.blend(
      exploitPool.length > 0 ? exploitPool : scoredList,
      explorePool,
      context.limit || 20
    );

    // 6. Diversity Re-ranking (preventing artist/album monotony)
    const finalRanked = diversityService.applyDiversity(blended, context, 1);

    // 7. Recommendation Logging for Observability
    for (let rank = 0; rank < finalRanked.length; rank++) {
      const item = finalRanked[rank];
      userAffinityStore.logRecommendation({
        userId: context.userId,
        sessionId: context.sessionId,
        candidateSongId: item.track.id,
        finalRank: rank + 1,
        recommendationScore: parseFloat(item.score.toFixed(4)),
        strategy: item.strategy,
        reason: item.reason
      });
    }

    return finalRanked;
  }
}

module.exports = RankingPipeline;
