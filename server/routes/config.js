const express = require('express');
const router = express.Router();
const { supportedLanguages } = require('../data/catalog');

// GET /api/v1/config/splash
router.get('/splash', (req, res) => {
  res.json({
    success: true,
    appName: 'Zynera',
    version: '2.4.0',
    minSupportedVersion: '2.0.0',
    forceUpdate: false,
    maintenanceMode: false,
    message: 'Welcome to Zynera Music Intelligence'
  });
});

// GET /api/v1/config/onboarding
router.get('/onboarding', (req, res) => {
  res.json({
    success: true,
    slides: [
      {
        title: 'Understand Why You Love Music',
        subtitle: '15 revolutionary Music Intelligence features analyzing your acoustic taste DNA.'
      },
      {
        title: 'Studio Master Lossless Audio',
        subtitle: 'Stream crystal-clear 24-bit 96kHz audio with zero compression artifacts.'
      },
      {
        title: 'Personalized AI DJ & Life Soundtracks',
        subtitle: 'Dynamic voice transitions and musical memories saved with every listening session.'
      }
    ]
  });
});

// GET /api/v1/config/languages
router.get('/languages', (req, res) => {
  res.json({
    success: true,
    title: 'Choose Music Languages',
    subtitle: 'Select languages you love to listen to',
    minSelection: 1,
    defaultSelected: ['hi', 'en'],
    languages: supportedLanguages
  });
});

// GET /api/v1/config/app-update
router.get('/app-update', (req, res) => {
  res.json({
    success: true,
    hasUpdate: false,
    latestVersion: '2.4.0',
    downloadUrl: '/download/apk',
    releaseNotes: 'Performance optimizations, reactive profile avatars, and 15 Music Intelligence enhancements.'
  });
});

// GET /api/v1/config/permissions
router.get('/permissions', (req, res) => {
  res.json({
    success: true,
    notificationsEnabled: true,
    downloadsEnabled: true
  });
});

module.exports = router;
