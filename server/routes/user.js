const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { users } = require('./auth');
const { tracks, playlists } = require('../data/catalog');

// Avatar upload configuration
const uploadDir = path.join(__dirname, '..', 'public', 'uploads', 'avatars');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `avatar_${Date.now()}${ext}`);
  }
});
const upload = multer({ storage, limits: { fileSize: 10 * 1024 * 1024 } });

// User settings store
const userSettings = {
  audioQuality: 'Lossless (320k)',
  streamingQuality: 'High',
  downloadQuality: 'Lossless (320k)',
  downloadOverWifiOnly: false,
  crossfade: true,
  crossfadeDurationSec: 4,
  normalizeVolume: true,
  gaplessPlayback: true,
  explicitContent: true,
  offlineMode: false,
  pushNotifications: true,
  friendActivity: true,
  newReleaseAlerts: true,
  theme: 'Dark',
  accentStyle: 'Vibrant Magenta',
  twoFactorEnabled: false,
  personalizedAds: false,
  shareListeningActivity: true
};

const userLikedTrackIds = new Set(['track_2', 'track_3', 'track_5']);

// Listening history store (persistent across requests)
const userListeningHistory = [
  {
    id: 'hist_1',
    contentId: 'track_2',
    contentType: 'song',
    title: 'Apna Bana Le',
    artist: 'Arijit Singh, Sachin-Jigar',
    coverUrl: 'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80',
    playedAt: new Date(Date.now() - 15 * 60 * 1000).toISOString(),
    progressSeconds: 180,
    durationSeconds: 261,
    completed: false
  },
  {
    id: 'hist_2',
    contentId: 'ab_geeta',
    contentType: 'audiobook',
    title: 'श्रीमद्भगवद्गीता (Complete Geeta)',
    artist: 'Shailendra Bharti',
    coverUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80',
    chapterId: 'ab_geeta_ch1',
    chapterTitle: 'अध्याय 1: अर्जुनविषादयोग',
    playedAt: new Date(Date.now() - 90 * 60 * 1000).toISOString(),
    progressSeconds: 620,
    durationSeconds: 2400,
    completed: false
  }
];

// Playback progress map: key = contentId -> progress object
const userPlaybackProgressMap = new Map();
userPlaybackProgressMap.set('track_2', {
  contentId: 'track_2',
  contentType: 'song',
  positionMs: 180000,
  durationMs: 261000,
  percentage: 69,
  updatedAt: new Date(Date.now() - 15 * 60 * 1000).toISOString()
});
userPlaybackProgressMap.set('ab_geeta', {
  contentId: 'ab_geeta',
  contentType: 'audiobook',
  chapterId: 'ab_geeta_ch1',
  chapterNumber: 1,
  positionMs: 620000,
  durationMs: 2400000,
  percentage: 26,
  updatedAt: new Date(Date.now() - 90 * 60 * 1000).toISOString()
});

// Bookmarks list
const userBookmarksList = [
  {
    id: 'bm_1',
    contentId: 'ab_geeta',
    chapterId: 'ab_geeta_ch1',
    chapterTitle: 'अध्याय 1: अर्जुनविषादयोग',
    positionSeconds: 450,
    note: 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन',
    createdAt: new Date(Date.now() - 120 * 60 * 1000).toISOString()
  }
];

// Helper to get active user
function getActiveUser(req) {
  // Return first user or default profile
  return Array.from(users.values())[0] || {
    userId: 'usr_default_1',
    email: 'listener@zynera.app',
    name: 'Raju Pandey',
    handle: '@rajupandey',
    bio: 'Music enthusiast • Hi-Fi Audio lover • Exploring AI vibes',
    profilePicUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=RP&backgroundColor=8b5cf6,ec4899&textColor=ffffff',
    isPremium: true,
    isEmailVerified: true,
    followersCount: 248,
    followingCount: 192
  };
}

// GET /api/v1/user/profile
router.get('/profile', (req, res) => {
  const user = getActiveUser(req);
  res.json({
    success: true,
    data: {
      userId: user.userId,
      name: user.name,
      email: user.email,
      handle: user.handle || `@${user.name.toLowerCase().replace(/\s+/g, '')}`,
      bio: user.bio || 'Listening on Zynera',
      profilePicUrl: user.profilePicUrl || '',
      isPremium: user.isPremium ?? true,
      isEmailVerified: user.isEmailVerified ?? true,
      followersCount: user.followersCount ?? 248,
      followingCount: user.followingCount ?? 192
    }
  });
});

