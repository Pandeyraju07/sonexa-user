/**
 * DynamicQueueService
 * Continuously adapts the playback queue in real-time as user interacts
 * (completing, skipping, liking, switching vibes).
 */

const { hybridRecommendationEngine } = require('../engine/HybridRecommendationEngine');
const PlaybackContext = require('../model/PlaybackContext');
const { catalogStore } = require('../../catalog/catalogStore');

class DynamicQueueService {
  /**
   * Generates or refreshes a dynamic queue given current session and recent events
   * @param {Object} params - { currentSongId, userId, sessionId, skippedGenres, recentSongIds, limit }
   * @returns {{ currentSong: Object, queue: Array<Object>, adaptedContext: Object }}
   */
  generateQueue(params = {}) {
    const currentTrack = params.currentSongId ? catalogStore.getTrackById(params.currentSongId) : null;
    const allTracks = catalogStore.getAllTracks();
    const activeTrack = currentTrack || allTracks[0];

    const context = PlaybackContext.fromTrack(activeTrack, {
      userId: params.userId || 'usr_anonymous',
      sessionId: params.sessionId,
      recentSongIds: params.recentSongIds || [],
      skippedGenres: params.skippedGenres || [],
      limit: params.limit || 15
    });

    const recommendations = hybridRecommendationEngine.recommend(context);
    const queueTracks = recommendations.map(r => ({
      ...r.track,
      recommendationReason: r.reason
    }));

    return {
      currentSong: activeTrack,
      queue: queueTracks,
      adaptedContext: {
        currentMood: context.currentMood,
        currentGenre: context.currentGenre,
        skippedGenres: params.skippedGenres || []
      }
    };
  }
}

const dynamicQueueService = new DynamicQueueService();

module.exports = {
  DynamicQueueService,
  dynamicQueueService
};
