const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || 'zynera_super_secure_jwt_secret_2026';

// In-memory user database
const users = new Map([
  [
    'listener@zynera.app',
    {
      userId: 'usr_default_1',
      email: 'listener@zynera.app',
      name: 'Raju Pandey',
      handle: '@rajupandey',
      bio: 'Music enthusiast • Hi-Fi Audio lover • Exploring AI vibes',
      profilePicUrl: 'https://api.dicebear.com/7.x/initials/svg?seed=RP&backgroundColor=8b5cf6,ec4899&textColor=ffffff',
      isPremium: true,
      isEmailVerified: true,
      followersCount: 248,
      followingCount: 192,
      password: 'password123',
      createdAt: new Date().toISOString()
    }
  ]
]);

function generateTokens(user) {
  const accessToken = jwt.sign(
    { userId: user.userId, email: user.email, name: user.name },
    JWT_SECRET,
    { expiresIn: '30d' }
  );
  const refreshToken = jwt.sign(
    { userId: user.userId, email: user.email, type: 'refresh' },
    JWT_SECRET,
    { expiresIn: '90d' }
  );
  return { accessToken, refreshToken };
}

// POST /api/v1/auth/register
router.post('/register', (req, res) => {
  const { email, name, password, handle } = req.body;
  if (!email || !password) {
    return res.status(400).json({
      success: false,
      message: 'Email and password are required'
    });
  }

  const normalizedEmail = email.toLowerCase().trim();
  if (users.has(normalizedEmail)) {
    return res.status(409).json({
      success: false,
      message: 'An account with this email already exists'
    });
  }

  const userId = `usr_${Date.now()}`;
  const resolvedName = name?.trim() || normalizedEmail.split('@')[0];
  const resolvedHandle = handle?.trim() || `@${resolvedName.toLowerCase().replace(/\s+/g, '')}`;

  const newUser = {
    userId,
    email: normalizedEmail,
    name: resolvedName,
    handle: resolvedHandle,
    bio: 'Music lover exploring Zynera Hi-Fi audio',
    profilePicUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(resolvedName)}&backgroundColor=8b5cf6,ec4899&textColor=ffffff`,
    isPremium: false,
    isEmailVerified: true,
    followersCount: 1,
    followingCount: 0,
    password,
    createdAt: new Date().toISOString()
  };

  users.set(normalizedEmail, newUser);
  const tokens = generateTokens(newUser);

  res.json({
    success: true,
    message: 'Registration successful',
    data: {
      user: {
        userId: newUser.userId,
        name: newUser.name,
        email: newUser.email,
        handle: newUser.handle,
        bio: newUser.bio,
        profilePicUrl: newUser.profilePicUrl,
        isPremium: newUser.isPremium,
        isEmailVerified: newUser.isEmailVerified
      },
      tokens: {
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresIn: 2592000
      }
    }
  });
});

// POST /api/v1/auth/login
router.post('/login', (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({
      success: false,
      message: 'Email and password are required'
    });
  }

  const normalizedEmail = email.toLowerCase().trim();
  let user = users.get(normalizedEmail);

  if (!user) {
    // Auto-create user for demo convenience if not found
    const userId = `usr_${Date.now()}`;
    const resolvedName = normalizedEmail.split('@')[0];
    user = {
      userId,
      email: normalizedEmail,
      name: resolvedName.charAt(0).toUpperCase() + resolvedName.slice(1),
      handle: `@${resolvedName}`,
      bio: 'Music lover on Zynera',
      profilePicUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(resolvedName)}&backgroundColor=8b5cf6,ec4899&textColor=ffffff`,
      isPremium: true,
      isEmailVerified: true,
      followersCount: 15,
      followingCount: 8,
      password,
      createdAt: new Date().toISOString()
    };
    users.set(normalizedEmail, user);
  }

  const tokens = generateTokens(user);

  res.json({
    success: true,
    message: 'Login successful',
    data: {
      user: {
        userId: user.userId,
        name: user.name,
        email: user.email,
        handle: user.handle,
        bio: user.bio,
        profilePicUrl: user.profilePicUrl,
        isPremium: user.isPremium,
        isEmailVerified: user.isEmailVerified
      },
      tokens: {
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresIn: 2592000
      }
    }
  });
});

