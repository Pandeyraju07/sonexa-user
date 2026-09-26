/**
 * User Affinity & Interaction Store
 * Maintains real-time behavioral profiles for songs, artists, genres, moods, and sessions.
 */

class UserAffinityStore {
  constructor() {
    // Map<userId, Map<songId, UserSongAffinity>>
    this.userSongAffinities = new Map();
    // Map<userId, Map<artistKey, UserArtistAffinity>>
    this.userArtistAffinities = new Map();
    // Map<userId, Map<genreKey, UserGenreAffinity>>
    this.userGenreAffinities = new Map();
    // Map<userId, Map<moodKey, UserMoodAffinity>>
    this.userMoodAffinities = new Map();

    // Map<sessionId, RecommendationSession>
    this.sessions = new Map();

    // Array<PlaybackEvent>
    this.eventsLog = [];

    // Array<RecommendationLog>
    this.recommendationLogs = [];
  }

  getSongAffinity(userId, songId) {
    const userMap = this.userSongAffinities.get(userId);
    if (!userMap || !userMap.has(songId)) {
      return {
        userId,
        songId,
        playCount: 0,
        completionCount: 0,
        skipCount: 0,
        earlySkipCount: 0,
        likeCount: 0,
        replayCount: 0,
        lastPlayedAt: null,
        affinityScore: 0.0
      };
    }
    return userMap.get(songId);
  }

  setSongAffinity(userId, songId, affinity) {
    if (!this.userSongAffinities.has(userId)) {
      this.userSongAffinities.set(userId, new Map());
    }
    this.userSongAffinities.get(userId).set(songId, affinity);
  }

  getArtistAffinity(userId, artistName) {
    const key = (artistName || '').toLowerCase().trim();
    const userMap = this.userArtistAffinities.get(userId);
    if (!userMap || !userMap.has(key)) {
      return {
        userId,
        artistName,
        playCount: 0,
        completionCount: 0,
        skipCount: 0,
        likeCount: 0,
        lastPlayedAt: null,
        affinityScore: 0.0
      };
    }
    return userMap.get(key);
  }

  setArtistAffinity(userId, artistName, affinity) {
    const key = (artistName || '').toLowerCase().trim();
    if (!this.userArtistAffinities.has(userId)) {
      this.userArtistAffinities.set(userId, new Map());
    }
    this.userArtistAffinities.get(userId).set(key, affinity);
  }

  getGenreAffinity(userId, genre) {
    const key = (genre || '').toLowerCase().trim();
    const userMap = this.userGenreAffinities.get(userId);
    if (!userMap || !userMap.has(key)) {
      return { userId, genre, score: 0.0, playCount: 0, skipCount: 0 };
    }
    return userMap.get(key);
  }

  setGenreAffinity(userId, genre, affinity) {
    const key = (genre || '').toLowerCase().trim();
    if (!this.userGenreAffinities.has(userId)) {
      this.userGenreAffinities.set(userId, new Map());
    }
    this.userGenreAffinities.get(userId).set(key, affinity);
  }

  getMoodAffinity(userId, mood) {
    const key = (mood || '').toUpperCase().trim();
    const userMap = this.userMoodAffinities.get(userId);
    if (!userMap || !userMap.has(key)) {
      return { userId, mood: key, score: 0.0, playCount: 0, skipCount: 0 };
    }
    return userMap.get(key);
  }

  setMoodAffinity(userId, mood, affinity) {
    const key = (mood || '').toUpperCase().trim();
    if (!this.userMoodAffinities.has(userId)) {
      this.userMoodAffinities.set(userId, new Map());
    }
    this.userMoodAffinities.get(userId).set(key, affinity);
  }

  getSession(sessionId) {
    return this.sessions.get(sessionId) || null;
  }

  saveSession(session) {
    this.sessions.set(session.sessionId, session);
  }

  recordEvent(event) {
    this.eventsLog.push({
      ...event,
      timestamp: event.timestamp || Date.now()
    });
    if (this.eventsLog.length > 50000) {
      this.eventsLog.splice(0, 10000);
    }
  }

  logRecommendation(logEntry) {
    this.recommendationLogs.push({
      ...logEntry,
      timestamp: Date.now()
    });
    if (this.recommendationLogs.length > 20000) {
      this.recommendationLogs.splice(0, 5000);
    }
  }

  getUserProfileSummary(userId) {
    const songMap = this.userSongAffinities.get(userId) || new Map();
    const artistMap = this.userArtistAffinities.get(userId) || new Map();
    const genreMap = this.userGenreAffinities.get(userId) || new Map();
    const moodMap = this.userMoodAffinities.get(userId) || new Map();

    const topArtists = Array.from(artistMap.values())
      .sort((a, b) => b.affinityScore - a.affinityScore)
      .slice(0, 5);

    const topGenres = Array.from(genreMap.values())
      .sort((a, b) => b.score - a.score)
      .slice(0, 5);

    const topMoods = Array.from(moodMap.values())
      .sort((a, b) => b.score - a.score)
      .slice(0, 5);

    const topSongs = Array.from(songMap.values())
      .sort((a, b) => b.affinityScore - a.affinityScore)
      .slice(0, 5);

    return {
      userId,
      topArtists,
      topGenres,
      topMoods,
      topSongs,
      totalInteractions: songMap.size
    };
  }
}

const userAffinityStore = new UserAffinityStore();

module.exports = {
  userAffinityStore,
  UserAffinityStore
};
