/**
 * SearchIntentDetector
 * Deep intent extraction mapping raw search queries into structured domain intents.
 */

const SearchIntentType = require('./SearchIntentType');
const { entityResolutionService } = require('../entity/EntityResolutionService');
const { normalizeString } = require('../../catalog/catalogStore');

class SearchIntentDetector {
  detect(rawQuery) {
    const query = (rawQuery || '').trim();
    const norm = normalizeString(query);

    // 1. Similar Song Intent (e.g. "songs like Tum Hi Ho", "similar to Kesariya")
    const similarMatch = norm.match(/(?:songs\s+like|similar\s+to|more\s+like)\s+(.+)/);
    if (similarMatch && similarMatch[1]) {
      return {
        type: SearchIntentType.SIMILAR_SONG,
        query,
        targetSongTitle: similarMatch[1].trim(),
        confidence: 0.96
      };
    }

    // 2. Year Range / Era Intent (e.g. "90s Bollywood songs", "80s songs", "retro")
    const yearMatch = norm.match(/\b(90s|80s|70s|2000s|2010s|2020s|retro|classic)\b/);
    if (yearMatch) {
      const era = yearMatch[1];
      const yearRange = era === '90s' ? [1990, 1999]
        : (era === '80s' ? [1980, 1989]
          : (era === '2000s' ? [2000, 2009] : [1970, 2026]));

      const genre = norm.includes('bollywood') ? 'Bollywood' : null;
      return {
        type: SearchIntentType.YEAR_RANGE,
        query,
        era,
        yearRange,
        genre,
        confidence: 0.94
      };
    }

    // 3. Artist Recency (e.g. "latest Arijit songs", "new songs of Diljit")
    const isRecency = norm.includes('latest') || norm.includes('new') || norm.includes('fresh');
    const artistResolution = entityResolutionService.resolveArtist(norm.replace(/\b(latest|new|fresh)\b/g, ''));
    if (isRecency && artistResolution) {
      return {
        type: SearchIntentType.ARTIST_RECENCY,
        query,
        artist: artistResolution.entity,
        confidence: 0.95
      };
    }

    // 4. Movie Songs Intent (e.g. "Dhurandhar songs", "animal soundtrack", "kabir singh songs")
    const movieResolution = entityResolutionService.resolveMovie(norm);
    if (movieResolution) {
      return {
        type: SearchIntentType.MOVIE_SONGS,
        query,
        movie: movieResolution.entity,
        confidence: movieResolution.confidence
      };
    }

    // 5. Artist + Mood Intent (e.g. "Arijit Singh romantic songs", "sad songs of Arijit")
    const moodResolution = entityResolutionService.resolveMood(norm);
    if (artistResolution && moodResolution) {
      return {
        type: SearchIntentType.ARTIST_MOOD,
        query,
        artist: artistResolution.entity,
        mood: moodResolution.mood,
        confidence: 0.95
      };
    }

    // 6. Genre + Mood Intent (e.g. "Punjabi party songs")
    const genreResolution = entityResolutionService.resolveGenre(norm);
    if (genreResolution && moodResolution) {
      return {
        type: SearchIntentType.GENRE_MOOD,
        query,
        genre: genreResolution.genre,
        mood: moodResolution.mood,
        confidence: 0.92
      };
    }

    // 7. Pure Artist Intent (e.g. "Arijit Singh", "The Weeknd")
    if (artistResolution && !moodResolution) {
      return {
        type: SearchIntentType.ARTIST,
        query,
        artist: artistResolution.entity,
        confidence: artistResolution.confidence
      };
    }

    // 8. Pure Mood Intent (e.g. "sad songs", "romantic songs")
    if (moodResolution && !artistResolution) {
      return {
        type: SearchIntentType.MOOD,
        query,
        mood: moodResolution.mood,
        confidence: 0.90
      };
    }

    // 9. Natural Language Discovery (e.g. "romantic songs for late night")
    if (norm.split(' ').length >= 3) {
      return {
        type: SearchIntentType.NATURAL_LANGUAGE,
        query,
        confidence: 0.85
      };
    }

    return {
      type: SearchIntentType.GENERAL,
      query,
      confidence: 0.75
    };
  }
}

const searchIntentDetector = new SearchIntentDetector();

module.exports = {
  SearchIntentDetector,
  searchIntentDetector
};
