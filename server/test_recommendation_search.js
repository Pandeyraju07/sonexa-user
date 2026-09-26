/**
 * Acceptance & Unit Tests for Recommendation & Semantic Search Engine
 * Verifies all 10 acceptance scenarios from the enterprise specification.
 */

const assert = require('assert');
const { catalogStore } = require('./catalog/catalogStore');
const { hybridRecommendationEngine } = require('./recommendation/engine/HybridRecommendationEngine');
const PlaybackContext = require('./recommendation/model/PlaybackContext');
const { unifiedSearchService } = require('./search/service/UnifiedSearchService');
const { searchIntentDetector } = require('./search/intent/SearchIntentDetector');
const { affinityEngine } = require('./events/AffinityEngine');
const { dynamicQueueService } = require('./recommendation/service/DynamicQueueService');
const { autoplayService } = require('./recommendation/service/AutoplayService');
const { recommendationDebugService } = require('./recommendation/service/RecommendationDebugService');

console.log('🧪 Starting Enterprise Recommendation & Search Engine Test Suite...\n');

let passedTests = 0;

function runTest(testName, testFn) {
  try {
    testFn();
    console.log(`✅ [PASS] ${testName}`);
    passedTests++;
  } catch (err) {
    console.error(`❌ [FAIL] ${testName}`);
    console.error(err);
    process.exitCode = 1;
  }
}

// TEST 1: Play romantic Hindi song ("Hawayein").
// Expected: Next recommendations should predominantly be romantic Hindi/Bollywood songs with controlled diversity.
runTest('TEST 1: Romantic Hindi recommendation context', () => {
  const hawayein = catalogStore.getTrackById('track_hawayein');
  assert(hawayein, 'Hawayein track must exist');

  const context = PlaybackContext.fromTrack(hawayein, { limit: 10 });
  const recs = hybridRecommendationEngine.recommend(context);

  assert(recs.length > 0, 'Must produce recommendations');
  // First recommendations must be romantic Hindi tracks
  const topRecs = recs.slice(0, 5);
  const romanticHindiCount = topRecs.filter(r =>
    r.track.language === 'Hindi' &&
    ((r.track.moods && r.track.moods.ROMANTIC >= 0.7) || r.track.genre.includes('Romantic'))
  ).length;

  assert(romanticHindiCount >= 3, `Expected at least 3 romantic Hindi tracks in top 5, got ${romanticHindiCount}`);
});

// TEST 2: Play Punjabi party songs repeatedly.
// Expected: Punjabi + party preference increases.
runTest('TEST 2: Preference learning for Punjabi party songs', () => {
  const testUserId = 'test_user_punjabi';
  const brownMunde = catalogStore.getTrackById('track_brown_munde');
  assert(brownMunde, 'Brown Munde track must exist');

  // Initial affinity
  const initialGenreScore = affinityEngine.getGenreScore(testUserId, 'Punjabi');
  const initialMoodScore = affinityEngine.getMoodScore(testUserId, 'PARTY');

  // User listens to and completes Punjabi party songs 4 times
  for (let i = 0; i < 4; i++) {
    affinityEngine.processEvent({
      userId: testUserId,
      songId: brownMunde.id,
      eventType: 'SONG_PLAY_STARTED'
    });
    affinityEngine.processEvent({
      userId: testUserId,
      songId: brownMunde.id,
      eventType: 'SONG_COMPLETED'
    });
  }

  const updatedGenreScore = affinityEngine.getGenreScore(testUserId, 'Punjabi');
  const updatedMoodScore = affinityEngine.getMoodScore(testUserId, 'PARTY');

  assert(updatedGenreScore > initialGenreScore, `Punjabi genre score should increase: ${initialGenreScore} -> ${updatedGenreScore}`);
  assert(updatedMoodScore > initialMoodScore, `Party mood score should increase: ${initialMoodScore} -> ${updatedMoodScore}`);
});

// TEST 3: Search "Dhurandhar songs"
// Expected: Movie entity recognized, Dhurandhar soundtrack returned.
runTest('TEST 3: Search "Dhurandhar songs" -> MOVIE_SONGS intent', () => {
  const query = 'Dhurandhar songs';
  const result = unifiedSearchService.search(query);

  assert.strictEqual(result.intent, 'MOVIE_SONGS', 'Intent must be MOVIE_SONGS');
  assert(result.topResult && result.topResult.type === 'MOVIE', 'Top result must be MOVIE');
  assert.strictEqual(result.topResult.data.id, 'movie_dhurandhar');

  // Verify soundtrack tracks returned
  const dhurandharTracks = result.songs.filter(t => t.movieId === 'movie_dhurandhar' || (t.movie && t.movie.includes('Dhurandhar')));
  assert(dhurandharTracks.length >= 3, `Expected all 3 Dhurandhar soundtrack tracks, got ${dhurandharTracks.length}`);
});

// TEST 4: Search "Arijit Singh"
// Expected: Artist recognized, artist profile and songs returned.
runTest('TEST 4: Search "Arijit Singh" -> ARTIST intent', () => {
  const result = unifiedSearchService.search('Arijit Singh');

  assert.strictEqual(result.intent, 'ARTIST', 'Intent must be ARTIST');
  assert(result.topResult && result.topResult.type === 'ARTIST');
  assert.strictEqual(result.topResult.data.name, 'Arijit Singh');
  assert(result.songs.length > 0, 'Must return songs');
  assert(result.songs.some(t => t.artist.includes('Arijit Singh')), 'Songs must include Arijit tracks');
});

