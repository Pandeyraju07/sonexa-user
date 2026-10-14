package com.sonexa.app.data.repository

import android.content.Context
import com.sonexa.app.data.api.RetrofitClient
import com.sonexa.app.data.local.AudiobookStore
import com.sonexa.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudiobookRepository(private val context: Context? = null) {

    private val api = RetrofitClient.audiobookApiService

    init {
        context?.let { AudiobookStore.init(it) }
    }

    suspend fun getAudiobooks(
        category: String? = null,
        language: String? = null,
        search: String? = null,
        featured: Boolean? = null
    ): Result<List<AudiobookDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAudiobooks(
                category = category?.takeIf { it != "All" },
                language = language?.takeIf { it != "All" },
                search = search?.takeIf { it.isNotBlank() },
                featured = featured
            )
            if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {
                val list = response.body()!!.data.map { book ->
                    val clean = sanitizeAudiobook(book)
                    clean.copy(isSaved = AudiobookStore.isSaved(clean.id))
                }
                return@withContext Result.success(list)
            }
        } catch (_: Exception) {}

        // Fallback to local catalog cache / lawful public domain collection
        var fallback = bundledAudiobooks()
        if (!category.isNullOrBlank() && category != "All") {
            val catLower = category.lowercase()
            fallback = fallback.filter {
                it.category.lowercase().contains(catLower) ||
                it.categories.any { c -> c.lowercase().contains(catLower) }
            }
        }
        if (!language.isNullOrBlank() && language != "All") {
            val langLower = language.lowercase()
            fallback = fallback.filter {
                it.languageCode.lowercase() == langLower ||
                it.languageName.lowercase() == langLower
            }
        }
        if (!search.isNullOrBlank()) {
            val q = search.lowercase().trim()
            fallback = fallback.filter {
                it.title.lowercase().contains(q) ||
                it.author.lowercase().contains(q) ||
                it.narrator.lowercase().contains(q) ||
                it.categories.any { c -> c.lowercase().contains(q) }
            }
        }
        if (featured == true) {
            fallback = fallback.filter { it.rating >= 4.9f }
        }

        val enriched = fallback.map { book ->
            book.copy(isSaved = AudiobookStore.isSaved(book.id))
        }
        Result.success(enriched)
    }

    suspend fun getCategories(): Result<List<AudiobookCategoryDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getCategories()
            val list = response.body()?.data
            if (response.isSuccessful && !list.isNullOrEmpty()) {
                return@withContext Result.success(list)
            }
        } catch (_: Exception) {}

        Result.success(bundledCategories())
    }

    suspend fun getLanguages(): Result<List<AudiobookLanguageDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLanguages()
            val list = response.body()?.data
            if (response.isSuccessful && !list.isNullOrEmpty()) {
                return@withContext Result.success(list)
            }
        } catch (_: Exception) {}

        Result.success(bundledLanguages())
    }

    suspend fun getContinueListening(): Result<List<AudiobookDto>> = withContext(Dispatchers.IO) {
        val localProgress = AudiobookStore.progressMap.value
        if (localProgress.isNotEmpty()) {
            val all = bundledAudiobooks()
            val list = localProgress.keys.mapNotNull { id ->
                all.find { it.id == id }
            }
            if (list.isNotEmpty()) {
                return@withContext Result.success(list)
            }
        }

        try {
            val response = api.getContinueListening()
            val list = response.body()?.data
            if (response.isSuccessful && !list.isNullOrEmpty()) {
                return@withContext Result.success(list)
            }
        } catch (_: Exception) {}

        // Fallback default sample continue listening
        val defaults = bundledAudiobooks().take(2)
        Result.success(defaults)
    }

    suspend fun getAudiobookDetail(id: String): Result<AudiobookDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getAudiobookDetail(id)
            if (response.isSuccessful && response.body()?.data != null) {
                val book = sanitizeAudiobook(response.body()!!.data!!)
                return@withContext Result.success(book.copy(isSaved = AudiobookStore.isSaved(book.id)))
            }
        } catch (_: Exception) {}

        val fallback = bundledAudiobooks().find { it.id == id }
            ?: bundledAudiobooks().first()
        Result.success(fallback.copy(isSaved = AudiobookStore.isSaved(fallback.id)))
    }

    suspend fun getChapters(audiobookId: String): Result<List<AudiobookChapterDto>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getChapters(audiobookId)
            val list = response.body()?.data
            if (response.isSuccessful && !list.isNullOrEmpty()) {
                val clean = list.mapIndexed { idx, ch ->
                    if (ch.streamUrl.isBlank() || ch.streamUrl.contains("soundhelix.com", ignoreCase = true)) {
                        ch.copy(streamUrl = resolveSpokenStreamUrl(audiobookId, idx))
                    } else ch
                }
                return@withContext Result.success(clean)
            }
        } catch (_: Exception) {}

        val book = bundledAudiobooks().find { it.id == audiobookId }
            ?: bundledAudiobooks().first()
        Result.success(book.chapters)
    }

    suspend fun saveProgress(
        audiobookId: String,
        chapterId: String,
        chapterNumber: Int,
        chapterTitle: String,
        positionSeconds: Long,
        totalDurationSeconds: Long
    ) = withContext(Dispatchers.IO) {
        AudiobookStore.saveProgress(
            audiobookId = audiobookId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            chapterTitle = chapterTitle,
            positionSeconds = positionSeconds,
            totalDurationSeconds = totalDurationSeconds
        )

        try {
            api.updateProgress(
                id = audiobookId,
                body = mapOf(
                    "currentChapterId" to chapterId,
                    "currentChapterNumber" to chapterNumber,
                    "positionSeconds" to positionSeconds,
                    "totalListenedSeconds" to positionSeconds
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun addBookmark(
        audiobookId: String,
        chapterId: String,
        chapterNumber: Int,
        positionSeconds: Long,
        note: String
    ): AudiobookBookmarkDto = withContext(Dispatchers.IO) {
        val bookmark = AudiobookStore.addBookmark(
            audiobookId = audiobookId,
            chapterId = chapterId,
            chapterNumber = chapterNumber,
            positionSeconds = positionSeconds,
            note = note
        )

        try {
            api.createBookmark(
                id = audiobookId,
                body = mapOf(
                    "chapterId" to chapterId,
                    "chapterNumber" to chapterNumber,
                    "positionSeconds" to positionSeconds,
                    "note" to note
                )
            )
        } catch (_: Exception) {}

        bookmark
    }

    suspend fun removeBookmark(audiobookId: String, bookmarkId: String) = withContext(Dispatchers.IO) {
        AudiobookStore.removeBookmark(audiobookId, bookmarkId)
        try {
            api.deleteBookmark(audiobookId, bookmarkId)
        } catch (_: Exception) {}
    }

    fun toggleSave(audiobookId: String): Boolean {
        return AudiobookStore.toggleSaveAudiobook(audiobookId)
    }

    companion object {
        fun resolveSpokenStreamUrl(audiobookId: String, chapterIndex: Int = 0): String {
            val book = bundledAudiobooks().find { it.id == audiobookId }
            val chapter = book?.chapters?.getOrNull(chapterIndex)
            if (chapter != null && !chapter.streamUrl.contains("soundhelix.com", ignoreCase = true)) {
                return chapter.streamUrl
            }
            return when (audiobookId) {
                "ab_bhagavad_gita" -> {
                    val ch = (chapterIndex + 1).coerceIn(1, 18)
                    "https://archive.org/download/YatharthGeetaHindiAudio/%02dChapter%d.mp3".format(ch + 1, ch)
                }
                "ab_godan", "ab_idgah" -> {
                    when (chapterIndex % 3) {
                        0 -> "https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3"
                        1 -> "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3"
                        else -> "https://archive.org/download/Kayar/pc-kayar-ac.mp3"
                    }
                }
                "ab_kabuliwala" -> "https://archive.org/download/KABULIWALA_201501/KABULIWALA_64kb.mp3"
                "ab_hound_baskervilles" -> {
                    val ch = (chapterIndex + 1).coerceIn(1, 5)
                    "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_%02d_doyle_64kb.mp3".format(ch)
                }
                "ab_art_of_war", "ab_atomic_habits_guide" -> "https://archive.org/download/art_of_war_librivox/art_of_war_01-02_sun_tzu_64kb.mp3"
                "ab_pride_prejudice" -> "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_01-03_austen_64kb.mp3"
                "ab_count_monte_cristo", "ab_psychology_wealth" -> "https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_001_dumas_64kb.mp3"
                else -> "https://archive.org/download/YatharthGeetaHindiAudio/02Chapter1.mp3"
            }
        }

        fun sanitizeAudiobook(book: AudiobookDto): AudiobookDto {
            val cleanChapters = book.chapters.mapIndexed { idx, chapter ->
                val isBrokenUrl = chapter.streamUrl.isBlank() || chapter.streamUrl.contains("soundhelix.com", ignoreCase = true)
                if (isBrokenUrl) {
                    chapter.copy(streamUrl = resolveSpokenStreamUrl(book.id, idx))
                } else {
                    chapter
                }
            }
            return book.copy(chapters = cleanChapters)
        }

        fun bundledCategories(): List<AudiobookCategoryDto> = listOf(
            AudiobookCategoryDto("cat_classics", "Classic Literature", "classics", "Timeless world & Indian masterpieces", "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600", 18),
            AudiobookCategoryDto("cat_hindi_lit", "Hindi & Urdu Literature", "hindi-literature", "Works by Premchand, Manto & Tagore", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600", 12),
            AudiobookCategoryDto("cat_self_growth", "Self-Improvement", "self-improvement", "Habits, mindset & productivity", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600", 24),
            AudiobookCategoryDto("cat_business", "Business & Finance", "business-finance", "Finance, strategy & investing", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600", 15),
            AudiobookCategoryDto("cat_fiction", "Fiction & Drama", "fiction", "Gripping stories and thriller listens", "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600", 30),
            AudiobookCategoryDto("cat_scifi", "Sci-Fi & Fantasy", "scifi-fantasy", "Future worlds & mystical realms", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600", 8),
            AudiobookCategoryDto("cat_spirituality", "Philosophy & Spirituality", "philosophy-spirituality", "Gita, mindfulness & ancient wisdom", "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600", 16),
            AudiobookCategoryDto("cat_short_stories", "Short Stories", "short-stories", "Quick listens under 30 minutes", "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600", 20)
        )

        fun bundledLanguages(): List<AudiobookLanguageDto> = listOf(
            AudiobookLanguageDto("hi", "Hindi", "हिन्दी", 14),
            AudiobookLanguageDto("en", "English", "English", 22),
            AudiobookLanguageDto("pa", "Punjabi", "ਪੰਜਾਬੀ", 6),
            AudiobookLanguageDto("ur", "Urdu", "اردو", 5),
            AudiobookLanguageDto("bn", "Bengali", "বাংলা", 8),
            AudiobookLanguageDto("mr", "Marathi", "मराठी", 6),
            AudiobookLanguageDto("ta", "Tamil", "தமிழ்", 7),
            AudiobookLanguageDto("te", "Telugu", "తెలుగు", 5)
        )

        fun bundledAudiobooks(): List<AudiobookDto> = listOf(
            AudiobookDto(
                id = "ab_godan",
                title = "Godan (गोदान)",
                subtitle = "The Epic Novel of Indian Peasantry",
                author = "Munshi Premchand",
                narrator = "Premchand Audio Archive",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Hindi Literature",
                categories = listOf("Classics", "Hindi Literature", "Fiction", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 28800,
                chapterCount = 6,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.95f,
                reviewsCount = 3840,
                publicationYear = 1936,
                description = "Godan is Munshi Premchand's magnum opus, chronicling the life of Hori Mahato, a poor peasant, and his lifelong dream of owning a cow. Set in rural North India, it remains one of the greatest socio-realist masterworks in Hindi literature.",
                chapters = listOf(
                    AudiobookChapterDto("ab_godan_ch1", "ab_godan", 1, "अध्याय 1: होरी का सपना व आत्माराम", 4800, "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3", "होरी सुबह उठकर अपने खेतों की ओर निकलता है और गाय खरीदने की तमन्ना करता है।"),
                    AudiobookChapterDto("ab_godan_ch2", "ab_godan", 2, "अध्याय 2: भोला से भेंट व गुल्ली डंडा", 4600, "https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3", "होरी की मुलाकात भोला से होती है और गाय के सौदे की बातचीत शुरू होती है।"),
                    AudiobookChapterDto("ab_godan_ch3", "ab_godan", 3, "अध्याय 3: धनिया की चिंता व कायर", 5100, "https://archive.org/download/Kayar/pc-kayar-ac.mp3", "धनिया परिवार के आर्थिक संकट को लेकर होरी को सचेत करती है।"),
                    AudiobookChapterDto("ab_godan_ch4", "ab_godan", 4, "अध्याय 4: गोबर का विद्रोह व अनाथ लड़की", 4900, "https://archive.org/download/AnathLadkiPremchand/Anath%20Ladki%20-%20Premchand.mp3", "गोबर पुरानी रीतियों और सामाजिक कुरीतियों के खिलाफ आवाज उठाता है।"),
                    AudiobookChapterDto("ab_godan_ch5", "ab_godan", 5, "अध्याय 5: ऋण का चक्रव्यूह", 4700, "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram.mp3", "महाजन और साहूकारों के ब्याज के जाल में किसान परिवार का संघर्ष।"),
                    AudiobookChapterDto("ab_godan_ch6", "ab_godan", 6, "अध्याय 6: अंतिम दान व मुक्ति", 4700, "https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda.mp3", "होरी का अटूट स्वाभिमान और उपन्यास का मार्मिक अंत।")
                )
            ),
            AudiobookDto(
                id = "ab_sherlock",
                title = "The Hound of the Baskervilles",
                subtitle = "A Sherlock Holmes Master Mystery",
                author = "Sir Arthur Conan Doyle",
                narrator = "David Clarke (LibriVox Master)",
                languageCode = "en",
                languageName = "English",
                category = "Classics",
                categories = listOf("Classics", "Fiction", "Mystery"),
                coverUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 21600,
                chapterCount = 5,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.88f,
                reviewsCount = 5120,
                publicationYear = 1902,
                description = "Sherlock Holmes and Dr. Watson investigate the legendary curse of the spectral hound haunting the fog-shrouded moors of Devonshire.",
                chapters = listOf(
                    AudiobookChapterDto("ab_sherlock_ch1", "ab_sherlock", 1, "Chapter 1: Mr. Sherlock Holmes", 4200, "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_01_doyle_64kb.mp3", "Holmes and Watson examine a mysterious walking stick left by Dr. Mortimer."),
                    AudiobookChapterDto("ab_sherlock_ch2", "ab_sherlock", 2, "Chapter 2: The Curse of the Baskervilles", 4500, "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_02_doyle_64kb.mp3", "Dr. Mortimer reads the manuscript detailing the demonic hound."),
                    AudiobookChapterDto("ab_sherlock_ch3", "ab_sherlock", 3, "Chapter 3: The Problem on Baker Street", 4300, "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_03_doyle_64kb.mp3", "Sir Henry Baskerville arrives in London with an anonymous warning."),
                    AudiobookChapterDto("ab_sherlock_ch4", "ab_sherlock", 4, "Chapter 4: The Grimpen Mire", 4400, "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_04_doyle_64kb.mp3", "Watson accompanies Sir Henry to Baskerville Hall amidst eerie howling in the mist."),
                    AudiobookChapterDto("ab_sherlock_ch5", "ab_sherlock", 5, "Chapter 5: The Hound Unleashed", 4200, "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_05_doyle_64kb.mp3", "Holmes springs his trap as the glowing beast emerges from the fog.")
                )
            ),
            AudiobookDto(
                id = "ab_idgah",
                title = "Idgah & Other Classic Tales (ईदगाह)",
                subtitle = "Heartfelt Short Stories by Premchand",
                author = "Munshi Premchand",
                narrator = "Anurag Kashyap",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Short Stories",
                categories = listOf("Classics", "Hindi Literature", "Short Stories", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 7200,
                chapterCount = 3,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.96f,
                reviewsCount = 2980,
                publicationYear = 1933,
                description = "The heartwarming story of four-year-old orphan Hamid who visits the Eid fair with only three paise and buys a chimta (tongs) for his grandmother Amina.",
                chapters = listOf(
                    AudiobookChapterDto("ab_idgah_ch1", "ab_idgah", 1, "कहानी 1: ईदगाह (Idgah)", 2400, "https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3", "हामिद का मेला जाना और दादी अमीना के लिए चिमटा खरीदना।"),
                    AudiobookChapterDto("ab_idgah_ch2", "ab_idgah", 2, "कहानी 2: नमक का दरोगा", 2600, "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3", "मुंशी वंशीधर की कर्तव्यनिष्ठा और पंडित अलोपीदीन का धन-बल।"),
                    AudiobookChapterDto("ab_idgah_ch3", "ab_idgah", 3, "कहानी 3: पंच परमेश्वर", 2200, "https://archive.org/download/Kayar/pc-kayar-ac.mp3", "जुम्मन शेख और अलगू चौधरी की मित्रता और न्याय की निष्पक्षता।")
                )
            ),
            AudiobookDto(
                id = "ab_pride_prejudice",
                title = "Pride and Prejudice",
                subtitle = "The Classic Romance of Elizabeth Bennet & Mr. Darcy",
                author = "Jane Austen",
                narrator = "Annie Coleman (LibriVox Master)",
                languageCode = "en",
                languageName = "English",
                category = "Classics",
                categories = listOf("Classics", "Fiction", "Romance"),
                coverUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 25200,
                chapterCount = 4,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.89f,
                reviewsCount = 6420,
                publicationYear = 1813,
                description = "Jane Austen's sparkling romantic comedy of manners following the independent Elizabeth Bennet and the proud Fitzwilliam Darcy.",
                chapters = listOf(
                    AudiobookChapterDto("ab_pp_ch1", "ab_pride_prejudice", 1, "Chapters 1-3: Netherfield Park is Let", 6200, "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_01-03_austen_64kb.mp3", "Mrs. Bennet urges Mr. Bennet to visit Charles Bingley."),
                    AudiobookChapterDto("ab_pp_ch2", "ab_pride_prejudice", 2, "Chapters 4-5: The Meryton Assembly Ball", 6400, "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_04-05_austen_64kb.mp3", "Elizabeth first observes Mr. Darcy's proud refusal to dance."),
                    AudiobookChapterDto("ab_pp_ch3", "ab_pride_prejudice", 3, "Chapter 6: Observations at Lucas Lodge", 6300, "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_06_austen_64kb.mp3", "Darcy begins to recognize Elizabeth's intelligence and charm."),
                    AudiobookChapterDto("ab_pp_ch4", "ab_pride_prejudice", 4, "Chapter 7: Jane at Netherfield", 6300, "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_07_austen_64kb.mp3", "Elizabeth travels through the rain to be at her sister's side.")
                )
            ),
            AudiobookDto(
                id = "ab_atomic_habits_guide",
                title = "The Art of War (रणनीति व आत्मसंयम)",
                subtitle = "Timeless Strategy, Mindset & Daily Execution",
                author = "Sun Tzu (अनुवाद: लाल बिहारी)",
                narrator = "Moira Fogarty (LibriVox Master)",
                languageCode = "en",
                languageName = "English",
                category = "Self-Improvement",
                categories = listOf("Self-Improvement", "Business & Finance", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 18000,
                chapterCount = 4,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.93f,
                reviewsCount = 4180,
                publicationYear = 2024,
                description = "Master Sun's 2,500-year-old principles on discipline, positioning, knowing oneself and the adversary, and achieving effortless victory in daily life and enterprise.",
                chapters = listOf(
                    AudiobookChapterDto("ab_af_ch1", "ab_atomic_habits_guide", 1, "Part 1: Laying Plans & Waging War", 4500, "https://archive.org/download/art_of_war_librivox/art_of_war_01-02_sun_tzu_64kb.mp3", "Five fundamental factors of success and the calculus of preparation."),
                    AudiobookChapterDto("ab_af_ch2", "ab_atomic_habits_guide", 2, "Part 2: Attack by Stratagem & Tactical Dispositions", 4600, "https://archive.org/download/art_of_war_librivox/art_of_war_03-04_sun_tzu_64kb.mp3", "Supreme excellence consists in breaking the enemy's resistance without fighting."),
                    AudiobookChapterDto("ab_af_ch3", "ab_atomic_habits_guide", 3, "Part 3: Energy & Weak Points and Strong", 4400, "https://archive.org/download/art_of_war_librivox/art_of_war_05-06_sun_tzu_64kb.mp3", "Momentum, adaptability like water, and seizing initiative."),
                    AudiobookChapterDto("ab_af_ch4", "ab_atomic_habits_guide", 4, "Part 4: Maneuvering & Variation in Tactics", 4500, "https://archive.org/download/art_of_war_librivox/art_of_war_07-08_sun_tzu_64kb.mp3", "Turning the devious into the direct and misfortune into gain.")
                )
            ),
            AudiobookDto(
                id = "ab_psychology_wealth",
                title = "The Count of Monte Cristo (धन, धैर्य व न्याय)",
                subtitle = "Dumas's Masterpiece on Fortune, Patience & Triumph",
                author = "Alexandre Dumas",
                narrator = "John Van Stan (LibriVox Master)",
                languageCode = "en",
                languageName = "English",
                category = "Business & Finance",
                categories = listOf("Business & Finance", "Self-Improvement", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 14400,
                chapterCount = 3,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.87f,
                reviewsCount = 2740,
                publicationYear = 2025,
                description = "The ultimate epic of Edmond Dantès, who overcomes betrayal and imprisonment to discover the legendary treasure of Monte Cristo and master the twin forces of wisdom: Wait and Hope.",
                chapters = listOf(
                    AudiobookChapterDto("ab_pw_ch1", "ab_psychology_wealth", 1, "Chapter 1: Marseilles — The Arrival", 4800, "https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_001_dumas_64kb.mp3", "The Pharaon arrives in port carrying Edmond Dantès and fateful letters."),
                    AudiobookChapterDto("ab_pw_ch2", "ab_psychology_wealth", 2, "Chapter 2: Father and Son", 4800, "https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_002_dumas_64kb.mp3", "Dantès returns home to embrace his impoverished father in love and gratitude."),
                    AudiobookChapterDto("ab_pw_ch3", "ab_psychology_wealth", 3, "Chapter 3: The Catalans & Fernand", 4800, "https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_003_dumas_64kb.mp3", "The jealousy that ignites the betrayal and sets destiny in motion.")
                )
            ),
            AudiobookDto(
                id = "ab_kabuliwala",
                title = "Kabuliwala & Gitanjali (काबुलीवाला)",
                subtitle = "Stories & Poetry of Bengal",
                author = "Rabindranath Tagore",
                narrator = "Tagore Audio Archive",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Classics",
                categories = listOf("Classics", "Hindi Literature", "Short Stories", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 9000,
                chapterCount = 3,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.92f,
                reviewsCount = 3120,
                publicationYear = 1892,
                description = "Rabindranath Tagore's touching tale of friendship between little Mini and a dry-fruit seller from Kabul, capturing universal fatherly affection.",
                chapters = listOf(
                    AudiobookChapterDto("ab_kab_ch1", "ab_kabuliwala", 1, "अध्याय 1: काबुलीवाला (Kabuliwala Complete)", 3000, "https://archive.org/download/KABULIWALA_201501/KABULIWALA.mp3", "कोलकाता में पांच वर्षीय मिनी और रहमत काबुलीवाला की अमर दास्तान।"),
                    AudiobookChapterDto("ab_kab_ch2", "ab_kabuliwala", 2, "अध्याय 2: आत्माराम (Atmaram)", 3000, "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3", "प्रेमचंद की मार्मिक कहानी आत्माराम का सम्पूर्ण पाठ।"),
                    AudiobookChapterDto("ab_kab_ch3", "ab_kabuliwala", 3, "अध्याय 3: कायर (Kayar)", 3000, "https://archive.org/download/Kayar/pc-kayar-ac.mp3", "मानवीय संवेदनाओं और आत्मबोध की श्रेष्ठ कहानी।")
                )
            ),
            AudiobookDto(
                id = "ab_bhagavad_gita",
                title = "Sampoorna Shrimad Bhagavad Gita (सम्पूर्ण श्रीमद्भगवद्गीता)",
                subtitle = "All 18 Adhyayas with Hindi Meaning & Shlokas",
                author = "Maharishi Ved Vyasa",
                narrator = "Shailendra Bharti / Swami Adgadanandji (यथार्थ गीता)",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Philosophy & Spirituality",
                categories = listOf("Philosophy & Spirituality", "Classics", "Hindi & Urdu Literature", "Most Listened"),
                coverUrl = "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 64800,
                chapterCount = 18,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.99f,
                reviewsCount = 14850,
                publicationYear = -500,
                description = "पवित्र श्रीमद्भगवद्गीता के सभी १८ अध्यायों का सम्पूर्ण पाठ व सरल हिन्दी व्याख्या, सुप्रसिद्ध गायक एवं उद्घोषक शैलेंद्र भारती (Shailendra Bharti) व यथार्थ गीता के दिव्य स्वर में। कुरुक्षेत्र के युद्धस्थल पर भगवान श्रीकृष्ण द्वारा अर्जुन को दिया गया निष्काम कर्म, ज्ञान और भक्तियोग का सार्वभौमिक अमर उपदेश।",
                chapters = listOf(
                    AudiobookChapterDto("ab_bg_ch1", "ab_bhagavad_gita", 1, "अध्याय 1: अर्जुनविषादयोग (Arjuna's Dilemma)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/02Chapter1.mp3", "कुरुक्षेत्र की रणभूमि में दोनों सेनाओं का अवलोकन और अपनों के प्रति मोह व विषाद।"),
                    AudiobookChapterDto("ab_bg_ch2", "ab_bhagavad_gita", 2, "अध्याय 2: सांख्ययोग (The Eternal Soul)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/03Chapter2.mp3", "आत्मा की अमरता, क्षत्रिय धर्म और स्थितप्रज्ञ पुरुष के लक्षण।"),
                    AudiobookChapterDto("ab_bg_ch3", "ab_bhagavad_gita", 3, "अध्याय 3: कर्मयोग (The Path of Action)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/04Chapter3.mp3", "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन — फल की आसक्ति रहित निष्काम कर्म।"),
                    AudiobookChapterDto("ab_bg_ch4", "ab_bhagavad_gita", 4, "अध्याय 4: ज्ञानकर्मसंन्यासयोग (Wisdom in Action)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/05Chapter4.mp3", "भगवान के दिव्य अवतार का रहस्य, ज्ञान-यज्ञ और कर्म की पवित्रता।"),
                    AudiobookChapterDto("ab_bg_ch5", "ab_bhagavad_gita", 5, "अध्याय 5: कर्मसंन्यासयोग (Yoga of Renunciation)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/06Chapter5.mp3", "संन्यास और कर्मयोग दोनों का समन्वय तथा परम शांति का उपाय।"),
                    AudiobookChapterDto("ab_bg_ch6", "ab_bhagavad_gita", 6, "अध्याय 6: आत्मसंयमयोग / ध्यानयोग (Meditation)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/07Chapter6.mp3", "चंचल मन पर विजय पाने की विधि, आसन, प्राणायाम और समत्व दृष्टि।"),
                    AudiobookChapterDto("ab_bg_ch7", "ab_bhagavad_gita", 7, "अध्याय 7: ज्ञानविज्ञानयोग (Knowledge of Reality)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/08Chapter7.mp3", "भगवान की अपरा और परा प्रकृति तथा चार प्रकार के भक्तों का वर्णन।"),
                    AudiobookChapterDto("ab_bg_ch8", "ab_bhagavad_gita", 8, "अध्याय 8: अक्षरब्रह्मयोग (The Imperishable Absolute)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/09Chapter8.mp3", "अन्तकाल में भगवत्स्मरण, ॐ का जप और शाश्वत मोक्ष की प्राप्ति।"),
                    AudiobookChapterDto("ab_bg_ch9", "ab_bhagavad_gita", 9, "अध्याय 9: राजविद्याराजगुह्ययोग (Sovereign Secret)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/10Chapter9.mp3", "परम गोपनीय दिव्य ज्ञान और भगवान के प्रति अनन्य प्रेम का फल।"),
                    AudiobookChapterDto("ab_bg_ch10", "ab_bhagavad_gita", 10, "अध्याय 10: विभूतियोग (Divine Glories & Splendors)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/11Chapter10.mp3", "सृष्टि के समस्त श्रेष्ठ रूपों में भगवान की सर्वव्यापी उपस्थिति का दिग्दर्शन।"),
                    AudiobookChapterDto("ab_bg_ch11", "ab_bhagavad_gita", 11, "अध्याय 11: विश्वरूपदर्शनयोग (Vision of Universal Form)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/12Chapter11.mp3", "अर्जुन को भगवान श्रीकृष्ण के विराट विश्वरूप का साक्षात दर्शन।"),
                    AudiobookChapterDto("ab_bg_ch12", "ab_bhagavad_gita", 12, "अध्याय 12: भक्तियोग (Path of Divine Devotion)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/13Chapter12.mp3", "सगुण भक्ति का सुगम मार्ग और प्रभु के प्रिय भक्त के श्रेष्ठ गुण।"),
                    AudiobookChapterDto("ab_bg_ch13", "ab_bhagavad_gita", 13, "अध्याय 13: क्षेत्रक्षेत्रज्ञविभागयोग (The Knower & Field)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/14Chapter13.mp3", "भौतिक शरीर (क्षेत्र) और अविनाशी आत्मा (क्षेत्रज्ञ) का यथार्थ विवेक।"),
                    AudiobookChapterDto("ab_bg_ch14", "ab_bhagavad_gita", 14, "अध्याय 14: गुणत्रयविभागयोग (Three Gunas of Nature)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/15Chapter14.mp3", "सत्त्व, रज और तम तीनों गुणों का प्रभाव और गुणातीत होने का मार्ग।"),
                    AudiobookChapterDto("ab_bg_ch15", "ab_bhagavad_gita", 15, "अध्याय 15: पुरुषोत्तमयोग (Supreme Divine Being)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/16Chapter15.mp3", "उर्ध्वमूल संसार-वृक्ष का रहस्य और क्षर-अक्षर से परे परम पुरुषोत्तम।"),
                    AudiobookChapterDto("ab_bg_ch16", "ab_bhagavad_gita", 16, "अध्याय 16: दैवासुरसम्पद्विभागयोग (Divine & Demonic)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/17Chapter16.mp3", "कल्याणकारी दैवी संपदा और पतनकारी आसुरी प्रवृत्तियों का सूक्ष्म भेद।"),
                    AudiobookChapterDto("ab_bg_ch17", "ab_bhagavad_gita", 17, "अध्याय 17: श्रद्धात्रयविभागयोग (Threefold Faith)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/18Chapter17.mp3", "सात्त्विक, राजसिक व तामसिक श्रद्धा, आहार, यज्ञ और ॐ तत्सत् का महत्व।"),
                    AudiobookChapterDto("ab_bg_ch18", "ab_bhagavad_gita", 18, "अध्याय 18: मोक्षसंन्यासयोग (Liberation & Surrender)", 3600, "https://archive.org/download/YatharthGeetaHindiAudio/19Chapter18.mp3", "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज — सम्पूर्ण गीता का सार व परम मोक्ष।")
                )
            )
        )
    }
}
