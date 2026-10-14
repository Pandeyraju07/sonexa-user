// ==========================================================================
// ZYNERA WEB PORTAL CLIENT ARCHITECTURE
// Redesigned with Apple Design, Publicis Sapient Design System,
// and Dual Music + Audiobooks Intelligent Streaming Platform
// ==========================================================================

// Seed Music Tracks (Fallbacks if server is in offline test mode)
let musicPlaylist = [
  {
    id: "m_starboy",
    title: "Starboy",
    artist: "The Weeknd, Daft Punk",
    coverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    vibe: "🔥 High Energy • Electropop",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
  },
  {
    id: "m_kesariya",
    title: "Kesariya",
    artist: "Arijit Singh, Pritam",
    coverUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    vibe: "❤️ Romantic Melodic • Hindi",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
  },
  {
    id: "m_husn",
    title: "Husn",
    artist: "Anuv Jain",
    coverUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
    vibe: "☕ Chill Acoustic • Indie Folk",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
  },
  {
    id: "m_blinding",
    title: "Blinding Lights",
    artist: "The Weeknd",
    coverUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
    vibe: "🌙 Late Night Synthwave • Pop",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
  }
];

// Seed Audiobooks Catalog (Public-Domain Literary Masterpieces & Classics)
let audiobooksCatalog = [
  {
    id: "ab_godan",
    title: "Godan (गोदान)",
    author: "Munshi Premchand",
    narrator: "Sameer Goswami",
    language: "Hindi",
    category: "Classic Literature",
    durationText: "14h 20m",
    coverUrl: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
    vibe: "📖 Munshi Premchand • Hindi Classic",
    description: "The seminal masterpiece of modern Hindi-Urdu literature portraying rural life, peasant dignity, and human resilience.",
    chapters: [
      { id: "ch1", title: "अध्याय 1: होरी का सपना व आत्माराम", streamUrl: "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3" },
      { id: "ch2", title: "अध्याय 2: भोला से भेंट व गुल्ली डंडा", streamUrl: "https://archive.org/download/GulliDandaByPremchand/Premchand-GulliDanda_64kb.mp3" },
      { id: "ch3", title: "अध्याय 3: धनिया की चिंता व कायर", streamUrl: "https://archive.org/download/Kayar/pc-kayar-ac.mp3" },
      { id: "ch4", title: "अध्याय 4: गोबर का विद्रोह व अनाथ लड़की", streamUrl: "https://archive.org/download/AnathLadkiPremchand/Anath%20Ladki%20-%20Premchand.mp3" }
    ]
  },
  {
    id: "ab_sherlock",
    title: "The Hound of the Baskervilles",
    author: "Sir Arthur Conan Doyle",
    narrator: "David Clarke",
    language: "English",
    category: "Mystery & Thriller",
    durationText: "6h 40m",
    coverUrl: "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&auto=format&fit=crop&q=80",
    vibe: "🕵️ Sherlock Holmes • Mystery Classic",
    description: "Sherlock Holmes and Dr. Watson investigate the terrifying curse of a spectral hound roaming Dartmoor's mist-shrouded moors.",
    chapters: [
      { id: "ch1", title: "Chapter 1: Mr. Sherlock Holmes", streamUrl: "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_01_doyle_64kb.mp3" },
      { id: "ch2", title: "Chapter 2: The Curse of the Baskervilles", streamUrl: "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_02_doyle_64kb.mp3" },
      { id: "ch3", title: "Chapter 3: The Problem", streamUrl: "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_03_doyle_64kb.mp3" },
      { id: "ch4", title: "Chapter 4: Sir Henry Baskerville", streamUrl: "https://archive.org/download/hound_baskervilles_librivox/houndofbaskervilles_04_doyle_64kb.mp3" }
    ]
  },
  {
    id: "ab_kabuliwala",
    title: "Kabuliwala & Selected Stories",
    author: "Rabindranath Tagore",
    narrator: "Tagore Audio Archive",
    language: "Bengali / Hindi",
    category: "Nobel Laureate Classics",
    durationText: "4h 15m",
    coverUrl: "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600&auto=format&fit=crop&q=80",
    vibe: "🕊️ Rabindranath Tagore • Nobel Classic",
    description: "An unforgettable tale of unconditional fatherly affection between an Afghan dry-fruit seller and young Mini in Calcutta.",
    chapters: [
      { id: "ch1", title: "Story 1: Kabuliwala (काबुलीवाला)", streamUrl: "https://archive.org/download/KABULIWALA_201501/KABULIWALA.mp3" },
      { id: "ch2", title: "Story 2: Atmaram (आत्माराम)", streamUrl: "https://archive.org/download/AtmaramByPremchand/Premchand-Atmaram_64kb.mp3" },
      { id: "ch3", title: "Story 3: Kayar (कायर)", streamUrl: "https://archive.org/download/Kayar/pc-kayar-ac.mp3" }
    ]
  },
  {
    id: "ab_pride",
    title: "Pride and Prejudice",
    author: "Jane Austen",
    narrator: "Annie Coleman",
    language: "English",
    category: "Classic Fiction",
    durationText: "11h 35m",
    coverUrl: "https://images.unsplash.com/photo-1495640388908-05fa85288e61?w=600&auto=format&fit=crop&q=80",
    vibe: "👑 Jane Austen • Timeless Romance",
    description: "The sharp-witted Elizabeth Bennet clashes with the reserved and wealthy Fitzwilliam Darcy across 19th-century England.",
    chapters: [
      { id: "ch1", title: "Chapters 1-3: Longbourn Estate", streamUrl: "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_01-03_austen_64kb.mp3" },
      { id: "ch2", title: "Chapters 4-5: The Meryton Assembly", streamUrl: "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_04-05_austen_64kb.mp3" },
      { id: "ch3", title: "Chapter 6: Jane and Bingley", streamUrl: "https://archive.org/download/pride_and_prejudice_librivox/prideandprejudice_06_austen_64kb.mp3" }
    ]
  },
  {
    id: "ab_gita",
    title: "Sampoorna Shrimad Bhagavad Gita (सम्पूर्ण श्रीमद्भगवद्गीता)",
    author: "Maharishi Ved Vyasa",
    narrator: "Shailendra Bharti / यथार्थ गीता",
    language: "Sanskrit / Hindi",
    category: "Philosophy & Spirituality",
    durationText: "18h 00m",
    coverUrl: "https://images.unsplash.com/photo-1506126613408-eca07ce68773?w=600&auto=format&fit=crop&q=80",
    vibe: "🪔 Ved Vyasa • Eternal Wisdom",
    description: "The timeless philosophical dialogue between Lord Krishna and Arjuna on dharma, karma, and inner mastery.",
    chapters: [
      { id: "ch1", title: "अध्याय 1: अर्जुनविषादयोग (Arjuna's Dilemma)", streamUrl: "https://archive.org/download/YatharthGeetaHindiAudio/02Chapter1.mp3" },
      { id: "ch2", title: "अध्याय 2: सांख्ययोग (Yoga of Knowledge)", streamUrl: "https://archive.org/download/YatharthGeetaHindiAudio/03Chapter2.mp3" },
      { id: "ch3", title: "अध्याय 3: कर्मयोग (Yoga of Action)", streamUrl: "https://archive.org/download/YatharthGeetaHindiAudio/04Chapter3.mp3" },
      { id: "ch4", title: "अध्याय 4: ज्ञानकर्मसंन्यासयोग (Wisdom in Action)", streamUrl: "https://archive.org/download/YatharthGeetaHindiAudio/05Chapter4.mp3" },
      { id: "ch18", title: "अध्याय 18: मोक्षसंन्यासयोग (Liberation)", streamUrl: "https://archive.org/download/YatharthGeetaHindiAudio/19Chapter18.mp3" }
    ]
  },
  {
    id: "ab_art_of_war",
    title: "The Art of War (रणनीति)",
    author: "Sun Tzu",
    narrator: "Moira Fogarty",
    language: "English",
    category: "Philosophy & Strategy",
    durationText: "4h 30m",
    coverUrl: "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
    vibe: "⚔️ Sun Tzu • Timeless Strategy",
    description: "Master Sun's timeless principles on positioning, calculation, and winning without fighting.",
    chapters: [
      { id: "ch1", title: "Part 1: Laying Plans & Waging War", streamUrl: "https://archive.org/download/art_of_war_librivox/art_of_war_01-02_sun_tzu_64kb.mp3" },
      { id: "ch2", title: "Part 2: Attack by Stratagem & Tactical Dispositions", streamUrl: "https://archive.org/download/art_of_war_librivox/art_of_war_03-04_sun_tzu_64kb.mp3" }
    ]
  }
];

