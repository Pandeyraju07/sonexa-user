/**
 * LocalEmbeddingService
 * Deterministic, multi-dimensional feature embedding generator for tracks without external paid APIs.
 */

const EmbeddingService = require('./EmbeddingService');
const { MOODS } = require('../../catalog/musicTaxonomy');

const MOOD_KEYS = Object.values(MOODS);

class LocalEmbeddingService extends EmbeddingService {
  constructor() {
    super();
    this.embeddingCache = new Map();
  }

  generateSongEmbedding(track) {
    if (!track) return new Array(32).fill(0);
    if (this.embeddingCache.has(track.id)) {
      return this.embeddingCache.get(track.id);
    }

    const af = track.acousticFeatures || {};
    const moods = track.moods || {};

    const vector = [];

    // 1. Acoustic Features (7 dims)
    vector.push(Math.min(1, Math.max(0, (af.tempo || 100) / 200)));
    vector.push(af.energy ?? 0.6);
    vector.push(af.valence ?? af.happiness ?? 0.6);
    vector.push(af.danceability ?? 0.6);
    vector.push(af.acousticness ?? 0.5);
    vector.push(af.instrumentalness ?? 0.05);
    vector.push(af.popularity ?? 0.8);

    // 2. Normalized Mood Vector (18 dims)
    for (const moodKey of MOOD_KEYS) {
      vector.push(moods[moodKey] ?? 0);
    }

    // 3. Era representation (1 dim)
    const year = track.year || 2024;
    vector.push(Math.min(1, Math.max(0, (year - 1970) / 60)));

    // 4. Genre hashing (3 dims)
    const genreStr = `${track.genre || ''} ${track.subgenre || ''}`;
    let hash1 = 0, hash2 = 0;
    for (let i = 0; i < genreStr.length; i++) {
      hash1 = (hash1 * 31 + genreStr.charCodeAt(i)) % 1000;
      hash2 = (hash2 * 17 + genreStr.charCodeAt(i)) % 1000;
    }
    vector.push(hash1 / 1000);
    vector.push(hash2 / 1000);

    // 5. Language encoding (2 dims)
    const lang = (track.language || 'Hindi').toLowerCase();
    vector.push(lang === 'hindi' ? 1.0 : (lang === 'punjabi' ? 0.7 : 0.2));
    vector.push(lang === 'english' ? 1.0 : 0.0);

    // 6. Movie affinity flag (1 dim)
    vector.push(track.movieId ? 1.0 : 0.0);

    this.embeddingCache.set(track.id, vector);
    return vector;
  }

  generateTextEmbedding(text) {
    // Generates heuristic embedding for natural language search queries
    const lower = (text || '').toLowerCase();
    const vector = new Array(32).fill(0);

    // Acoustic cues in text
    if (lower.includes('calm') || lower.includes('chill') || lower.includes('relax') || lower.includes('soft')) {
      vector[1] = 0.3; // low energy
      vector[4] = 0.8; // high acousticness
    } else if (lower.includes('party') || lower.includes('workout') || lower.includes('gym') || lower.includes('energy')) {
      vector[1] = 0.95; // high energy
      vector[3] = 0.90; // danceability
      vector[4] = 0.10; // low acousticness
    }

    // Mood cues
    for (let i = 0; i < MOOD_KEYS.length; i++) {
      const mood = MOOD_KEYS[i];
      if (lower.includes(mood.toLowerCase())) {
        vector[7 + i] = 1.0;
      }
    }

    if (lower.includes('romantic') || lower.includes('love') || lower.includes('pyaar')) {
      vector[7] = 0.95; // ROMANTIC
    }
    if (lower.includes('sad') || lower.includes('dard') || lower.includes('heartbreak')) {
      vector[8] = 0.95; // SAD
    }

    // Language cues
    if (lower.includes('punjabi')) {
      vector[29] = 0.7;
    } else if (lower.includes('hindi') || lower.includes('bollywood')) {
      vector[29] = 1.0;
    } else if (lower.includes('english')) {
      vector[30] = 1.0;
    }

    return vector;
  }
}

module.exports = LocalEmbeddingService;
