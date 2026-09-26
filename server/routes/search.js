/**
 * Search Routes
 * Implements /api/search and /api/v1/search endpoints.
 */

const express = require('express');
const router = express.Router();
const { unifiedSearchService } = require('../search/service/UnifiedSearchService');

router.get('/', (req, res) => {
  const query = req.query.q || '';
  const userId = req.query.userId || req.headers['x-user-id'] || 'usr_default_1';

  const startTime = Date.now();
  const searchResults = unifiedSearchService.search(query, userId);
  const latencyMs = Date.now() - startTime;

  res.json({
    ...searchResults,
    latencyMs
  });
});

module.exports = router;
