// Zynera Music Catalog & Knowledge Base
const tracks = [
  {
    id: "track_1",
    title: "Starboy",
    artist: "The Weeknd, Daft Punk",
    artists: ["The Weeknd", "Daft Punk"],
    album: "Starboy",
    albumId: "album_1",
    durationMs: 230000,
    durationSec: 230,
    coverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
    genre: "Electropop",
    language: "English",
    playsCount: 2450000,
    likesCount: 182000,
    isLiked: false,
    lyrics: "[00:12.00] I'm tryna put you in the worst mood, ah\n[00:15.50] P1 cleaner than your church shoes, ah\n[00:18.00] Milli point two just to hurt you, ah\n[00:21.00] All red Lamb' just to tease you, ah",
    acousticFeatures: { energy: 0.82, happiness: 0.65, danceability: 0.78, acousticness: 0.12, tempo: 186 }
  },
  {
    id: "track_2",
    title: "Kesariya",
    artist: "Arijit Singh, Pritam",
    artists: ["Arijit Singh", "Pritam"],
    album: "Brahmāstra",
    albumId: "album_2",
    durationMs: 268000,
    durationSec: 268,
    coverUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
    genre: "Bollywood Melodic",
    language: "Hindi",
    playsCount: 3890000,
    likesCount: 310000,
    isLiked: true,
    lyrics: "[00:15.00] Mujhko itna bataye koi\n[00:20.00] Kaise tujhse dil na lagaye koi\n[00:25.00] Rabba ne tujhko banane me\n[00:29.00] Kar di hai husn ki khaali tijoriyan",
    acousticFeatures: { energy: 0.62, happiness: 0.85, danceability: 0.58, acousticness: 0.45, tempo: 110 }
  },
  {
    id: "track_3",
    title: "Husn",
    artist: "Anuv Jain",
    artists: ["Anuv Jain"],
    album: "Husn - Single",
    albumId: "album_3",
    durationMs: 217000,
    durationSec: 217,
    coverUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
    genre: "Indie Acoustic",
    language: "Hindi",
    playsCount: 1980000,
    likesCount: 175000,
    isLiked: true,
    lyrics: "[00:10.00] Dekho dekho kaise baatein yahan ki\n[00:15.00] Hai sath tere sabhi toh kya kami\n[00:22.00] Par phir bhi lage sab sunsaan sa",
    acousticFeatures: { energy: 0.35, happiness: 0.45, danceability: 0.42, acousticness: 0.88, tempo: 92 }
  },
  {
    id: "track_4",
    title: "Blinding Lights",
    artist: "The Weeknd",
    artists: ["The Weeknd"],
    album: "After Hours",
    albumId: "album_4",
    durationMs: 200000,
    durationSec: 200,
    coverUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
    genre: "Synthwave",
    language: "English",
    playsCount: 4200000,
    likesCount: 390000,
    isLiked: false,
    lyrics: "[00:20.00] I've been tryna call\n[00:23.00] I've been on my own for long enough\n[00:27.00] Maybe you can show me how to love, maybe",
    acousticFeatures: { energy: 0.91, happiness: 0.72, danceability: 0.85, acousticness: 0.05, tempo: 171 }
  },
  {
    id: "track_5",
    title: "Apna Bana Le",
    artist: "Arijit Singh, Sachin-Jigar",
    artists: ["Arijit Singh", "Sachin-Jigar"],
    album: "Bhediya",
    albumId: "album_5",
    durationMs: 261000,
    durationSec: 261,
    coverUrl: "https://images.unsplash.com/photo-1445985543468-79abfeae063b?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
    genre: "Romantic Melodic",
    language: "Hindi",
    playsCount: 2900000,
    likesCount: 240000,
    isLiked: true,
    lyrics: "[00:18.00] Tu mera koi na hoke bhi kuch laage\n[00:25.00] Tu mera koi na hoke bhi kuch laage\n[00:32.00] Apna bana le piya",
    acousticFeatures: { energy: 0.55, happiness: 0.78, danceability: 0.50, acousticness: 0.62, tempo: 98 }
  },
  {
    id: "track_6",
    title: "Levitating",
    artist: "Dua Lipa",
    artists: ["Dua Lipa"],
    album: "Future Nostalgia",
    albumId: "album_6",
    durationMs: 203000,
    durationSec: 203,
    coverUrl: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
    losslessUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3",
    genre: "Disco Pop",
    language: "English",
    playsCount: 3100000,
    likesCount: 280000,
    isLiked: false,
    lyrics: "[00:12.00] If you wanna run away with me, I know a galaxy\n[00:16.00] And I can take you for a ride\n[00:20.00] I had a premonition that we fell into a rhythm",
    acousticFeatures: { energy: 0.88, happiness: 0.90, danceability: 0.92, acousticness: 0.08, tempo: 103 }
  }
];