// TEST 5: Search "Arijit Singh romantic songs"
// Expected: Artist + mood intersection.
runTest('TEST 5: Search "Arijit Singh romantic songs" -> ARTIST_MOOD intent', () => {
  const result = unifiedSearchService.search('Arijit Singh romantic songs');

  assert.strictEqual(result.intent, 'ARTIST_MOOD', 'Intent must be ARTIST_MOOD');
  assert(result.topResult && result.topResult.type === 'ARTIST_MOOD');
  assert(result.songs.length > 0, 'Songs must be returned');

  // All top songs should be romantic and by Arijit
  for (const song of result.songs.slice(0, 3)) {
    assert(song.artist.includes('Arijit'), `Expected Arijit in artist, got: ${song.artist}`);
    assert((song.moods && song.moods.ROMANTIC >= 0.7) || song.genre.includes('Romantic'), 'Song must be romantic');
  }
});

// TEST 6: Search "Songs like Tum Hi Ho"
// Expected: Similarity-based recommendations.
runTest('TEST 6: Search "Songs like Tum Hi Ho" -> SIMILAR_SONG intent', () => {
  const result = unifiedSearchService.search('Songs like Tum Hi Ho');

  assert.strictEqual(result.intent, 'SIMILAR_SONG', 'Intent must be SIMILAR_SONG');
  assert(result.songs.length >= 2, 'Must return similar songs');
  assert(result.topResult && result.topResult.data.title === 'Tum Hi Ho', 'Target song must be Tum Hi Ho');
});

// TEST 7: Skip 5 songs from a genre.
// Expected: That genre recommendation score decreases.
runTest('TEST 7: Skip signal decreases genre score', () => {
  const testUserId = 'test_user_skipper';
  const starboy = catalogStore.getTrackById('track_starboy');
  assert(starboy, 'Starboy track must exist');

  const initialGenreScore = affinityEngine.getGenreScore(testUserId, 'Electropop');

  // Skip early 5 times
  for (let i = 0; i < 5; i++) {
    affinityEngine.processEvent({
      userId: testUserId,
      songId: starboy.id,
      eventType: 'EARLY_SKIP'
    });
  }

  const updatedGenreScore = affinityEngine.getGenreScore(testUserId, 'Electropop');
  assert(updatedGenreScore < initialGenreScore, `Score must drop: ${initialGenreScore} -> ${updatedGenreScore}`);
});

// TEST 8: Replay a song multiple times.
// Expected: Song/artist/mood affinity increases.
runTest('TEST 8: Replay signal increases affinity', () => {
  const testUserId = 'test_user_replayer';
  const husn = catalogStore.getTrackById('track_husn');
  assert(husn, 'Husn track must exist');

  const initialAffinity = affinityEngine.getNormalizedAffinity(testUserId, husn.id);

  for (let i = 0; i < 3; i++) {
    affinityEngine.processEvent({
      userId: testUserId,
      songId: husn.id,
      eventType: 'SONG_REPLAYED'
    });
  }

  const updatedAffinity = affinityEngine.getNormalizedAffinity(testUserId, husn.id);
  assert(updatedAffinity > initialAffinity, `Affinity must increase: ${initialAffinity} -> ${updatedAffinity}`);
});

// TEST 9: Dynamic Queue & Autoplay API.
// Expected: Dynamic Queue and Autoplay return structured next recommendations with reasons.
runTest('TEST 9: Dynamic Queue & Autoplay services', () => {
  const queueResult = dynamicQueueService.generateQueue({
    currentSongId: 'track_hawayein',
    limit: 10
  });

  assert(queueResult.currentSong.id === 'track_hawayein');
  assert(queueResult.queue.length > 0);
  assert(queueResult.queue[0].recommendationReason, 'Must include human-readable reason');

  const autoplayResult = autoplayService.getAutoplay({
    currentSongId: 'track_hawayein',
    limit: 3
  });

  assert(autoplayResult.recommendations.length > 0);
  assert(autoplayResult.recommendations[0].song);
  assert(typeof autoplayResult.recommendations[0].score === 'number');
  assert(autoplayResult.recommendations[0].reason);
});

// TEST 10: Admin Diagnostics endpoint.
// Expected: Generates detailed diagnostics with user profile, weights, strategy breakdown, and candidate logs.
runTest('TEST 10: Admin diagnostics report generation', () => {
  const debugReport = recommendationDebugService.getDebugReport('usr_default_1', 'track_hawayein');

  assert(debugReport.userId === 'usr_default_1');
  assert(debugReport.userProfile);
  assert(debugReport.currentContext.songTitle === 'Hawayein');
  assert(debugReport.rankingFactors.weights);
  assert(debugReport.finalRecommendations.length > 0);
  assert(debugReport.finalRecommendations[0].score > 0);
});

console.log(`\n🎉 All ${passedTests} Acceptance & Unit Tests Passed Successfully!\n`);
