/**
 * Playback & User Event Tracking Routes
 * Implements /api/events/playback and /api/v1/events/playback endpoints.
 */

const express = require('express');
const router = express.Router();
const { affinityEngine } = require('../events/AffinityEngine');

router.post('/playback', (req, res) => {
  const event = req.body || {};
  const userId = event.userId || req.headers['x-user-id'] || 'usr_default_1';

  // Asynchronous event processing to keep playback fast (<50ms response)
  setImmediate(() => {
    affinityEngine.processEvent({
      ...event,
      userId
    });
  });

  res.json({
    success: true,
    message: 'Playback event ingested successfully',
    eventId: event.eventId || `evt_${Date.now()}`
  });
});

// Generic event fallback
router.post('/', (req, res) => {
  const event = req.body || {};
  const userId = event.userId || req.headers['x-user-id'] || 'usr_default_1';

  setImmediate(() => {
    affinityEngine.processEvent({
      ...event,
      userId,
      eventType: event.eventType || event.type || 'INTERACTION'
    });
  });

  res.json({
    success: true,
    message: 'Event recorded'
  });
});

module.exports = router;
