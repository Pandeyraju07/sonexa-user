/**
 * Admin Diagnostics & Recommendation Debug Routes
 * Implements /api/admin/recommendations/debug/:userId
 */

const express = require('express');
const router = express.Router();
const { recommendationDebugService } = require('../recommendation/service/RecommendationDebugService');
const { activeWeights } = require('../recommendation/model/RecommendationWeights');

// GET /api/admin/recommendations/debug/:userId
router.get('/recommendations/debug/:userId', (req, res) => {
  const userId = req.params.userId;
  const songId = req.query.songId || null;

  const debugReport = recommendationDebugService.getDebugReport(userId, songId);
  res.json(debugReport);
});

// GET /api/admin/weights
router.get('/weights', (req, res) => {
  res.json({
    success: true,
    weights: activeWeights
  });
});

// POST /api/admin/weights (Runtime weight adjustment)
router.post('/weights', (req, res) => {
  const updates = req.body || {};
  Object.assign(activeWeights, updates);
  activeWeights.normalize();

  res.json({
    success: true,
    message: 'Recommendation weights updated',
    weights: activeWeights
  });
});

module.exports = router;
