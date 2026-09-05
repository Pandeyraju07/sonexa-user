const express = require('express');
const router = express.Router();
const { tracks, artists } = require('../data/catalog');

// POST /api/v1/ai/signature
router.post('/signature', (req, res) => {
  const { mood, prompt } = req.body || {};
  res.json({
    success: true,
    data: {
      signatureName: prompt ? `Sonic Blend: ${prompt.slice(0, 24)}` : 'Cosmic Melodic Flow',
      acousticProfile: {
        primaryGenre: 'Indie Acoustic & Bollywood Melodic',
        energyRating: 0.72,
        danceabilityRating: 0.65,
        emotionalDepth: 'High Melancholy / Uplifting',
        tempoRange: '95 - 128 BPM'
      },
      recommendedTracks: tracks.slice(0, 4)
    }
  });
});

// POST /api/v1/ai/chat
router.post('/chat', (req, res) => {
  const { message, conversationHistory } = req.body || {};
  const query = (message || '').toLowerCase();

  let reply = "I'm your Zynera AI Music Intelligence assistant. I can curate playlists, explain song cultures, or adjust your queue based on your vibe.";
  let suggestedAction = null;

  if (query.includes('arijit') || query.includes('romantic')) {
    reply = "Playing soulful romantic melodies starting with 'Kesariya' by Arijit Singh & Pritam.";
    suggestedAction = { type: 'PLAY_TRACK', track: tracks[1] };
  } else if (query.includes('party') || query.includes('dance') || query.includes('energy')) {
    reply = "Turning up the energy with 'Starboy' and 'Blinding Lights'!";
    suggestedAction = { type: 'PLAY_TRACK', track: tracks[0] };
  } else if (query.includes('chill') || query.includes('acoustic') || query.includes('peace')) {
    reply = "Here is a calming acoustic session featuring Anuv Jain's 'Husn'.";
    suggestedAction = { type: 'PLAY_TRACK', track: tracks[2] };
  }

  res.json({
    success: true,
    data: {
      reply,
      suggestedAction,
      context: 'Music Intelligence Session Active'
    }
  });
});

// POST /api/v1/ai/intent
router.post('/intent', (req, res) => {
  const { prompt, query } = req.body || {};
  const text = prompt || query || 'play something chill';
  res.json({
    success: true,
    data: {
      intentType: 'PLAY_MOOD_SELECTION',
      recognizedArtists: ['Arijit Singh', 'Anuv Jain'],
      targetMood: 'Chill Acoustic',
      energyTarget: 0.55,
      suggestedQueue: tracks
    }
  });
});

// POST /api/v1/ai/change-vibe
router.post('/change-vibe', (req, res) => {
  const { targetVibe, energyDelta } = req.body || {};
  res.json({
    success: true,
    data: {
      message: `Vibe shifted to ${targetVibe || 'Energetic Uplifting'}`,
      updatedQueue: tracks.slice().reverse()
    }
  });
});

// POST /api/v1/ai/fix-queue
router.post('/fix-queue', (req, res) => {
  res.json({
    success: true,
    data: {
      message: 'Queue smoothed for harmonic transition',
      smoothedQueue: tracks
    }
  });
});

// POST /api/v1/ai/music-journey
router.post('/music-journey', (req, res) => {
  const { theme, duration } = req.query || {};
  res.json({
    success: true,
    data: {
      journeyId: `journey_${Date.now()}`,
      theme: theme || 'CALM_TO_ENERGETIC',
      durationMinutes: parseInt(duration) || 60,
      phases: [
        { phase: 'Calm Introduction', tracks: [tracks[2]] },
        { phase: 'Harmonic Rise', tracks: [tracks[1], tracks[4]] },
        { phase: 'Peak Energy', tracks: [tracks[0], tracks[3], tracks[5]] },
        { phase: 'Warm Cooldown', tracks: [tracks[2]] }
      ]
    }
  });
});

// POST /api/v1/ai/dj/next
router.post('/dj/next', (req, res) => {
  const currentTrack = req.body || tracks[0];
  const nextTrack = tracks.find(t => t.id !== currentTrack.id) || tracks[1];
  res.json({
    success: true,
    data: {
      track: nextTrack,
      confidence: 0.94,
      djVoiceIntro: `Next up, flowing from ${currentTrack.artist || 'the last vibe'} into ${nextTrack.title}.`,
      reason: 'Harmonic tempo match and acoustic energy alignment'
    }
  });
});

// POST /api/v1/ai/playlist
router.post('/playlist', (req, res) => {
  res.json({
    success: true,
    data: tracks
  });
});

module.exports = router;
