/**
 * EntityResolutionService
 * Generic entity resolver handling typos, aliases, prefixes, and fuzzy matching
 * for Movies, Artists, Albums, Moods, Genres, and Songs.
 */

const { catalogStore, normalizeString } = require('../../catalog/catalogStore');
const { MOODS } = require('../../catalog/musicTaxonomy');

function levenshteinDistance(a, b) {
  if (a.length === 0) return b.length;
  if (b.length === 0) return a.length;
  const matrix = [];
  for (let i = 0; i <= b.length; i++) matrix[i] = [i];
  for (let j = 0; j <= a.length; j++) matrix[0][j] = j;

  for (let i = 1; i <= b.length; i++) {
    for (let j = 1; j <= a.length; j++) {
      if (b.charAt(i - 1) === a.charAt(j - 1)) {
        matrix[i][j] = matrix[i - 1][j - 1];
      } else {
        matrix[i][j] = Math.min(
          matrix[i - 1][j - 1] + 1, // substitution
          matrix[i][j - 1] + 1,     // insertion
          matrix[i - 1][j] + 1      // deletion
        );
      }
    }
  }
  return matrix[b.length][a.length];
}

class EntityResolutionService {
  constructor() {
    this.moodMap = {
      romantic: MOODS.ROMANTIC,
      romance: MOODS.ROMANTIC,
      love: MOODS.ROMANTIC,
      pyaar: MOODS.ROMANTIC,
      sad: MOODS.SAD,
      dard: MOODS.SAD,
      breakup: MOODS.SAD,
      party: MOODS.PARTY,
      club: MOODS.PARTY,
      dance: MOODS.DANCE,
      chill: MOODS.CHILL,
      relax: MOODS.CHILL,
      peaceful: MOODS.PEACEFUL,
      workout: MOODS.WORKOUT,
      gym: MOODS.WORKOUT,
      motivational: MOODS.MOTIVATIONAL,
      emotional: MOODS.EMOTIONAL,
      melancholy: MOODS.MELANCHOLY,
      lofi: MOODS.LOFI,
      devotional: MOODS.DEVOTIONAL,
      bhakti: MOODS.DEVOTIONAL
    };

    this.genreMap = {
      bollywood: 'Bollywood',
      punjabi: 'Punjabi',
      indie: 'Indie Acoustic',
      pop: 'Electropop',
      synthwave: 'Synthwave',
      hiphop: 'Punjabi Hip-Hop',
      bhangra: 'Punjabi Pop',
      retro: 'Bollywood Retro'
    };

    // Aliases & common typos
    this.knownAliases = {
      dharandhar: 'dhurandhar',
      dhurander: 'dhurandhar',
      dhurandher: 'dhurandhar',
      arjit: 'arijit singh',
      'arjit singh': 'arijit singh',
      shreya: 'shreya ghoshal',
      weeknd: 'the weeknd',
      dua: 'dua lipa',
      anuv: 'anuv jain',
      diljit: 'diljit dosanjh'
    };
  }

  resolveMovie(text) {
    const norm = normalizeString(text)
      .replace(/^(songs of|soundtrack of|all songs of|movie|film)\s+/g, '')
      .replace(/\s+(songs|soundtrack|ost|movie|film|all songs)$/g, '')
      .trim();

    if (!norm) return null;

    const aliased = this.knownAliases[norm] || norm;

    // 1. Exact match
    const exact = catalogStore.findMovie(aliased);
    if (exact) return { entity: exact, type: 'MOVIE', confidence: 0.98 };

    // 2. Fuzzy match across all movies
    for (const movie of catalogStore.movies) {
      const mNorm = normalizeString(movie.title);
      const dist = levenshteinDistance(aliased, mNorm);
      const maxLen = Math.max(aliased.length, mNorm.length);
      const similarity = 1 - (dist / maxLen);
      if (similarity >= 0.75) {
        return { entity: movie, type: 'MOVIE', confidence: similarity };
      }
    }

    return null;
  }

  resolveArtist(text) {
    const norm = normalizeString(text)
      .replace(/\s+(ke gaane|songs|all songs|hits|discography)$/g, '')
      .trim();

    if (!norm) return null;

    const aliased = this.knownAliases[norm] || norm;

    const exact = catalogStore.findArtist(aliased);
    if (exact) return { entity: exact, type: 'ARTIST', confidence: 0.98 };

    for (const artist of catalogStore.artists) {
      const aNorm = normalizeString(artist.name);
      const dist = levenshteinDistance(aliased, aNorm);
      const similarity = 1 - (dist / Math.max(aliased.length, aNorm.length));
      if (similarity >= 0.75) {
        return { entity: artist, type: 'ARTIST', confidence: similarity };
      }
    }

    return null;
  }

  resolveMood(text) {
    const norm = normalizeString(text);
    for (const [key, moodVal] of Object.entries(this.moodMap)) {
      if (norm.includes(key)) {
        return { mood: moodVal, key, confidence: 0.95 };
      }
    }
    return null;
  }

  resolveGenre(text) {
    const norm = normalizeString(text);
    for (const [key, genreVal] of Object.entries(this.genreMap)) {
      if (norm.includes(key)) {
        return { genre: genreVal, confidence: 0.95 };
      }
    }
    return null;
  }
}

const entityResolutionService = new EntityResolutionService();

module.exports = {
  EntityResolutionService,
  entityResolutionService,
  levenshteinDistance
};