// App State
let currentMode = 'music'; // 'music' | 'audiobook'
let musicIndex = 0;
let audiobookIndex = 0;
let chapterIndex = 0;
let isPlaying = false;
let playbackSpeeds = [1.0, 1.25, 1.5, 2.0, 0.75];
let currentSpeedIndex = 0;

// DOM Elements
const audio = document.getElementById('audioElement');
const playBtn = document.getElementById('playBtn');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const rewindBtn = document.getElementById('rewindBtn');
const forwardBtn = document.getElementById('forwardBtn');
const playIcon = document.getElementById('playIcon');
const pauseIcon = document.getElementById('pauseIcon');
const currentCover = document.getElementById('currentCover');
const currentTitle = document.getElementById('currentTitle');
const currentArtist = document.getElementById('currentArtist');
const currentVibe = document.getElementById('currentVibe');
const waveform = document.getElementById('waveform');
const modeMusicBtn = document.getElementById('modeMusic');
const modeAudiobookBtn = document.getElementById('modeAudiobook');
const audiobookMetaRow = document.getElementById('audiobookMetaRow');
const chapterSelect = document.getElementById('chapterSelect');
const speedBtn = document.getElementById('speedBtn');
const seekBar = document.getElementById('seekBar');
const currentTimeEl = document.getElementById('currentTime');
const totalDurationEl = document.getElementById('totalDuration');
const playerQualityBadge = document.getElementById('playerQualityBadge');
const audiobooksGrid = document.getElementById('audiobooksGrid');

