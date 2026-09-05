const express = require('express');
const cors = require('cors');
const compression = require('compression');
const path = require('path');
const fs = require('fs');

const { router: authRouter } = require('./routes/auth');
const userRouter = require('./routes/user');
const { router: musicRouter, buildHomeFeed } = require('./routes/music');
const aiRouter = require('./routes/ai');
const configRouter = require('./routes/config');
const { tracks, liveEvents } = require('./data/catalog');

const app = express();
const PORT = process.env.PORT || 8080;

// Security & Performance Middlewares
app.use(cors({ origin: '*' }));
app.use(compression());
app.use(express.json({ limit: '20mb' }));
app.use(express.urlencoded({ extended: true, limit: '20mb' }));

// Static files (Landing page & UI assets)
app.use(express.static(path.join(__dirname, 'public')));
app.use('/uploads', express.static(path.join(__dirname, 'public', 'uploads')));

// Health Check Endpoints (for Render zero-downtime monitoring)
app.get(['/health', '/api/v1/health', '/status'], (req, res) => {
  res.json({
    status: 'ok',
    app: 'Zynera Music Intelligence API',
    version: '2.4.0',
    timestamp: new Date().toISOString(),
    uptimeSeconds: Math.floor(process.uptime())
  });
});

// APK Direct Download Endpoints
const apkLocations = [
  path.join(__dirname, 'public', 'downloads', 'Zynera-v2.4.0.apk'),
  path.join(__dirname, '..', 'Zynera-v2.4.0.apk'),
  path.join(__dirname, '..', 'Zynera-Music-v2.4.0.apk'),
  path.join(__dirname, '..', 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
];

function handleApkDownload(req, res) {
  const existingApk = apkLocations.find(loc => fs.existsSync(loc));
  if (existingApk) {
    res.setHeader('Content-Type', 'application/vnd.android.package-archive');
    res.setHeader('Content-Disposition', 'attachment; filename="Zynera-v2.4.0.apk"');
    return res.sendFile(path.resolve(existingApk));
  }
  res.status(404).send('APK build in progress. Please check back shortly.');
}

app.get(['/download/apk', '/download/latest', '/apk', '/Zynera-v2.4.0.apk'], handleApkDownload);

// Mount API v1 Routers
app.use('/api/v1/auth', authRouter);
app.use('/api/v1/user', userRouter);
app.use('/api/v1/music', musicRouter);
app.use('/api/v1/ai', aiRouter);
app.use('/api/v1/config', configRouter);

// Flat fallback routes for compatibility
app.use('/api/v1/home', (req, res) => res.json({ success: true, data: buildHomeFeed() }));
app.use('/home', (req, res) => res.json({ success: true, data: buildHomeFeed() }));
app.use('/api/v1/events', (req, res) => res.json({ success: true, data: 'Event recorded' }));
app.use('/events', (req, res) => res.json({ success: true, data: 'Event recorded' }));

// Live Events
app.get('/api/v1/live-events/feed', (req, res) => {
  res.json({ success: true, data: liveEvents });
});
app.get('/api/v1/live-events/:id', (req, res) => {
  const event = liveEvents.find(e => e.id === req.params.id) || liveEvents[0];
  res.json({ success: true, data: event });
});

// Recommendations top-level aliases
app.get('/api/v1/recommendations', (req, res) => res.json({ success: true, data: tracks }));
app.get('/api/v1/recommendations/daily-mix', (req, res) => res.json({ success: true, data: tracks }));
app.get('/api/v1/recommendations/surprise', (req, res) => res.json({ success: true, data: tracks }));
app.get('/api/v1/recommendations/predictions', (req, res) => {
  res.json({
    success: true,
    data: [
      { track: tracks[0], probability: 0.92, rationale: 'Energy rise sequence match' },
      { track: tracks[1], probability: 0.88, rationale: 'Vocal timbre affinity' }
    ]
  });
});
app.get('/api/v1/recommendations/why/:trackId', (req, res) => {
  res.json({
    success: true,
    data: {
      trackId: req.params.trackId,
      explanation: 'Matches your acoustic preference for melodic chords and warm vocals.',
      matchingFeatures: ['Acoustic warmth', 'Minor key harmonic flow', 'High vocal presence']
    }
  });
});

// Music DNA
app.get('/api/v1/me/music-dna', (req, res) => {
  res.json({
    success: true,
    data: {
      corePersona: 'Emotional Explorer',
      primaryGenres: ['Indie Folk', 'Bollywood Melodic', 'Synthwave'],
      energyPreference: 0.68,
      acousticPreference: 0.74,
      danceability: 0.62,
      topAttributes: ['Soulful Lyrics', 'Dynamic Crescendos', 'Late Night Vibes']
    }
  });
});

// Profile setup aliases
app.get('/api/v1/profile-setup/genres', (req, res) => {
  res.json({
    success: true,
    data: [
      { id: 'g1', name: 'Bollywood', color: '#EC4899' },
      { id: 'g2', name: 'Indie Pop', color: '#8B5CF6' },
      { id: 'g3', name: 'Punjabi', color: '#10B981' },
      { id: 'g4', name: 'Global Hits', color: '#38BDF8' }
    ]
  });
});
app.post(['/api/v1/profile-setup/genres', '/api/v1/profile-setup/artists', '/api/v1/profile-setup/moods', '/api/v1/profile-setup/languages', '/api/v1/profile-setup/permissions'], (req, res) => {
  res.json({ success: true, message: 'Saved successfully' });
});

// SPA / Web Landing page fallback
app.get('*', (req, res) => {
  if (req.accepts('html')) {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
  } else {
    res.status(404).json({ success: false, message: 'Endpoint not found' });
  }
});

function startServer(portToUse) {
  const server = app.listen(portToUse, '0.0.0.0', () => {
    console.log(`🚀 Zynera Music Intelligence Web & API Server running on port ${portToUse}`);
    console.log(`🌐 Landing page & APK download ready at http://localhost:${portToUse}`);
    console.log(`📡 API v1 live at http://localhost:${portToUse}/api/v1/`);
  });

  server.on('error', (err) => {
    if (err.code === 'EACCES' || err.code === 'EADDRINUSE') {
      const fallbackPort = portToUse === 8080 ? 3000 : (portToUse === 3000 ? 5000 : 0);
      if (fallbackPort !== 0) {
        console.warn(`Port ${portToUse} unavailable (${err.code}). Trying fallback port ${fallbackPort}...`);
        startServer(fallbackPort);
      } else {
        console.error('Failed to bind server port:', err);
      }
    } else {
      console.error('Server error:', err);
    }
  });
}

startServer(PORT);

