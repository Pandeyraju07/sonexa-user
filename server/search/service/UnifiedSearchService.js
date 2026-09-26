/**
 * UnifiedSearchService
 * Executes intent-driven search with entity resolution, ranking, and grouped response structures.
 */

const { searchIntentDetector } = require('../intent/SearchIntentDetector');
const { searchRankingService } = require('../ranking/SearchRankingService');
const { catalogStore, normalizeString } = require('../../catalog/catalogStore');
const { hybridRecommendationEngine } = require('../../recommendation/engine/HybridRecommendationEngine');
const { affinityEngine } = require('../../events/AffinityEngine');

class UnifiedSearchService {
  search(rawQuery, userId = 'usr_anonymous') {
    const query = (rawQuery || '').trim();
    if (!query) {
      return {
        query: '',
        intent: 'GENERAL',
        entities: [],
        topResult: null,
        songs: catalogStore.getAllTracks().slice(0, 5),
        artists: catalogStore.artists.slice(0, 3),
        albums: catalogStore.albums.slice(0, 2),
        movies: catalogStore.movies.slice(0, 2),
        playlists: catalogStore.playlists.slice(0, 2)
      };
    }

    const intent = searchIntentDetector.detect(query);
    const allTracks = catalogStore.getAllTracks();
    let matchedSongs = [];
    let matchedArtists = [];
    let matchedAlbums = [];
    let matchedMovies = [];
    let matchedPlaylists = [];
    let topResult = null;

    // Track search affinity update
    if (intent.artist) {
      affinityEngine.processEvent({
        userId,
        songId: null,
        eventType: 'SEARCH_PERFORMED',
        metadata: { query, artist: intent.artist.name }
      });
    }

    switch (intent.type) {
      case 'MOVIE_SONGS': {
        const movie = intent.movie;
        matchedMovies = [movie];
        matchedSongs = catalogStore.getTracksByMovie(movie.id);
        matchedAlbums = catalogStore.albums.filter(a => a.movieId === movie.id);
        topResult = {
          type: 'MOVIE',
          data: movie,
          badge: 'Movie Soundtrack'
        };
        break;
      }

      case 'ARTIST': {
        const artist = intent.artist;
        matchedArtists = [artist];
        matchedSongs = catalogStore.getTracksByArtist(artist.name);
        matchedAlbums = catalogStore.albums.filter(a =>
          normalizeString(a.artist).includes(normalizeString(artist.name))
        );
        topResult = {
          type: 'ARTIST',
          data: artist,
          badge: 'Top Artist'
        };
        break;
      }

      case 'ARTIST_MOOD': {
        const artist = intent.artist;
        const mood = intent.mood;
        matchedArtists = [artist];
        const artistTracks = catalogStore.getTracksByArtist(artist.name);
        matchedSongs = artistTracks.filter(t => (t.moods || {})[mood] >= 0.65);
        if (matchedSongs.length === 0) matchedSongs = artistTracks;
        topResult = {
          type: 'ARTIST_MOOD',
          data: { artist, mood },
          badge: `${artist.name} • ${mood.toLowerCase()}`
        };
        break;
      }

      case 'GENRE_MOOD': {
        const genre = intent.genre;
        const mood = intent.mood;
        matchedSongs = allTracks.filter(t => {
          const gMatch = (t.genre && t.genre.toLowerCase().includes(genre.toLowerCase())) ||
            (t.language && t.language.toLowerCase().includes(genre.toLowerCase()));
          const mMatch = (t.moods || {})[mood] >= 0.65;
          return gMatch && mMatch;
        });
        topResult = {
          type: 'GENRE_MOOD',
          data: { genre, mood },
          badge: `${genre} • ${mood.toLowerCase()}`
        };
        break;
      }

      case 'MOOD': {
        matchedSongs = catalogStore.getTracksByMood(intent.mood, 0.7);
        topResult = {
          type: 'MOOD',
          data: { mood: intent.mood },
          badge: `${intent.mood.toLowerCase()} vibe`
        };
        break;
      }

      case 'SIMILAR_SONG': {
        const targetTitle = normalizeString(intent.targetSongTitle);
        const targetTrack = allTracks.find(t => normalizeString(t.title).includes(targetTitle));
        if (targetTrack) {
          const similar = hybridRecommendationEngine.getSimilarTracks(targetTrack.id, 10);
          matchedSongs = [targetTrack, ...similar.map(s => s.track)];
          topResult = {
            type: 'SONG',
            data: targetTrack,
            badge: `Similar to ${targetTrack.title}`
          };
        } else {
          matchedSongs = allTracks.slice(0, 5);
        }
        break;
      }

      case 'ARTIST_RECENCY': {
        const artist = intent.artist;
        matchedArtists = [artist];
        const artistTracks = catalogStore.getTracksByArtist(artist.name);
        matchedSongs = [...artistTracks].sort((a, b) => (b.year || 2024) - (a.year || 2024));
        topResult = {
          type: 'ARTIST',
          data: artist,
          badge: `Latest releases by ${artist.name}`
        };
        break;
      }

      case 'YEAR_RANGE': {
        const [minYear, maxYear] = intent.yearRange;
        matchedSongs = allTracks.filter(t => {
          const y = t.year || 2024;
          const yearMatch = y >= minYear && y <= maxYear;
          const genreMatch = !intent.genre || (t.genre && t.genre.toLowerCase().includes(intent.genre.toLowerCase()));
          return yearMatch && genreMatch;
        });
        topResult = {
          type: 'ERA',
          data: { era: intent.era, range: intent.yearRange },
          badge: `${intent.era.toUpperCase()} Hits`
        };
        break;
      }

      case 'NATURAL_LANGUAGE': {
        const semanticResults = hybridRecommendationEngine.vectorSearchService.semanticSearch(query, 10);
        matchedSongs = semanticResults.map(r => r.track);
        topResult = {
          type: 'SEMANTIC_DISCOVERY',
          data: { query },
          badge: 'AI Curated Match'
        };
        break;
      }

      default: {
        const qNorm = normalizeString(query);
        matchedSongs = allTracks.filter(t =>
          normalizeString(t.title).includes(qNorm) ||
          normalizeString(t.artist).includes(qNorm) ||
          (t.movie && normalizeString(t.movie).includes(qNorm)) ||
          normalizeString(t.genre).includes(qNorm)
        );
        matchedArtists = catalogStore.artists.filter(a => normalizeString(a.name).includes(qNorm));
        matchedMovies = catalogStore.movies.filter(m => normalizeString(m.title).includes(qNorm));
        matchedAlbums = catalogStore.albums.filter(alb => normalizeString(alb.title).includes(qNorm));
        break;
      }
    }

    // Rank matched songs using 7-factor search ranking service
    const rankedSongs = searchRankingService.rankTracks(
      matchedSongs.length > 0 ? matchedSongs : allTracks.slice(0, 4),
      query,
      intent,
      userId
    );

    if (!topResult && rankedSongs.length > 0) {
      topResult = {
        type: 'SONG',
        data: rankedSongs[0],
        badge: 'Top Match'
      };
    }

    return {
      query,
      intent: intent.type,
      entities: [
        intent.movie ? { type: 'MOVIE', name: intent.movie.title } : null,
        intent.artist ? { type: 'ARTIST', name: intent.artist.name } : null,
        intent.mood ? { type: 'MOOD', name: intent.mood } : null,
        intent.genre ? { type: 'GENRE', name: intent.genre } : null
      ].filter(Boolean),
      topResult,
      songs: rankedSongs,
      albums: matchedAlbums,
      artists: matchedArtists,
      movies: matchedMovies,
      playlists: catalogStore.playlists.filter(p =>
        normalizeString(p.title).includes(normalizeString(query))
      )
    };
  }
}

const unifiedSearchService = new UnifiedSearchService();

module.exports = {
  UnifiedSearchService,
  unifiedSearchService
};
