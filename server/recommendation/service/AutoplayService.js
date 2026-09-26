/**
 * AutoplayService
 * Generates continuous, non-stop autoplay recommendations when the queue finishes.
 */

const { hybridRecommendationEngine } = require('../engine/HybridRecommendationEngine');
const PlaybackContext = require('../model/PlaybackContext');
const { catalogStore } = require('../../catalog/catalogStore');

class AutoplayService {
  getAutoplay(params = {}) {
    const { currentSongId, userId = 'usr_anonymous', sessionId, limit = 5 } = params;
    const currentTrack = currentSongId ? catalogStore.getTrackById(currentSongId) : catalogStore.getAllTracks()[0];

    const context = PlaybackContext.fromTrack(currentTrack, {
      userId,
      sessionId,
      limit: parseInt(limit) || 5
    });

    const recommendations = hybridRecommendationEngine.recommend(context);

    return {
      currentSong: currentTrack,
      recommendations: recommendations.slice(0, limit).map(r => ({
        song: r.track,
        score: parseFloat(r.score.toFixed(4)),
        reason: r.reason
      }))
    };
  }
}

const autoplayService = new AutoplayService();

module.exports = {
  AutoplayService,
  autoplayService
};
