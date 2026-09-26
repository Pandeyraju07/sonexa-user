const express = require('express');
const router = express.Router();
const { catalogStore } = require('../catalog/catalogStore');
const { unifiedSearchService } = require('../search/service/UnifiedSearchService');
const { hybridRecommendationEngine } = require('../recommendation/engine/HybridRecommendationEngine');
const PlaybackContext = require('../recommendation/model/PlaybackContext');
const { tracks: fallbackTracks, artists: fallbackArtists, playlists: fallbackPlaylists, podcasts, liveEvents } = require('../data/catalog');

// Helper to build Home Feed payload
function buildHomeFeed() {
  const allTracks = catalogStore.getAllTracks();
  const context = new PlaybackContext({ limit: 10 });
  const recs = hybridRecommendationEngine.recommend(context);

  return {
    userDisplayName: 'Raju Pandey',
    userPhotoUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=RP&backgroundColor=8b5cf6,ec4899&textColor=ffffff',
    continueListening: allTracks.slice(0, 4),
    trendingNow: allTracks,
    madeForYou: recs.slice(0, 6).map(r => r.track),
    featuredPlaylists: catalogStore.playlists,
    topArtists: catalogStore.artists,
    recommendedForYou: recs.slice(0, 4).map(r => r.track),
    popularRadio: [
      { id: 'rad_1', title: 'Arijit Singh Radio', subtitle: 'With Pritam, Atif Aslam, Mohit Chauhan', coverUrl: allTracks[1].coverUrl },
      { id: 'rad_2', title: 'Indie Vibes Radio', subtitle: 'With Anuv Jain, Prateek Kuhad, Jasleen Royal', coverUrl: allTracks[2].coverUrl }
    ],
    categories: ['All', 'Music', 'Podcasts', 'Live Events', 'I-Pop'],
    heroBanners: [
      {
        id: 'banner_1',
        title: 'Zynera Music Intelligence 2.0',
        subtitle: 'Enterprise Hybrid Recommendation & Semantic Search Engine Active',
        badge: 'SPOTIFY-GRADE AI',
        actionUrl: 'zynera://music-intelligence'
      }
    ]
  };
}

// GET /api/v1/music/home
router.get('/home', (req, res) => {
  res.json({
    success: true,
    data: buildHomeFeed()
  });
});

// GET /api/v1/music/trending
router.get('/trending', (req, res) => {
  res.json({
    success: true,
    data: catalogStore.getAllTracks()
  });
});

// GET /api/v1/music/search (Upgraded with semantic intent and ranking)
router.get('/search', (req, res) => {
  const query = (req.query.q || '').trim();
  const userId = req.query.userId || req.headers['x-user-id'] || 'usr_default_1';

  const searchResult = unifiedSearchService.search(query, userId);

  res.json({
    success: true,
    data: {
      tracks: searchResult.songs,
      artists: searchResult.artists,
      playlists: searchResult.playlists,
      albums: searchResult.albums,
      movies: searchResult.movies,
      intent: searchResult.intent,
      topResult: searchResult.topResult
    }
  });
});

// GET /api/v1/music/tracks/:id
router.get('/tracks/:id', (req, res) => {
  const track = catalogStore.getTrackById(req.params.id) || catalogStore.getAllTracks()[0];
  res.json({
    success: true,
    data: track
  });
});

// GET /api/v1/music/artists/:id
router.get('/artists/:id', (req, res) => {
  const artist = artists.find(a => a.id === req.params.id) || artists[0];
  const artistTracks = tracks.filter(t => t.artists?.includes(artist.name) || t.artist.includes(artist.name));
  res.json({
    success: true,
    data: {
      ...artist,
      tracks: artistTracks.length ? artistTracks : tracks.slice(0, 3)
    }
  });
});

// GET /api/v1/music/artists
router.get('/artists', (req, res) => {
  res.json({
    success: true,
    data: artists
  });
});

// GET /api/v1/music/playlists/:id
router.get('/playlists/:id', (req, res) => {
  const playlist = playlists.find(p => p.id === req.params.id) || playlists[0];
  const resolvedTracks = tracks.filter(t => playlist.tracks?.includes(t.id));
  res.json({
    success: true,
    data: {
      ...playlist,
      tracks: resolvedTracks.length ? resolvedTracks : tracks
    }
  });
});

// GET /api/v1/music/genres
router.get('/genres', (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 'g_bollywood', name: 'Bollywood', coverUrl: tracks[1].coverUrl, color: '#EC4899' },
      { id: 'g_indie', name: 'Indie Acoustic', coverUrl: tracks[2].coverUrl, color: '#8B5CF6' },
      { id: 'g_pop', name: 'Global Pop', coverUrl: tracks[5].coverUrl, color: '#38BDF8' },
      { id: 'g_synth', name: 'Synthwave / R&B', coverUrl: tracks[3].coverUrl, color: '#F59E0B' },
      { id: 'g_punjabi', name: 'Punjabi Hits', coverUrl: tracks[0].coverUrl, color: '#10B981' }
    ]
  });
});

// GET /api/v1/music/moods
router.get('/moods', (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 'm_chill', name: 'Chill & Relax', icon: '☕', color: '#6366F1' },
      { id: 'm_focus', name: 'Deep Focus & Study', icon: '🧠', color: '#10B981' },
      { id: 'm_workout', name: 'High Energy Workout', icon: '⚡', color: '#EF4444' },
      { id: 'm_drive', name: 'Late Night Drive', icon: '🌙', color: '#8B5CF6' },
      { id: 'm_romance', name: 'Romantic Flow', icon: '❤️', color: '#EC4899' }
    ]
  });
});

// GET /api/v1/music/tracks/:id/lyrics
router.get('/tracks/:id/lyrics', (req, res) => {
  const track = tracks.find(t => t.id === req.params.id) || tracks[0];
  res.json({
    success: true,
    data: {
      trackId: track.id,
      title: track.title,
      artist: track.artist,
      plainLyrics: track.lyrics?.replace(/\[.*?\]\s*/g, '') || 'Lyrics available soon.',
      syncedLyrics: track.lyrics || ''
    }
  });
});

// GET /api/v1/music/lyrics
router.get('/lyrics', (req, res) => {
  const trackId = req.query.trackId;
  const track = tracks.find(t => t.id === trackId) || tracks[0];
  res.json({
    success: true,
    data: {
      trackId: track.id,
      title: track.title,
      artist: track.artist,
      plainLyrics: track.lyrics?.replace(/\[.*?\]\s*/g, '') || '',
      syncedLyrics: track.lyrics || ''
    }
  });
});

module.exports = { router, buildHomeFeed };
