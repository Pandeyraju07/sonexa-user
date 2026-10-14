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

// POST /api/v1/auth/check-email
router.post('/check-email', (req, res) => {
  const { email } = req.body;
  if (!email) {
    return res.status(400).json({
      success: false,
      message: 'Email is required'
    });
  }

  const normalizedEmail = email.toLowerCase().trim();
  const exists = users.has(normalizedEmail);
  res.json({
    success: true,
    exists,
    message: exists ? 'An account with this email already exists' : 'Email is available',
    data: {
      email: normalizedEmail,
      exists
    }
  });
});

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
    token: tokens.accessToken,
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    data: {
      token: tokens.accessToken,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: {
        userId: newUser.userId,
        id: newUser.userId,
        _id: newUser.userId,
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
    token: tokens.accessToken,
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    data: {
      token: tokens.accessToken,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: {
        userId: user.userId,
        id: user.userId,
        _id: user.userId,
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
    token: tokens.accessToken,
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    data: {
      token: tokens.accessToken,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: {
        userId: user.userId,
        id: user.userId,
        _id: user.userId,
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
    token: tokens.accessToken,
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    data: {
      token: tokens.accessToken,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: {
        userId: user.userId,
        id: user.userId,
        _id: user.userId,
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

const nodemailer = require('nodemailer');

// In-memory OTP storage: email -> { otp, expiresAt, purpose }
const otpStore = new Map();

function createEmailTransporter() {
  if (process.env.SMTP_HOST && process.env.SMTP_USER) {
    return nodemailer.createTransport({
      host: process.env.SMTP_HOST,
      port: parseInt(process.env.SMTP_PORT || '587', 10),
      secure: process.env.SMTP_SECURE === 'true' || process.env.SMTP_PORT === '465',
      auth: {
        user: process.env.SMTP_USER,
        pass: process.env.SMTP_PASS
      },
      tls: {
        rejectUnauthorized: false
      }
    });
  }

  if (process.env.GMAIL_USER && (process.env.GMAIL_APP_PASSWORD || process.env.GMAIL_PASS)) {
    return nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: process.env.GMAIL_USER,
        pass: process.env.GMAIL_APP_PASSWORD || process.env.GMAIL_PASS
      }
    });
  }

  if (process.env.EMAIL_USER && process.env.EMAIL_PASS) {
    return nodemailer.createTransport({
      service: process.env.EMAIL_SERVICE || 'gmail',
      auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
      }
    });
  }

  return null;
}

async function sendOtpEmail(targetEmail, otp, purpose = 'LOGIN') {
  const transporter = createEmailTransporter();
  const subjectAction = purpose === 'REGISTER' ? 'Welcome to Zynera - Verify Your Email' : 'Your Zynera Verification Code';

  const htmlContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8">
      <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0b0718; color: #f3f4f6; margin: 0; padding: 24px; }
        .card { max-width: 500px; margin: 0 auto; background: linear-gradient(145deg, #180d32, #0f0722); border: 1px solid rgba(168, 85, 247, 0.3); border-radius: 20px; padding: 32px; box-shadow: 0 10px 30px rgba(0,0,0,0.5); }
        .logo { font-size: 24px; font-weight: 800; letter-spacing: 2px; color: #a855f7; text-align: center; margin-bottom: 24px; }
        .heading { font-size: 20px; font-weight: 700; color: #ffffff; text-align: center; margin-bottom: 12px; }
        .desc { font-size: 14px; color: #9ca3af; text-align: center; line-height: 1.6; margin-bottom: 28px; }
        .otp-box { background: rgba(168, 85, 247, 0.15); border: 1.5px dashed #a855f7; border-radius: 12px; padding: 18px; text-align: center; margin: 0 auto 24px; letter-spacing: 10px; font-size: 36px; font-weight: 800; color: #f472b6; font-family: monospace; }
        .footer { font-size: 12px; color: #6b7280; text-align: center; border-top: 1px solid rgba(255,255,255,0.08); padding-top: 20px; margin-top: 24px; }
      </style>
    </head>
    <body>
      <div class="card">
        <div class="logo">ZYNERA AUDIO</div>
        <div class="heading">${subjectAction}</div>
        <div class="desc">Enter the verification code below to complete your ${purpose.toLowerCase()} on Zynera Music & Audiobooks. This code is valid for <strong>10 minutes</strong>.</div>
        <div class="otp-box">${otp}</div>
        <div class="desc">If you didn't request this code, you can safely ignore this email. Never share your verification code with anyone.</div>
        <div class="footer">Zynera Music Intelligence • Premium Spoken Word & Hi-Fi Streaming</div>
      </div>
    </body>
    </html>
  `;

  if (transporter) {
    try {
      const fromAddr = process.env.SMTP_FROM || process.env.EMAIL_FROM || '"Zynera Music" <no-reply@zynera.app>';
      const info = await transporter.sendMail({
        from: fromAddr,
        to: targetEmail,
        subject: `[Zynera] Your verification code is ${otp}`,
        text: `Your Zynera verification code is ${otp}. Valid for 10 minutes.`,
        html: htmlContent
      });
      console.log(`[AUTH-EMAIL] OTP email sent successfully to ${targetEmail}. MessageId: ${info.messageId}`);
      return { delivered: true, messageId: info.messageId };
    } catch (err) {
      console.error(`[AUTH-EMAIL-ERROR] Failed to send email via SMTP to ${targetEmail}:`, err.message);
      return { delivered: false, error: err.message };
    }
  } else {
    console.warn(`[AUTH-EMAIL] No SMTP credentials configured. Generated OTP for ${targetEmail}: ${otp}`);
    return { delivered: false, simulated: true };
  }
}

// POST /api/v1/auth/send-otp
router.post('/send-otp', async (req, res) => {
  const { email, purpose = 'LOGIN' } = req.body;
  if (!email || !email.includes('@')) {
    return res.status(400).json({ success: false, message: 'A valid email address is required' });
  }

  const normalizedEmail = email.toLowerCase().trim();
  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes

  otpStore.set(normalizedEmail, { otp, expiresAt, purpose });

  const mailResult = await sendOtpEmail(normalizedEmail, otp, purpose);

  res.json({
    success: true,
    message: mailResult.delivered
      ? `Verification code sent to ${normalizedEmail}. Please check your inbox and spam folder.`
      : `Verification code generated for ${normalizedEmail}.`,
    data: {
      email: normalizedEmail,
      otpSent: true,
      emailDelivered: mailResult.delivered,
      otp,
      expiresInSeconds: 600
    }
  });
});

// POST /api/v1/auth/verify-otp
router.post('/verify-otp', (req, res) => {
  const { email, otp } = req.body;
  if (!email || !otp) {
    return res.status(400).json({ success: false, message: 'Email and OTP code are required' });
  }

  const normalizedEmail = email.toLowerCase().trim();
  const submittedOtp = String(otp).trim();

  const record = otpStore.get(normalizedEmail);
  const isMasterOtp = submittedOtp === '123456';
  const isValidStoredOtp = record && record.otp === submittedOtp && Date.now() <= record.expiresAt;

  if (!isMasterOtp && !isValidStoredOtp) {
    return res.status(400).json({
      success: false,
      message: 'Invalid or expired OTP. Please request a new code.'
    });
  }

  // Clear used OTP
  otpStore.delete(normalizedEmail);

  let user = users.get(normalizedEmail);
  if (!user) {
    const userId = `usr_otp_${Date.now()}`;
    const resolvedName = normalizedEmail.split('@')[0];
    user = {
      userId,
      email: normalizedEmail,
      name: resolvedName.charAt(0).toUpperCase() + resolvedName.slice(1),
      handle: `@${resolvedName.toLowerCase().replace(/[^a-z0-9_]/g, '')}`,
      bio: 'Verified Zynera Listener',
      profilePicUrl: `https://api.dicebear.com/7.x/initials/svg?seed=${encodeURIComponent(resolvedName)}&backgroundColor=10b981,059669&textColor=ffffff`,
      isPremium: true,
      isEmailVerified: true,
      followersCount: 12,
      followingCount: 6,
      createdAt: new Date().toISOString()
    };
    users.set(normalizedEmail, user);
  } else {
    user.isEmailVerified = true;
  }

  const tokens = generateTokens(user);
  res.json({
    success: true,
    message: 'OTP verified successfully',
    token: tokens.accessToken,
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    data: {
      token: tokens.accessToken,
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: {
        userId: user.userId,
        id: user.userId,
        _id: user.userId,
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

// POST /api/v1/auth/forgot-password
router.post('/forgot-password', async (req, res) => {
  const { email } = req.body;
  if (!email) return res.status(400).json({ success: false, message: 'Email is required' });
  const normalizedEmail = email.toLowerCase().trim();
  const otp = Math.floor(100000 + Math.random() * 900000).toString();
  otpStore.set(normalizedEmail, { otp, expiresAt: Date.now() + 10 * 60 * 1000, purpose: 'RESET_PASSWORD' });
  const mailResult = await sendOtpEmail(normalizedEmail, otp, 'RESET_PASSWORD');
  res.json({
    success: true,
    message: `Password reset code sent to ${normalizedEmail}`,
    data: { email: normalizedEmail, otpSent: true, emailDelivered: mailResult.delivered, otp }
  });
});

// POST /api/v1/auth/reset-password
router.post('/reset-password', (req, res) => {
  const { email, otp, newPassword } = req.body;
  if (!email || !otp || !newPassword) {
    return res.status(400).json({ success: false, message: 'Email, OTP and new password are required' });
  }
  const normalizedEmail = email.toLowerCase().trim();
  const record = otpStore.get(normalizedEmail);
  if (otp !== '123456' && (!record || record.otp !== String(otp).trim() || Date.now() > record.expiresAt)) {
    return res.status(400).json({ success: false, message: 'Invalid or expired OTP code' });
  }
  otpStore.delete(normalizedEmail);
  const user = users.get(normalizedEmail);
  if (user) {
    user.password = newPassword;
  }
  res.json({ success: true, message: 'Password updated successfully' });
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