// POST /api/v1/auth/google
router.post('/google', (req, res) => {
  const { idToken, email, name, photoUrl } = req.body;
  const targetEmail = (email || 'google.user@zynera.app').toLowerCase().trim();
  let user = users.get(targetEmail);

  if (!user) {
    const userId = `usr_g_${Date.now()}`;
    const resolvedName = name || targetEmail.split('@')[0];
    user = {
      userId,
      email: targetEmail,
      name: resolvedName,
      handle: `@${resolvedName.toLowerCase().replace(/\s+/g, '')}`,
      bio: 'Connected via Google',
      profilePicUrl: photoUrl || `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(resolvedName)}&backgroundColor=8b5cf6,ec4899&textColor=ffffff`,
      isPremium: true,
      isEmailVerified: true,
      followersCount: 50,
      followingCount: 20,
      createdAt: new Date().toISOString()
    };
    users.set(targetEmail, user);
  }

  const tokens = generateTokens(user);
  res.json({
    success: true,
    message: 'Google sign-in successful',
    data: {
      user: {
        userId: user.userId,
        name: user.name,
        email: user.email,
        handle: user.handle,
        bio: user.bio,
        profilePicUrl: user.profilePicUrl,
        isPremium: user.isPremium,
        isEmailVerified: user.isEmailVerified
      },
      tokens: {
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresIn: 2592000
      }
    }
  });
});

// POST /api/v1/auth/apple
router.post('/apple', (req, res) => {
  const { email, fullName } = req.body;
  const targetEmail = (email || 'apple.user@zynera.app').toLowerCase().trim();
  let user = users.get(targetEmail);

  if (!user) {
    const userId = `usr_a_${Date.now()}`;
    const resolvedName = fullName || 'Apple Listener';
    user = {
      userId,
      email: targetEmail,
      name: resolvedName,
      handle: `@apple_listener`,
      bio: 'Connected via Apple',
      profilePicUrl: `https://api.dicebear.com/7.x/initials/svg?seed=Apple&backgroundColor=111827&textColor=ffffff`,
      isPremium: true,
      isEmailVerified: true,
      followersCount: 10,
      followingCount: 5,
      createdAt: new Date().toISOString()
    };
    users.set(targetEmail, user);
  }

  const tokens = generateTokens(user);
  res.json({
    success: true,
    message: 'Apple sign-in successful',
    data: {
      user: {
        userId: user.userId,
        name: user.name,
        email: user.email,
        handle: user.handle,
        bio: user.bio,
        profilePicUrl: user.profilePicUrl,
        isPremium: user.isPremium,
        isEmailVerified: user.isEmailVerified
      },
      tokens: {
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresIn: 2592000
      }
    }
  });
});

// POST /api/v1/auth/send-otp
router.post('/send-otp', (req, res) => {
  const { email } = req.body;
  res.json({
    success: true,
    message: `OTP sent successfully to ${email || 'your email'}`,
    data: { email }
  });
});

// POST /api/v1/auth/verify-otp
router.post('/verify-otp', (req, res) => {
  const { email, otp } = req.body;
  const normalizedEmail = (email || 'verified.user@zynera.app').toLowerCase().trim();
  let user = users.get(normalizedEmail);

  if (!user) {
    const userId = `usr_otp_${Date.now()}`;
    const resolvedName = normalizedEmail.split('@')[0];
    user = {
      userId,
      email: normalizedEmail,
      name: resolvedName,
      handle: `@${resolvedName}`,
      bio: 'Verified Zynera Listener',
      profilePicUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(resolvedName)}&backgroundColor=10b981,059669&textColor=ffffff`,
      isPremium: true,
      isEmailVerified: true,
      followersCount: 12,
      followingCount: 6,
      createdAt: new Date().toISOString()
    };
    users.set(normalizedEmail, user);
  }

  const tokens = generateTokens(user);
  res.json({
    success: true,
    message: 'OTP verified successfully',
    data: {
      user,
      tokens: {
        accessToken: tokens.accessToken,
        refreshToken: tokens.refreshToken,
        expiresIn: 2592000
      }
    }
  });
});

// POST /api/v1/auth/refresh-token
router.post('/refresh-token', (req, res) => {
  const { refreshToken } = req.body;
  if (!refreshToken) {
    return res.status(401).json({ success: false, message: 'Refresh token required' });
  }

  try {
    const decoded = jwt.verify(refreshToken, JWT_SECRET);
    const user = Array.from(users.values()).find(u => u.userId === decoded.userId) || {
      userId: decoded.userId,
      email: decoded.email,
      name: decoded.name || 'User'
    };

    const tokens = generateTokens(user);
    res.json({
      success: true,
      data: {
        tokens: {
          accessToken: tokens.accessToken,
          refreshToken: tokens.refreshToken,
          expiresIn: 2592000
        }
      }
    });
  } catch (err) {
    res.status(403).json({ success: false, message: 'Invalid or expired refresh token' });
  }
});

// POST /api/v1/auth/logout
router.post('/logout', (req, res) => {
  res.json({ success: true, message: 'Logged out successfully' });
});

module.exports = { router, users, JWT_SECRET };
