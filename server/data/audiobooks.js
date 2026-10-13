/**
 * Zynera Audiobooks Catalog & Public-Domain / Licensed Audio Repository
 * Provides metadata, chapters, lawful audio streams, and multilingual classifications.
 */

const audiobookCategories = [
  { id: 'cat_classics', name: 'Classic Literature', slug: 'classics', description: 'Timeless masterpieces from world and Indian literature', imageUrl: 'https://images.unsplash.com/photo-1461360370896-922624d12aa1?w=600' },
  { id: 'cat_hindi_lit', name: 'Hindi & Urdu Literature', slug: 'hindi-literature', description: 'Legendary stories by Premchand, Manto, and Dinkar', imageUrl: 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600' },
  { id: 'cat_self_growth', name: 'Self-Improvement', slug: 'self-improvement', description: 'Mindset, habits, productivity, and emotional intelligence', imageUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600' },
  { id: 'cat_business', name: 'Business & Finance', slug: 'business-finance', description: 'Wealth creation, entrepreneurship, and strategy', imageUrl: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600' },
  { id: 'cat_fiction', name: 'Fiction & Drama', slug: 'fiction', description: 'Captivating stories, mysteries, and human drama', imageUrl: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600' },
  { id: 'cat_scifi', name: 'Sci-Fi & Fantasy', slug: 'scifi-fantasy', description: 'Future worlds, cybernetics, and mythical realms', imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600' },
  { id: 'cat_spirituality', name: 'Philosophy & Spirituality', slug: 'philosophy-spirituality', description: 'Ancient wisdom, mindfulness, and inner peace', imageUrl: 'https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600' },
  { id: 'cat_short_stories', name: 'Short Stories', slug: 'short-stories', description: 'Bite-sized listens under 30 minutes', imageUrl: 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?w=600' }
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
    narrator: 'Sameer Goswami',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Hindi Literature',
    categories: ['Classics', 'Hindi Literature', 'Fiction'],
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
        title: 'अध्याय 1: होरी का सपना (Hori\'s Dream)',
        durationSec: 4800,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
        summary: 'होरी सुबह उठकर अपने खेतों की ओर निकलता है और गाय खरीदने की अपनी दिली तमन्ना पर विचार करता है।'
      },
      {
        id: 'ab_godan_ch2',
        audiobookId: 'ab_godan',
        chapterNumber: 2,
        title: 'अध्याय 2: भोला से भेंट (Meeting with Bhola)',
        durationSec: 4600,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
        summary: 'होरी की मुलाकात भोला से होती है और गाय के सौदे की बातचीत शुरू होती है।'
      },
      {
        id: 'ab_godan_ch3',
        audiobookId: 'ab_godan',
        chapterNumber: 3,
        title: 'अध्याय 3: धनिया की चिंता (Dhaniya\'s Concern)',
        durationSec: 5100,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
        summary: 'धनिया परिवार के आर्थिक संकट को लेकर होरी को सचेत करती है।'
      },
      {
        id: 'ab_godan_ch4',
        audiobookId: 'ab_godan',
        chapterNumber: 4,
        title: 'अध्याय 4: गोबर का विद्रोह (Gobar\'s Rebellion)',
        durationSec: 4900,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
        summary: 'गोबर पुरानी रीतियों और सामाजिक कुरीतियों के खिलाफ आवाज उठाता है।'
      },
      {
        id: 'ab_godan_ch5',
        audiobookId: 'ab_godan',
        chapterNumber: 5,
        title: 'अध्याय 5: ऋण का चक्रव्यूह (Web of Debt)',
        durationSec: 4700,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
        summary: 'महाजन और साहूकारों के ब्याज के जाल में किसान परिवार का संघर्ष।'
      },
      {
        id: 'ab_godan_ch6',
        audiobookId: 'ab_godan',
        chapterNumber: 6,
        title: 'अध्याय 6: अंतिम दान (The Final Offering)',
        durationSec: 4700,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
        summary: 'होरी का अटूट स्वाभिमान और उपन्यास का मार्मिक अंत।'
      }
    ]
  },
  {
    id: 'ab_sherlock',
    title: 'The Hound of the Baskervilles',
    subtitle: 'A Sherlock Holmes Master Mystery',
    author: 'Sir Arthur Conan Doyle',
    narrator: 'David Clarke (LibriVox Public Domain)',
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
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
        summary: 'Holmes and Watson examine a mysterious walking stick left behind by Dr. James Mortimer.'
      },
      {
        id: 'ab_sherlock_ch2',
        audiobookId: 'ab_sherlock',
        chapterNumber: 2,
        title: 'Chapter 2: The Curse of the Baskervilles',
        durationSec: 4500,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
        summary: 'Dr. Mortimer reads the ancient manuscript detailing the demonic hound that killed Sir Hugo Baskerville.'
      },
      {
        id: 'ab_sherlock_ch3',
        audiobookId: 'ab_sherlock',
        chapterNumber: 3,
        title: 'Chapter 3: The Problem on Baker Street',
        durationSec: 4300,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
        summary: 'Sir Henry Baskerville arrives in London and receives a threatening anonymous warning.'
      },
      {
        id: 'ab_sherlock_ch4',
        audiobookId: 'ab_sherlock',
        chapterNumber: 4,
        title: 'Chapter 4: The Grimpen Mire',
        durationSec: 4400,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
        summary: 'Watson accompanies Sir Henry to Baskerville Hall amidst eerie howling in the mist.'
      },
      {
        id: 'ab_sherlock_ch5',
        audiobookId: 'ab_sherlock',
        chapterNumber: 5,
        title: 'Chapter 5: The Hound Unleashed',
        durationSec: 4200,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
        summary: 'Holmes springs his trap as the glowing beast emerges from the fog.'
      }
    ]
  },
  {
    id: 'ab_idgah',
    title: 'Idgah & Other Classic Tales (ईदगाह)',
    subtitle: 'Heartfelt Short Stories by Premchand',
    author: 'Munshi Premchand',
    narrator: 'Anurag Kashyap V/O',
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
        title: 'कहानी 1: ईदगाह (Idgah - The Chimta of Love)',
        durationSec: 2400,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
        summary: 'हामिद का मेला जाना और दादी अमीना के लिए चिमटा खरीदना।'
      },
      {
        id: 'ab_idgah_ch2',
        audiobookId: 'ab_idgah',
        chapterNumber: 2,
        title: 'कहानी 2: नमक का दारोगा (Namak Ka Daroga)',
        durationSec: 2600,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
        summary: 'ईमानदार दारोगा वंशीधर और भ्रष्ट धनवान पंडित अलोपीदीन की कालजयी दास्तान।'
      },
      {
        id: 'ab_idgah_ch3',
        audiobookId: 'ab_idgah',
        chapterNumber: 3,
        title: 'कहानी 3: पंच परमेश्वर (Panch Parmeshwar)',
        durationSec: 2200,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
        summary: 'जुम्मन शेख और अलगू चौधरी की दोस्ती और न्याय का धर्मसंकट।'
      }
    ]
  },
  {
    id: 'ab_pride_prejudice',
    title: 'Pride and Prejudice',
    subtitle: 'The Classic Romance of Elizabeth Bennet & Mr. Darcy',
    author: 'Jane Austen',
    narrator: 'Karen Savage (LibriVox Public Domain)',
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
        title: 'Chapter 1: Netherfield Park is Let at Last',
        durationSec: 6200,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
        summary: 'Mrs. Bennet urges Mr. Bennet to visit the wealthy newcomer Charles Bingley.'
      },
      {
        id: 'ab_pp_ch2',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 2,
        title: 'Chapter 2: The Meryton Assembly Ball',
        durationSec: 6400,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-16.mp3',
        summary: 'Elizabeth first observes Mr. Darcy\'s proud demeanor and unyielding refusal to dance.'
      },
      {
        id: 'ab_pp_ch3',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 3,
        title: 'Chapter 3: The Proposal at Hunsford',
        durationSec: 6300,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
        summary: 'Darcy professes his love against his better judgment, sparking Elizabeth\'s fierce rebuke.'
      },
      {
        id: 'ab_pp_ch4',
        audiobookId: 'ab_pride_prejudice',
        chapterNumber: 4,
        title: 'Chapter 4: Pemberley and Reconciliation',
        durationSec: 6300,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
        summary: 'Elizabeth tours Pemberley and realizes Darcy\'s true nobility of character.'
      }
    ]
  },
  {
    id: 'ab_atomic_habits_guide',
    title: 'Atomic Focus & Daily Mastery',
    subtitle: 'Proven Systems for Small Changes and Monumental Results',
    author: 'Vikram Sengupta',
    narrator: 'Arjun Mathur',
    languageCode: 'en',
    languageName: 'English',
    category: 'Self-Improvement',
    categories: ['Self-Improvement', 'Business & Finance'],
    coverUrl: 'https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 18000, // 5 hours
    chapterCount: 4,
    isClassic: false,
    isPublicDomain: false,
    rating: 4.92,
    reviewsCount: 4180,
    publicationYear: 2024,
    description: 'A revolutionary practical audio handbook on designing daily habit loops, breaking procrastination cycles, and compounding incremental 1% gains in health, finance, and career.',
    chapters: [
      {
        id: 'ab_af_ch1',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 1,
        title: 'Chapter 1: The Mathematics of 1% Daily Compounding',
        durationSec: 4500,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
        summary: 'Why systems trump willpower and how identity-based habits create enduring shifts.'
      },
      {
        id: 'ab_af_ch2',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 2,
        title: 'Chapter 2: The Four Laws of Behavioral Design',
        durationSec: 4600,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3',
        summary: 'Make it obvious, make it attractive, make it easy, make it satisfying.'
      },
      {
        id: 'ab_af_ch3',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 3,
        title: 'Chapter 3: Environment Priming & Habit Stacking',
        durationSec: 4400,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3',
        summary: 'How to structure your physical and digital desk to eliminate distraction by default.'
      },
      {
        id: 'ab_af_ch4',
        audiobookId: 'ab_atomic_habits_guide',
        chapterNumber: 4,
        title: 'Chapter 4: The Plateau of Latent Potential',
        durationSec: 4500,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3',
        summary: 'Navigating the valley of disappointment before exponential breakthroughs occur.'
      }
    ]
  },
  {
    id: 'ab_kabuliwala',
    title: 'Kabuliwala & Gitanjali (काबुलीवाला)',
    subtitle: 'Timeless Stories & Poetry of Bengal',
    author: 'Rabindranath Tagore',
    narrator: 'Soumitra Roy & Devashish',
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
        title: 'अध्याय 1: मिनी और काबुलीवाला (Mini and Rahamat)',
        durationSec: 3000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3',
        summary: 'कोलकाता में पांच वर्षीय मिनी और रहमत की पहली मुलाकात और उनकी अनोखी दोस्ती।'
      },
      {
        id: 'ab_kab_ch2',
        audiobookId: 'ab_kabuliwala',
        chapterNumber: 2,
        title: 'अध्याय 2: कारावास और वियोग (Separation)',
        durationSec: 3000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3',
        summary: 'रहमत का दुर्भाग्यवश जेल जाना और मिनी का बड़ा होना।'
      },
      {
        id: 'ab_kab_ch3',
        audiobookId: 'ab_kabuliwala',
        chapterNumber: 3,
        title: 'अध्याय 3: विदाई और गीतांजलि के छंद (The Wedding Day)',
        durationSec: 3000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3',
        summary: 'मिनी की शादी का दिन और रहमत की अपने वतन वापसी।'
      }
    ]
  },
  {
    id: 'ab_psychology_wealth',
    title: 'The Psychology of Financial Freedom',
    subtitle: 'Timeless Lessons on Wealth, Greed, and Happiness',
    author: 'Neha Singhal, CFA',
    narrator: 'Pooja Bhattacharya',
    languageCode: 'en',
    languageName: 'English',
    category: 'Business & Finance',
    categories: ['Business & Finance', 'Self-Improvement'],
    coverUrl: 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 14400, // 4 hours
    chapterCount: 3,
    isClassic: false,
    isPublicDomain: false,
    rating: 4.87,
    reviewsCount: 2740,
    publicationYear: 2025,
    description: 'Doing well with money isn\'t necessarily about what you know. It\'s about how you behave. Learn the psychological blind spots that dictate investment decisions and peace of mind.',
    chapters: [
      {
        id: 'ab_pw_ch1',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 1,
        title: 'Chapter 1: No One\'s Crazy — Your Personal Financial History',
        durationSec: 4800,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3',
        summary: 'How economic conditions of our youth mold risk appetite for decades.'
      },
      {
        id: 'ab_pw_ch2',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 2,
        title: 'Chapter 2: Luck, Risk, and Knowing When You Have Enough',
        durationSec: 4800,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3',
        summary: 'The ceiling of ambition and the insidious trap of moving goalposts.'
      },
      {
        id: 'ab_pw_ch3',
        audiobookId: 'ab_psychology_wealth',
        chapterNumber: 3,
        title: 'Chapter 3: Wealth is What You Don\'t See',
        durationSec: 4800,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3',
        summary: 'The hidden distinction between spending money to show status and true autonomy.'
      }
    ]
  },
  {
    id: 'ab_bhagavad_gita',
    title: 'Bhagavad Gita — The Path of Purpose (श्रीमद्भगवद्गीता)',
    subtitle: 'Timeless Wisdom for Modern Living',
    author: 'Maharishi Ved Vyasa',
    narrator: 'Dr. Radhakrishnan Shankaran',
    languageCode: 'hi',
    languageName: 'Hindi',
    category: 'Philosophy & Spirituality',
    categories: ['Classics', 'Philosophy & Spirituality'],
    coverUrl: 'https://images.unsplash.com/photo-1507692049790-de58290a4334?w=600&auto=format&fit=crop&q=80',
    totalDurationSec: 18000, // 5 hours
    chapterCount: 3,
    isClassic: true,
    isPublicDomain: true,
    rating: 4.98,
    reviewsCount: 8940,
    publicationYear: -500,
    description: 'The sublime philosophical dialogue between Prince Arjuna and Lord Krishna on the battlefield of Kurukshetra, guiding the human spirit through duty, detachment, and supreme consciousness.',
    chapters: [
      {
        id: 'ab_bg_ch1',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 1,
        title: 'अध्याय 1 व 2: अर्जुनविषादयोग एवं सांख्ययोग (Arjuna\'s Dilemma & Wisdom)',
        durationSec: 6000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3',
        summary: 'युद्धभूमि पर कर्तव्य और मोह का अंतर्द्वंद तथा आत्मा की अमरता का उपदेश।'
      },
      {
        id: 'ab_bg_ch2',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 2,
        title: 'अध्याय 3: कर्मयोग — निष्काम कर्म की शक्ति (The Yoga of Action)',
        durationSec: 6000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3',
        summary: 'कर्मण्येवाधिकारस्ते मा फलेषु कदाचन — फल की आसक्ति के बिना श्रेष्ठ कर्म का मार्ग।'
      },
      {
        id: 'ab_bg_ch3',
        audiobookId: 'ab_bhagavad_gita',
        chapterNumber: 3,
        title: 'अध्याय 6: ध्यानयोग — मन पर विजय (Meditation & Inner Peace)',
        durationSec: 6000,
        streamUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-15.mp3',
        summary: 'चंचल मन को एकाग्र करने और समत्व भाव प्राप्त करने की पद्धति।'
      }
    ]
  }
];

module.exports = {
  audiobookCategories,
  supportedAudiobookLanguages,
  audiobooks
};
