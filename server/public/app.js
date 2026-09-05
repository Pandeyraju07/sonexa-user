// Zynera Web Portal Client Logic

const playlist = [
  {
    title: "Starboy",
    artist: "The Weeknd, Daft Punk",
    coverUrl: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
    vibe: "🔥 High Energy • Electropop",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
  },
  {
    title: "Kesariya",
    artist: "Arijit Singh, Pritam",
    coverUrl: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
    vibe: "❤️ Romantic Melodic • Hindi",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
  },
  {
    title: "Husn",
    artist: "Anuv Jain",
    coverUrl: "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
    vibe: "☕ Chill Acoustic • Indie Folk",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
  },
  {
    title: "Blinding Lights",
    artist: "The Weeknd",
    coverUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=600&auto=format&fit=crop&q=80",
    vibe: "🌙 Late Night Synthwave • Pop",
    streamUrl: "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
  }
];

let currentIndex = 0;
let isPlaying = false;

const audio = document.getElementById('audioElement');
const playBtn = document.getElementById('playBtn');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const playIcon = document.getElementById('playIcon');
const pauseIcon = document.getElementById('pauseIcon');
const currentCover = document.getElementById('currentCover');
const currentTitle = document.getElementById('currentTitle');
const currentArtist = document.getElementById('currentArtist');
const currentVibe = document.getElementById('currentVibe');
const waveform = document.getElementById('waveform');

function loadTrack(index) {
  const track = playlist[index];
  currentTitle.textContent = track.title;
  currentArtist.textContent = track.artist;
  currentCover.src = track.coverUrl;
  currentVibe.textContent = track.vibe;
  audio.src = track.streamUrl;
}

function togglePlay() {
  if (isPlaying) {
    audio.pause();
  } else {
    audio.play().catch(e => console.log('Audio autoplay policy:', e));
  }
}

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

audio.addEventListener('ended', () => {
  currentIndex = (currentIndex + 1) % playlist.length;
  loadTrack(currentIndex);
  audio.play();
});

playBtn.addEventListener('click', togglePlay);

prevBtn.addEventListener('click', () => {
  currentIndex = (currentIndex - 1 + playlist.length) % playlist.length;
  loadTrack(currentIndex);
  if (isPlaying) audio.play();
});

nextBtn.addEventListener('click', () => {
  currentIndex = (currentIndex + 1) % playlist.length;
  loadTrack(currentIndex);
  if (isPlaying) audio.play();
});

// Initial load
loadTrack(0);

// Live API Test Ping
const testApiBtn = document.getElementById('testApiBtn');
const apiOutput = document.getElementById('apiOutput');
const apiOutputCode = document.getElementById('apiOutputCode');

testApiBtn.addEventListener('click', async () => {
  testApiBtn.disabled = true;
  testApiBtn.textContent = 'Pinging...';
  apiOutput.style.display = 'block';
  apiOutputCode.textContent = 'Sending GET request to /api/v1/health ...';

  try {
    const res = await fetch('/api/v1/health');
    const data = await res.json();
    apiOutputCode.textContent = JSON.stringify(data, null, 2);
  } catch (err) {
    apiOutputCode.textContent = `Error connecting to API: ${err.message}`;
  } finally {
    testApiBtn.disabled = false;
    testApiBtn.textContent = 'Test Live Ping';
  }
});
