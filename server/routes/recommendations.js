/**
 * Recommendation Routes
 * Implements /api/recommendations/* and /api/v1/recommendations/* endpoints.
 */

const express = require('express');
const router = express.Router();

const { hybridRecommendationEngine } = require('../recommendation/engine/HybridRecommendationEngine');
const { dynamicQueueService } = require('../recommendation/service/DynamicQueueService');
const { autoplayService } = require('../recommendation/service/AutoplayService');
const { coldStartService } = require('../recommendation/service/ColdStartService');
const PlaybackContext = require('../recommendation/model/PlaybackContext');
const { catalogStore } = require('../catalog/catalogStore');

// GET /api/recommendations/home
router.get('/home', (req, res) => {
  const userId = req.query.userId || req.headers['x-user-id'] || 'usr_default_1';
  const allTracks = catalogStore.getAllTracks();

  const context = new PlaybackContext({
    userId,
    currentMood: req.query.mood || 'ROMANTIC',
    currentLanguage: req.query.language || 'Hindi',
    limit: 20
  });

  const recommendations = hybridRecommendationEngine.recommend(context);

  res.json({
    success: true,
    data: {
      madeForYou: recommendations.slice(0, 6).map(r => r.track),
      continueListening: allTracks.slice(0, 4),
      trendingHits: allTracks.filter(t => (t.acousticFeatures?.popularity ?? 0) >= 0.9),
      recommendationsWithReasons: recommendations.map(r => ({
        song: r.track,
        score: parseFloat(r.score.toFixed(4)),
        reason: r.reason
      }))
    }
  });
});

// GET /api/recommendations/for-you
router.get('/for-you', (req, res) => {
  const userId = req.query.userId || req.headers['x-user-id'] || 'usr_default_1';
  const context = new PlaybackContext({
    userId,
    currentMood: req.query.mood,
    currentGenre: req.query.genre,
    currentLanguage: req.query.language || 'Hindi',
    limit: parseInt(req.query.limit) || 20
  });

  const recommendations = hybridRecommendationEngine.recommend(context);
  res.json({
    success: true,
    data: recommendations.map(r => ({
      song: r.track,
      score: parseFloat(r.score.toFixed(4)),
      reason: r.reason
    }))
  });
});

// GET /api/recommendations/queue
router.get('/queue', (req, res) => {
  const { currentSongId, userId, sessionId, skippedGenres } = req.query;
  const queueResult = dynamicQueueService.generateQueue({
    currentSongId,
    userId: userId || 'usr_default_1',
    sessionId,
    skippedGenres: skippedGenres ? skippedGenres.split(',') : [],
    limit: parseInt(req.query.limit) || 15
  });

  res.json({
    success: true,
    data: queueResult
  });
});

// GET /api/recommendations/autoplay
router.get('/autoplay', (req, res) => {
  const { currentSongId, userId, sessionId, limit } = req.query;
  const result = autoplayService.getAutoplay({
    currentSongId,
    userId: userId || 'usr_default_1',
    sessionId,
    limit: limit ? parseInt(limit) : 5
  });

  res.json(result);
});

// GET /api/recommendations/similar/:songId
router.get('/similar/:songId', (req, res) => {
  const songId = req.params.songId;
  const similar = hybridRecommendationEngine.getSimilarTracks(songId, parseInt(req.query.limit) || 10);
  res.json({
    success: true,
    data: similar.map(s => ({
      song: s.track,
      score: parseFloat(s.score.toFixed(4)),
      reason: s.reason
    }))
  });
});

// GET /api/recommendations/artist/:artistId
router.get('/artist/:artistId', (req, res) => {
  const artist = catalogStore.getArtistById(req.params.artistId) || catalogStore.findArtist(req.params.artistId);
  if (!artist) {
    return res.status(404).json({ success: false, message: 'Artist not found' });
  }

  const artistTracks = catalogStore.getTracksByArtist(artist.name);
  const context = new PlaybackContext({
    currentArtistName: artist.name,
    limit: 15
  });
  const peerRecommendations = hybridRecommendationEngine.recommend(context);

  res.json({
    success: true,
    data: {
      artist,
      topTracks: artistTracks,
      relatedRecommendations: peerRecommendations.map(r => ({
        song: r.track,
        score: parseFloat(r.score.toFixed(4)),
        reason: r.reason
      }))
    }
  });
});

// GET /api/recommendations/album/:albumId
router.get('/album/:albumId', (req, res) => {
  const album = catalogStore.getAlbumById(req.params.albumId) || catalogStore.albums[0];
  const albumTracks = catalogStore.getAllTracks().filter(t => t.albumId === album.id);
  const similarAlbums = hybridRecommendationEngine.vectorSearchService.findSimilarAlbums(album.id, 5);

  res.json({
    success: true,
    data: {
      album,
      tracks: albumTracks,
      similarAlbums
    }
  });
});

// GET /api/recommendations/mood/:mood
router.get('/mood/:mood', (req, res) => {
  const mood = req.params.mood.toUpperCase();
  const context = new PlaybackContext({ currentMood: mood, limit: 15 });
  const recommendations = hybridRecommendationEngine.recommend(context);

  res.json({
    success: true,
    data: recommendations.map(r => ({
      song: r.track,
      score: parseFloat(r.score.toFixed(4)),
      reason: r.reason
    }))
  });
});

// GET /api/recommendations/genre/:genre
router.get('/genre/:genre', (req, res) => {
  const genre = req.params.genre;
  const context = new PlaybackContext({ currentGenre: genre, limit: 15 });
  const recommendations = hybridRecommendationEngine.recommend(context);

  res.json({
    success: true,
    data: recommendations.map(r => ({
      song: r.track,
      score: parseFloat(r.score.toFixed(4)),
      reason: r.reason
    }))
  });
});

// GET /api/recommendations/movie/:movieId
router.get('/movie/:movieId', (req, res) => {
  const movie = catalogStore.getMovieById(req.params.movieId) || catalogStore.findMovie(req.params.movieId);
  if (!movie) {
    return res.status(404).json({ success: false, message: 'Movie not found' });
  }

  const movieTracks = catalogStore.getTracksByMovie(movie.id);
  res.json({
    success: true,
    data: {
      movie,
      soundtrack: movieTracks,
      trackCount: movieTracks.length
    }
  });
});

// GET /api/recommendations/cold-start
router.get('/cold-start', (req, res) => {
  const { languages, genres, moods } = req.query;
  const results = coldStartService.getColdStartRecommendations({
    languages: languages ? languages.split(',') : ['Hindi'],
    genres: genres ? genres.split(',') : ['Bollywood'],
    moods: moods ? moods.split(',') : ['ROMANTIC']
  });

  res.json({
    success: true,
    data: results
  });
});

module.exports = router;
