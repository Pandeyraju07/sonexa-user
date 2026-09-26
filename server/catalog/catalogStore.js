/**
 * Indexed In-Memory Catalog Store
 * Provides fast O(1) indexed lookups by ID, title, artist, album, movie, genre, language, mood
 * and candidate filtering for recommendations & search.
 */

const { tracks, artists, albums, movies, playlists } = require('./catalogData');

function normalizeString(str) {
  if (!str) return '';
  return str.toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

class CatalogStore {
  constructor() {
    this.tracks = tracks;
    this.artists = artists;
    this.albums = albums;
    this.movies = movies;
    this.playlists = playlists;

    this.tracksById = new Map();
    this.tracksByTitle = new Map();
    this.tracksByArtist = new Map();
    this.tracksByMovie = new Map();
    this.tracksByAlbum = new Map();
    this.tracksByGenre = new Map();
    this.tracksByLanguage = new Map();
    this.tracksByMood = new Map();

    this.artistsById = new Map();
    this.artistsByName = new Map();
    this.artistsByAlias = new Map();

    this.moviesById = new Map();
    this.moviesByTitle = new Map();

    this.albumsById = new Map();
    this.albumsByTitle = new Map();

    this.buildIndexes();
  }

  buildIndexes() {
    // 1. Index Movies
    for (const movie of this.movies) {
      this.moviesById.set(movie.id, movie);
      const norm = normalizeString(movie.title);
      this.moviesByTitle.set(norm, movie);
      if (movie.normalizedTitle) {
        this.moviesByTitle.set(normalizeString(movie.normalizedTitle), movie);
      }
    }

    // 2. Index Artists
    for (const artist of this.artists) {
      this.artistsById.set(artist.id, artist);
      const norm = normalizeString(artist.name);
      this.artistsByName.set(norm, artist);
      if (artist.aliases) {
        for (const alias of artist.aliases) {
          this.artistsByAlias.set(normalizeString(alias), artist);
        }
      }
    }

    // 3. Index Albums
    for (const album of this.albums) {
      this.albumsById.set(album.id, album);
      this.albumsByTitle.set(normalizeString(album.title), album);
    }

    // 4. Index Tracks
    for (const track of this.tracks) {
      this.tracksById.set(track.id, track);

      // By Normalized Title
      const normTitle = normalizeString(track.title);
      if (!this.tracksByTitle.has(normTitle)) this.tracksByTitle.set(normTitle, []);
      this.tracksByTitle.get(normTitle).push(track);

      // By Artists
      const trackArtists = Array.isArray(track.artists) ? track.artists : [track.artist];
      for (const a of trackArtists) {
        const normA = normalizeString(a);
        if (!this.tracksByArtist.has(normA)) this.tracksByArtist.set(normA, []);
        this.tracksByArtist.get(normA).push(track);
      }

      // By Movie
      if (track.movie) {
        const normM = normalizeString(track.movie);
        if (!this.tracksByMovie.has(normM)) this.tracksByMovie.set(normM, []);
        this.tracksByMovie.get(normM).push(track);
      }

      // By Album
      if (track.album) {
        const normAlb = normalizeString(track.album);
        if (!this.tracksByAlbum.has(normAlb)) this.tracksByAlbum.set(normAlb, []);
        this.tracksByAlbum.get(normAlb).push(track);
      }

      // By Genre
      const allGenres = [...(track.genres || []), track.genre, track.subgenre].filter(Boolean);
      for (const g of allGenres) {
        const normG = normalizeString(g);
        if (!this.tracksByGenre.has(normG)) this.tracksByGenre.set(normG, []);
        this.tracksByGenre.get(normG).push(track);
      }

      // By Language
      if (track.language) {
        const normL = normalizeString(track.language);
        if (!this.tracksByLanguage.has(normL)) this.tracksByLanguage.set(normL, []);
        this.tracksByLanguage.get(normL).push(track);
      }

      // By Moods
      if (track.moods) {
        for (const [mood, weight] of Object.entries(track.moods)) {
          if (weight >= 0.5) {
            const normMood = mood.toUpperCase();
            if (!this.tracksByMood.has(normMood)) this.tracksByMood.set(normMood, []);
            this.tracksByMood.get(normMood).push(track);
          }
        }
      }
    }
  }

  // Fast getters
  getAllTracks() {
    return this.tracks;
  }

  getTrackById(id) {
    return this.tracksById.get(id) || null;
  }

  getArtistById(id) {
    return this.artistsById.get(id) || null;
  }

  getAlbumById(id) {
    return this.albumsById.get(id) || null;
  }

  getMovieById(id) {
    return this.moviesById.get(id) || null;
  }

  findArtist(query) {
    const norm = normalizeString(query);
    if (!norm) return null;
    return this.artistsByName.get(norm) ||
      this.artistsByAlias.get(norm) ||
      this.artists.find(a => normalizeString(a.name).includes(norm) || norm.includes(normalizeString(a.name))) ||
      null;
  }

  findMovie(query) {
    const norm = normalizeString(query);
    if (!norm) return null;
    return this.moviesByTitle.get(norm) ||
      this.movies.find(m => normalizeString(m.title).includes(norm) || norm.includes(normalizeString(m.title))) ||
      null;
  }

  getTracksByArtist(artistName) {
    const norm = normalizeString(artistName);
    return this.tracks.filter(t => {
      const artNorm = normalizeString(t.artist);
      const artistsNorm = (t.artists || []).map(a => normalizeString(a));
      return artNorm.includes(norm) || norm.includes(artNorm) || artistsNorm.some(a => a.includes(norm) || norm.includes(a));
    });
  }

  getTracksByMovie(movieNameOrId) {
    const norm = normalizeString(movieNameOrId);
    return this.tracks.filter(t => {
      if (t.movieId === movieNameOrId) return true;
      if (!t.movie) return false;
      const mNorm = normalizeString(t.movie);
      return mNorm.includes(norm) || norm.includes(mNorm);
    });
  }

  getTracksByGenre(genre) {
    const norm = normalizeString(genre);
    return this.tracks.filter(t => {
      const gNorm = normalizeString(t.genre);
      const subNorm = normalizeString(t.subgenre || '');
      const allNorm = (t.genres || []).map(g => normalizeString(g));
      return gNorm.includes(norm) || subNorm.includes(norm) || allNorm.some(g => g.includes(norm));
    });
  }

  getTracksByMood(mood, minWeight = 0.5) {
    const targetMood = mood.toUpperCase();
    return this.tracks.filter(t => {
      if (!t.moods) return false;
      const w = t.moods[targetMood];
      return typeof w === 'number' && w >= minWeight;
    });
  }

  getTracksByLanguage(language) {
    const norm = normalizeString(language);
    return this.tracks.filter(t => normalizeString(t.language) === norm);
  }
}

// Singleton catalog store
const catalogStore = new CatalogStore();

module.exports = {
  catalogStore,
  normalizeString
};