// Format seconds into MM:SS
function formatTime(seconds) {
  if (isNaN(seconds) || seconds < 0) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${s < 10 ? '0' : ''}${s}`;
}

// --------------------------------------------------------------------------
// PLAYER LOGIC: MUSIC & AUDIOBOOKS
// --------------------------------------------------------------------------

function setMode(mode) {
  currentMode = mode;
  if (mode === 'music') {
    modeMusicBtn.classList.add('active');
    modeAudiobookBtn.classList.remove('active');
    audiobookMetaRow.classList.remove('active');
    if (rewindBtn) rewindBtn.style.display = 'none';
    if (forwardBtn) forwardBtn.style.display = 'none';
    playerQualityBadge.textContent = '24-bit 96kHz Lossless';
    loadCurrent();
  } else {
    modeMusicBtn.classList.remove('active');
    modeAudiobookBtn.classList.add('active');
    audiobookMetaRow.classList.add('active');
    if (rewindBtn) rewindBtn.style.display = 'flex';
    if (forwardBtn) forwardBtn.style.display = 'flex';
    playerQualityBadge.textContent = 'Audible-Grade Chapter Audio';
    loadCurrent();
  }
}

function loadCurrent() {
  if (currentMode === 'music') {
    const track = musicPlaylist[musicIndex] || musicPlaylist[0];
    currentTitle.textContent = track.title;
    currentArtist.textContent = track.artist;
    currentCover.src = track.coverUrl;
    currentVibe.textContent = track.vibe;
    audio.src = track.streamUrl;
  } else {
    const book = audiobooksCatalog[audiobookIndex] || audiobooksCatalog[0];
    const chapter = book.chapters[chapterIndex] || book.chapters[0];
    currentTitle.textContent = book.title;
    currentArtist.textContent = `${book.author} • Narrated by ${book.narrator}`;
    currentCover.src = book.coverUrl;
    currentVibe.textContent = `${book.vibe} (${book.durationText})`;

    // Populate Chapter dropdown
    chapterSelect.innerHTML = '';
    book.chapters.forEach((ch, idx) => {
      const opt = document.createElement('option');
      opt.value = idx;
      opt.textContent = ch.title;
      if (idx === chapterIndex) opt.selected = true;
      chapterSelect.appendChild(opt);
    });

    audio.src = chapter.streamUrl;
  }
  audio.playbackRate = playbackSpeeds[currentSpeedIndex];
}

function togglePlay() {
  if (isPlaying) {
    audio.pause();
  } else {
    audio.play().catch(e => console.log('Autoplay restricted:', e));
  }
}

function playNext() {
  if (currentMode === 'music') {
    musicIndex = (musicIndex + 1) % musicPlaylist.length;
    loadCurrent();
    if (isPlaying) audio.play();
  } else {
    const book = audiobooksCatalog[audiobookIndex];
    if (chapterIndex < book.chapters.length - 1) {
      chapterIndex++;
    } else {
      audiobookIndex = (audiobookIndex + 1) % audiobooksCatalog.length;
      chapterIndex = 0;
    }
    loadCurrent();
    if (isPlaying) audio.play();
  }
}

function playPrev() {
  if (currentMode === 'music') {
    musicIndex = (musicIndex - 1 + musicPlaylist.length) % musicPlaylist.length;
    loadCurrent();
    if (isPlaying) audio.play();
  } else {
    if (audio.currentTime > 5) {
      audio.currentTime = 0;
      return;
    }
    if (chapterIndex > 0) {
      chapterIndex--;
    } else {
      audiobookIndex = (audiobookIndex - 1 + audiobooksCatalog.length) % audiobooksCatalog.length;
      chapterIndex = 0;
    }
    loadCurrent();
    if (isPlaying) audio.play();
  }
}

// Rewind 15s (Audiobook control)
if (rewindBtn) {
  rewindBtn.addEventListener('click', () => {
    audio.currentTime = Math.max(0, audio.currentTime - 15);
  });
}

// Forward 30s (Audiobook control)
if (forwardBtn) {
  forwardBtn.addEventListener('click', () => {
    audio.currentTime = Math.min(audio.duration || 9999, audio.currentTime + 30);
  });
}

// Chapter selection change
if (chapterSelect) {
  chapterSelect.addEventListener('change', (e) => {
    chapterIndex = parseInt(e.target.value, 10);
    const book = audiobooksCatalog[audiobookIndex];
    const chapter = book.chapters[chapterIndex];
    audio.src = chapter.streamUrl;
    audio.currentTime = 0;
    if (isPlaying) audio.play();
  });
}

// Speed toggle button
if (speedBtn) {
  speedBtn.addEventListener('click', () => {
    currentSpeedIndex = (currentSpeedIndex + 1) % playbackSpeeds.length;
    const spd = playbackSpeeds[currentSpeedIndex];
    speedBtn.textContent = `${spd.toFixed(spd % 1 === 0 ? 1 : 2)}x`;
    audio.playbackRate = spd;
  });
}

// Seek bar scrubbing
if (seekBar) {
  seekBar.addEventListener('input', () => {
    if (audio.duration) {
      audio.currentTime = (seekBar.value / 100) * audio.duration;
    }
  });
}

// Mode Buttons Event Listeners
if (modeMusicBtn) modeMusicBtn.addEventListener('click', () => setMode('music'));
if (modeAudiobookBtn) modeAudiobookBtn.addEventListener('click', () => setMode('audiobook'));

// Play / Pause / Skip
if (playBtn) playBtn.addEventListener('click', togglePlay);
if (prevBtn) prevBtn.addEventListener('click', playPrev);
if (nextBtn) nextBtn.addEventListener('click', playNext);

// Audio Event Handlers
if (audio) {
  audio.addEventListener('play', () => {
    isPlaying = true;
    playIcon.style.display = 'none';
    pauseIcon.style.display = 'block';
    waveform.classList.add('playing');
  });

  audio.addEventListener('pause', () => {
    isPlaying = false;
    playIcon.style.display = 'block';
    pauseIcon.style.display = 'none';
    waveform.classList.remove('playing');
  });

  audio.addEventListener('timeupdate', () => {
    if (audio.duration && !isNaN(audio.duration)) {
      const progress = (audio.currentTime / audio.duration) * 100;
      seekBar.value = progress;
      currentTimeEl.textContent = formatTime(audio.currentTime);
      totalDurationEl.textContent = formatTime(audio.duration);
    }
  });

  audio.addEventListener('loadedmetadata', () => {
    totalDurationEl.textContent = formatTime(audio.duration);
  });

  audio.addEventListener('ended', () => {
    playNext();
  });
}

// --------------------------------------------------------------------------
// RENDER AUDIOBOOKS & CLASSICS SHOWCASE
// --------------------------------------------------------------------------
function renderAudiobooksShowcase() {
  if (!audiobooksGrid) return;
  audiobooksGrid.innerHTML = '';

  audiobooksCatalog.forEach((book, bIndex) => {
    const card = document.createElement('div');
    card.className = 'book-card';
    card.innerHTML = `
      <div class="book-cover-wrap">
        <img src="${book.coverUrl}" alt="${book.title}" class="book-cover-img" loading="lazy">
        <span class="book-badge">${book.category}</span>
        <span class="book-lang-badge">${book.language}</span>
      </div>
      <div class="book-content">
        <h4 class="book-title">${book.title}</h4>
        <p class="book-author">By ${book.author}</p>
        <p class="book-desc">${book.description}</p>
        <div class="book-meta-footer">
          <span>${book.chapters.length} Chapters • ${book.durationText}</span>
          <button class="btn-listen-book" data-book-index="${bIndex}">
            <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
            <span>Listen</span>
          </button>
        </div>
      </div>
    `;

    // Listen Button Click Handler
    const listenBtn = card.querySelector('.btn-listen-book');
    listenBtn.addEventListener('click', () => {
      audiobookIndex = bIndex;
      chapterIndex = 0;
      setMode('audiobook');
      loadCurrent();
      audio.play().catch(e => console.log('Audio autoplay prevented:', e));
      // Smooth scroll to live player
      const previewSec = document.getElementById('preview');
      if (previewSec) previewSec.scrollIntoView({ behavior: 'smooth' });
    });

    audiobooksGrid.appendChild(card);
  });
}

// --------------------------------------------------------------------------
// LIVE BACKEND DATA INTEGRATION
// --------------------------------------------------------------------------
async function syncWithServerCatalog() {
  try {
    // 1. Fetch live audiobooks catalog
    const abRes = await fetch('/api/v1/audiobooks');
    if (abRes.ok) {
      const json = await abRes.json();
      if (json.success && Array.isArray(json.data) && json.data.length > 0) {
        audiobooksCatalog = json.data.map(b => ({
          id: b.id,
          title: b.title,
          author: b.author,
          narrator: b.narrator,
          language: b.languageName || b.languageCode,
          category: b.category,
          durationText: `${Math.round(b.totalDurationSec / 3600)}h ${Math.round((b.totalDurationSec % 3600) / 60)}m`,
          coverUrl: b.coverUrl,
          vibe: `📖 ${b.author} • ${b.category}`,
          description: b.description,
          chapters: (b.chapters && b.chapters.length > 0) ? b.chapters : [
            { id: `${b.id}_ch1`, title: 'Chapter 1', streamUrl: b.previewAudioUrl || 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3' }
          ]
        }));
        renderAudiobooksShowcase();
      }
    }
  } catch (e) {
    console.log('Using local audiobooks catalog:', e.message);
  }

  try {
    // 2. Fetch live music tracks from home feed
    const musicRes = await fetch('/api/v1/music/home');
    if (musicRes.ok) {
      const json = await musicRes.json();
      const trending = json.data?.trendingNow || json.data?.recommendedTracks || [];
      if (trending.length > 0) {
        musicPlaylist = trending.map((t, idx) => ({
          id: t.id,
          title: t.title,
          artist: t.artist,
          coverUrl: t.effectiveCoverUrl || t.albumCoverUrl || t.coverUrl,
          vibe: `🔥 ${t.genre || 'Desi Hits'} • Zynera Master`,
          streamUrl: t.audioUrl || `https://www.soundhelix.com/examples/mp3/SoundHelix-Song-${(idx % 16) + 1}.mp3`
        }));
      }
    }
  } catch (e) {
    console.log('Using local music catalog:', e.message);
  }
}