// PUT /api/v1/user/profile
router.put('/profile', (req, res) => {
  const { name, bio, handle, profilePicUrl } = req.body;
  const user = getActiveUser(req);

  if (name) user.name = name.trim();
  if (bio !== undefined) user.bio = bio.trim();
  if (handle) user.handle = handle.startsWith('@') ? handle : `@${handle}`;
  if (profilePicUrl !== undefined) user.profilePicUrl = profilePicUrl;

  res.json({
    success: true,
    message: 'Profile updated successfully',
    data: user
  });
});

// POST /api/v1/user/avatar (Upload photo)
router.post('/avatar', upload.single('photo'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, message: 'No photo uploaded' });
  }
  const host = req.get('host');
  const protocol = req.protocol;
  const photoUrl = `${protocol}://${host}/uploads/avatars/${req.file.filename}`;

  const user = getActiveUser(req);
  user.profilePicUrl = photoUrl;

  res.json({
    success: true,
    message: 'Avatar uploaded successfully',
    data: {
      profilePicUrl: photoUrl
    }
  });
});

// GET /api/v1/user/library
router.get('/library', (req, res) => {
  const liked = tracks.filter(t => userLikedTrackIds.has(t.id));
  res.json({
    success: true,
    data: {
      likedSongs: liked,
      playlists: playlists,
      downloadedCount: 42,
      savedAlbumsCount: 14,
      followedArtistsCount: 28
    }
  });
});

// POST /api/v1/user/like
router.post('/like', (req, res) => {
  const { trackId, songId, isLiked } = req.body;
  const targetId = trackId || songId;

  if (isLiked) {
    userLikedTrackIds.add(targetId);
  } else {
    userLikedTrackIds.delete(targetId);
  }

  res.json({
    success: true,
    isLiked: userLikedTrackIds.has(targetId),
    message: isLiked ? 'Added to Liked Songs' : 'Removed from Liked Songs'
  });
});

// GET /api/v1/user/playlists
router.get('/playlists', (req, res) => {
  res.json({
    success: true,
    data: playlists
  });
});

// POST /api/v1/user/playlists
router.post('/playlists', (req, res) => {
  const { title, description } = req.body;
  const newPl = {
    id: `pl_user_${Date.now()}`,
    title: title || 'My New Playlist',
    description: description || 'Created with Zynera',
    coverUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80',
    trackCount: 0,
    tracks: [],
    isCurated: false
  };
  playlists.push(newPl);
  res.json(newPl);
});

// GET /api/v1/user/settings
router.get('/settings', (req, res) => {
  res.json({
    success: true,
    data: userSettings
  });
});

// PUT /api/v1/user/settings
router.put('/settings', (req, res) => {
  Object.assign(userSettings, req.body);
  res.json({
    success: true,
    message: 'Settings updated successfully',
    data: userSettings
  });
});

// GET /api/v1/user/sessions
router.get('/sessions', (req, res) => {
  res.json({
    success: true,
    data: [
      {
        id: 'sess_1',
        deviceName: 'Android Device (Current)',
        deviceType: 'Smartphone',
        location: 'Mumbai, India',
        lastActive: 'Active now',
        isCurrent: true
      },
      {
        id: 'sess_2',
        deviceName: 'Chrome Web Player',
        deviceType: 'Desktop Browser',
        location: 'Bengaluru, India',
        lastActive: '2 days ago',
        isCurrent: false
      }
    ]
  });
});

// DELETE /api/v1/user/sessions/:sessionId
router.delete('/sessions/:sessionId', (req, res) => {
  res.json({
    success: true,
    message: `Session ${req.params.sessionId} revoked`
  });
});

// DELETE /api/v1/user/account
router.delete('/account', (req, res) => {
  res.json({
    success: true,
    message: 'Account scheduled for deletion'
  });
});

// GET /api/v1/user/premium
router.get('/premium', (req, res) => {
  res.json({
    success: true,
    data: {
      planName: 'Zynera Hi-Fi Individual',
      status: 'Active',
      expiresAt: '2027-09-05T00:00:00Z',
      features: [
        'Lossless 24-bit 96kHz Master Audio',
        'Offline Downloads & Unlimited Skips',
        '15 AI Music Intelligence Features & AI DJ',
        'Ad-free uninterrupted streaming'
      ]
    }
  });
});

