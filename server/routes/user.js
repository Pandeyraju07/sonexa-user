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

module.exports = router;
