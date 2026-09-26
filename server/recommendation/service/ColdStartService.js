/**
 * ColdStartService
 * Handles new user onboarding signals and content cold-start for newly released songs.
 */

const { catalogStore } = require('../../catalog/catalogStore');

class ColdStartService {
  /**
   * Generates recommendations for a brand new user with no history
   * @param {Object} onboardingPreferences - { languages: [], genres: [], moods: [] }
   * @param {number} limit
   * @returns {Array<Object>}
   */
  getColdStartRecommendations(onboardingPreferences = {}, limit = 20) {
    const allTracks = catalogStore.getAllTracks();
    const prefLanguages = (onboardingPreferences.languages || ['Hindi']).map(l => l.toLowerCase());
    const prefGenres = (onboardingPreferences.genres || ['Bollywood']).map(g => g.toLowerCase());
    const prefMoods = (onboardingPreferences.moods || ['ROMANTIC']).map(m => m.toUpperCase());

    const scored = allTracks.map(track => {
      let score = 0.5; // baseline trending

      // Language match
      if (track.language && prefLanguages.includes(track.language.toLowerCase())) {
        score += 0.25;
      }

      // Genre match
      if (track.genre && prefGenres.some(g => track.genre.toLowerCase().includes(g))) {
        score += 0.20;
      }

      // Mood match
      if (track.moods && prefMoods.some(m => (track.moods[m] || 0) >= 0.6)) {
        score += 0.15;
      }

      // Popularity
      score += (track.acousticFeatures?.popularity ?? 0.8) * 0.1;

      return {
        track,
        score: Math.min(1.0, score),
        reason: 'Trending discovery based on your onboarding preferences'
      };
    });

    scored.sort((a, b) => b.score - a.score);
    return scored.slice(0, limit);
  }
}

const coldStartService = new ColdStartService();

module.exports = {
  ColdStartService,
  coldStartService
};