// --------------------------------------------------------------------------
// DESIGN SYSTEM INTERACTIVE CONTROLLERS (COLOR SELECTION & BOX DESIGN)
// --------------------------------------------------------------------------
const paletteButtons = document.querySelectorAll('[data-palette]');
const boxButtons = document.querySelectorAll('[data-box-style]');

function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('zynera_theme', theme);
  paletteButtons.forEach(btn => {
    if (btn.getAttribute('data-palette') === theme) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

function setBoxStyle(boxStyle) {
  document.documentElement.setAttribute('data-box', boxStyle);
  localStorage.setItem('zynera_box_style', boxStyle);
  boxButtons.forEach(btn => {
    if (btn.getAttribute('data-box-style') === boxStyle) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

paletteButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    setTheme(btn.getAttribute('data-palette'));
  });
});

boxButtons.forEach(btn => {
  btn.addEventListener('click', () => {
    setBoxStyle(btn.getAttribute('data-box-style'));
  });
});

const storedTheme = localStorage.getItem('zynera_theme') || 'sapient';
const storedBox = localStorage.getItem('zynera_box_style') || 'squircle';
setTheme(storedTheme);
setBoxStyle(storedBox);

// --------------------------------------------------------------------------
// LIVE API HEALTH PING TEST
// --------------------------------------------------------------------------
const testApiBtn = document.getElementById('testApiBtn');
const apiOutput = document.getElementById('apiOutput');
const apiOutputCode = document.getElementById('apiOutputCode');

if (testApiBtn) {
  testApiBtn.addEventListener('click', async () => {
    testApiBtn.disabled = true;
    testApiBtn.textContent = 'Pinging...';
    apiOutput.style.display = 'block';
    apiOutputCode.textContent = 'Sending GET /api/v1/health and GET /api/v1/audiobooks ...';

    try {
      const [healthRes, abRes] = await Promise.all([
        fetch('/api/v1/health'),
        fetch('/api/v1/audiobooks')
      ]);
      const healthData = await healthRes.json();
      const abData = await abRes.json();
      
      const combinedReport = {
        server_health: healthData,
        audiobooks_catalog_status: {
          success: abData.success,
          total_books: abData.total,
          sample_titles: abData.data ? abData.data.slice(0, 3).map(b => `${b.title} by ${b.author}`) : []
        }
      };

      apiOutputCode.textContent = JSON.stringify(combinedReport, null, 2);
    } catch (err) {
      apiOutputCode.textContent = `Error connecting to API: ${err.message}`;
    } finally {
      testApiBtn.disabled = false;
      testApiBtn.textContent = 'Test Live Ping';
    }
  });
}

// --------------------------------------------------------------------------
// INITIALIZATION
// --------------------------------------------------------------------------
loadCurrent();
renderAudiobooksShowcase();
syncWithServerCatalog();
