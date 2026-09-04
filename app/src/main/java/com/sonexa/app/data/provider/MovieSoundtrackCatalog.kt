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
    val tracks: List<TrackDto> = emptyList(),
    val searchTerms: List<String> = emptyList()
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

    private val soundtracks: List<MovieSoundtrack> = listOf(
        MovieSoundtrack(
            movieId = "movie_aashiqui_2",
            movieTitle = "Aashiqui 2",
            normalizedAliases = listOf("aashiqui 2", "aashiqui2", "aashiqui 2 songs", "aashiqui 2 movie"),
            releaseYear = "2013",
            bannerUrl = "https://c.saavncdn.com/264/Aashiqui-2-Hindi-2013-500x500.jpg",
            musicDirector = "Mithoon, Ankit Tiwari, Jeet Gannguli",
            searchTerms = listOf("Tum Hi Ho", "Sunn Raha Hai", "Chahun Main Ya Naa", "Bhula Dena", "Piya Aaye Na", "Milne Hai Mujhse Aayi", "Meri Aashiqui")
        ),
        MovieSoundtrack(
            movieId = "movie_kabir_singh",
            movieTitle = "Kabir Singh",
            normalizedAliases = listOf("kabir singh", "kabir", "kabir singh movie", "kabir singh songs"),
            releaseYear = "2019",
            bannerUrl = "https://c.saavncdn.com/352/Kabir-Singh-Hindi-2019-20240130132014-500x500.jpg",
            musicDirector = "Mithoon, Amaal Mallik, Vishal Mishra, Sachet–Parampara, Akhil Sachdeva",
            searchTerms = listOf("Bekhayali", "Tujhe Kitna Chahne Lage", "Kaise Hua", "Tera Ban Jaunga", "Pehla Pyaar", "Mere Sohneya")
        ),
        MovieSoundtrack(
            movieId = "movie_animal",
            movieTitle = "Animal",
            normalizedAliases = listOf("animal", "animal movie", "animal songs", "animal soundtrack"),
            releaseYear = "2023",
            bannerUrl = "https://c.saavncdn.com/092/ANIMAL-Hindi-2023-20231124191438-500x500.jpg",
            musicDirector = "Pritam, JAM8, Vishal Mishra, Manan Bhardwaj, Shreyas Puranik",
            searchTerms = listOf("Satranga", "Hua Main", "Pehle Bhi Main", "Arjan Vailly", "Papa Meri Jaan", "Saari Duniya Jalaa Denge", "Marham")
        ),
        MovieSoundtrack(
            movieId = "movie_shershaah",
            movieTitle = "Shershaah",
            normalizedAliases = listOf("shershaah", "shershah", "shershaah movie", "shershaah songs"),
            releaseYear = "2021",
            bannerUrl = "https://c.saavncdn.com/238/Shershaah-Original-Motion-Picture-Soundtrack-Hindi-2021-20210815181610-500x500.jpg",
            musicDirector = "Tanishk Bagchi, B Praak, Jasleen Royal, Javed-Mohsin",
            searchTerms = listOf("Raataan Lambiyan", "Ranjha", "Mann Bharryaa 2.0", "Kabhii Tumhhe", "Jai Hind Ki Senaa")
        ),
        MovieSoundtrack(
            movieId = "movie_yjhd",
            movieTitle = "Yeh Jawaani Hai Deewani",
            normalizedAliases = listOf("yeh jawaani hai deewani", "yjhd", "yeh jawani hai deewani"),
            releaseYear = "2013",
            bannerUrl = "https://c.saavncdn.com/001/Yeh-Jawaani-Hai-Deewani-Hindi-2013-500x500.jpg",
            musicDirector = "Pritam",
            searchTerms = listOf("Badtameez Dil", "Balam Pichkari", "Ilahi", "Kabira", "Subhanallah", "Ghagra", "Dilliwaali Girlfriend")
        ),
        MovieSoundtrack(
            movieId = "movie_rockstar",
            movieTitle = "Rockstar",
            normalizedAliases = listOf("rockstar", "rockstar movie", "rockstar songs"),
            releaseYear = "2011",
            bannerUrl = "https://c.saavncdn.com/264/Rockstar-Hindi-2011-500x500.jpg",
            musicDirector = "A.R. Rahman",
            searchTerms = listOf("Kun Faya Kun", "Nadaan Parindey", "Sadda Haq", "Tum Ho", "Jo Bhi Main", "Phir Se Ud Chala", "Hawaa Hawaa", "Katiya Karun")
        ),
        MovieSoundtrack(
            movieId = "movie_jab_we_met",
            movieTitle = "Jab We Met",
            normalizedAliases = listOf("jab we met", "jab we met songs"),
            releaseYear = "2007",
            bannerUrl = "https://c.saavncdn.com/152/Jab-We-Met-Hindi-2007-500x500.jpg",
            musicDirector = "Pritam, Sandesh Shandilya",
            searchTerms = listOf("Tum Se Hi", "Mauja Hi Mauja", "Nagada Nagada", "Aaoge Jab Tum", "Yeh Ishq Hai", "Aao Milo Chalo")
        ),
        MovieSoundtrack(
            movieId = "movie_brahmastra",
            movieTitle = "Brahmāstra: Part One – Shiva",
            normalizedAliases = listOf("brahmastra", "brahmastra songs", "brahmastra part one shiva"),
            releaseYear = "2022",
            bannerUrl = "https://c.saavncdn.com/007/Brahmastra-Hindi-2022-20221006180034-500x500.jpg",
            musicDirector = "Pritam",
            searchTerms = listOf("Kesariya", "Deva Deva", "Dance Ka Bhoot", "Rasiya", "Kesariya Audio Teaser")
        ),
        MovieSoundtrack(
            movieId = "movie_pushpa",
            movieTitle = "Pushpa: The Rise",
            normalizedAliases = listOf("pushpa", "pushpa the rise", "pushpa songs"),
            releaseYear = "2021",
            bannerUrl = "https://c.saavncdn.com/712/Pushpa-The-Rise-Hindi-2021-20211223201449-500x500.jpg",
            musicDirector = "Devi Sri Prasad",
            searchTerms = listOf("Srivalli", "Oo Bolega Ya Oo Oo Bolega", "Sami Sami", "Eyy Bidda Ye Mera Adda", "Jaago Jaago Bakre")
        ),
        MovieSoundtrack(
            movieId = "movie_pushpa_2",
            movieTitle = "Pushpa 2: The Rule",
            normalizedAliases = listOf("pushpa 2", "pushpa 2 the rule", "pushpa 2 songs"),
            releaseYear = "2024",
            bannerUrl = "https://c.saavncdn.com/956/Pushpa-2-The-Rule-Hindi-2024-20240501170014-500x500.jpg",
            musicDirector = "Devi Sri Prasad",
            searchTerms = listOf("Pushpa Pushpa", "Angaaron", "The Couple Song", "Kissik", "Peelings")
        ),
        MovieSoundtrack(
            movieId = "movie_jawan",
            movieTitle = "Jawan",
            normalizedAliases = listOf("jawan", "jawaan", "jawan movie", "jawan songs"),
            releaseYear = "2023",
            bannerUrl = "https://c.saavncdn.com/445/Jawan-Hindi-2023-20230906103328-500x500.jpg",
            musicDirector = "Anirudh Ravichander",
            searchTerms = listOf("Chaleya", "Zinda Banda", "Not Ramaiya Vastavaiya", "Aararaari Raaro", "Jawan Title Track")
        ),
        MovieSoundtrack(
            movieId = "movie_pathaan",
            movieTitle = "Pathaan",
            normalizedAliases = listOf("pathaan", "pathan", "pathaan movie", "pathaan songs"),
            releaseYear = "2023",
            bannerUrl = "https://c.saavncdn.com/001/Pathaan-Hindi-2023-20230125102542-500x500.jpg",
            musicDirector = "Vishal-Shekhar",
            searchTerms = listOf("Besharam Rang", "Jhoome Jo Pathaan", "Jim's Theme", "Pathaan's Theme")
        ),
        MovieSoundtrack(
            movieId = "movie_dunki",
            movieTitle = "Dunki",
            normalizedAliases = listOf("dunki", "dunki movie", "dunki songs"),
            releaseYear = "2023",
            bannerUrl = "https://c.saavncdn.com/152/Dunki-Hindi-2023-20231221111624-500x500.jpg",
            musicDirector = "Pritam",
            searchTerms = listOf("Lutt Putt Gaya", "Nikle The Kabhi Hum Ghar Se", "O Maahi", "Banda", "Main Tera Rasta Dekhunga")
        ),
        MovieSoundtrack(
            movieId = "movie_sanam_teri_kasam",
            movieTitle = "Sanam Teri Kasam",
            normalizedAliases = listOf("sanam teri kasam", "sanam teri kasam songs"),
            releaseYear = "2016",
            bannerUrl = "https://c.saavncdn.com/264/Sanam-Teri-Kasam-Hindi-2016-500x500.jpg",
            musicDirector = "Himesh Reshammiya",
            searchTerms = listOf("Sanam Teri Kasam", "Kheech Meri Photo", "Tera Chehra", "Bewajah", "Haal-E-Dil", "Ek Number")
        ),
        MovieSoundtrack(
            movieId = "movie_laila_majnu",
            movieTitle = "Laila Majnu",
            normalizedAliases = listOf("laila majnu", "laila majnu songs"),
            releaseYear = "2018",
            bannerUrl = "https://c.saavncdn.com/001/Laila-Majnu-Hindi-2018-500x500.jpg",
            musicDirector = "Niladri Kumar, Joi Barua",
            searchTerms = listOf("O Meri Laila", "Aahista", "Hafiz Hafiz", "Tum", "Sarphiri", "Katyu Chuko")
        ),
        MovieSoundtrack(
            movieId = "movie_adhm",
            movieTitle = "Ae Dil Hai Mushkil",
            normalizedAliases = listOf("ae dil hai mushkil", "adhm", "ae dil hai mushkil songs"),
            releaseYear = "2016",
            bannerUrl = "https://c.saavncdn.com/264/Ae-Dil-Hai-Mushkil-Deluxe-Edition-Hindi-2016-500x500.jpg",
            musicDirector = "Pritam",
            searchTerms = listOf("Ae Dil Hai Mushkil", "Bulleya", "Channa Mereya", "The Breakup Song", "Cutiepie", "Alizeh")
        ),
        MovieSoundtrack(
            movieId = "movie_stree_2",
            movieTitle = "Stree 2",
            normalizedAliases = listOf("stree 2", "stree 2 songs", "stree 2 full album"),
            releaseYear = "2024",
            bannerUrl = "https://c.saavncdn.com/492/Stree-2-Hindi-2024-20240816111624-500x500.jpg",
            musicDirector = "Sachin-Jigar",
            searchTerms = listOf("Aaj Ki Raat", "Aayi Nai", "Khoobsurat", "Tumhare Hi Rahenge Hum")
        ),
        MovieSoundtrack(
            movieId = "movie_fighter",
            movieTitle = "Fighter",
            normalizedAliases = listOf("fighter", "fighter movie", "fighter songs"),
            releaseYear = "2024",
            bannerUrl = "https://c.saavncdn.com/001/Fighter-Hindi-2024-20240125152011-500x500.jpg",
            musicDirector = "Vishal-Shekhar",
            searchTerms = listOf("Sher Khul Gaye", "Ishq Jaisa Kuch", "Heer Aasmani", "Bekaar Dil", "Mitti")
        ),
        MovieSoundtrack(
            movieId = "movie_tjmm",
            movieTitle = "Tu Jhoothi Main Makkaar",
            normalizedAliases = listOf("tu jhoothi main makkaar", "tjmm", "tu jhuthi mai makkar"),
            releaseYear = "2023",
            bannerUrl = "https://c.saavncdn.com/264/Tu-Jhoothi-Main-Makkaar-Hindi-2023-20230308111624-500x500.jpg",
            musicDirector = "Pritam",
            searchTerms = listOf("Tere Pyaar Mein", "Pyaar Hota Kayi Baar Hai", "Show Me The Thumka", "Jaadui", "O Bedardeya")
        ),
        MovieSoundtrack(
            movieId = "movie_kal_ho_naa_ho",
            movieTitle = "Kal Ho Naa Ho",
            normalizedAliases = listOf("kal ho naa ho", "kal ho na ho", "khnh"),
            releaseYear = "2003",
            bannerUrl = "https://c.saavncdn.com/001/Kal-Ho-Naa-Ho-Hindi-2003-500x500.jpg",
            musicDirector = "Shankar-Ehsaan-Loy",
            searchTerms = listOf("Kal Ho Naa Ho", "Maahi Ve", "Kuch To Hua Hai", "Pretty Woman", "It's The Time To Disco", "Heartbeat")
        ),
        MovieSoundtrack(
            movieId = "movie_ddlj",
            movieTitle = "Dilwale Dulhania Le Jayenge",
            normalizedAliases = listOf("ddlj", "dilwale dulhania le jayenge", "dilwale dulhaniya le jayenge"),
            releaseYear = "1995",
            bannerUrl = "https://c.saavncdn.com/264/Dilwale-Dulhania-Le-Jayenge-Hindi-1995-500x500.jpg",
            musicDirector = "Jatin-Lalit",
            searchTerms = listOf("Tujhe Dekha Toh", "Mehndi Laga Ke Rakhna", "Mere Khwabon Mein", "Ruk Ja O Dil Deewane", "Ho Gaya Hai Tujhko Toh Pyar Sajna", "Zara Sa Jhoom Loon Main")
        )
    )

    fun findMovieSoundtrack(query: String): MovieSoundtrack? {
        val qNorm = normalize(query)
        if (qNorm.isBlank()) return null

        return soundtracks.firstOrNull { ost ->
            normalize(ost.movieTitle) == qNorm ||
                    ost.normalizedAliases.any { normalize(it) == qNorm } ||
                    qNorm.contains(normalize(ost.movieTitle)) ||
                    normalize(ost.movieTitle).contains(qNorm)
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
            trackCount = movie.searchTerms.size.coerceAtLeast(5)
        )
    }
}
