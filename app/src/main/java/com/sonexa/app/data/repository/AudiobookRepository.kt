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
                    book.copy(isSaved = AudiobookStore.isSaved(book.id))
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
                val book = response.body()!!.data!!
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
                return@withContext Result.success(list)
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
        fun bundledCategories(): List<AudiobookCategoryDto> = listOf(
            AudiobookCategoryDto("cat_classics", "Classic Literature", "classics", "Timeless world & Indian masterpieces", "https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600", 18),
            AudiobookCategoryDto("cat_hindi_lit", "Hindi Literature", "hindi-literature", "Works by Premchand, Dinkar & Tagore", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600", 12),
            AudiobookCategoryDto("cat_self_growth", "Self-Improvement", "self-improvement", "Habits, mindset & productivity", "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600", 24),
            AudiobookCategoryDto("cat_business", "Business & Wealth", "business-finance", "Finance, strategy & investing", "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600", 15),
            AudiobookCategoryDto("cat_fiction", "Fiction & Mystery", "fiction", "Gripping stories and thriller listens", "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600", 30),
            AudiobookCategoryDto("cat_spirituality", "Spirituality", "spirituality", "Gita, mindfulness & ancient wisdom", "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600", 14),
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
                narrator = "Sameer Goswami",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Hindi Literature",
                categories = listOf("Classics", "Hindi Literature", "Fiction"),
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
                    AudiobookChapterDto("ab_godan_ch1", "ab_godan", 1, "अध्याय 1: होरी का सपना (Hori's Dream)", 4800, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "होरी सुबह उठकर अपने खेतों की ओर निकलता है और गाय खरीदने की तमन्ना करता है।"),
                    AudiobookChapterDto("ab_godan_ch2", "ab_godan", 2, "अध्याय 2: भोला से भेंट (Meeting Bhola)", 4600, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "होरी की मुलाकात भोला से होती है और गाय के सौदे की बातचीत शुरू होती है।"),
                    AudiobookChapterDto("ab_godan_ch3", "ab_godan", 3, "अध्याय 3: धनिया की चिंता (Dhaniya's Concern)", 5100, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "धनिया परिवार के आर्थिक संकट को लेकर होरी को सचेत करती है।"),
                    AudiobookChapterDto("ab_godan_ch4", "ab_godan", 4, "अध्याय 4: गोबर का विद्रोह (Gobar's Rebellion)", 4900, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "गोबर पुरानी रीतियों और सामाजिक कुरीतियों के खिलाफ आवाज उठाता है।"),
                    AudiobookChapterDto("ab_godan_ch5", "ab_godan", 5, "अध्याय 5: ऋण का चक्रव्यूह (Web of Debt)", 4700, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "महाजन और साहूकारों के ब्याज के जाल में किसान परिवार का संघर्ष।"),
                    AudiobookChapterDto("ab_godan_ch6", "ab_godan", 6, "अध्याय 6: अंतिम दान (The Final Offering)", 4700, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "होरी का अटूट स्वाभिमान और उपन्यास का मार्मिक अंत।")
                )
            ),
            AudiobookDto(
                id = "ab_sherlock",
                title = "The Hound of the Baskervilles",
                subtitle = "A Sherlock Holmes Master Mystery",
                author = "Sir Arthur Conan Doyle",
                narrator = "David Clarke (LibriVox)",
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
                    AudiobookChapterDto("ab_sherlock_ch1", "ab_sherlock", 1, "Chapter 1: Mr. Sherlock Holmes", 4200, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "Holmes and Watson examine a mysterious walking stick left by Dr. Mortimer."),
                    AudiobookChapterDto("ab_sherlock_ch2", "ab_sherlock", 2, "Chapter 2: The Curse of the Baskervilles", 4500, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "Dr. Mortimer reads the manuscript detailing the demonic hound."),
                    AudiobookChapterDto("ab_sherlock_ch3", "ab_sherlock", 3, "Chapter 3: The Problem on Baker Street", 4300, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "Sir Henry Baskerville arrives in London with an anonymous warning."),
                    AudiobookChapterDto("ab_sherlock_ch4", "ab_sherlock", 4, "Chapter 4: The Grimpen Mire", 4400, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3", "Watson accompanies Sir Henry to Baskerville Hall amidst eerie howling in the mist."),
                    AudiobookChapterDto("ab_sherlock_ch5", "ab_sherlock", 5, "Chapter 5: The Hound Unleashed", 4200, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3", "Holmes springs his trap as the glowing beast emerges from the fog.")
                )
            ),
            AudiobookDto(
                id = "ab_idgah",
                title = "Idgah & Other Classic Tales (ईदगाह)",
                subtitle = "Heartfelt Short Stories by Premchand",
                author = "Munshi Premchand",
                narrator = "Anurag Kashyap V/O",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Short Stories",
                categories = listOf("Classics", "Hindi Literature", "Short Stories"),
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
                    AudiobookChapterDto("ab_idgah_ch1", "ab_idgah", 1, "कहानी 1: ईदगाह (Idgah - Chimta of Love)", 2400, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3", "हामिद का मेला जाना और दादी अमीना के लिए चिमटा खरीदना।"),
                    AudiobookChapterDto("ab_idgah_ch2", "ab_idgah", 2, "कहानी 2: नमक का दारोगा (Namak Ka Daroga)", 2600, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3", "ईमानदार दारोगा वंशीधर और भ्रष्ट धनवान पंडित अलोपीदीन की दास्तान।"),
                    AudiobookChapterDto("ab_idgah_ch3", "ab_idgah", 3, "कहानी 3: पंच परमेश्वर (Panch Parmeshwar)", 2200, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3", "जुम्मन शेख और अलगू चौधरी की न्यायगाथा।")
                )
            ),
            AudiobookDto(
                id = "ab_pride_prejudice",
                title = "Pride and Prejudice",
                subtitle = "The Classic Romance of Elizabeth Bennet & Mr. Darcy",
                author = "Jane Austen",
                narrator = "Karen Savage (LibriVox)",
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
                    AudiobookChapterDto("ab_pp_ch1", "ab_pride_prejudice", 1, "Chapter 1: Netherfield Park is Let", 6200, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3", "Mrs. Bennet urges Mr. Bennet to visit Charles Bingley."),
                    AudiobookChapterDto("ab_pp_ch2", "ab_pride_prejudice", 2, "Chapter 2: The Meryton Assembly Ball", 6400, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3", "Elizabeth first observes Mr. Darcy's proud refusal to dance."),
                    AudiobookChapterDto("ab_pp_ch3", "ab_pride_prejudice", 3, "Chapter 3: The Proposal at Hunsford", 6300, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", "Darcy professes his love against his better judgment."),
                    AudiobookChapterDto("ab_pp_ch4", "ab_pride_prejudice", 4, "Chapter 4: Pemberley & Reconciliation", 6300, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", "Elizabeth tours Pemberley and realizes Darcy's nobility.")
                )
            ),
            AudiobookDto(
                id = "ab_atomic_habits_guide",
                title = "Atomic Focus & Daily Mastery",
                subtitle = "Systems for Small Changes and Monumental Results",
                author = "Vikram Sengupta",
                narrator = "Arjun Mathur",
                languageCode = "en",
                languageName = "English",
                category = "Self-Improvement",
                categories = listOf("Self-Improvement", "Business & Finance"),
                coverUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 18000,
                chapterCount = 4,
                isClassic = false,
                isPublicDomain = false,
                rating = 4.93f,
                reviewsCount = 4180,
                publicationYear = 2024,
                description = "A practical audio handbook on designing daily habit loops, breaking procrastination cycles, and compounding 1% gains in health, finance, and career.",
                chapters = listOf(
                    AudiobookChapterDto("ab_af_ch1", "ab_atomic_habits_guide", 1, "Chapter 1: The Mathematics of 1% Compounding", 4500, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3", "Why systems trump willpower and how identity habits endure."),
                    AudiobookChapterDto("ab_af_ch2", "ab_atomic_habits_guide", 2, "Chapter 2: The Four Laws of Behavioral Design", 4600, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3", "Make it obvious, attractive, easy, and satisfying."),
                    AudiobookChapterDto("ab_af_ch3", "ab_atomic_habits_guide", 3, "Chapter 3: Environment Priming & Habit Stacking", 4400, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3", "How to structure your workspace to eliminate friction."),
                    AudiobookChapterDto("ab_af_ch4", "ab_atomic_habits_guide", 4, "Chapter 4: The Plateau of Latent Potential", 4500, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", "Navigating the valley of disappointment before breakthrough.")
                )
            ),
            AudiobookDto(
                id = "ab_kabuliwala",
                title = "Kabuliwala & Gitanjali (काबुलीवाला)",
                subtitle = "Stories & Poetry of Bengal",
                author = "Rabindranath Tagore",
                narrator = "Soumitra Roy & Devashish",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Classics",
                categories = listOf("Classics", "Hindi Literature", "Short Stories"),
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
                    AudiobookChapterDto("ab_kab_ch1", "ab_kabuliwala", 1, "अध्याय 1: मिनी और काबुलीवाला (Mini and Rahamat)", 3000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", "कोलकाता में पांच वर्षीय मिनी और रहमत की अनोखी दोस्ती।"),
                    AudiobookChapterDto("ab_kab_ch2", "ab_kabuliwala", 2, "अध्याय 2: कारावास और वियोग (Separation)", 3000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3", "रहमत का जेल जाना और मिनी का बड़ा होना।"),
                    AudiobookChapterDto("ab_kab_ch3", "ab_kabuliwala", 3, "अध्याय 3: विदाई (The Wedding Day)", 3000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3", "मिनी की शादी का दिन और रहमत की अपने वतन वापसी।")
                )
            ),
            AudiobookDto(
                id = "ab_bhagavad_gita",
                title = "Bhagavad Gita — The Path of Purpose (श्रीमद्भगवद्गीता)",
                subtitle = "Timeless Wisdom for Modern Living",
                author = "Maharishi Ved Vyasa",
                narrator = "Dr. Radhakrishnan Shankaran",
                languageCode = "hi",
                languageName = "Hindi",
                category = "Philosophy & Spirituality",
                categories = listOf("Classics", "Philosophy & Spirituality"),
                coverUrl = "https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&auto=format&fit=crop&q=80",
                totalDurationSec = 18000,
                chapterCount = 3,
                isClassic = true,
                isPublicDomain = true,
                rating = 4.99f,
                reviewsCount = 8940,
                publicationYear = -500,
                description = "The sublime philosophical dialogue between Prince Arjuna and Lord Krishna on the battlefield of Kurukshetra, guiding duty and peace of mind.",
                chapters = listOf(
                    AudiobookChapterDto("ab_bg_ch1", "ab_bhagavad_gita", 1, "अध्याय 1 व 2: सांख्ययोग (Arjuna's Dilemma & Wisdom)", 6000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3", "कर्तव्य और मोह का अंतर्द्वंद तथा आत्मा की अमरता का उपदेश।"),
                    AudiobookChapterDto("ab_bg_ch2", "ab_bhagavad_gita", 2, "अध्याय 3: कर्मयोग (The Yoga of Action)", 6000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3", "निष्काम कर्म की शक्ति और समत्व भाव।"),
                    AudiobookChapterDto("ab_bg_ch3", "ab_bhagavad_gita", 3, "अध्याय 6: ध्यानयोग (Meditation & Inner Peace)", 6000, "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3", "चंचल मन को एकाग्र करने और शांति पाने की पद्धति।")
                )
            )
        )
    }
}