// POST /api/v1/user/premium/subscribe
router.post('/premium/subscribe', (req, res) => {
  res.json({
    success: true,
    message: 'Subscribed to Zynera Hi-Fi plan!'
  });
});

// POST /api/v1/user/premium/redeem
router.post('/premium/redeem', (req, res) => {
  const { code } = req.body;
  res.json({
    success: true,
    message: `Coupon '${code || 'VIP'}' redeemed! 30 days free Hi-Fi activated.`,
    data: { daysAdded: 30 }
  });
});

// GET /api/v1/user/history (or /api/v1/me/history)
router.get('/history', (req, res) => {
  res.json({
    success: true,
    data: userListeningHistory
  });
});

// POST /api/v1/user/history
router.post('/history', (req, res) => {
  const { contentId, contentType, title, artist, coverUrl, chapterId, chapterTitle, progressSeconds, durationSeconds, completed } = req.body;
  const historyItem = {
    id: `hist_${Date.now()}`,
    contentId: contentId || `item_${Date.now()}`,
    contentType: contentType || 'song',
    title: title || 'Unknown',
    artist: artist || 'Zynera Artist',
    coverUrl: coverUrl || '',
    chapterId: chapterId || null,
    chapterTitle: chapterTitle || null,
    playedAt: new Date().toISOString(),
    progressSeconds: Number(progressSeconds) || 0,
    durationSeconds: Number(durationSeconds) || 0,
    completed: Boolean(completed)
  };
  userListeningHistory.unshift(historyItem);
  if (userListeningHistory.length > 50) userListeningHistory.pop();
  res.json({
    success: true,
    message: 'Added to listening history',
    data: historyItem
  });
});

// GET /api/v1/user/progress (or /api/v1/me/progress)
router.get('/progress', (req, res) => {
  const { contentId } = req.query;
  if (contentId) {
    const item = userPlaybackProgressMap.get(contentId);
    return res.json({
      success: true,
      data: item || null
    });
  }
  res.json({
    success: true,
    data: Array.from(userPlaybackProgressMap.values())
  });
});

// POST /api/v1/user/progress
router.post('/progress', (req, res) => {
  const { contentId, contentType, chapterId, chapterNumber, positionMs, durationMs } = req.body;
  if (!contentId) {
    return res.status(400).json({ success: false, message: 'contentId is required' });
  }
  const pos = Number(positionMs) || 0;
  const dur = Number(durationMs) || 1;
  const record = {
    contentId,
    contentType: contentType || 'song',
    chapterId: chapterId || null,
    chapterNumber: chapterNumber ? Number(chapterNumber) : null,
    positionMs: pos,
    durationMs: dur,
    percentage: Math.min(100, Math.round((pos / dur) * 100)),
    updatedAt: new Date().toISOString()
  };
  userPlaybackProgressMap.set(contentId, record);
  res.json({
    success: true,
    message: 'Playback progress synced successfully',
    data: record
  });
});

// GET /api/v1/user/bookmarks (or /api/v1/me/bookmarks)
router.get('/bookmarks', (req, res) => {
  const { contentId } = req.query;
  let result = userBookmarksList;
  if (contentId) {
    result = result.filter(b => b.contentId === contentId);
  }
  res.json({
    success: true,
    data: result
  });
});

// POST /api/v1/user/bookmarks
router.post('/bookmarks', (req, res) => {
  const { contentId, chapterId, chapterTitle, positionSeconds, note } = req.body;
  const newBm = {
    id: `bm_${Date.now()}`,
    contentId: contentId || 'item',
    chapterId: chapterId || null,
    chapterTitle: chapterTitle || null,
    positionSeconds: Number(positionSeconds) || 0,
    note: note ? note.trim() : '',
    createdAt: new Date().toISOString()
  };
  userBookmarksList.unshift(newBm);
  res.json({
    success: true,
    message: 'Bookmark saved',
    data: newBm
  });
});

// DELETE /api/v1/user/bookmarks/:id
router.delete('/bookmarks/:id', (req, res) => {
  const idx = userBookmarksList.findIndex(b => b.id === req.params.id);
  if (idx !== -1) {
    userBookmarksList.splice(idx, 1);
  }
  res.json({
    success: true,
    message: 'Bookmark removed'
  });
});

module.exports = router;

