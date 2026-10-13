const express = require('express');
const router = express.Router();
const { audiobooks, audiobookCategories, supportedAudiobookLanguages } = require('../data/audiobooks');

// In-memory persistent stores for user audiobook progress & bookmarks
const userProgressMap = new Map();
const userBookmarksMap = new Map();

// Seed initial continue-listening session for demonstration
userProgressMap.set('ab_godan', {
  audiobookId: 'ab_godan',
  currentChapterId: 'ab_godan_ch1',
  currentChapterNumber: 1,
  chapterTitle: 'अध्याय 1: होरी का सपना (Hori\'s Dream)',
  positionSeconds: 1240,
  totalListenedSeconds: 1240,
  completionPercentage: 18,
  lastListenedAt: new Date(Date.now() - 3600000).toISOString()
});

userProgressMap.set('ab_sherlock', {
  audiobookId: 'ab_sherlock',
  currentChapterId: 'ab_sherlock_ch2',
  currentChapterNumber: 2,
  chapterTitle: 'Chapter 2: The Curse of the Baskervilles',
  positionSeconds: 840,
  totalListenedSeconds: 5040,
  completionPercentage: 32,
  lastListenedAt: new Date(Date.now() - 7200000).toISOString()
});

// GET /api/v1/audiobooks - List audiobooks with filters
router.get('/', (req, res) => {
  const { category, language, search, featured, mood } = req.query;
  let result = [...audiobooks];

  if (category && category !== 'All') {
    const catLower = category.toLowerCase();
    result = result.filter(b =>
      b.category.toLowerCase().includes(catLower) ||
      b.categories.some(c => c.toLowerCase().includes(catLower))
    );
  }

  if (language && language !== 'All') {
    const langLower = language.toLowerCase();
    result = result.filter(b =>
      b.languageCode.toLowerCase() === langLower ||
      b.languageName.toLowerCase() === langLower
    );
  }

  if (search) {
    const q = search.toLowerCase().trim();
    result = result.filter(b =>
      b.title.toLowerCase().includes(q) ||
      b.author.toLowerCase().includes(q) ||
      b.narrator.toLowerCase().includes(q) ||
      b.description.toLowerCase().includes(q) ||
      b.categories.some(c => c.toLowerCase().includes(q))
    );
  }

  if (featured === 'true') {
    result = result.filter(b => b.rating >= 4.9);
  }

  res.json({
    success: true,
    data: result,
    total: result.length
  });
});

// GET /api/v1/audiobooks/categories
router.get('/categories', (req, res) => {
  res.json({
    success: true,
    data: audiobookCategories
  });
});

// GET /api/v1/audiobooks/languages
router.get('/languages', (req, res) => {
  res.json({
    success: true,
    data: supportedAudiobookLanguages
  });
});

// GET /api/v1/audiobooks/continue-listening
router.get('/continue-listening', (req, res) => {
  const list = [];
  for (const [bookId, prog] of userProgressMap.entries()) {
    const book = audiobooks.find(b => b.id === bookId);
    if (book) {
      list.push({
        ...book,
        progress: prog
      });
    }
  }
  // Sort by lastListenedAt descending
  list.sort((a, b) => new Date(b.progress.lastListenedAt) - new Date(a.progress.lastListenedAt));

  res.json({
    success: true,
    data: list
  });
});

// GET /api/v1/audiobooks/:id
router.get('/:id', (req, res) => {
  const book = audiobooks.find(b => b.id === req.params.id);
  if (!book) {
    return res.status(404).json({ success: false, message: 'Audiobook not found' });
  }

  const progress = userProgressMap.get(book.id) || null;
  const bookmarks = userBookmarksMap.get(book.id) || [];
  const related = audiobooks.filter(b => b.id !== book.id && (b.category === book.category || b.languageCode === book.languageCode)).slice(0, 4);

  res.json({
    success: true,
    data: {
      ...book,
      progress,
      bookmarks,
      related
    }
  });
});

// GET /api/v1/audiobooks/:id/chapters
router.get('/:id/chapters', (req, res) => {
  const book = audiobooks.find(b => b.id === req.params.id);
  if (!book) {
    return res.status(404).json({ success: false, message: 'Audiobook not found' });
  }

  res.json({
    success: true,
    data: book.chapters
  });
});

// GET /api/v1/audiobooks/:id/progress
router.get('/:id/progress', (req, res) => {
  const progress = userProgressMap.get(req.params.id) || {
    audiobookId: req.params.id,
    currentChapterId: '',
    currentChapterNumber: 1,
    positionSeconds: 0,
    totalListenedSeconds: 0,
    completionPercentage: 0,
    lastListenedAt: null
  };

  res.json({
    success: true,
    data: progress
  });
});

// PUT /api/v1/audiobooks/:id/progress
router.put('/:id/progress', (req, res) => {
  const { currentChapterId, currentChapterNumber, positionSeconds, totalListenedSeconds, completionPercentage } = req.body;
  const book = audiobooks.find(b => b.id === req.params.id);

  const updatedProgress = {
    audiobookId: req.params.id,
    currentChapterId: currentChapterId || '',
    currentChapterNumber: currentChapterNumber || 1,
    positionSeconds: Math.floor(positionSeconds || 0),
    totalListenedSeconds: Math.floor(totalListenedSeconds || 0),
    completionPercentage: Math.min(100, Math.max(0, completionPercentage || 0)),
    lastListenedAt: new Date().toISOString()
  };

  userProgressMap.set(req.params.id, updatedProgress);

  res.json({
    success: true,
    data: updatedProgress
  });
});

// GET /api/v1/audiobooks/:id/bookmarks
router.get('/:id/bookmarks', (req, res) => {
  const bookmarks = userBookmarksMap.get(req.params.id) || [];
  res.json({
    success: true,
    data: bookmarks
  });
});

// POST /api/v1/audiobooks/:id/bookmarks
router.post('/:id/bookmarks', (req, res) => {
  const { chapterId, chapterNumber, positionSeconds, note } = req.body;
  const list = userBookmarksMap.get(req.params.id) || [];

  const newBookmark = {
    id: `bm_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
    audiobookId: req.params.id,
    chapterId: chapterId || '',
    chapterNumber: chapterNumber || 1,
    positionSeconds: Math.floor(positionSeconds || 0),
    note: (note || '').trim(),
    createdAt: new Date().toISOString()
  };

  list.unshift(newBookmark);
  userBookmarksMap.set(req.params.id, list);

  res.status(201).json({
    success: true,
    data: newBookmark
  });
});

// DELETE /api/v1/audiobooks/:id/bookmarks/:bookmarkId
router.delete('/:id/bookmarks/:bookmarkId', (req, res) => {
  const list = userBookmarksMap.get(req.params.id) || [];
  const filtered = list.filter(bm => bm.id !== req.params.bookmarkId);
  userBookmarksMap.set(req.params.id, filtered);

  res.json({
    success: true,
    message: 'Bookmark deleted successfully'
  });
});

// POST /api/v1/audiobooks/:id/download-authorisation
router.post('/:id/download-authorisation', (req, res) => {
  const book = audiobooks.find(b => b.id === req.params.id);
  if (!book) {
    return res.status(404).json({ success: false, message: 'Audiobook not found' });
  }

  res.json({
    success: true,
    data: {
      authorized: true,
      audiobookId: book.id,
      expiresAt: new Date(Date.now() + 86400000 * 7).toISOString(),
      rights: book.isPublicDomain ? 'PUBLIC_DOMAIN' : 'COMMERCIAL_STREAM_LICENSE'
    }
  });
});

module.exports = router;
