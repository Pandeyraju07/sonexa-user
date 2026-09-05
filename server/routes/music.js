const express = require('express');
const router = express.Router();
const { tracks, artists, playlists, podcasts, liveEvents } = require('../data/catalog');

// Helper to build Home Feed payload
function buildHomeFeed() {
  return {
    userDisplayName: 'Raju Pandey',
    userPhotoUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=RP&backgroundColor=8b5cf6,ec4899&textColor=ffffff',
    continueListening: [tracks[0], tracks[1]],
    trendingNow: tracks,
    madeForYou: [tracks[2], tracks[4], tracks[0]],
    featuredPlaylists: playlists,
    topArtists: artists,
    recommendedForYou: [tracks[3], tracks[5]],
    popularRadio: [
      { id: 'rad_1', title: 'Arijit Singh Radio', subtitle: 'With Pritam, Atif Aslam, Mohit Chauhan', coverUrl: tracks[1].coverUrl },
      { id: 'rad_2', title: 'Indie Vibes Radio', subtitle: 'With Anuv Jain, Prateek Kuhad, Jasleen Royal', coverUrl: tracks[2].coverUrl }
    ],
    categories: ['All', 'Music', 'Podcasts', 'Live Events', 'I-Pop'],
    heroBanners: [
      {
        id: 'banner_1',
        title: 'Zynera Music Intelligence 2.0',
        subtitle: 'Understand why you love music with 15 breakthrough AI features',
        badge: 'NEW ERA',
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
    data: tracks
  });
});

// GET /api/v1/music/search
router.get('/search', (req, res) => {
  const query = (req.query.q || '').toLowerCase().trim();
  if (!query) {
    return res.json({
      success: true,
      data: {
        tracks: tracks.slice(0, 5),
        artists: artists.slice(0, 3),
        playlists: playlists.slice(0, 2),
        albums: []
      }
    });
  }

  const matchedTracks = tracks.filter(t =>
    t.title.toLowerCase().includes(query) ||
    t.artist.toLowerCase().includes(query) ||
    t.genre.toLowerCase().includes(query)
  );

  const matchedArtists = artists.filter(a =>
    a.name.toLowerCase().includes(query) ||
    a.genre.toLowerCase().includes(query)
  );

  const matchedPlaylists = playlists.filter(p =>
    p.title.toLowerCase().includes(query) ||
    p.description.toLowerCase().includes(query)
  );

  res.json({
    success: true,
    data: {
      tracks: matchedTracks.length ? matchedTracks : tracks.slice(0, 3),
      artists: matchedArtists,
      playlists: matchedPlaylists,
      albums: []
    }
  });
});

// GET /api/v1/music/tracks/:id
router.get('/tracks/:id', (req, res) => {
  const track = tracks.find(t => t.id === req.params.id) || tracks[0];
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
