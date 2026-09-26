/**
 * AffinityEngine
 * Evaluates behavioral events and adjusts affinity scores for songs, artists, genres, and moods.
 */

const { userAffinityStore } = require('./UserAffinityStore');
const { catalogStore } = require('../catalog/catalogStore');
const { recencyDecayService } = require('../recommendation/ranking/RecencyDecayService');

class AffinityEngine {
  processEvent(event) {
    const { userId, songId, eventType, position, duration, metadata } = event;
    if (!userId) return;

    userAffinityStore.recordEvent(event);

    const track = songId ? catalogStore.getTrackById(songId) : null;
    if (!track) return;

    const now = Date.now();

    // 1. Process Song Affinity
    const songAffinity = userAffinityStore.getSongAffinity(userId, songId);
    songAffinity.lastPlayedAt = now;

    // 2. Fetch Artist, Genre, and Mood affinities
    const artistAffinity = userAffinityStore.getArtistAffinity(userId, track.artist);
    artistAffinity.lastPlayedAt = now;

    const genre = track.genre;
    const genreAffinity = genre ? userAffinityStore.getGenreAffinity(userId, genre) : null;

    const moods = track.moods || {};

    // 3. Evaluate Event Type
    switch (eventType) {
      case 'SONG_PLAY_STARTED': {
        songAffinity.playCount += 1;
        artistAffinity.playCount += 1;
        if (genreAffinity) genreAffinity.playCount += 1;
        songAffinity.affinityScore += 0.2;
        break;
      }

      case 'EARLY_SKIP':
      case 'SONG_SKIPPED_EARLY': {
        // Skipped within 10 seconds - strong negative signal
        songAffinity.skipCount += 1;
        songAffinity.earlySkipCount += 1;
        artistAffinity.skipCount += 1;
        if (genreAffinity) genreAffinity.skipCount += 1;

        songAffinity.affinityScore = Math.max(-5.0, songAffinity.affinityScore - 1.2);
        artistAffinity.affinityScore = Math.max(-5.0, artistAffinity.affinityScore - 0.4);
        if (genreAffinity) genreAffinity.score = Math.max(-5.0, genreAffinity.score - 0.35);

        for (const [m, w] of Object.entries(moods)) {
          const moodAff = userAffinityStore.getMoodAffinity(userId, m);
          moodAff.skipCount += 1;
          moodAff.score = Math.max(-5.0, moodAff.score - 0.3 * w);
          userAffinityStore.setMoodAffinity(userId, m, moodAff);
        }
        break;
      }

      case 'SONG_SKIPPED': {
        // Normal skip
        songAffinity.skipCount += 1;
        artistAffinity.skipCount += 1;
        if (genreAffinity) genreAffinity.skipCount += 1;

        songAffinity.affinityScore = Math.max(-5.0, songAffinity.affinityScore - 0.5);
        artistAffinity.affinityScore = Math.max(-5.0, artistAffinity.affinityScore - 0.2);
        if (genreAffinity) genreAffinity.score = Math.max(-5.0, genreAffinity.score - 0.15);

        for (const [m, w] of Object.entries(moods)) {
          const moodAff = userAffinityStore.getMoodAffinity(userId, m);
          moodAff.skipCount += 1;
          moodAff.score = Math.max(-5.0, moodAff.score - 0.15 * w);
          userAffinityStore.setMoodAffinity(userId, m, moodAff);
        }
        break;
      }

      case 'SONG_50_PERCENT': {
        // Strong play signal
        songAffinity.affinityScore += 0.5;
        artistAffinity.affinityScore += 0.3;
        if (genreAffinity) genreAffinity.score += 0.2;
        break;
      }

      case 'SONG_COMPLETED': {
        // Complete play (>80%) - strong positive signal
        songAffinity.completionCount += 1;
        artistAffinity.completionCount += 1;
        songAffinity.affinityScore += 1.5;
        artistAffinity.affinityScore += 1.0;
        if (genreAffinity) genreAffinity.score += 0.8;

        for (const [m, w] of Object.entries(moods)) {
          const moodAff = userAffinityStore.getMoodAffinity(userId, m);
          moodAff.playCount += 1;
          moodAff.score += 0.8 * w;
          userAffinityStore.setMoodAffinity(userId, m, moodAff);
        }
        break;
      }

      case 'SONG_REPLAYED': {
        // Replay - very strong positive signal
        songAffinity.replayCount += 1;
        songAffinity.affinityScore += 2.0;
        artistAffinity.affinityScore += 1.5;
        if (genreAffinity) genreAffinity.score += 1.2;

        for (const [m, w] of Object.entries(moods)) {
          const moodAff = userAffinityStore.getMoodAffinity(userId, m);
          moodAff.score += 1.0 * w;
          userAffinityStore.setMoodAffinity(userId, m, moodAff);
        }
        break;
      }

      case 'SONG_LIKED': {
        songAffinity.likeCount = 1;
        songAffinity.affinityScore += 2.5;
        artistAffinity.likeCount += 1;
        artistAffinity.affinityScore += 1.8;
        if (genreAffinity) genreAffinity.score += 1.0;
        break;
      }

      case 'SONG_UNLIKED': {
        songAffinity.likeCount = 0;
        songAffinity.affinityScore = Math.max(0, songAffinity.affinityScore - 1.5);
        break;
      }

      default:
        break;
    }

    // Save updated affinities
    userAffinityStore.setSongAffinity(userId, songId, songAffinity);
    userAffinityStore.setArtistAffinity(userId, track.artist, artistAffinity);
    if (genreAffinity) {
      userAffinityStore.setGenreAffinity(userId, genre, genreAffinity);
    }
  }

  getNormalizedAffinity(userId, songId) {
    const raw = userAffinityStore.getSongAffinity(userId, songId);
    if (raw.affinityScore <= 0) return Math.max(0, 0.5 + raw.affinityScore * 0.1);
    const decayed = recencyDecayService.decayScore(raw.affinityScore, raw.lastPlayedAt);
    return Math.min(1.0, 0.5 + decayed * 0.1);
  }

  getArtistScore(userId, artistName) {
    const raw = userAffinityStore.getArtistAffinity(userId, artistName);
    if (raw.affinityScore <= 0) return Math.max(0, 0.5 + raw.affinityScore * 0.1);
    const decayed = recencyDecayService.decayScore(raw.affinityScore, raw.lastPlayedAt);
    return Math.min(1.0, 0.5 + decayed * 0.1);
  }

  getGenreScore(userId, genre) {
    const raw = userAffinityStore.getGenreAffinity(userId, genre);
    if (raw.score <= 0) return Math.max(0, 0.5 + raw.score * 0.1);
    return Math.min(1.0, 0.5 + raw.score * 0.1);
  }

  getMoodScore(userId, mood) {
    const raw = userAffinityStore.getMoodAffinity(userId, mood);
    if (raw.score <= 0) return Math.max(0, 0.5 + raw.score * 0.1);
    return Math.min(1.0, 0.5 + raw.score * 0.1);
  }
}

const affinityEngine = new AffinityEngine();

module.exports = {
  AffinityEngine,
  affinityEngine
};
