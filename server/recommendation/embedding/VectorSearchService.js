/**
 * VectorSearchService
 * Performs vector similarity retrieval across songs, artists, and natural language queries.
 */

const { catalogStore } = require('../../catalog/catalogStore');

class VectorSearchService {
  constructor(embeddingService) {
    this.embeddingService = embeddingService;
  }

  findSimilarSongs(targetSongId, limit = 10, filters = {}) {
    const targetSong = catalogStore.getTrackById(targetSongId);
    if (!targetSong) return [];

    const targetVector = this.embeddingService.generateSongEmbedding(targetSong);
    const allTracks = catalogStore.getAllTracks();

    const scored = [];
    for (const track of allTracks) {
      if (track.id === targetSongId) continue;

      // Filter restrictions if specified
      if (filters.language && track.language !== filters.language) continue;
      if (filters.genre && track.genre !== filters.genre) continue;

      const trackVector = this.embeddingService.generateSongEmbedding(track);
      const similarity = this.embeddingService.computeCosineSimilarity(targetVector, trackVector);

      scored.push({
        track,
        score: similarity,
        reason: this.explainVectorSimilarity(targetSong, track, similarity)
      });
    }

    scored.sort((a, b) => b.score - a.score);
    return scored.slice(0, limit);
  }

  findSimilarArtists(artistId, limit = 5) {
    const artist = catalogStore.getArtistById(artistId);
    if (!artist) return [];

    const allArtists = catalogStore.artists;
    const scored = [];

    for (const other of allArtists) {
      if (other.id === artistId) continue;
      let score = 0;
      if (other.genre === artist.genre) score += 0.5;
      const commonGenres = (other.genres || []).filter(g => (artist.genres || []).includes(g));
      score += commonGenres.length * 0.25;

      scored.push({
        artist: other,
        score: Math.min(1.0, score)
      });
    }

    scored.sort((a, b) => b.score - a.score);
    return scored.slice(0, limit).map(s => s.artist);
  }

  findSimilarAlbums(albumId, limit = 5) {
    const album = catalogStore.getAlbumById(albumId);
    if (!album) return [];

    return catalogStore.albums
      .filter(a => a.id !== albumId)
      .slice(0, limit);
  }

  semanticSearch(queryText, limit = 10) {
    const textVector = this.embeddingService.generateTextEmbedding(queryText);
    const allTracks = catalogStore.getAllTracks();

    const scored = allTracks.map(track => {
      const trackVector = this.embeddingService.generateSongEmbedding(track);
      const similarity = this.embeddingService.computeCosineSimilarity(textVector, trackVector);
      return {
        track,
        score: similarity
      };
    });

    scored.sort((a, b) => b.score - a.score);
    return scored.slice(0, limit);
  }

  explainVectorSimilarity(targetSong, candidateSong, similarity) {
    if (targetSong.movie && candidateSong.movie && targetSong.movie === candidateSong.movie) {
      return `From the same soundtrack: ${targetSong.movie}`;
    }
    if (targetSong.artist === candidateSong.artist) {
      return `Also by ${targetSong.artist}`;
    }
    const sharedMoods = Object.keys(targetSong.moods || {}).filter(m => (candidateSong.moods || {})[m] >= 0.6);
    if (sharedMoods.length > 0) {
      return `Matches ${sharedMoods[0].toLowerCase()} vibe and acoustic tempo`;
    }
    return 'Similar acoustic profile and musical energy';
  }
}

module.exports = VectorSearchService;
