/**
 * PlaybackContext Model
 * Captures all dimensional attributes of the current playback session.
 */

class PlaybackContext {
  constructor(params = {}) {
    this.userId = params.userId || 'usr_anonymous';
    this.sessionId = params.sessionId || `sess_${Date.now()}`;
    this.currentSongId = params.currentSongId || null;
    this.currentArtistId = params.currentArtistId || null;
    this.currentArtistName = params.currentArtistName || null;
    this.currentAlbumId = params.currentAlbumId || null;
    this.currentMovieId = params.currentMovieId || null;
    this.currentMovieName = params.currentMovieName || null;
    this.currentGenre = params.currentGenre || null;
    this.currentMood = params.currentMood || null;
    this.currentLanguage = params.currentLanguage || 'Hindi';
    this.currentYear = params.currentYear ? parseInt(params.currentYear) : 2024;
    this.currentTempo = params.currentTempo ? parseFloat(params.currentTempo) : 100;
    this.currentEnergy = params.currentEnergy ? parseFloat(params.currentEnergy) : 0.6;
    this.currentValence = params.currentValence ? parseFloat(params.currentValence) : 0.6;
    this.currentPopularity = params.currentPopularity ? parseFloat(params.currentPopularity) : 0.8;
    this.deviceType = params.deviceType || 'android';
    this.timeOfDay = params.timeOfDay || PlaybackContext.getTimeOfDay();
    this.recentSongIds = Array.isArray(params.recentSongIds) ? params.recentSongIds : [];
    this.recentArtistIds = Array.isArray(params.recentArtistIds) ? params.recentArtistIds : [];
    this.recentMoods = Array.isArray(params.recentMoods) ? params.recentMoods : [];
    this.skippedGenres = Array.isArray(params.skippedGenres) ? params.skippedGenres : [];
    this.limit = params.limit ? parseInt(params.limit) : 20;
  }

  static getTimeOfDay() {
    const hour = new Date().getHours();
    if (hour >= 5 && hour < 12) return 'MORNING';
    if (hour >= 12 && hour < 17) return 'AFTERNOON';
    if (hour >= 17 && hour < 21) return 'EVENING';
    return 'LATE_NIGHT';
  }

  static fromTrack(track, extra = {}) {
    if (!track) return new PlaybackContext(extra);
    return new PlaybackContext({
      currentSongId: track.id,
      currentArtistId: (track.artistIds && track.artistIds[0]) || null,
      currentArtistName: track.artist,
      currentAlbumId: track.albumId,
      currentMovieId: track.movieId,
      currentMovieName: track.movie,
      currentGenre: track.genre,
      currentMood: track.moods ? Object.keys(track.moods)[0] : null,
      currentLanguage: track.language,
      currentYear: track.year,
      currentTempo: track.acousticFeatures?.tempo,
      currentEnergy: track.acousticFeatures?.energy,
      currentValence: track.acousticFeatures?.valence,
      currentPopularity: track.acousticFeatures?.popularity,
      ...extra
    });
  }
}

module.exports = PlaybackContext;