const artists = [
  {
    id: "artist_1",
    name: "Arijit Singh",
    genre: "Bollywood Romantic",
    monthlyListeners: 42500000,
    followers: 9800000,
    bio: "Iconic playback singer celebrated for emotive voice and unforgettable romantic anthems across Bollywood.",
    avatarUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    topTracks: ["track_2", "track_5"],
    albums: ["album_2", "album_5"]
  },
  {
    id: "artist_2",
    name: "The Weeknd",
    genre: "R&B / Synthwave",
    monthlyListeners: 105000000,
    followers: 18500000,
    bio: "Global sensation bridging dark R&B, cinematic synthwave, and blockbuster pop masterpieces.",
    avatarUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    topTracks: ["track_1", "track_4"],
    albums: ["album_1", "album_4"]
  },
  {
    id: "artist_3",
    name: "Anuv Jain",
    genre: "Indie Acoustic",
    monthlyListeners: 12500000,
    followers: 3400000,
    bio: "Indie singer-songwriter whose heartfelt acoustic guitar and raw lyrical storytelling captivated millions.",
    avatarUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
    topTracks: ["track_3"],
    albums: ["album_3"]
  },
  {
    id: "artist_4",
    name: "Dua Lipa",
    genre: "Nu-Disco Pop",
    monthlyListeners: 78000000,
    followers: 14200000,
    bio: "British pop powerhouse redefining disco and modern electronic dance-pop.",
    avatarUrl: "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
    topTracks: ["track_6"],
    albums: ["album_6"]
  }
];

const playlists = [
  {
    id: "pl_today_hits",
    title: "Today's Top Hits",
    description: "The hottest tracks right now across India and globally.",
    coverUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
    trackCount: 50,
    tracks: ["track_1", "track_2", "track_4", "track_6"],
    isCurated: true
  },
  {
    id: "pl_monsoon_chill",
    title: "Monsoon & Chai",
    description: "Gentle acoustic hindi, warm guitars, and soft rains.",
    coverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    trackCount: 35,
    tracks: ["track_3", "track_5", "track_2"],
    isCurated: true
  },
  {
    id: "pl_hifi_lossless",
    title: "Zynera Hi-Fi Master Showcase",
    description: "Experience 24-bit 96kHz lossless audio fidelity across all sound spectrums.",
    coverUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
    trackCount: 25,
    tracks: ["track_1", "track_2", "track_3", "track_4", "track_5", "track_6"],
    isCurated: true
  }
];

const podcasts = [
  {
    id: "pod_1",
    title: "The Musician's Mind",
    host: "Karan Johar & Amit Trivedi",
    coverUrl: "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80",
    description: "Deep conversations with composers, producers, and lyricists about creating timeless melodies.",
    episodeCount: 24,
    category: "Music & Culture",
    episodes: [
      {
        id: "ep_1",
        title: "Ep 1: The Magic of Minor Chords in Indian Cinema",
        duration: "45m",
        streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"
      }
    ]
  },
  {
    id: "pod_2",
    title: "AI Audio Revolution",
    host: "Dr. Anya Sharma",
    coverUrl: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?w=600&auto=format&fit=crop&q=80",
    description: "Exploring spatial audio, neural synthesizers, and intelligent recommendation architectures.",
    episodeCount: 16,
    category: "Technology",
    episodes: [
      {
        id: "ep_2",
        title: "Ep 1: How LLMs Understand Harmony and Timbre",
        duration: "38m",
        streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
      }
    ]
  }
];

const liveEvents = [
  {
    id: "event_1",
    title: "Arijit Singh — One Night Only Live Arena Tour",
    date: "2026-10-15T19:30:00Z",
    venue: "Jio World Garden, Mumbai",
    city: "Mumbai",
    category: "Live Concert",
    coverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    priceRange: "₹1,499 - ₹8,999",
    status: "Selling Fast"
  },
  {
    id: "event_2",
    title: "Sunburn Goa 2026 — Electronic Dance Festival",
    date: "2026-12-28T16:00:00Z",
    venue: "Vagator Beach, Goa",
    city: "Goa",
    category: "EDM Festival",
    coverUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
    priceRange: "₹3,999 - ₹15,000",
    status: "Early Bird Open"
  }
];

const supportedLanguages = [
  { code: "hi", name: "Hindi", nativeName: "हिन्दी" },
  { code: "en", name: "English", nativeName: "English" },
  { code: "pa", name: "Punjabi", nativeName: "ਪੰਜਾਬੀ" },
  { code: "ta", name: "Tamil", nativeName: "தமிழ்" },
  { code: "te", name: "Telugu", nativeName: "తెలుగు" },
  { code: "bn", name: "Bengali", nativeName: "বাংলা" },
  { code: "mr", name: "Marathi", nativeName: "मराठी" },
  { code: "gu", name: "Gujarati", nativeName: "ગુજરાતી" },
  { code: "kn", name: "Kannada", nativeName: "ಕನ್ನಡ" },
  { code: "ml", name: "Malayalam", nativeName: "മലയാളം" },
  { code: "ur", name: "Urdu", nativeName: "اردو" }
];

module.exports = {
  tracks,
  artists,
  playlists,
  podcasts,
  liveEvents,
  supportedLanguages
};
