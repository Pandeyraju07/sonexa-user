package com.sonexa.app.data.provider

import com.sonexa.app.data.model.AlbumDto
import com.sonexa.app.data.model.TrackDto
import java.text.Normalizer
import java.util.Locale

data class MovieSoundtrack(
    val movieId: String,
    val movieTitle: String,
    val normalizedAliases: List<String>,
    val releaseYear: String,
    val bannerUrl: String,
    val musicDirector: String,
    val tracks: List<TrackDto> = emptyList()
)

object MovieSoundtrackCatalog {

    /**
     * Extracts movie query intent from search phrases like:
     * - "songs of dhurandher" -> "dhurandher"
     * - "songs of animal" -> "animal"
     * - "kabir singh songs" -> "kabir singh"
     * - "jawan all songs" -> "jawan"
     * - "stree 2 full album" -> "stree 2"
     */
    fun extractMovieQuery(rawQuery: String): String {
        val trimmed = rawQuery.trim().lowercase(Locale.ROOT)
        return trimmed
            .replace(Regex("""^(songs\s+of|all\s+songs\s+of|soundtrack\s+of|songs\s+from|music\s+of|album\s+of)\s+"""), "")
            .replace(Regex("""\s+(songs|all\s+songs|movie\s+songs|full\s+album|soundtrack|ost|movie|film)$"""), "")
            .replace(Regex("""^(movie|film)\s+"""), "")
            .trim()
    }

    private fun normalize(str: String): String {
        val decomposed = Normalizer.normalize(str, Normalizer.Form.NFD)
        return decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase(Locale.ROOT)
            .trim()
    }

    // Curated rich movie soundtrack index (Metadata-only; tracks resolved legitimately from music providers)
    private val soundtracks: List<MovieSoundtrack> = listOf(
        MovieSoundtrack(
            movieId = "movie_kabir_singh",
            movieTitle = "Kabir Singh",
            normalizedAliases = listOf("kabir singh", "kabir", "kabir singh movie", "kabir singh songs"),
            releaseYear = "2019",
            bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
            musicDirector = "Mithoon, Amaal Mallik, Vishal Mishra, Sachet–Parampara, Akhil Sachdeva"
        ),
        MovieSoundtrack(
            movieId = "movie_shershaah",
            movieTitle = "Shershaah",
            normalizedAliases = listOf("shershaah", "shershah", "shershaah movie", "shershaah songs"),
            releaseYear = "2021",
            bannerUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600",
            musicDirector = "Tanishk Bagchi, B Praak, Jasleen Royal, Javed-Mohsin"
        ),
        MovieSoundtrack(
            movieId = "movie_rockstar",
            movieTitle = "Rockstar",
            normalizedAliases = listOf("rockstar", "rockstar movie", "rockstar songs"),
            releaseYear = "2011",
            bannerUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            musicDirector = "A.R. Rahman"
        ),
        MovieSoundtrack(
            movieId = "movie_dhurandhar",
            movieTitle = "Dhurandhar",
            normalizedAliases = listOf("dhurandher", "dhurandhar", "dhurandhar movie", "dhurander"),
            releaseYear = "2025",
            bannerUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600",
            musicDirector = "Shankar-Ehsaan-Loy"
        ),
        MovieSoundtrack(
            movieId = "movie_animal",
            movieTitle = "Animal",
            normalizedAliases = listOf("animal", "animal movie", "animal songs"),
            releaseYear = "2023",
            bannerUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600",
            musicDirector = "Pritam, JAM8, Vishal Mishra, Manan Bhardwaj, Shreyas Puranik"
        ),
        MovieSoundtrack(
            movieId = "movie_jawan",
            movieTitle = "Jawan",
            normalizedAliases = listOf("jawan", "jawaan", "jawan movie", "jawan songs"),
            releaseYear = "2023",
            bannerUrl = "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600",
            musicDirector = "Anirudh Ravichander"
        ),
        MovieSoundtrack(
            movieId = "movie_dunki",
            movieTitle = "Dunki",
            normalizedAliases = listOf("dunki", "dunki movie", "dunki songs"),
            releaseYear = "2023",
            bannerUrl = "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?w=600",
            musicDirector = "Pritam"
        ),
        MovieSoundtrack(
            movieId = "movie_pathaan",
            movieTitle = "Pathaan",
            normalizedAliases = listOf("pathaan", "pathan", "pathaan movie"),
            releaseYear = "2023",
            bannerUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
            musicDirector = "Vishal-Shekhar"
        )
    )

    fun findMovieSoundtrack(query: String): MovieSoundtrack? {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return null

        return soundtracks.firstOrNull { ost ->
            normalize(ost.movieTitle) == qNorm ||
                    ost.normalizedAliases.any { normalize(it) == qNorm } ||
                    qNorm.contains(normalize(ost.movieTitle))
        }
    }

    fun getAllSoundtracks(): List<MovieSoundtrack> = soundtracks

    fun toAlbumDto(movie: MovieSoundtrack): AlbumDto {
        return AlbumDto(
            id = "alb_" + normalize(movie.movieTitle),
            title = "${movie.movieTitle} (Original Soundtrack)",
            artist = movie.musicDirector,
            year = movie.releaseYear,
            coverUrl = movie.bannerUrl,
            trackCount = 6
        )
    }
}
