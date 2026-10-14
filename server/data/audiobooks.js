/**
 * Zynera Audiobooks Catalog & Public-Domain / Licensed Audio Repository
 * Provides metadata, chapters, lawful audio streams, and multilingual classifications.
 */

const audiobookCategories = [
  { id: 'cat_classics', name: 'Classic Literature', slug: 'classics', description: 'Timeless masterpieces from world and Indian literature', imageUrl: 'https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600', bookCount: 18 },
  { id: 'cat_hindi_lit', name: 'Hindi & Urdu Literature', slug: 'hindi-literature', description: 'Legendary stories by Premchand, Manto, and Dinkar', imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600', bookCount: 12 },
  { id: 'cat_self_growth', name: 'Self-Improvement', slug: 'self-improvement', description: 'Mindset, habits, productivity, and emotional intelligence', imageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600', bookCount: 24 },
  { id: 'cat_business', name: 'Business & Finance', slug: 'business-finance', description: 'Wealth creation, entrepreneurship, and strategy', imageUrl: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600', bookCount: 15 },
  { id: 'cat_fiction', name: 'Fiction & Drama', slug: 'fiction', description: 'Captivating stories, mysteries, and human drama', imageUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600', bookCount: 30 },
  { id: 'cat_scifi', name: 'Sci-Fi & Fantasy', slug: 'scifi-fantasy', description: 'Future worlds, cybernetics, and mythical realms', imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600', bookCount: 8 },
  { id: 'cat_spirituality', name: 'Philosophy & Spirituality', slug: 'philosophy-spirituality', description: 'Ancient wisdom, mindfulness, and inner peace', imageUrl: 'https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600', bookCount: 16 },
  { id: 'cat_short_stories', name: 'Short Stories', slug: 'short-stories', description: 'Bite-sized listens under 30 minutes', imageUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600', bookCount: 20 }
];

const supportedAudiobookLanguages = [
  { code: 'hi', name: 'Hindi', nativeName: 'हिन्दी', bookCount: 12 },
  { code: 'en', name: 'English', nativeName: 'English', bookCount: 16 },
  { code: 'pa', name: 'Punjabi', nativeName: 'ਪੰਜਾਬੀ', bookCount: 5 },
  { code: 'ur', name: 'Urdu', nativeName: 'اردو', bookCount: 4 },
  { code: 'bn', name: 'Bengali', nativeName: 'বাংলা', bookCount: 6 },
  { code: 'mr', name: 'Marathi', nativeName: 'मराठी', bookCount: 4 },
  { code: 'ta', name: 'Tamil', nativeName: 'தமிழ்', bookCount: 5 },
  { code: 'te', name: 'Telugu', nativeName: 'తెలుగు', bookCount: 4 }
];

const audiobooks = [
  {
    id: 'ab_godan',
    title: 'Godan (गोदान)',
    subtitle: 'The Epic Novel of Indian Peasantry',
    author: 'Munshi Premchand',
    narrator: 'Premchand Audio Archive',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Hindi Literature',
    categories: ['Classics', 'Hindi Literature', 'Fiction', 'Most Listened'],
    coverUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 28800, // 8 hours
    chapterCount: 6,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.9,
    reviewsCount: 3840,
    publicationYear: 1936,
    description: 'Godan is Munshi Premchand\'s magnum opus, chronicling the life of Hori Mahato, a poor peasant, and his lifelong dream of owning a cow. Set in rural North India, it remains one of the greatest socio-realist masterworks in Hindi literature.',
    chapters: [
      {
        id: 'ab_godan_ch1',
        audiobookId: 'ab_godan',
        chapterNumber: 1,
        title: 'अध्याय 1: होरी का सपना व आत्माराम',
        durationSec: 4800,
        streamUrl: 'https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3',
        summary: 'होरी सुबह उठकर अपने खेतों की ओर निकलता है और गाय खरीदने की अपनी दिली तमन्ना पर विचार करता है।'
      },
      {
        id: 'ab_godan_ch2',
        audiobookId: 'ab_godan',
        chapterNumber: 2,
        title: 'अध्याय 2: भोला से भेंट व गुल्ली डंडा',
        durationSec: 4600,
        streamUrl: 'https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3',
        summary: 'होरी की मुलाकात भोला से होती है और गाय के सौदे की बातचीत शुरू होती है।'
      },
      {
        id: 'ab_godan_ch3',
        audiobookId: 'ab_godan',
        chapterNumber: 3,
        title: 'अध्याय 3: धनिया की चिंता व कायर',
        durationSec: 5100,
        streamUrl: 'https://archive.org/download/Kayar/pc-kayar-ac.mp3',
        summary: 'धनिया परिवार के आर्थिक संकट को लेकर होरी को सचेत करती है।'
      },
      {
        id: 'ab_godan_ch4',
        audiobookId: 'ab_godan',
        chapterNumber: 4,
        title: 'अध्याय 4: गोबर का विद्रोह व अनाथ लड़की',
        durationSec: 4900,
        streamUrl: 'https://archive.org/download/AnathLadkiPremchand/Anath%20Ladki%20-%20Premchand.mp3',
        summary: 'गोबर पुरानी रीतियों और सामाजिक कुरीतियों के खिलाफ आवाज उठाता है।'
      },
      {
        id: 'ab_godan_ch5',
        audiobookId: 'ab_godan',
        chapterNumber: 5,
        title: 'अध्याय 5: ऋण का चक्रव्यूह',
        durationSec: 4700,
        streamUrl: 'https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram.mp3',
        summary: 'महाजन और साहूकारों के ब्याज के जाल में किसान परिवार का संघर्ष।'
      },
      {
        id: 'ab_godan_ch6',
        audiobookId: 'ab_godan',
        chapterNumber: 6,
        title: 'अध्याय 6: अंतिम दान व मुक्ति',
        durationSec: 4700,
        streamUrl: 'https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda.mp3',
        summary: 'होरी का अटूट स्वाभिमान और उपन्यास का मार्मिक अंत।'
      }
    ]
  },
  {
    id: 'ab_sherlock',
    title: 'The Hound of the Baskervilles',
    subtitle: 'A Sherlock Holmes Master Mystery',
    author: 'Sir Arthur Conan Doyle',
    narrator: 'David Clarke (LibriVox Master)',
    languageCode: 'en',
    languageName: 'English',
    category: 'Classics',
    categories: ['Classics', 'Mystery', 'Fiction'],
    coverUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 21600, // 6 hours
    chapterCount: 5,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.85,
    reviewsCount: 5120,
    publicationYear: 1902,
    description: 'Sherlock Holmes and Dr. Watson investigate the legendary curse of the spectral hound haunting the fog-shrouded moors of Devonshire.',
    chapters: [
      {
        id: 'ab_sherlock_ch1',
        audiobookId: 'ab_sherlock',
        chapterNumber: 1,
        title: 'Chapter 1: Mr. Sherlock Holmes',
        durationSec: 4200,
        streamUrl: 'https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_01_doyle_64kb.mp3',
        summary: 'Holmes and Watson examine a mysterious walking stick left behind by Dr. James Mortimer.'
      },
      {
        id: 'ab_sherlock_ch2',
        audiobookId: 'ab_sherlock',
        chapterNumber: 2,
        title: 'Chapter 2: The Curse of the Baskervilles',
        durationSec: 4500,
        streamUrl: 'https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_02_doyle_64kb.mp3',
        summary: 'Dr. Mortimer reads the ancient manuscript detailing the demonic hound that killed Sir Hugo Baskerville.'
      },
      {
        id: 'ab_sherlock_ch3',
        audiobookId: 'ab_sherlock',
        chapterNumber: 3,
        title: 'Chapter 3: The Problem on Baker Street',
        durationSec: 4300,
        streamUrl: 'https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_03_doyle_64kb.mp3',
        summary: 'Sir Henry Baskerville arrives in London and receives a threatening anonymous warning.'
      },
      {
        id: 'ab_sherlock_ch4',
        audiobookId: 'ab_sherlock',
        chapterNumber: 4,
        title: 'Chapter 4: The Grimpen Mire',
        durationSec: 4400,
        streamUrl: 'https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_04_doyle_64kb.mp3',
        summary: 'Watson accompanies Sir Henry to Baskerville Hall amidst eerie howling in the mist.'
      },
      {
        id: 'ab_sherlock_ch5',
        audiobookId: 'ab_sherlock',
        chapterNumber: 5,
        title: 'Chapter 5: The Hound Unleashed',
        durationSec: 4200,
        streamUrl: 'https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_05_doyle_64kb.mp3',
        summary: 'Holmes springs his trap as the glowing beast emerges from the fog.'
      }
    ]
  },
  {
    id: 'ab_idgah',
    title: 'Idgah & Other Classic Tales (ईदगाह)',
    subtitle: 'Heartfelt Short Stories by Premchand',
    author: 'Munshi Premchand',
    narrator: 'Anurag Kashyap',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Short Stories',
    categories: ['Classics', 'Hindi Literature', 'Short Stories'],
    coverUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 7200, // 2 hours
    chapterCount: 3,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.95,
    reviewsCount: 2980,
    publicationYear: 1933,
    description: 'The heartwarming story of four-year-old orphan Hamid who visits the Eid fair with only three paise and buys a chimta (tongs) for his grandmother Amina so her fingers won\'t burn while baking rotis.',
    chapters: [
      {
        id: 'ab_idgah_ch1',
        audiobookId: 'ab_idgah',
        chapterNumber: 1,
        title: 'कहानी 1: ईदगाह (Idgah)',
        durationSec: 2400,
        streamUrl: 'https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3',
        summary: 'हामिद का मेला जाना और दादी अमीना के लिए चिमटा खरीदना।'
      },
      {
        id: 'ab_idgah_ch2',
        audiobookId: 'ab_idgah',
        chapterNumber: 2,
        title: 'कहानी 2: नमक का दरोगा',
        durationSec: 2600,
        streamUrl: 'https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3',
        summary: 'मुंशी वंशीधर की कर्तव्यनिष्ठा और पंडित अलोपीदीन का धन-बल।'
      },
      {
        id: 'ab_idgah_ch3',
        audiobookId: 'ab_idgah',
        chapterNumber: 3,
        title: 'कहानी 3: पंच परमेश्वर',
        durationSec: 2200,
        streamUrl: 'https://archive.org/download/Kayar/pc-kayar-ac.mp3',
        summary: 'जुम्मन शेख और अलगू चौधरी की मित्रता और न्याय की निष्पक्षता।'
      }
    ]
  },
  {
    id: 'ab_pride_prejudice',
    title: 'Pride and Prejudice',
    subtitle: 'The Classic Romance of Elizabeth Bennet & Mr. Darcy',
    author: 'Jane Austen',
    narrator: 'Annie Coleman (LibriVox Master)',
    languageCode: 'en',
    languageName: 'English',
    category: 'Classics',
    categories: ['Classics', 'Fiction', 'Romance'],
    coverUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 25200, // 7 hours
    chapterCount: 4,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.88,
    reviewsCount: 6420,
    publicationYear: 1813,
    description: 'Jane Austen\'s sparkling romantic comedy of manners following the independent Elizabeth Bennet and the proud Fitzwilliam Darcy as they overcome misconceptions.',
    chapters: [
      {
        id: 'ab_pp_ch1',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 1,
        title: 'Chapters 1-3: Netherfield Park is Let at Last',
        durationSec: 6200,
        streamUrl: 'https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_01-03_austen_64kb.mp3',
        summary: 'Mrs. Bennet urges Mr. Bennet to visit the wealthy newcomer Charles Bingley.'
      },
      {
        id: 'ab_pp_ch2',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 2,
        title: 'Chapters 4-5: The Meryton Assembly Ball',
        durationSec: 6400,
        streamUrl: 'https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_04-05_austen_64kb.mp3',
        summary: 'Elizabeth first observes Mr. Darcy\'s proud demeanor and unyielding refusal to dance.'
      },
      {
        id: 'ab_pp_ch3',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 3,
        title: 'Chapter 6: Observations at Lucas Lodge',
        durationSec: 6300,
        streamUrl: 'https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_06_austen_64kb.mp3',
        summary: 'Darcy begins to recognize Elizabeth\'s intelligence and charm.'
      },
      {
        id: 'ab_pp_ch4',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 4,
        title: 'Chapter 7: Jane at Netherfield',
        durationSec: 6300,
        streamUrl: 'https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_07_austen_64kb.mp3',
        summary: 'Elizabeth travels through the rain to be at her sister\'s side.'
      }
    ]
  },
  {
    id: 'ab_atomic_habits_guide',
    title: 'The Art of War (रणनीति व आत्मसंयम)',
    subtitle: 'Timeless Strategy, Mindset & Daily Execution',
    author: 'Sun Tzu (अनुवाद: लाल बिहारी)',
    narrator: 'Moira Fogarty (LibriVox Master)',
    languageCode: 'en',
    languageName: 'English',
    category: 'Self-Improvement',
    categories: ['Self-Improvement', 'Business & Finance', 'Most Listened'],
    coverUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 18000, // 5 hours
    chapterCount: 4,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.92,
    reviewsCount: 4180,
    publicationYear: 2024,
    description: 'Master Sun\'s 2,500-year-old principles on discipline, positioning, knowing oneself and the adversary, and achieving effortless victory in daily life and enterprise.',
    chapters: [
      {
        id: 'ab_af_ch1',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 1,
        title: 'Part 1: Laying Plans & Waging War',
        durationSec: 4500,
        streamUrl: 'https://archive.org/download/art_of_war_librivox/art_of_war_01-02_sun_tzu_64kb.mp3',
        summary: 'Five fundamental factors of success and the calculus of preparation.'
      },
      {
        id: 'ab_af_ch2',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 2,
        title: 'Part 2: Attack by Stratagem & Tactical Dispositions',
        durationSec: 4600,
        streamUrl: 'https://archive.org/download/art_of_war_librivox/art_of_war_03-04_sun_tzu_64kb.mp3',
        summary: 'Supreme excellence consists in breaking the enemy\'s resistance without fighting.'
      },
      {
        id: 'ab_af_ch3',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 3,
        title: 'Part 3: Energy & Weak Points and Strong',
        durationSec: 4400,
        streamUrl: 'https://archive.org/download/art_of_war_librivox/art_of_war_05-06_sun_tzu_64kb.mp3',
        summary: 'Momentum, adaptability like water, and seizing initiative.'
      },
      {
        id: 'ab_af_ch4',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 4,
        title: 'Part 4: Maneuvering & Variation in Tactics',
        durationSec: 4500,
        streamUrl: 'https://archive.org/download/art_of_war_librivox/art_of_war_07-08_sun_tzu_64kb.mp3',
        summary: 'Turning the devious into the direct and misfortune into gain.'
      }
    ]
  },
  {
    id: 'ab_kabuliwala',
    title: 'Kabuliwala & Gitanjali (काबुलीवाला)',
    subtitle: 'Timeless Stories & Poetry of Bengal',
    author: 'Rabindranath Tagore',
    narrator: 'Tagore Audio Archive',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Classics',
    categories: ['Classics', 'Hindi Literature', 'Short Stories'],
    coverUrl: 'https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 9000, // 2.5 hours
    chapterCount: 3,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.91,
    reviewsCount: 3120,
    publicationYear: 1892,
    description: 'Rabindranath Tagore\'s touching tale of the friendship between little Mini and a dry-fruit seller from Kabul, capturing universal fatherly love across distance and time.',
    chapters: [
      {
        id: 'ab_kab_ch1',
        audiobookId: 'ab_kabuliwala',
        chapterNumber: 1,
        title: 'अध्याय 1: काबुलीवाला (Kabuliwala Complete)',
        durationSec: 3000,
        streamUrl: 'https://archive.org/download/KABULIWALA_201501/KABULIWALA.mp3',
        summary: 'कोलकाता में पांच वर्षीय मिनी और रहमत की पहली मुलाकात और उनकी अनोखी दोस्ती।'
      },
      {
        id: 'ab_kab_ch2',
        audiobookId: 'ab_kabuliwala',
        chapterNumber: 2,
        title: 'अध्याय 2: आत्माराम (Atmaram)',
        durationSec: 3000,
        streamUrl: 'https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3',
        summary: 'प्रेमचंद की मार्मिक कहानी आत्माराम का सम्पूर्ण पाठ।'
      },
      {
        id: 'ab_kab_ch3',
        audiobookId: 'ab_kabuliwala',
        chapterNumber: 3,
        title: 'अध्याय 3: कायर (Kayar)',
        durationSec: 3000,
        streamUrl: 'https://archive.org/download/Kayar/pc-kayar-ac.mp3',
        summary: 'मानवीय संवेदनाओं और आत्मबोध की श्रेष्ठ कहानी।'
      }
    ]
  },
  {
    id: 'ab_psychology_wealth',
    title: 'The Count of Monte Cristo (धन, धैर्य व न्याय)',
    subtitle: 'Dumas\'s Masterpiece on Fortune, Patience & Triumph',
    author: 'Alexandre Dumas',
    narrator: 'John Van Stan (LibriVox Master)',
    languageCode: 'en',
    languageName: 'English',
    category: 'Business & Finance',
    categories: ['Business & Finance', 'Self-Improvement', 'Most Listened'],
    coverUrl: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 14400, // 4 hours
    chapterCount: 3,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.87,
    reviewsCount: 2740,
    publicationYear: 2025,
    description: 'Doing well with money isn\'t necessarily about what you know. It\'s about how you behave. Edmond Dantès discovers the treasure of Monte Cristo and learns that all human wisdom is contained in two words: Wait and Hope.',
    chapters: [
      {
        id: 'ab_pw_ch1',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 1,
        title: 'Chapter 1: Marseilles — The Arrival',
        durationSec: 4800,
        streamUrl: 'https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_001_dumas_64kb.mp3',
        summary: 'The Pharaon arrives in port carrying Edmond Dantès and fateful letters.'
      },
      {
        id: 'ab_pw_ch2',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 2,
        title: 'Chapter 2: Father and Son',
        durationSec: 4800,
        streamUrl: 'https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_002_dumas_64kb.mp3',
        summary: 'Dantès returns home to embrace his impoverished father in love and gratitude.'
      },
      {
        id: 'ab_pw_ch3',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 3,
        title: 'Chapter 3: The Catalans & Fernand',
        durationSec: 4800,
        streamUrl: 'https://archive.org/download/count_monte_cristo_0711_librivox/count_of_monte_cristo_003_dumas_64kb.mp3',
        summary: 'The jealousy that ignites the betrayal and sets destiny in motion.'
      }
    ]
  },
  {
    id: 'ab_bhagavad_gita',
    title: 'Sampoorna Shrimad Bhagavad Gita (सम्पूर्ण श्रीमद्भगवद्गीता)',
    subtitle: 'All 18 Adhyayas with Hindi Meaning & Shlokas',
    author: 'Maharishi Ved Vyasa',
    narrator: 'Shailendra Bharti / Swami Adgadanandji (यथार्थ गीता)',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Philosophy & Spirituality',
    categories: ['Philosophy & Spirituality', 'Classics', 'Hindi & Urdu Literature', 'Most Listened'],
    coverUrl: 'https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 64800, // 18 hours
    chapterCount: 18,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.99,
    reviewsCount: 14850,
    publicationYear: -500,
    description: 'पवित्र श्रीमद्भगवद्गीता के सभी १८ अध्यायों का सम्पूर्ण पाठ व सरल हिन्दी व्याख्या, सुप्रसिद्ध गायक एवं उद्घोषक शैलेंद्र भारती (Shailendra Bharti) व यथार्थ गीता के दिव्य स्वर में। कुरुक्षेत्र के युद्धस्थल पर भगवान श्रीकृष्ण द्वारा अर्जुन को दिया गया निष्काम कर्म, ज्ञान और भक्तियोग का सार्वभौमिक अमर उपदेश।',
    chapters: [
      {
        id: 'ab_bg_ch1',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 1,
        title: 'अध्याय 1: अर्जुनविषादयोग (Arjuna\'s Dilemma)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/02Chapter1.mp3',
        summary: 'कुरुक्षेत्र की रणभूमि में दोनों सेनाओं का अवलोकन और अपनों के प्रति मोह व विषाद।'
      },
      {
        id: 'ab_bg_ch2',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 2,
        title: 'अध्याय 2: सांख्ययोग (The Eternal Reality of the Soul)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/03Chapter2.mp3',
        summary: 'आत्मा की अमरता, क्षत्रिय धर्म और स्थितप्रज्ञ पुरुष के लक्षण।'
      },
      {
        id: 'ab_bg_ch3',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 3,
        title: 'अध्याय 3: कर्मयोग (The Path of Selfless Action)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/04Chapter3.mp3',
        summary: 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन — फल की आसक्ति रहित निष्काम कर्म का श्रेष्ठ मार्ग।'
      },
      {
        id: 'ab_bg_ch4',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 4,
        title: 'अध्याय 4: ज्ञानकर्मसंन्यासयोग (Wisdom in Action & Sacrifice)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/05Chapter4.mp3',
        summary: 'भगवान के दिव्य अवतार का रहस्य, ज्ञान-यज्ञ और कर्म की पवित्रता।'
      },
      {
        id: 'ab_bg_ch5',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 5,
        title: 'अध्याय 5: कर्मसंन्यासयोग (The Yoga of Renunciation)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/06Chapter5.mp3',
        summary: 'संन्यास और कर्मयोग दोनों का समन्वय तथा परम शांति का उपाय।'
      },
      {
        id: 'ab_bg_ch6',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 6,
        title: 'अध्याय 6: आत्मसंयमयोग / ध्यानयोग (Meditation & Inner Peace)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/07Chapter6.mp3',
        summary: 'चंचल मन पर विजय पाने की विधि, आसन, प्राणायाम और समत्व दृष्टि।'
      },
      {
        id: 'ab_bg_ch7',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 7,
        title: 'अध्याय 7: ज्ञानविज्ञानयोग (Knowledge of Ultimate Reality)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/08Chapter7.mp3',
        summary: 'भगवान की अपरा और परा प्रकृति तथा चार प्रकार के भक्तों का वर्णन।'
      },
      {
        id: 'ab_bg_ch8',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 8,
        title: 'अध्याय 8: अक्षरब्रह्मयोग (The Imperishable Absolute)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/09Chapter8.mp3',
        summary: 'अन्तकाल में भगवत्स्मरण, ॐ का जप और शाश्वत मोक्ष की प्राप्ति।'
      },
      {
        id: 'ab_bg_ch9',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 9,
        title: 'अध्याय 9: राजविद्याराजगुह्ययोग (The Sovereign Science & Secret)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/10Chapter9.mp3',
        summary: 'परम गोपनीय दिव्य ज्ञान और भगवान के प्रति अनन्य प्रेम का फल।'
      },
      {
        id: 'ab_bg_ch10',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 10,
        title: 'अध्याय 10: विभूतियोग (The Divine Glories & Splendors)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/11Chapter10.mp3',
        summary: 'सृष्टि के समस्त श्रेष्ठ रूपों में भगवान की सर्वव्यापी उपस्थिति का दिग्दर्शन।'
      },
      {
        id: 'ab_bg_ch11',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 11,
        title: 'अध्याय 11: विश्वरूपदर्शनयोग (Vision of the Universal Form)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/12Chapter11.mp3',
        summary: 'अर्जुन को भगवान श्रीकृष्ण के विराट, आलोकिक और भयप्रद विश्वरूप का साक्षात दर्शन।'
      },
      {
        id: 'ab_bg_ch12',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 12,
        title: 'अध्याय 12: भक्तियोग (The Path of Divine Devotion)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/13Chapter12.mp3',
        summary: 'सगुण भक्ति का सुगम मार्ग और प्रभु के प्रिय भक्त के श्रेष्ठ गुण।'
      },
      {
        id: 'ab_bg_ch13',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 13,
        title: 'अध्याय 13: क्षेत्रक्षेत्रज्ञविभागयोग (The Knower and the Field)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/14Chapter13.mp3',
        summary: 'भौतिक शरीर (क्षेत्र) और अविनाशी आत्मा (क्षेत्रज्ञ) का यथार्थ विवेक।'
      },
      {
        id: 'ab_bg_ch14',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 14,
        title: 'अध्याय 14: गुणत्रयविभागयोग (The Three Gunas of Nature)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/15Chapter14.mp3',
        summary: 'सत्त्व, रज और तम तीनों गुणों का प्रभाव और गुणातीत होने का मार्ग।'
      },
      {
        id: 'ab_bg_ch15',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 15,
        title: 'अध्याय 15: पुरुषोत्तमयोग (The Supreme Divine Being)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/16Chapter15.mp3',
        summary: 'उर्ध्वमूल संसार-वृक्ष का रहस्य और क्षर-अक्षर से परे परम पुरुषोत्तम।'
      },
      {
        id: 'ab_bg_ch16',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 16,
        title: 'अध्याय 16: दैवासुरसम्पद्विभागयोग (Divine and Demonic Traits)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/17Chapter16.mp3',
        summary: 'कल्याणकारी दैवी संपदा और पतनकारी आसुरी प्रवृत्तियों का सूक्ष्म भेद।'
      },
      {
        id: 'ab_bg_ch17',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 17,
        title: 'अध्याय 17: श्रद्धात्रयविभागयोग (The Threefold Divisions of Faith)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/18Chapter17.mp3',
        summary: 'सात्त्विक, राजसिक व तामसिक श्रद्धा, आहार, यज्ञ और ॐ तत्सत् का महत्व।'
      },
      {
        id: 'ab_bg_ch18',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 18,
        title: 'अध्याय 18: मोक्षसंन्यासयोग (Liberation through Surrender)',
        durationSec: 3600,
        streamUrl: 'https://archive.org/download/YatharthGeetaHindiAudio/19Chapter18.mp3',
        summary: 'सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज — सम्पूर्ण गीता का सार, शरणागति और परम मोक्ष।'
      }
    ]
  }
];

module.exports = {
  audiobookCategories,
  supportedAudiobookLanguages,
  audiobooks
};
