const express = require('express');
const router = express.Router();
const { tracks } = require('../data/catalog');

/**
 * POST /api/v1/voice/commands
 */
router.post('/commands', (req, res) => {
  const { text = '', locale = 'en-IN', context = {} } = req.body || {};
  const lower = text.trim().toLowerCase();

  // Direct playback controls
  if (lower === 'pause' || lower.includes('pause music') || lower.includes('ruk') || lower.includes('roko')) {
    return res.json({
      success: true,
      intent: 'PAUSE',
      message: 'Music paused.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'PAUSE', targetId: null, targetTitle: null }
    });
  }

  if (lower === 'resume' || lower.includes('resume music') || lower.includes('chalao')) {
    return res.json({
      success: true,
      intent: 'RESUME',
      message: 'Resuming playback.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'RESUME', targetId: null, targetTitle: null }
    });
  }

  if (lower === 'next' || lower.includes('next song') || lower.includes('agla gana') || lower.includes('skip')) {
    return res.json({
      success: true,
      intent: 'NEXT',
      message: 'Skipping to next track.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'NEXT', targetId: null, targetTitle: null }
    });
  }

  if (lower === 'previous' || lower.includes('previous song') || lower.includes('pichhla gana')) {
    return res.json({
      success: true,
      intent: 'PREVIOUS',
      message: 'Playing previous track.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'PREVIOUS', targetId: null, targetTitle: null }
    });
  }

  if (lower.includes('shuffle')) {
    return res.json({
      success: true,
      intent: 'SHUFFLE',
      message: 'Toggled shuffle mode.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'SHUFFLE', targetId: null, targetTitle: null }
    });
  }

  if (lower.includes('repeat') || lower.includes('loop')) {
    return res.json({
      success: true,
      intent: 'REPEAT',
      message: 'Toggled repeat mode.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'REPEAT', targetId: null, targetTitle: null }
    });
  }

  if (lower.includes('like') || lower.includes('favorite')) {
    return res.json({
      success: true,
      intent: 'LIKE_TRACK',
      message: 'Added to Liked Songs.',
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'LIKE_TRACK', targetId: null, targetTitle: null }
    });
  }

  // Audiobooks
  if (lower.includes('audiobook') || lower.includes('kitab') || lower.includes('story')) {
    const bookTitle = text.replace(/(play|find|listen to|audiobook|book|in hindi|in english)/gi, '').trim() || 'Classics';
    return res.json({
      success: true,
      intent: 'PLAY_AUDIOBOOK',
      message: `Opening audiobook '${bookTitle}'`,
      requiresConfirmation: false,
      results: [],
      action: { actionType: 'PLAY_AUDIOBOOK', targetId: null, targetTitle: bookTitle }
    });
  }

  // Music Catalog Resolution
  const query = text.replace(/^(play|search|find|listen to|sunao|chalao|bajao)\s+/i, '').trim() || text;
  const matched = tracks.filter(t => t.title?.toLowerCase().includes(query.toLowerCase()) || t.artist?.toLowerCase().includes(query.toLowerCase()));
  const candidates = matched.length ? matched : tracks.slice(0, 5);

  const isPlay = lower.startsWith('play') || lower.includes('chalao') || lower.includes('bajao');
  const intent = isPlay ? 'PLAY_TRACK' : 'SEARCH_CONTENT';
  const ambiguous = candidates.length > 3;

  return res.json({
    success: true,
    intent,
    message: candidates.length === 0 ? 'No matching tracks found.' : (ambiguous ? 'Found multiple tracks. Which one would you like?' : `Playing ${candidates[0].title}`),
    requiresConfirmation: ambiguous,
    results: candidates,
    action: candidates.length ? { actionType: intent, targetId: candidates[0].id, targetTitle: candidates[0].title } : null
  });
});

module.exports = router;
