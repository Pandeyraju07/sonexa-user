/**
 * Normalized Music Taxonomy
 * - 18 Normalized Moods with semantic associations
 * - Hierarchical Multi-Level Genres
 * - Language Mapping
 */

const MOODS = Object.freeze({
  ROMANTIC: 'ROMANTIC',
  SAD: 'SAD',
  HAPPY: 'HAPPY',
  ENERGETIC: 'ENERGETIC',
  CHILL: 'CHILL',
  DEVOTIONAL: 'DEVOTIONAL',
  PARTY: 'PARTY',
  MOTIVATIONAL: 'MOTIVATIONAL',
  MELANCHOLY: 'MELANCHOLY',
  FOCUS: 'FOCUS',
  WORKOUT: 'WORKOUT',
  DANCE: 'DANCE',
  NOSTALGIC: 'NOSTALGIC',
  PEACEFUL: 'PEACEFUL',
  LOFI: 'LOFI',
  DREAMY: 'DREAMY',
  EMOTIONAL: 'EMOTIONAL',
  PATRIOTIC: 'PATRIOTIC'
});

// Acoustic and tempo expectations per mood
const MOOD_PROFILES = Object.freeze({
  [MOODS.ROMANTIC]: { energyRange: [0.3, 0.7], valenceRange: [0.4, 0.8], acousticness: 0.5, typicalTempo: [75, 115] },
  [MOODS.SAD]: { energyRange: [0.1, 0.5], valenceRange: [0.05, 0.4], acousticness: 0.7, typicalTempo: [60, 95] },
  [MOODS.HAPPY]: { energyRange: [0.6, 0.95], valenceRange: [0.65, 1.0], acousticness: 0.2, typicalTempo: [100, 135] },
  [MOODS.ENERGETIC]: { energyRange: [0.75, 1.0], valenceRange: [0.5, 0.9], acousticness: 0.1, typicalTempo: [115, 160] },
  [MOODS.CHILL]: { energyRange: [0.2, 0.55], valenceRange: [0.35, 0.7], acousticness: 0.65, typicalTempo: [70, 100] },
  [MOODS.DEVOTIONAL]: { energyRange: [0.3, 0.7], valenceRange: [0.4, 0.85], acousticness: 0.6, typicalTempo: [65, 105] },
  [MOODS.PARTY]: { energyRange: [0.8, 1.0], valenceRange: [0.65, 1.0], acousticness: 0.1, typicalTempo: [118, 145] },
  [MOODS.MOTIVATIONAL]: { energyRange: [0.7, 0.95], valenceRange: [0.55, 0.9], acousticness: 0.25, typicalTempo: [110, 140] },
  [MOODS.MELANCHOLY]: { energyRange: [0.2, 0.5], valenceRange: [0.1, 0.35], acousticness: 0.75, typicalTempo: [60, 90] },
  [MOODS.FOCUS]: { energyRange: [0.2, 0.5], valenceRange: [0.3, 0.6], acousticness: 0.6, typicalTempo: [65, 95] },
  [MOODS.WORKOUT]: { energyRange: [0.8, 1.0], valenceRange: [0.5, 0.9], acousticness: 0.1, typicalTempo: [125, 155] },
  [MOODS.DANCE]: { energyRange: [0.75, 0.98], valenceRange: [0.6, 0.95], acousticness: 0.15, typicalTempo: [115, 138] },
  [MOODS.NOSTALGIC]: { energyRange: [0.3, 0.65], valenceRange: [0.35, 0.75], acousticness: 0.55, typicalTempo: [75, 110] },
  [MOODS.PEACEFUL]: { energyRange: [0.15, 0.45], valenceRange: [0.4, 0.8], acousticness: 0.8, typicalTempo: [55, 85] },
  [MOODS.LOFI]: { energyRange: [0.2, 0.5], valenceRange: [0.3, 0.65], acousticness: 0.7, typicalTempo: [70, 90] },
  [MOODS.DREAMY]: { energyRange: [0.25, 0.55], valenceRange: [0.35, 0.7], acousticness: 0.6, typicalTempo: [70, 100] },
  [MOODS.EMOTIONAL]: { energyRange: [0.25, 0.65], valenceRange: [0.2, 0.55], acousticness: 0.65, typicalTempo: [65, 105] },
  [MOODS.PATRIOTIC]: { energyRange: [0.65, 0.95], valenceRange: [0.5, 0.85], acousticness: 0.3, typicalTempo: [100, 130] }
});

const HIERARCHICAL_GENRES = Object.freeze({
  INDIAN: {
    name: 'Indian Music',
    children: {
      BOLLYWOOD: {
        name: 'Bollywood',
        subgenres: ['Bollywood Romantic', 'Bollywood Sad', 'Bollywood Party', 'Bollywood Dance', 'Bollywood Retro', 'Bollywood Melodic']
      },
      PUNJABI: {
        name: 'Punjabi',
        subgenres: ['Punjabi Pop', 'Bhangra', 'Punjabi Folk', 'Punjabi Hip-Hop', 'Punjabi Romantic']
      },
      SOUTH_INDIAN: {
        name: 'South Indian',
        subgenres: ['Tamil', 'Telugu', 'Malayalam', 'Kannada']
      },
      INDIE: {
        name: 'Indie',
        subgenres: ['Indie Acoustic', 'Indie Pop', 'Indie Folk', 'Dream Pop']
      },
      SUFI_GHAZAL: {
        name: 'Ghazal & Sufi',
        subgenres: ['Ghazal', 'Sufi', 'Qawwali']
      },
      DEVOTIONAL: {
        name: 'Devotional',
        subgenres: ['Bhajan', 'Aarti', 'Kirtan', 'Mantra']
      },
      CLASSICAL: {
        name: 'Classical',
        subgenres: ['Hindustani', 'Carnatic']
      }
    }
  },
  GLOBAL: {
    name: 'Global Music',
    children: {
      POP: { name: 'Pop', subgenres: ['Dance Pop', 'Synthpop', 'Electropop', 'Nu-Disco'] },
      RNB: { name: 'R&B / Soul', subgenres: ['Contemporary R&B', 'Synthwave', 'Soul'] },
      HIPHOP: { name: 'Hip-Hop / Rap', subgenres: ['Trap', 'Melodic Rap', 'Boom Bap'] },
      ELECTRONIC: { name: 'Electronic', subgenres: ['EDM', 'House', 'Ambient', 'Lofi Beats'] }
    }
  }
});

const SUPPORTED_LANGUAGES = Object.freeze([
  'Hindi',
  'Punjabi',
  'Tamil',
  'Telugu',
  'Malayalam',
  'Kannada',
  'Bengali',
  'Marathi',
  'Gujarati',
  'English',
  'Haryanvi',
  'Bhojpuri',
  'Urdu'
]);

module.exports = {
  MOODS,
  MOOD_PROFILES,
  HIERARCHICAL_GENRES,
  SUPPORTED_LANGUAGES
};
