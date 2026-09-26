/**
 * RecommendationExplanationService
 * Generates transparent, human-readable explanations without exposing numerical scores.
 */

class RecommendationExplanationService {
  explain(track, context, strategy, reason) {
    if (reason && reason.length > 5) return reason;

    if (context.currentMovieName && track.movie && track.movie.toLowerCase() === context.currentMovieName.toLowerCase()) {
      return `From the movie ${track.movie}`;
    }

    if (context.currentArtistName && track.artist && track.artist.toLowerCase().includes(context.currentArtistName.toLowerCase())) {
      return `Because you listened to ${context.currentArtistName}`;
    }

    if (track.moods && context.currentMood && track.moods[context.currentMood.toUpperCase()] >= 0.7) {
      return `Because you enjoy ${context.currentMood.toLowerCase()} music`;
    }

    if (track.genre && context.currentGenre && track.genre.toLowerCase() === context.currentGenre.toLowerCase()) {
      return `Popular in ${track.genre}`;
    }

    return 'Curated based on your current vibe';
  }
}

const recommendationExplanationService = new RecommendationExplanationService();

module.exports = {
  RecommendationExplanationService,
  recommendationExplanationService
};
