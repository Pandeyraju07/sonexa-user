/**
 * RecommendationDebugService
 * Provides detailed diagnostics on user profiles, candidate pools, strategy scores, and final decisions.
 */

const { userAffinityStore } = require('../../events/UserAffinityStore');
const { hybridRecommendationEngine } = require('../engine/HybridRecommendationEngine');
const PlaybackContext = require('../model/PlaybackContext');
const { catalogStore } = require('../../catalog/catalogStore');
const { activeWeights } = require('../model/RecommendationWeights');

class RecommendationDebugService {
  getDebugReport(userId = 'usr_default_1', songId = null) {
    const userProfile = userAffinityStore.getUserProfileSummary(userId);
    const activeTrack = songId ? catalogStore.getTrackById(songId) : catalogStore.getAllTracks()[0];

    const currentContext = PlaybackContext.fromTrack(activeTrack, { userId });
    const allTracks = catalogStore.getAllTracks();

    // Strategy candidate generation details
    const candidates = [];
    const scores = [];
    const strategyBreakdown = [];

    for (const strategy of hybridRecommendationEngine.strategies) {
      const results = strategy.generateCandidates(currentContext, allTracks, activeWeights);
      strategyBreakdown.push({
        strategy: strategy.name,
        topCandidate: results[0] ? { songTitle: results[0].track.title, score: results[0].score } : null
      });
    }

    const finalRecommendations = hybridRecommendationEngine.recommend(currentContext);

    return {
      userId,
      userProfile,
      currentContext: {
        songTitle: activeTrack?.title,
        artist: activeTrack?.artist,
        movie: activeTrack?.movie,
        genre: activeTrack?.genre,
        mood: currentContext.currentMood,
        language: currentContext.currentLanguage
      },
      rankingFactors: {
        weights: activeWeights,
        strategyBreakdown
      },
      candidatesCount: allTracks.length,
      finalRecommendations: finalRecommendations.slice(0, 10).map((r, idx) => ({
        rank: idx + 1,
        songId: r.track.id,
        songTitle: r.track.title,
        artist: r.track.artist,
        score: parseFloat(r.score.toFixed(4)),
        strategy: r.strategy,
        reason: r.reason
      }))
    };
  }
}

const recommendationDebugService = new RecommendationDebugService();

module.exports = {
  RecommendationDebugService,
  recommendationDebugService
};
