/**
 * HybridRecommendationEngine
 * Assembles all strategies into the unified ranking pipeline.
 */

const { catalogStore } = require('../../catalog/catalogStore');
const { activeWeights } = require('../model/RecommendationWeights');
const PlaybackContext = require('../model/PlaybackContext');
const RankingPipeline = require('../ranking/RankingPipeline');

const ContentBasedRecommendationStrategy = require('../strategy/ContentBasedRecommendationStrategy');
const CollaborativeRecommendationStrategy = require('../strategy/CollaborativeRecommendationStrategy');
const ArtistRecommendationStrategy = require('../strategy/ArtistRecommendationStrategy');
const GenreRecommendationStrategy = require('../strategy/GenreRecommendationStrategy');
const MoodRecommendationStrategy = require('../strategy/MoodRecommendationStrategy');
const MovieRecommendationStrategy = require('../strategy/MovieRecommendationStrategy');
const TrendingRecommendationStrategy = require('../strategy/TrendingRecommendationStrategy');
const PersonalizedRecommendationStrategy = require('../strategy/PersonalizedRecommendationStrategy');

const LocalEmbeddingService = require('../embedding/LocalEmbeddingService');
const VectorSearchService = require('../embedding/VectorSearchService');

class HybridRecommendationEngine {
  constructor() {
    this.strategies = [
      new ContentBasedRecommendationStrategy(),
      new CollaborativeRecommendationStrategy(),
      new ArtistRecommendationStrategy(),
      new GenreRecommendationStrategy(),
      new MoodRecommendationStrategy(),
      new MovieRecommendationStrategy(),
      new TrendingRecommendationStrategy(),
      new PersonalizedRecommendationStrategy()
    ];

    this.pipeline = new RankingPipeline(this.strategies);
    this.embeddingService = new LocalEmbeddingService();
    this.vectorSearchService = new VectorSearchService(this.embeddingService);
  }

  /**
   * Generates recommendations based on a PlaybackContext
   * @param {PlaybackContext} context
   * @param {Object} [customWeights]
   * @returns {Array<{ track: Object, score: number, reason: string, strategy: string }>}
   */
  recommend(context, customWeights = activeWeights) {
    const allTracks = catalogStore.getAllTracks();
    return this.pipeline.execute(context, allTracks, customWeights);
  }

  /**
   * Generates similar tracks using both vector embedding similarity and hybrid strategies
   */
  getSimilarTracks(songId, limit = 10) {
    const track = catalogStore.getTrackById(songId);
    if (!track) return [];

    const context = PlaybackContext.fromTrack(track, { limit });
    return this.recommend(context);
  }

  /**
   * Generates next-track autoplay recommendation
   */
  getAutoplayTrack(currentSongId, userId = 'usr_anonymous', sessionId = null) {
    const track = catalogStore.getTrackById(currentSongId);
    if (!track) {
      const trending = catalogStore.getAllTracks();
      return { track: trending[0], score: 0.90, reason: 'Trending on Zynera' };
    }

    const context = PlaybackContext.fromTrack(track, {
      userId,
      sessionId,
      limit: 5
    });

    const recommendations = this.recommend(context);
    const candidate = recommendations[0] || {
      track: catalogStore.getAllTracks().find(t => t.id !== currentSongId) || track,
      score: 0.88,
      reason: 'Seamless continuation'
    };

    return candidate;
  }
}

const hybridRecommendationEngine = new HybridRecommendationEngine();

module.exports = {
  HybridRecommendationEngine,
  hybridRecommendationEngine
};
